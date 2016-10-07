package com.mobicage.rogerthat.plugins.news;


import com.mobicage.rogerthat.MainService;
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

import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.DNSUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final int KEEPALIVE_DELAY = 30;
    private final MainService mService;
    private NewsChannelCallbackHandler mNewsChannelCallbackHandler;
    public String host;
    public int port;
    public boolean ssl;
    private Channel channel;
    private EventLoopGroup eventLoopGroup;
    private boolean connected;
    private ConfigurationProvider configurationProvider;
    private boolean isRetryingToConnect = false;

    public boolean isConnected() {
        return connected;
    }

    public enum Command {
        AUTH("AUTH"),
        SET_INFO("SET INFO"),
        NEWS_READ("NEWS READ"),
        NEWS_ROGER("NEWS ROGER"),
        ACK_NEWS_ROGER("ACK NEWS ROGER"),
        ACK_NEWS_READ("ACK NEWS READ"),
        NEWS_STATS_READ("NEWS STATS READ"),
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
            return this.name;
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
        this.mNewsChannelCallbackHandler = handler;
        this.mService = handler.getMainService();
        this.configurationProvider = configurationProvider;
        this.ssl = false;

        if (mService.getNetworkConnectivityManager().isConnected()) {
            getConfiguration();
        }
    }

    public void internetConnected() {
        T.BIZZ();
        if (this.host == null || this.port == -1) {
            getConfiguration();
        }
        connect();
    }

    public void internetDisconnected() {
        disconnect();
    }


    public void connect() {
        T.BIZZ();
        if (this.connected) {
            L.d("Already connected to news channel");
            return;
        } else if (!mService.getNetworkConnectivityManager().isConnected()) {
            L.d("Cannot connect to news channel: no internet connection.");
            return;
        }
        if (this.host == null) {
            L.d("Not connecting to news channel because no host was found");
            attemptToReconnect(10);
            return;
        } else if (this.port == -1) {
            L.d("Not connecting to news channel because no port was found");
            attemptToReconnect(10);
            return;
        }
        L.d("Attemping to connect to news channel...");
        final SslContext sslCtx;
        if (this.ssl) {
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
        this.eventLoopGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
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
        this.channel = b.connect(this.host, this.port).channel();
        this.connected = true;
        L.d("Connected to news channel.");
        keepAlive();
    }

    private void keepAlive() {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (connected) {
                            sendLine(Command.PING.toString());
                            keepAlive();
                        }
                    }
                },
                KEEPALIVE_DELAY * 1000
        );
    }

    public void sendLine(String line) {
        L.d("[NEWS] >> " + line);
        if (channel == null || !connected)
            return;
        channel.writeAndFlush(line + "\r\n");
    }

    public void sendCommand(Command command, String data) {
        sendLine(String.format("%s: %s", command, data));
    }

    public void disconnect() {
        if (this.channel == null || this.eventLoopGroup == null) {
            return;
        }
        channel.closeFuture();
        this.eventLoopGroup.shutdownGracefully();
        this.channel = null;
        this.eventLoopGroup = null;
        this.connected = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.authenticate();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        L.d("News channel inactive");
        super.channelInactive(ctx);
        this.connected = false;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        L.d("News channel unregistered");
        super.channelUnregistered(ctx);
        this.connected = false;
    }

    private void getConfiguration() {
        NetworkConnectivityManager networkConnectivityManager = mService.getNetworkConnectivityManager();
        NewsConfigurationFactory configurationFactory = new NewsConfigurationFactory(this.configurationProvider,
                networkConnectivityManager, null);
        try {
            DNSUtil.HostAddress hostAddress = configurationFactory.getSafeNewsConnectionHost(false);
            this.host = hostAddress.getHost();
            this.port = hostAddress.getPort();
        } catch (NewsConfigurationConnectionException ignored) {
        } catch (NewsConfigurationException e) {
            L.bug(e);
        }
    }

    private void delayGetConfiguration() {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (mService.getNetworkConnectivityManager().isConnected()) {
                            getConfiguration();
                        } else {
                            delayGetConfiguration();
                        }
                    }
                },
                5000
        );
    }

    private void attemptToReconnect(final int backoffTime) {
        if (!this.connected) {
            if (this.isRetryingToConnect) {
                return;
            }
            this.isRetryingToConnect = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (mService.getNetworkConnectivityManager().isConnected()) {
                                SafeRunnable safeRunnable = new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        NewsChannel.this.isRetryingToConnect = false;
                                        connect();
                                    }
                                };
                                mService.postAtFrontOfBIZZHandler(safeRunnable);
                            }
                        }
                    },
                    backoffTime * 1000
            );
        }
    }

    public boolean isTryingToReconnect(){
        return this.isRetryingToConnect;
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
            case NEWS_STATS_READ:
                newsReadUpdate(data);
                break;
            case NEWS_ROGER_UPDATE:
                newsRogerUpdate(data);
                break;
            case NEWS_PUSH:
                newsPush(data);
                break;
        }
    }

    private void ackNewsRoger(String data) {
        L.d(String.format("News successfully rogered: %s", data));
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
                mNewsChannelCallbackHandler.newsRogerUpdate(newsId, friendEmail);
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

    public void readNews(Long newsId) {
        sendCommand(Command.NEWS_READ, newsId.toString());
    }

    public void rogerNews(Long newsId) {
        sendCommand(Command.NEWS_ROGER, newsId.toString());
    }

    public void readStatsNews(List<Long> newsIds) {
        sendCommand(Command.NEWS_STATS_READ, android.text.TextUtils.join(" ", newsIds));
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        L.e(cause);
        ctx.close();
        this.connected = false;  // Not sure if necessary
    }
}
