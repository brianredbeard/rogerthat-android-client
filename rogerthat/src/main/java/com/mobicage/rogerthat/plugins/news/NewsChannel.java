package com.mobicage.rogerthat.plugins.news;


import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.news.AppNewsItemTO;
import com.mobicage.to.news.NewsInfoTO;

import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.DNSUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

@ChannelHandler.Sharable
public class NewsChannel extends SimpleChannelInboundHandler<String> {
    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.news.channel";
    private static final String CONFIG_TYPE_READ = "READ";
    private static final String CONFIG_TYPE_ROGER = "ROGER";
    private final int KEEPALIVE_DELAY = 30;

    private final MainService mService;
    private NewsChannelCallbackHandler mNewsChannelCallbackHandler;
    private String mHost;
    private int mPort;
    private boolean mIsSSL;
    private Channel mChannel;
    private EventLoopGroup mEventLoopGroup;
    private boolean mIsConnected;
    private ConfigurationProvider mConfigurationProvider;
    private boolean mIsRetryingToConnect = false;
    private Timer mKeepAliveTimer;
    private Timer mReconnectTimer;

    private Set<Long> mReadsToSend = new HashSet<>();
    private Set<Long> mRogersToSend = new HashSet<>();

    public boolean isConnected() {
        return mIsConnected;
    }

    private enum Command {
        AUTH("AUTH"),
        SET_INFO("SET INFO"),
        NEWS_READ("NEWS READ"),
        NEWS_ROGER("NEWS ROGER"),
        ACK_NEWS_ROGER("ACK NEWS ROGER"),
        ACK_NEWS_READ("ACK NEWS READ"),
        NEWS_STATS("NEWS STATS"),
        NEWS_READ_UPDATE("NEWS READ UPDATE"),
        NEWS_ROGER_UPDATE("NEWS ROGER UPDATE"),
        NEWS_PUSH("NEWS PUSH"),
        PING("PING"),
        PONG("PONG");

        private final String name;

        Command(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }

        private static Command fromValue(String value) {
            for (Command val : Command.values()) {
                if (String.valueOf(val).equals(value)) {
                    return val;
                }
            }
            return null;
        }
    }

    public NewsChannel(NewsChannelCallbackHandler handler, ConfigurationProvider configurationProvider) {
        mNewsChannelCallbackHandler = handler;
        mService = handler.getMainService();
        mConfigurationProvider = configurationProvider;
        mIsSSL = false;

        loadCallFromDB();

        if (mService.getNetworkConnectivityManager().isConnected()) {
            getConfiguration();
        }
    }

    public void internetConnected() {
        T.BIZZ();
        if (mHost == null || mPort == -1) {
            getConfiguration();
        }
        connect();
    }

    public void internetDisconnected() {
        disconnect();
    }


    public void connect() {
        T.BIZZ();
        if (mIsConnected) {
            L.d("Already connected to news channel");
            return;
        } else if (mIsRetryingToConnect) {
            L.d("Already trying to reconnect to news channel");
            return;
        } else if (!mService.getNetworkConnectivityManager().isConnected()) {
            L.d("Cannot connect to news channel: no internet connection.");
            return;
        } else if (mHost == null) {
            L.d("Not connecting to news channel because no host was found");
            attemptToReconnect(10);
            return;
        } else if (mPort == -1) {
            L.d("Not connecting to news channel because no port was found");
            attemptToReconnect(10);
            return;
        }
        
        L.d("Attemping to connect to news channel...");
        final SslContext sslCtx;
        if (mIsSSL) {
            try {
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } catch (SSLException e) {
                L.e(e);
                return;
            }
        } else {
            sslCtx = null;
        }
        mEventLoopGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(mEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), mHost, mPort));
                        }
                        // decoder
                        p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                        p.addLast(new StringDecoder());

                        //encoder
                        p.addLast(new StringEncoder());
                        p.addLast(NewsChannel.this);
                    }
                });
        // Bind and start to accept incoming connections.
        mChannel = b.connect(mHost, mPort).channel();
        mIsConnected = true;
        L.d("Connected to news channel.");
        resendUnsendItems();
        if(mReconnectTimer != null) {
            mReconnectTimer.cancel();
        }
        keepAlive();
    }

    private void keepAlive() {
        if(mKeepAliveTimer != null){
            mKeepAliveTimer.cancel();
        }
        mKeepAliveTimer = new Timer(true);
        mKeepAliveTimer.scheduleAtFixedRate(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (mIsConnected) {
                            sendLine(Command.PING.toString());
                        }
                    }
                },
                0,
                KEEPALIVE_DELAY * 1000
        );
    }

    private void sendLine(String line) {
        L.d("[NEWS] >> " + line);
        if (mChannel == null || !mIsConnected)
            return;
        mChannel.writeAndFlush(line + "\r\n");
    }

    private void sendCommand(Command command, String data) {
        sendLine(String.format("%s: %s", command, data));
    }

    public void disconnect() {
        if (mChannel == null || mEventLoopGroup == null) {
            return;
        }
        mChannel.closeFuture();
        mEventLoopGroup.shutdownGracefully();
        mChannel = null;
        mEventLoopGroup = null;
        mIsConnected = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        authenticate();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        L.d("News channel inactive");
        super.channelInactive(ctx);
        mIsConnected = false;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        L.d("News channel unregistered");
        super.channelUnregistered(ctx);
        mIsConnected = false;
    }

    private void getConfiguration() {
        NetworkConnectivityManager networkConnectivityManager = mService.getNetworkConnectivityManager();
        NewsConfigurationFactory configurationFactory = new NewsConfigurationFactory(mConfigurationProvider,
                networkConnectivityManager, null);
        try {
            DNSUtil.HostAddress hostAddress = configurationFactory.getSafeNewsConnectionHost(false);
            mHost = hostAddress.getHost();
            mPort = hostAddress.getPort();
        } catch (NewsConfigurationConnectionException ignored) {
        } catch (NewsConfigurationException e) {
            L.bug(e);
        }
    }

    private void attemptToReconnect(final int backoffTime) {
        if (!mIsConnected) {
            if (mIsRetryingToConnect) {
                return;
            }
            mIsRetryingToConnect = true;
            if(mReconnectTimer != null ){
                mReconnectTimer.cancel();
            }
            mReconnectTimer = new Timer(true);
            mReconnectTimer.scheduleAtFixedRate(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (mService.getNetworkConnectivityManager().isConnected()) {
                                SafeRunnable safeRunnable = new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        mIsRetryingToConnect = false;
                                        connect();
                                    }
                                };
                                mService.postAtFrontOfBIZZHandler(safeRunnable);
                            }
                        }
                    },
                    0,
                    backoffTime * 1000
            );
        }
    }

    public boolean isTryingToReconnect(){
        return mIsRetryingToConnect;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        L.d("[NEWS] << " + msg);
        if (Command.PONG.toString().equals(msg)) {
            return;
        }
        String[] result = msg.split(": ", 2);
        if (result.length < 2) {
            L.d("Unknown command");
            return;
        }
        Command command = Command.fromValue(result[0]);
        String data = result[1];
        if (command == null) {
            L.bug("Received unknown command: " + result[0]);
            return;
        }
        switch (command) {
            case AUTH:
                if ("ERROR".equals(data)) {
                    L.bug("Failed to authenticate user to kickserver.");
                } else if ("OK".equals(data)) {
                    setInfo();
                }
                break;
            case ACK_NEWS_READ:
                ackNewsRead(data);
                break;
            case ACK_NEWS_ROGER:
                ackNewsRoger(data);
                break;
            case NEWS_READ_UPDATE:
                newsReadUpdate(data);
                break;
            case NEWS_STATS:
                newsStatsReceived(data);
                break;
            case NEWS_ROGER_UPDATE:
                newsRogerUpdate(data);
                break;
            case NEWS_PUSH:
                newsPush(data);
                break;
        }
    }

    private void newsPush(String data) {
        JSONObject json = (JSONObject) JSONValue.parse(data);
        try {
            //noinspection unchecked
            final AppNewsItemTO newsItem = new AppNewsItemTO(json);
            mService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mNewsChannelCallbackHandler.newsPush(newsItem);
                }
            });
        } catch (IncompleteMessageException e) {
            L.bug(String.format("Invalid news item received from update server (%s)\n: %s", e.getMessage(), data));
        }
    }

    private void newsRogerUpdate(String data) {
        String[] splitData = data.split(" ");
        final long newsId = Long.parseLong(splitData[0]);
        final String friendEmail = splitData[1];
        mService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mNewsChannelCallbackHandler.newsRogerUpdate(newsId, new String[]{friendEmail});
            }
        });
    }

    private void newsReadUpdate(String data) {
        String[] stats = data.split(" "); // news_id1 read_count1 news_id2 read_count2
        final Map<Long, Long> statsMap = new HashMap<>();
        for (int i = 0; i < stats.length; i += 2) {
            statsMap.put(Long.parseLong(stats[i]), Long.parseLong(stats[i + 1]));
        }
        mService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mNewsChannelCallbackHandler.newsReadUpdate(statsMap);
            }
        });
    }

    private void newsStatsReceived(String data) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(data);
        final Map<Long, Long> readCountMap = new HashMap<>();
        final Map<Long, String[]> rogeredMap = new HashMap<>();
        for (Object o : jsonObject.keySet()) {
            String key = (String) o;
            try {
                Long newsId = Long.valueOf(key);
                //noinspection unchecked
                final NewsInfoTO newsInfo = new NewsInfoTO((Map<String, Object>) jsonObject.get(key));
                readCountMap.put(newsId, newsInfo.reach);
                rogeredMap.put(newsId, newsInfo.users_that_rogered);
            } catch (IncompleteMessageException e) {
                L.bug(e);
            }
        }
        mService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                for(Long newsId: rogeredMap.keySet()){
                    mNewsChannelCallbackHandler.newsRogerUpdate(newsId, rogeredMap.get(newsId));
                }
                mNewsChannelCallbackHandler.newsReadUpdate(readCountMap);
            }
        });
    }

    public void readNews(Long newsId) {
        addCallToDB(CONFIG_TYPE_READ, newsId);
        sendCommand(Command.NEWS_READ, newsId.toString());
    }

    public void rogerNews(Long newsId) {
        addCallToDB(CONFIG_TYPE_ROGER, newsId);
        sendCommand(Command.NEWS_ROGER, newsId.toString());
    }

    public void statsNews(List<Long> newsIds) {
        sendCommand(Command.NEWS_STATS, android.text.TextUtils.join(" ", newsIds));
    }

    private void authenticate() {
        Credentials credentials = mService.getCredentials();
        String username = Base64.encodeBytes(credentials.getUsername().getBytes(), Base64.DONT_BREAK_LINES);
        String password = Base64.encodeBytes(credentials.getPassword().getBytes(), Base64.DONT_BREAK_LINES);
        sendLine(String.format("AUTH: %s %s", username, password));
    }

    private void setInfo() {
        sendCommand(Command.SET_INFO, String.format("APP %s", AppConstants.APP_ID));
        sendCommand(Command.SET_INFO, String.format("ACCOUNT %s", mService.getIdentityStore().getIdentity().getEmail()));
        List<String> friendSet = mService.getPlugin(FriendsPlugin.class).getStore().getFriendSet();
        sendCommand(Command.SET_INFO, String.format("FRIENDS %s", JSONValue.toJSONString(friendSet)));
    }

    private void ackNewsRead(String newsId) {
        L.d(String.format("News %s marked as read", newsId));
        removeCallFromDB(CONFIG_TYPE_READ, Long.parseLong(newsId));
    }

    private void ackNewsRoger(String newsId) {
        L.d(String.format("News %s marked as rogered", newsId));
        removeCallFromDB(CONFIG_TYPE_ROGER, Long.parseLong(newsId));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        L.e(cause);
        ctx.close();
        mIsConnected = false;  // Not sure if necessary
    }

    private void addCallToDB(String type, Long newsId) {
        if (CONFIG_TYPE_READ.equals(type)) {
            mReadsToSend.add(newsId);
        } else if (CONFIG_TYPE_ROGER.equals(type)) {
            mRogersToSend.add(newsId);
        } else {
            L.e("saveCallInDB with unkown type: " + type);
            return;
        }

        saveCallInDB(type);
    }

    private void removeCallFromDB(String type, Long newsId) {
        if (CONFIG_TYPE_READ.equals(type)) {
            mReadsToSend.remove(newsId);
        } else if (CONFIG_TYPE_ROGER.equals(type)) {
            mRogersToSend.remove(newsId);
        } else {
            L.e("saveCallInDB with unkown type: " + type);
            return;
        }

        saveCallInDB(type);
    }

    private void saveCallInDB(String type) {
        JSONArray jsonNewsIds = new JSONArray();
        if (CONFIG_TYPE_READ.equals(type)) {
            for (Long readNewsId : mReadsToSend) {
                jsonNewsIds.add(readNewsId);
            }
        } else if (CONFIG_TYPE_ROGER.equals(type)) {
            for (Long rogerNewsId : mRogersToSend) {
                jsonNewsIds.add(rogerNewsId);
            }
        } else {
            L.e("saveCallInDB with unkown type: " + type);
            return;
        }
        Configuration cfg = mConfigurationProvider.getConfiguration(CONFIGKEY);
        cfg.put(type, JSONValue.toJSONString(jsonNewsIds));
        mConfigurationProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    private void loadCallFromDB() {
        Configuration cfg = mConfigurationProvider.getConfiguration(CONFIGKEY);
        final String readNewsIdsJSON = cfg.get(CONFIG_TYPE_READ, null);
        if (readNewsIdsJSON != null) {
            JSONArray jsonNewsIds = (JSONArray) JSONValue.parse(readNewsIdsJSON);
            for (Object jsonNewsId : jsonNewsIds) {
                mReadsToSend.add((Long) jsonNewsId);
            }
        }

        final String rogerNewsIdsJSON = cfg.get(CONFIG_TYPE_ROGER, null);
        if (rogerNewsIdsJSON != null) {
            JSONArray jsonNewsIds = (JSONArray) JSONValue.parse(rogerNewsIdsJSON);
            for (Object jsonNewsId : jsonNewsIds) {
                mRogersToSend.add((Long) jsonNewsId);
            }
        }
    }

    private void resendUnsendItems() {
        for (Long newsId : mReadsToSend) {
            sendCommand(Command.NEWS_READ, newsId.toString());
        }

        for (Long newsId : mRogersToSend) {
            sendCommand(Command.NEWS_ROGER, newsId.toString());
        }
    }
}
