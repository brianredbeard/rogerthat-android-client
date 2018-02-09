/*
 * Copyright 2018 GIG Technology NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.4@@
 */
package com.mobicage.rogerthat.plugins.messaging;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.cordova.CordovaActionScreenActivity;
import com.mobicage.rogerthat.cordova.CordovaSettings;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.FullscreenActionScreenActivity;
import com.mobicage.rogerthat.plugins.system.JSEmbedding;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickleable;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.RpcCall;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.app.UpdateAppAssetRequestTO;
import com.mobicage.to.friends.FriendTO;
import com.mobicage.to.friends.ServiceMenuItemTO;
import com.mobicage.to.js_embedding.JSEmbeddingItemTO;
import com.mobicage.to.messaging.MessageTO;
import com.soundcloud.android.crop.CropUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BrandingMgr implements Pickleable, Closeable {

    private static class DownloadNotCompletedException extends Exception {
        private static final long serialVersionUID = 243661742668367591L;
    }

    protected static class BrandedItem implements IJSONable, Comparable<BrandedItem> {

        protected static final int TYPE_MESSAGE = 1;
        protected static final int TYPE_FRIEND = 2;
        protected static final int TYPE_GENERIC = 3;
        protected static final int TYPE_JS_EMBEDDING_PACKET = 4;
        protected static final int TYPE_LOCAL_FLOW_ATTACHMENT = 5;
        protected static final int TYPE_LOCAL_FLOW_BRANDING = 6;
        protected static final int TYPE_ATTACHMENT = 7;
        protected static final int TYPE_APP_ASSET = 8;

        protected static final int STATUS_TODO = 1;
        protected static final int STATUS_DONE = 2;
        protected static final int STATUS_PROCESSING_CALLS = 3;
        protected static final int STATUS_DELETED = 4;

        protected static final SparseIntArray DOWNLOAD_PRIORITIES = new SparseIntArray();

        static {
            DOWNLOAD_PRIORITIES.put(TYPE_MESSAGE, 12);
            DOWNLOAD_PRIORITIES.put(TYPE_ATTACHMENT, 12);
            DOWNLOAD_PRIORITIES.put(TYPE_LOCAL_FLOW_ATTACHMENT, 8);
            DOWNLOAD_PRIORITIES.put(TYPE_LOCAL_FLOW_BRANDING, 8);
            DOWNLOAD_PRIORITIES.put(TYPE_JS_EMBEDDING_PACKET, 4);
            DOWNLOAD_PRIORITIES.put(TYPE_ATTACHMENT, 1);
            DOWNLOAD_PRIORITIES.put(TYPE_GENERIC, 0);
            DOWNLOAD_PRIORITIES.put(TYPE_FRIEND, -4);
        }

        public int type;
        public int status;
        public IJSONable object;
        public String brandingKey;
        private List<RpcCall> calls = new ArrayList<RpcCall>();
        public int attemptsLeft;
        public Long downloadId; // id used by DownloadManager

        public BrandedItem(int type, IJSONable object, String brandingKey) {
            this.type = type;
            this.status = STATUS_TODO;
            this.object = object;
            this.brandingKey = brandingKey;
            if (type == TYPE_LOCAL_FLOW_ATTACHMENT || type == TYPE_LOCAL_FLOW_BRANDING) {
                this.attemptsLeft = 3;
            } else {
                this.attemptsLeft = 1;
            }
        }

        public BrandedItem(MessageTO message) {
            this.type = TYPE_MESSAGE;
            this.status = STATUS_TODO;
            this.object = message;
            this.brandingKey = message.branding;
            this.attemptsLeft = 1;
        }

        public BrandedItem(JSEmbeddingItemTO packet) {
            this.type = TYPE_JS_EMBEDDING_PACKET;
            this.status = STATUS_TODO;
            this.object = packet;
            this.brandingKey = packet.hash;
            this.attemptsLeft = 3;
        }

        public BrandedItem(UpdateAppAssetRequestTO appAsset, String url) {
            this.type = TYPE_APP_ASSET;
            this.status = STATUS_TODO;
            this.object = appAsset;
            this.brandingKey = url;
            this.attemptsLeft = 3;
        }

        public boolean usesDownloadManager() {
            // DownloadManager.COLUMN_LOCAL_FILENAME is added in API level 11
            return Build.VERSION.SDK_INT >= 11
                    && (this.type == BrandedItem.TYPE_ATTACHMENT || this.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> toJSONMap() {
            Map<String, Object> obj = new LinkedHashMap<String, Object>();
            obj.put("type", this.type);
            obj.put("status", this.status);
            obj.put("object", this.object == null ? null : this.object.toJSONMap());
            obj.put("branding", this.brandingKey);
            JSONArray arr = new JSONArray();
            for (RpcCall call : this.calls) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("function", call.function);
                map.put("arguments", call.arguments);
                arr.add(map);
            }
            obj.put("calls", arr);
            obj.put("attempsLeft", this.attemptsLeft);
            if (this.downloadId != null) {
                obj.put("downloadId", this.downloadId);
            }
            return obj;
        }

        @SuppressWarnings("unchecked")
        public BrandedItem(Map<String, Object> source) throws IncompleteMessageException {
            this.type = ((Long) source.get("type")).intValue();
            this.status = ((Long) source.get("status")).intValue();
            this.brandingKey = (String) source.get("branding");
            switch (this.type) {
                case TYPE_MESSAGE:
                    object = new Message((Map<String, Object>) source.get("object"));
                    break;
                case TYPE_FRIEND:
                    object = new FriendTO((Map<String, Object>) source.get("object"));
                    break;
                case TYPE_GENERIC:
                    object = (IJSONable) source.get("object");
                    break;
                case TYPE_JS_EMBEDDING_PACKET:
                    object = new JSEmbeddingItemTO((Map<String, Object>) source.get("object"));
                    break;
                case TYPE_LOCAL_FLOW_ATTACHMENT:
                    object = new StartFlowRequest((Map<String, Object>) source.get("object"));
                    break;
                case TYPE_LOCAL_FLOW_BRANDING:
                    object = new StartFlowRequest((Map<String, Object>) source.get("object"));
                    break;
                case TYPE_ATTACHMENT:
                    object = new AttachmentDownload((Map<String, Object>) source.get("object"));
                    break;
                case TYPE_APP_ASSET:
                    object = new UpdateAppAssetRequestTO((Map<String, Object>) source.get("object"));
                    break;
            }
            this.calls = new ArrayList<RpcCall>();
            JSONArray val_arr = (JSONArray) source.get("calls");
            if (val_arr != null) {
                for (int i = 0; i < val_arr.size(); i++) {
                    Map<String, Object> map = (Map<String, Object>) val_arr.get(i);
                    this.calls.add(BrandingMgr.createRpcCall((String) map.get("function"),
                            (Map<String, Object>) map.get("arguments")));
                }
            }

            Long attemptsLeft = (Long) source.get("attempsLeft");
            this.attemptsLeft = attemptsLeft == null ? 1 : attemptsLeft.intValue();
            this.downloadId = (Long) source.get("downloadId");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            if (obj == null)
                return false;

            if (!(obj instanceof BrandedItem))
                return false;

            BrandedItem other = (BrandedItem) obj;
            if (type != other.type)
                return false;

            if (status != other.status)
                return false;

            if (brandingKey == null) {
                if (other.brandingKey != null)
                    return false;
            } else if (!brandingKey.equals(other.brandingKey)) {
                return false;
            }

            if ((object == null && other.object != null) || (object != null && other.object == null)) {
                return false;
            } else {
                if (type == TYPE_MESSAGE) {
                    MessageTO msg = (MessageTO) object;
                    MessageTO otherMsg = (MessageTO) other.object;
                    if (!msg.key.equals(otherMsg.key)) {
                        return false;
                    }
                } else if (type == TYPE_FRIEND) {
                    FriendTO friend = (FriendTO) object;
                    FriendTO otherFriend = (FriendTO) other.object;
                    if (!friend.email.equals(otherFriend.email)) {
                        return false;
                    }
                } else if (type == TYPE_JS_EMBEDDING_PACKET) {
                    JSEmbeddingItemTO packet = (JSEmbeddingItemTO) object;
                    JSEmbeddingItemTO otherPacket = (JSEmbeddingItemTO) other.object;
                    if (!packet.name.equals(otherPacket.name)) {
                        return false;
                    }
                } else if (type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT
                        || type == BrandedItem.TYPE_LOCAL_FLOW_BRANDING) {
                    StartFlowRequest req = (StartFlowRequest) object;
                    StartFlowRequest otherReq = (StartFlowRequest) other.object;
                    if (!req.thread_key.equals(otherReq.thread_key)) {
                        return false;
                    }

                } else if (type == TYPE_ATTACHMENT) {
                    AttachmentDownload ad = (AttachmentDownload) object;
                    AttachmentDownload otherAd = (AttachmentDownload) other.object;
                    if (!(ad.threadKey.equals(otherAd.threadKey) && ad.messageKey.equals(otherAd.messageKey))) {
                        return false;
                    }
                } else if (type == TYPE_APP_ASSET) {
                    UpdateAppAssetRequestTO asset = (UpdateAppAssetRequestTO) object;
                    UpdateAppAssetRequestTO otherAsset = (UpdateAppAssetRequestTO) other.object;
                    if (!asset.kind.equals(otherAsset.kind)) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public int compareTo(BrandedItem another) {
            // importance: MESSAGES > JS_EMBEDDING > GENERIC > FRIENDS
            if (this == another || this.equals(another))
                return 0;

            int thisPriority = BrandedItem.DOWNLOAD_PRIORITIES.get(this.type);
            int otherPriority = BrandedItem.DOWNLOAD_PRIORITIES.get(another.type);

            return thisPriority > otherPriority ? -1 : 1;
        }
    }

    public enum ColorScheme {
        LIGHT, DARK
    }

    public enum Orientation {
        PORTRAIT, LANDSCAPE, DYNAMIC
    }

    public static class Dimension {
        public final int width;
        public final int height;

        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public enum DisplayType {
        NATIVE, WEBVIEW
    }

    public static class BrandingResult {
        public final File dir;
        public final File file;
        public final File watermark;
        public final Integer color;
        public final Integer menuItemColor;
        public final ColorScheme scheme;
        public final boolean showHeader;
        public final Dimension dimension1;
        public final Dimension dimension2;
        public final String contentType;
        public final Orientation orientation;
        public final boolean wakelockEnabeld;
        public final List<String> externalUrlPatterns;
        public final DisplayType displayType;
        public final File avatar;
        public final File logo;
        public final String message;
        public final String senderName;

        public BrandingResult(File dir, File file, File watermark, Integer color, Integer menuItemColor,
                              ColorScheme scheme, boolean showHeader, Dimension dimension1, Dimension dimension2,
                              String contentType, Orientation orientation, boolean wakelockEnabled, List<String>
                                      externalUrlPatterns, DisplayType displayType, File avatar, File logo,
                              String message, String senderName) {
            this.dir = dir;
            this.file = file;
            this.watermark = watermark;
            this.color = color;
            this.menuItemColor = menuItemColor;
            this.scheme = scheme;
            this.showHeader = showHeader;
            this.dimension1 = dimension1;
            this.dimension2 = dimension2;
            this.contentType = contentType;
            this.orientation = orientation;
            this.wakelockEnabeld = wakelockEnabled;
            this.externalUrlPatterns = externalUrlPatterns;
            this.displayType = displayType;
            this.avatar = avatar;
            this.logo = logo;
            this.message = message;
            this.senderName = senderName;
        }
    }

    private static final String ENCRYPTION_KEY = "acec505e55e120f6"; // secret used in 1.0.1013.A for AES encryption
    private static final byte[] ENCRYPTION_IV = new byte[]{-66, -70, 3, 86, -32, -49, -37, 46, -88, -126, -108, 26,
            113, -37, 27, -111}; // IV used in 1.0.1013.A for AES encryption

    private static final String NUNTIUZ_MESSAGE = "<nuntiuz_message/>";
    private static final String NUNTIUZ_TIMESTAMP = "<nuntiuz_timestamp/>";
    private static final String NUNTIUZ_IDENTITY_NAME = "<nuntiuz_identity_name/>";

    protected static final String CONFIGKEY = "BRANDING_MGR";
    protected static final String CONFIG_QUEUE = "QUEUE";
    public static final String SERVICE_BRANDING_AVAILABLE_INTENT = "com.mobicage.rogerthat.plugins.friends.BRANDING_AVAILABLE";
    public static final String GENERIC_BRANDING_AVAILABLE_INTENT = "com.mobicage.rogerthat.plugins.messaging.GENERIC_BRANDING_AVAILABLE";
    public static final String SERVICE_EMAIL = "email";
    public static final String BRANDING_KEY = "branding";
    public static final String ATTACHMENT_AVAILABLE_INTENT = "com.mobicage.rogerthat.plugins.messaging.ATTACHMENT_AVAILABLE_INTENT";
    public static final String THREAD_KEY = "thread_key";
    public static final String MESSAGE_KEY = "message_key";
    public static final String ATTACHMENT_URL_HASH = "attachment_url_hash";
    public static final String JS_EMBEDDING_AVAILABLE_INTENT = "com.mobicage.rogerthat.plugins.system.JS_EMBEDDING_AVAILABLE_INTENT";
    public static final String JS_EMBEDDING_NAME = "js_embedding_name";
    public static final String MUST_DELETE_ATTACHMENTS_INTENT = "com.mobicage.rogerthat.plugins.messaging.MUST_DELETE_ATTACHMENTS_INTENT";

    private static final int BUFFER_SIZE = 16384;
    protected final List<BrandedItem> mQueue = Collections.synchronizedList(new ArrayList<BrandedItem>());
    @SuppressLint("UseSparseArrays")
    protected final Map<Long, BrandedItem> mDownloadMgrQueue = Collections
            .synchronizedMap(new HashMap<Long, BrandedItem>());
    protected HandlerThread mDownloaderThread;
    protected Handler mDownloaderHandler;
    protected Object mLock = new Object();
    protected Object mFileLock = new Object();

    // TBD: all this volatile stuff... why?
    protected volatile Context mContext;
    protected volatile ConfigurationProvider mCfgProvider;
    protected volatile boolean mExternalStorageAvailable;
    protected volatile boolean mExternalStorageWriteable;
    protected volatile MainService mMainService;
    private boolean mInitialized;
    private byte[] mEncryptionKeyBytes = null;

    public static BrandingMgr createBrandingMgr(ConfigurationProvider cfgProvider, MainService mainService) {
        T.UI();
        final String serializedQueue = getSerializedQueue(cfgProvider);
        BrandingMgr mgr = null;
        if (!"".equals(serializedQueue)) {
            try {
                mgr = (BrandingMgr) Pickler.createObjectFromPickle(Base64.decode(serializedQueue));
            } catch (PickleException e) {
                L.bug(e);
            }
        }

        if (mgr == null)
            mgr = new BrandingMgr();

        return mgr;
    }

    public static String getSerializedQueue(ConfigurationProvider cfgProvider) {
        final Configuration cfg = cfgProvider.getConfiguration(CONFIGKEY);
        return cfg.get(CONFIG_QUEUE, "");
    }

    private byte[] getEncryptionKey() {
        T.dontCare();
        if (mEncryptionKeyBytes == null) {
            mEncryptionKeyBytes = SecurityUtils.md5(ENCRYPTION_KEY + mMainService.getPackageName());
        }
        return mEncryptionKeyBytes;
    }

    @Override
    public void close() {
        T.UI();
        if (!mInitialized)
            return;

        mContext.unregisterReceiver(mBroadcastReceiver);

        Looper looper = mDownloaderThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        try {
            mDownloaderThread.join();
        } catch (InterruptedException e) {
            L.d(e);
        }
        mInitialized = false;
    }

    public boolean isMessageInBrandingQueue(String key) {
        return getMessageFromQueue(key) != null;
    }

    private BrandedItem getMessageFromQueue(String key) {
        synchronized (mLock) {
            for (BrandedItem item : mQueue) {
                if (item.type == BrandedItem.TYPE_MESSAGE && ((MessageTO) item.object).key.equals(key)) {
                    return item;
                }
            }
            return null;
        }
    }

    public boolean isAttachmentInBrandingQueue(String threadKey, String messageKey, String downloadUrl) {
        return getAttachmentFromQueue(threadKey, messageKey, downloadUrl) != null;
    }

    private BrandedItem getAttachmentFromQueue(String threadKey, String messageKey, String downloadUrl) {
        synchronized (mLock) {
            for (BrandedItem item : mQueue) {
                if (item.type == BrandedItem.TYPE_ATTACHMENT) {
                    AttachmentDownload ad = (AttachmentDownload) item.object;
                    if (ad.threadKey.equals(threadKey) && ad.messageKey.equals(messageKey)
                            && ad.download_url.equals(downloadUrl)) {
                        return item;
                    }
                }
            }
            return null;
        }
    }

    public boolean queueIfNeeded(final String function, final IJSONable request, final String messageKey) {
        T.BIZZ();
        synchronized (mLock) {
            BrandedItem item = getMessageFromQueue(messageKey);
            if (item == null || item.status == BrandedItem.STATUS_PROCESSING_CALLS)
                return false;
            item.calls.add(createRpcCall(function, request.toJSONMap()));
            save();
            return true;
        }
    }

    public void deleteConversation(final String threadKey) {
        synchronized (mLock) {
            List<BrandedItem> toBeDeleted = new ArrayList<BrandingMgr.BrandedItem>();
            for (BrandedItem item : mQueue) {
                if (item.type == BrandedItem.TYPE_MESSAGE) {
                    MessageTO message = (MessageTO) item.object;
                    if (threadKey.equals(message.parent_key == null ? message.key : message.parent_key)) {
                        toBeDeleted.add(item);
                    }
                } else if (item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT
                        || item.type == BrandedItem.TYPE_LOCAL_FLOW_BRANDING) {
                    StartFlowRequest req = (StartFlowRequest) item.object;
                    if (threadKey.equals(req.thread_key)) {
                        toBeDeleted.add(item);
                    }
                } else if (item.type == BrandedItem.TYPE_ATTACHMENT) {
                    AttachmentDownload attachment = (AttachmentDownload) item.object;
                    if (threadKey.equals(attachment.threadKey)) {
                        toBeDeleted.add(item);
                    }
                }
            }
            if (toBeDeleted.size() != 0) {
                for (BrandedItem item : toBeDeleted) {
                    L.d("Canceling download of " + item.brandingKey);
                    item.status = BrandedItem.STATUS_DELETED; // is needed when item is currently downloading
                    mQueue.remove(item);
                }
                save();
            }
        }
    }

    private static RpcCall createRpcCall(final String function, final Map<String, Object> request) {
        final Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("request", request);
        return new RpcCall(null, -1, function, arguments);
    }

    public boolean queue(MessageTO message) {
        T.dontCare();
        return queue(new BrandedItem(message));
    }

    public boolean queue(FriendTO friend) {
        T.dontCare();
        Set<String> brandings = new HashSet<String>();
        if (friend.descriptionBranding != null)
            brandings.add(friend.descriptionBranding);

        if (friend.actionMenu != null) {
            if (friend.actionMenu.branding != null)
                brandings.add(friend.actionMenu.branding);

            for (ServiceMenuItemTO smi : friend.actionMenu.items)
                if (smi.screenBranding != null)
                    brandings.add(smi.screenBranding);

            if (friend.actionMenu.staticFlowBrandings != null)
                for (String branding : friend.actionMenu.staticFlowBrandings)
                    brandings.add(branding);
        }
        if (friend.contentBrandingHash != null) {
            brandings.add(friend.contentBrandingHash);
        }
        if (friend.homeBrandingHash != null) {
            brandings.add(friend.homeBrandingHash);
        }
        boolean hasQueuedBrandings = false;
        for (String branding : brandings) {
            boolean brandingAvailable = false;
            try {
                brandingAvailable = isBrandingAvailable(branding);
            } catch (BrandingFailureException e) {
                // Assume not available
            }
            if (!brandingAvailable) {
                if (queue(new BrandedItem(BrandedItem.TYPE_FRIEND, friend, branding))) {
                    hasQueuedBrandings = true;
                }
            }
        }
        return hasQueuedBrandings;
    }

    public boolean queue(JSEmbeddingItemTO packet) {
        T.dontCare();
        return queue(new BrandedItem(packet));
    }

    public boolean queue(StartFlowRequest flow) {
        T.dontCare();
        final List<String> items = new ArrayList<String>();

        for (String attachment : flow.attachments_to_dwnl) {
            BrandedItem item = new BrandedItem(BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT, flow, attachment);
            if (queue(item)) {
                items.add(item.brandingKey);
            }
        }
        for (String branding : flow.brandings_to_dwnl) {
            boolean brandingAvailable = false;
            try {
                brandingAvailable = isBrandingAvailable(branding);
            } catch (BrandingFailureException e) {
                // Assume not available
            }
            if (!brandingAvailable) {
                BrandedItem item = new BrandedItem(BrandedItem.TYPE_LOCAL_FLOW_BRANDING, flow, branding);
                if (queue(item)) {
                    items.add(item.brandingKey);
                }
            }
        }

        if (items.size() > 0) {
            synchronized (mFileLock) {
                try {
                    IOUtils.writeToFile(getLocalFlowContentFile(flow.thread_key), items);
                } catch (Exception e) {
                    L.w("Failed to write localFlow .content file. Flow will start when 1st attachment is downloaded.");
                }
            }
            return true;
        }

        return false;
    }

    public boolean queue(AttachmentDownload attachment) {
        T.dontCare();
        return queue(new BrandedItem(BrandedItem.TYPE_ATTACHMENT, attachment, attachment.download_url));
    }

    public boolean queue(UpdateAppAssetRequestTO assetRequestTO, String url) {
        T.dontCare();
        return queue(new BrandedItem(assetRequestTO, url));
    }

    public boolean queueGenericBranding(String brandingKey) {
        return queue(new BrandedItem(BrandedItem.TYPE_GENERIC, null, brandingKey));
    }

    private boolean queue(BrandedItem item) {
        if (item.type != BrandedItem.TYPE_MESSAGE
                && com.mobicage.rogerthat.util.TextUtils.isEmptyOrWhitespace(item.brandingKey))
            return false;

        if (item.type == BrandedItem.TYPE_FRIEND) {
            // Need to summarize Friend to prevent java.io.UTFDataFormatException: String more than 65535 UTF bytes long
            FriendTO friend = (FriendTO) item.object;
            FriendTO friendSummary = new FriendTO();
            friendSummary.email = friend.email;
            item.object = friendSummary;
        }

        if (item.usesDownloadManager()) {
            DownloadManager dwnlManager = getDownloadManager();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(item.brandingKey));
            request.setTitle(mMainService.getString(R.string.downloading));
            int flags = DownloadManager.Request.NETWORK_WIFI;
            if (!mMainService.getPlugin(SystemPlugin.class).getWifiOnlyDownloads()) {
                flags |= DownloadManager.Request.NETWORK_MOBILE;
            }
            request.setAllowedNetworkTypes(flags);
            final Long id = dwnlManager.enqueue(request);
            synchronized (mLock) {
                item.downloadId = id;
                mDownloadMgrQueue.put(id, item);
                save();
            }
        } else {
            synchronized (mLock) {
                mQueue.add(item);
                Collections.sort(mQueue);
                save();
            }
            if (mExternalStorageWriteable) {
                mDownloaderHandler.post(mQueueProcessor);
            }
        }
        return true;
    }

    private DownloadManager getDownloadManager() {
        return (DownloadManager) mMainService.getSystemService(MainService.DOWNLOAD_SERVICE);
    }

    private void deleteItemFromQueue(final BrandedItem item) {
        deleteItemFromQueue(item, true);
    }

    private void deleteItemFromQueue(final BrandedItem item, boolean logIfNotFound) {
        T.dontCare();
        synchronized (mLock) {
            final boolean found;
            if (item.usesDownloadManager()) {
                found = mDownloadMgrQueue.remove(item.downloadId) != null;
            } else {
                found = mQueue.remove(item);
            }

            if (found) {
                save();
            } else if (logIfNotFound) {
                L.bug("BrandingMgr.deleteItemFromQueue: item was not in mQueue!");
            }
        }
    }

    protected void dequeue(final BrandedItem item, final boolean failed) {
        synchronized (mLock) {
            if (item.status == BrandedItem.STATUS_DELETED) {
                deleteItemFromQueue(item, false);
                return;
            }

            if (failed) {
                item.attemptsLeft -= 1;
                if (item.attemptsLeft > 0) {
                    item.status = BrandedItem.STATUS_TODO;
                    deleteItemFromQueue(item, false);
                    queue(item);
                    return;
                }
            }

            item.status = BrandedItem.STATUS_DONE;
            save();
        }

        if (item.type == BrandedItem.TYPE_MESSAGE) {
            final MessageTO message = (MessageTO) item.object;
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    synchronized (mLock) {
                        T.BIZZ();
                        if (item.status == BrandedItem.STATUS_DELETED) {
                            return;
                        }
                        final MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
                        plugin.newMessage(message, true, true);

                        item.status = BrandedItem.STATUS_PROCESSING_CALLS;

                        for (int i = 0; i < item.calls.size(); i++) {
                            try {
                                CallReceiver.processCall(item.calls.get(i));
                            } catch (Exception e) {
                                L.bug(e);
                            }
                        }

                        deleteItemFromQueue(item);
                    }
                }
            });
        } else if (item.type == BrandedItem.TYPE_JS_EMBEDDING_PACKET) {
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    synchronized (mLock) {
                        T.BIZZ();
                        final JSEmbeddingItemTO packet = (JSEmbeddingItemTO) item.object;
                        deleteItemFromQueue(item);
                        if (!failed) {
                            try {
                                extractJSEmbedding(packet);
                                SystemPlugin systemPlugin = mMainService.getPlugin(SystemPlugin.class);
                                systemPlugin.updateJSEmbeddedPacket(packet.name, packet.hash,
                                        JSEmbedding.STATUS_AVAILABLE);

                                Intent intent = new Intent(JS_EMBEDDING_AVAILABLE_INTENT);
                                intent.putExtra(JS_EMBEDDING_NAME, attachmentDownloadUrlHash(packet.name));
                                mMainService.sendBroadcast(intent);
                            } catch (Exception e) {
                                L.bug("Could not unpack JS Embedding packet", e);
                            }
                        }

                        cleanupJSEmbeddingTmpDownloadFile(packet.name);
                    }
                }
            });

        } else if (item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT
                || item.type == BrandedItem.TYPE_LOCAL_FLOW_BRANDING) {
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.BIZZ();
                    synchronized (mLock) {
                        final StartFlowRequest flow = (StartFlowRequest) item.object;
                        deleteItemFromQueue(item);
                        validateStartFlowReady(item, flow);
                    }
                }
            });

        } else if (item.type == BrandedItem.TYPE_ATTACHMENT) {
            final AttachmentDownload attachment = (AttachmentDownload) item.object;
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    synchronized (mLock) {
                        T.BIZZ();
                        if (item.status == BrandedItem.STATUS_DELETED) {
                            return;
                        }

                        if (attachment.content_type.toLowerCase(Locale.US).startsWith("image/")) {
                            try {
                                // Make sure the image orientation is correct
                                final File attachmentFile = getAttachmentFile(attachment);
                                final String attachmentPath = attachmentFile.getPath();
                                final int exifRotation = CropUtil.getExifRotation(attachmentPath);
                                Bitmap bm = BitmapFactory.decodeFile(attachmentPath);
                                bm = ImageHelper.rotateBitmap(bm, exifRotation);
                                if (bm != null) {
                                    final File tmpFile = new File(attachmentPath + ".tmp");
                                    final FileOutputStream stream = new FileOutputStream(tmpFile);
                                    try {
                                        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    } finally {
                                        stream.close();
                                    }
                                    if (!tmpFile.renameTo(attachmentFile)) {
                                        IOUtils.copyFile(tmpFile, attachmentFile);
                                        tmpFile.delete();
                                    }

                                }

                            } catch (Exception e) {
                                L.bug("Failed to rotate the attachment.", e);
                            }
                        }
                        try {
                            final MessagingPlugin messagingPlugin = mMainService.getPlugin(MessagingPlugin.class);
                            messagingPlugin.createAttachmentThumbnail(attachment);
                        } catch (Exception e) {
                            L.bug("Failed to generate attachment thumbnail", e);
                        }

                        Intent intent = new Intent(ATTACHMENT_AVAILABLE_INTENT);
                        intent.putExtra(THREAD_KEY, attachment.threadKey);
                        intent.putExtra(MESSAGE_KEY, attachment.messageKey);
                        intent.putExtra(ATTACHMENT_URL_HASH, attachmentDownloadUrlHash(attachment.download_url));
                        mMainService.sendBroadcast(intent);

                        deleteItemFromQueue(item);
                    }
                }

            });
        } else if (item.type == BrandedItem.TYPE_APP_ASSET) {
            final UpdateAppAssetRequestTO updateAppAssetRequestTO = (UpdateAppAssetRequestTO) item.object;
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    synchronized (mLock) {
                        T.BIZZ();
                        if (item.status == BrandedItem.STATUS_DELETED) {
                            return;
                        }
                        Intent intent = new Intent(SystemPlugin.ASSET_AVAILABLE_INTENT);
                        intent.putExtra(SystemPlugin.ASSET_KIND, updateAppAssetRequestTO.kind);
                        mMainService.sendBroadcast(intent);

                        deleteItemFromQueue(item);
                    }
                }
            });
        } else {
            boolean brandingAvailable = false;
            try {
                brandingAvailable = isBrandingAvailable(item.brandingKey);
            } catch (BrandingFailureException e) {
                // Assume not available
            }
            deleteItemFromQueue(item);
            if (brandingAvailable) {
                if (item.type == BrandedItem.TYPE_FRIEND) {
                    FriendTO friend = (FriendTO) item.object;
                    // Caution: this Friend object only has an email property populated
                    Intent intent = new Intent(SERVICE_BRANDING_AVAILABLE_INTENT);
                    intent.putExtra(SERVICE_EMAIL, friend.email);
                    intent.putExtra(BRANDING_KEY, item.brandingKey);
                    mMainService.sendBroadcast(intent);

                } else if (item.type == BrandedItem.TYPE_GENERIC) {
                    Intent intent = new Intent(GENERIC_BRANDING_AVAILABLE_INTENT);
                    if (item.object != null && item.object instanceof FriendTO) {
                        intent.putExtra(SERVICE_EMAIL, ((FriendTO) item.object).email);
                    }
                    intent.putExtra(BRANDING_KEY, item.brandingKey);
                    mMainService.sendBroadcast(intent);
                }
            }
        }
    }

    public boolean isBrandingAvailable(String brandingKey) throws BrandingFailureException {
        T.dontCare();
        if (brandingKey == null)
            return true;
        if (!mExternalStorageAvailable)
            return false;
        return getBrandingFile(brandingKey).exists() || getOldBrandingFile(brandingKey).exists();
    }

    private String getCleanBrandingKey(String brandingKey) {
        String[] brandingKeyArray = brandingKey.split("-");
        return brandingKeyArray.length == 1 ? brandingKeyArray[0] : brandingKeyArray[1];
    }

    public boolean isCordovaBranding(String brandingKey) {
        if (brandingKey.startsWith("cordova-")) {
            if (CordovaSettings.PLUGINS.size() == 0) {
                L.e("App needs to be prepared when you want to use cordova");
            }
            return true;
        }
        return false;
    }

    public Class getFullscreenActionScreenActivityClass(String brandingKey) {
        if (isCordovaBranding(brandingKey))
            return CordovaActionScreenActivity.class;
        return FullscreenActionScreenActivity.class;
    }

    public Class getActionScreenActivityClass(String brandingKey) {
        if (isCordovaBranding(brandingKey))
            return CordovaActionScreenActivity.class;
        return ActionScreenActivity.class;
    }

    public BrandingResult prepareBranding(MessageTO message) throws BrandingFailureException {
        T.UI();
        return prepareBranding(new BrandedItem(message), false);
    }

    /**
     * Prepares branding for description screen
     *
     * @param friend
     * @return
     * @throws BrandingFailureException
     */
    public BrandingResult prepareBranding(FriendTO friend) throws BrandingFailureException {
        T.UI();
        return prepareBranding(new BrandedItem(BrandedItem.TYPE_FRIEND, friend, friend.descriptionBranding), false);
    }

    public BrandingResult prepareBranding(String brandingKey, FriendTO friend, boolean jsEnabled)
            throws BrandingFailureException {
        T.UI();
        BrandedItem item = new BrandedItem(BrandedItem.TYPE_GENERIC, null, brandingKey);
        item.object = friend;
        return prepareBranding(item, jsEnabled);
    }

    protected BrandingResult prepareBranding(final BrandedItem item, boolean jsEnabled) throws BrandingFailureException {
        T.UI();
        boolean aesEncrypted = true;
        File brandingCache = getBrandingFile(item.brandingKey);
        if (!brandingCache.exists()) {
            brandingCache = getOldBrandingFile(item.brandingKey);
            aesEncrypted = false;
        }
        if (!brandingCache.exists())
            throw new BrandingFailureException("Branding package " + item.brandingKey + " not found!");
        File tmpBrandingLocation = getBrandingDirectory();
        if (!(tmpBrandingLocation.exists() || tmpBrandingLocation.mkdir()))
            throw new BrandingFailureException("Could not create private branding dir!");
        File tmpBrandingDir = getBrandingDirectory(item.brandingKey, tmpBrandingLocation);
        if (tmpBrandingDir.exists() && !SystemUtils.deleteDir(tmpBrandingDir))
            throw new BrandingFailureException("Could not delete existing branding dir");
        if (!tmpBrandingDir.mkdir())
            throw new BrandingFailureException("Could not create branding dir");

        if (jsEnabled) {
            SystemPlugin systemPlugin = mMainService.getPlugin(SystemPlugin.class);
            Map<String, JSEmbedding> packets = systemPlugin.getJSEmbeddedPackets();
            if (packets.size() > 0) {
                for (String key : packets.keySet()) {
                    final JSEmbedding packet = packets.get(key);
                    if (isCordovaBranding(item.brandingKey)) {
                        if ("rogerthat".equals(packet.getName())) {
                            continue;
                        }
                    }
                    if (packet.getStatus() == JSEmbedding.STATUS_AVAILABLE) {
                        File sourceDir = getJSEmbeddingPacketDirectory(packet.getName());
                        File targetDir = getJSEmbeddingUnpackDirectory(tmpBrandingDir, packet.getName());
                        try {
                            L.i("Copying JSEmbedding packet: " + packet.getName());
                            IOUtils.copyDirectory(sourceDir, targetDir);
                        } catch (IOException e) {
                            L.bug("Could not copy js embedding packet '" + packet.getName() + "'.", e);
                        }
                    } else {
                        L.bug("JSEmbedding packet '" + packet.getName() + "' not downloaded yet. ");
                        JSEmbeddingItemTO jseito = new JSEmbeddingItemTO();
                        jseito.name = packet.getName();
                        jseito.hash = packet.getEmeddingHash();
                        queue(jseito);
                    }
                }
            } else {
                systemPlugin.refreshJsEmdedding();
            }
        }

        L.i("Decrypting " + brandingCache);
        final File tmpDecryptFile = new File(getBrandingRootDirectory(), ".tmp_decrypted_file");
        if (tmpDecryptFile.exists() && !tmpDecryptFile.delete()) {
            throw new BrandingFailureException("Could not remove " + tmpDecryptFile);
        }

        try {
            final BufferedInputStream is = new BufferedInputStream(new FileInputStream(brandingCache));
            final FileOutputStream os = new FileOutputStream(tmpDecryptFile);
            if (aesEncrypted) {
                SecurityUtils.decryptAES(getEncryptionKey(), ENCRYPTION_IV, is, os);
            }
        } catch (Exception e1) {
            brandingCache.delete();
            queue(item);
            throw new BrandingFailureException("Could not decrypt branding file " + brandingCache, e1);
        }

        BrandingResult br = extractBranding(item, brandingCache, tmpDecryptFile, tmpBrandingDir);

        if (isCordovaBranding(item.brandingKey)) {
            try {
                IOUtils.copyAssetFolder(mMainService.getAssets(), "cordova", br.dir.getAbsolutePath());
            } catch (IOException e) {
                L.bug(e);
                throw new BrandingFailureException("Could not copy the cordova asset folder", e);
            }
        }

        if (!aesEncrypted) {
            L.d("lazily convert " + brandingCache + " encrypted in 1.0.1012.A from DES to AES encryption");
            final File dest = getBrandingFile(item.brandingKey);
            try {
                if (!dest.exists())
                    SecurityUtils.encryptAES(getEncryptionKey(), ENCRYPTION_IV, new FileInputStream(tmpDecryptFile),
                            new FileOutputStream(dest));
            } catch (Exception e) {
                if (dest.exists() && !dest.delete())
                    L.d("Could not remove " + dest);
                throw new BrandingFailureException("Could not encrypt " + tmpDecryptFile, e);
            } finally {
                brandingCache.delete();
            }
        }

        return br;
    }

    private BrandingResult extractBranding(final BrandedItem item, final File encryptedBrandingFile,
                                           final File tmpDecryptedBrandingFile, final File tmpBrandingDir) throws BrandingFailureException {
        try {
            L.i("Extracting " + tmpDecryptedBrandingFile + " (" + item.brandingKey + ")");
            File brandingFile = new File(tmpBrandingDir, "branding.html");
            File watermarkFile = null;
            Integer backgroundColor = null;
            Integer menuItemColor = null;
            ColorScheme scheme = ColorScheme.LIGHT;
            Orientation orientation = CloudConstants.isContentBrandingApp() ? Orientation.LANDSCAPE : Orientation
                    .PORTRAIT;
            boolean showHeader = true;
            boolean showName = false;
            String contentType = null;
            boolean wakelockEnabled = false;
            ByteArrayOutputStream brandingBos = new ByteArrayOutputStream();
            File logoPath = null;
            File avatarPath = null;
            String message = "";
            String senderName = "";
            DisplayType displayType = DisplayType.WEBVIEW;
            try {
                MessageDigest digester = MessageDigest.getInstance("SHA256");
                DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(
                        tmpDecryptedBrandingFile)), digester);
                try {
                    ZipInputStream zis = new ZipInputStream(dis);
                    try {
                        byte data[] = new byte[BUFFER_SIZE];
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            L.d("Extracting: " + entry);
                            int count = 0;
                            if (entry.getName().equals("branding.html")) {
                                while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                                    brandingBos.write(data, 0, count);
                                }
                            } else {
                                if (entry.isDirectory()) {
                                    L.d("Skipping branding dir " + entry.getName());
                                    continue;
                                }
                                File destination = new File(tmpBrandingDir, entry.getName());
                                if (entry.getName().equals("avatar.jpg")) {
                                    avatarPath = destination;
                                } else if (entry.getName().equals("logo.jpg")) {
                                    logoPath = destination;
                                }
                                destination.getParentFile().mkdirs();
                                if ("__watermark__".equals(entry.getName())) {
                                    watermarkFile = destination;
                                }
                                final OutputStream fos = new BufferedOutputStream(new FileOutputStream(destination),
                                        BUFFER_SIZE);
                                try {
                                    while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                                        fos.write(data, 0, count);
                                    }
                                } finally {
                                    fos.close();
                                }

                            }
                        }
                        while (dis.read(data) >= 0)
                            ;
                    } finally {
                        zis.close();
                    }
                } finally {
                    dis.close();
                }
                String hexDigest = com.mobicage.rogerthat.util.TextUtils.toHex(digester.digest());
                if (!hexDigest.equals(getCleanBrandingKey(item.brandingKey))) {
                    encryptedBrandingFile.delete();
                    SystemUtils.deleteDir(tmpBrandingDir);
                    throw new BrandingFailureException("Branding cache was invalid!");
                }
                brandingBos.flush();
                byte[] brandingBytes = brandingBos.toByteArray();
                if (brandingBytes.length == 0) {
                    encryptedBrandingFile.delete();
                    SystemUtils.deleteDir(tmpBrandingDir);
                    throw new BrandingFailureException("Invalid branding package!");
                }
                String brandingHtml = new String(brandingBytes, "UTF8");

                Matcher matcher = RegexPatterns.BRANDING_SHOW_NAME.matcher(brandingHtml);
                if (matcher.find()) {
                    String str = matcher.group(1);
                    showName = "true".equalsIgnoreCase(str);
                }

                switch (item.type) {
                    case BrandedItem.TYPE_MESSAGE:
                        MessageTO messageTO = (MessageTO) item.object;
                        message = messageTO.message;
                        if (showName) {
                            FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
                            senderName = friendsPlugin.getName(messageTO.sender);
                        }

                        brandingHtml = brandingHtml.replace(NUNTIUZ_MESSAGE,
                                TextUtils.htmlEncode(message).replace("\r", "").replace("\n", "<br>"));
                        brandingHtml = brandingHtml.replace(NUNTIUZ_TIMESTAMP,
                                TimeUtils.getDayTimeStr(mContext, messageTO.timestamp * 1000));
                        brandingHtml = brandingHtml.replace(NUNTIUZ_IDENTITY_NAME, TextUtils.htmlEncode(senderName));
                        break;

                    case BrandedItem.TYPE_FRIEND:
                        FriendTO friend = (FriendTO) item.object;  // In this case Friend is fully populated
                        message = friend.description;
                        if (showName) {
                            senderName = friend.name;
                        }

                        brandingHtml = brandingHtml.replace(NUNTIUZ_MESSAGE, TextUtils.htmlEncode(friend.description)
                                .replace("\r", "").replace("\n", "<br>"));
                        brandingHtml = brandingHtml.replace(NUNTIUZ_IDENTITY_NAME, TextUtils.htmlEncode(friend.name));
                        break;

                    case BrandedItem.TYPE_GENERIC:
                        if (item.object instanceof FriendTO) {
                            brandingHtml = brandingHtml.replace(NUNTIUZ_IDENTITY_NAME,
                                    TextUtils.htmlEncode(((FriendTO) item.object).name));
                        }
                        break;
                }

                matcher = RegexPatterns.BRANDING_BACKGROUND_COLOR.matcher(brandingHtml);
                if (matcher.find()) {
                    String bg = matcher.group(1);
                    if (bg.length() == 4) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("#");
                        sb.append(bg.charAt(1));
                        sb.append(bg.charAt(1));
                        sb.append(bg.charAt(2));
                        sb.append(bg.charAt(2));
                        sb.append(bg.charAt(3));
                        sb.append(bg.charAt(3));
                        bg = sb.toString();
                    }
                    backgroundColor = Color.parseColor(bg);
                }

                matcher = RegexPatterns.BRANDING_MENU_ITEM_COLOR.matcher(brandingHtml);
                if (matcher.find()) {
                    String bg = matcher.group(1);
                    if (bg.length() == 4) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("#");
                        sb.append(bg.charAt(1));
                        sb.append(bg.charAt(1));
                        sb.append(bg.charAt(2));
                        sb.append(bg.charAt(2));
                        sb.append(bg.charAt(3));
                        sb.append(bg.charAt(3));
                        bg = sb.toString();
                    }
                    menuItemColor = Color.parseColor(bg);
                }

                matcher = RegexPatterns.BRANDING_COLOR_SCHEME.matcher(brandingHtml);
                if (matcher.find()) {
                    String schemeStr = matcher.group(1);
                    scheme = "dark".equalsIgnoreCase(schemeStr) ? ColorScheme.DARK : ColorScheme.LIGHT;
                }

                matcher = RegexPatterns.BRANDING_ORIENTATION.matcher(brandingHtml);
                if (matcher.find()) {
                    String orientationStr = matcher.group(1);
                    if ("dynamic".equalsIgnoreCase(orientationStr)) {
                        orientation = Orientation.DYNAMIC;
                    } else if ("landscape".equalsIgnoreCase(orientationStr)) {
                        orientation = Orientation.LANDSCAPE;
                    } else if ("portrait".equalsIgnoreCase(orientationStr)) {
                        orientation = Orientation.PORTRAIT;
                    } else {
                        L.w("Unknown orientation: " + orientationStr + ". Using default orientation " + orientation);
                    }
                }

                matcher = RegexPatterns.BRANDING_SHOW_HEADER.matcher(brandingHtml);
                if (matcher.find()) {
                    String showHeaderStr = matcher.group(1);
                    showHeader = "true".equalsIgnoreCase(showHeaderStr);
                }

                matcher = RegexPatterns.BRANDING_CONTENT_TYPE.matcher(brandingHtml);
                if (matcher.find()) {
                    String contentTypeStr = matcher.group(1);
                    L.i("Branding content-type: " + contentTypeStr);
                    if (AttachmentViewerActivity.CONTENT_TYPE_PDF.equalsIgnoreCase(contentTypeStr)) {
                        File tmpBrandingFile = new File(tmpBrandingDir, "embed.pdf");
                        if (tmpBrandingFile.exists()) {
                            contentType = AttachmentViewerActivity.CONTENT_TYPE_PDF;
                        }
                    }
                }

                Dimension dimension1 = null;
                Dimension dimension2 = null;
                matcher = RegexPatterns.BRANDING_DIMENSIONS.matcher(brandingHtml);
                if (matcher.find()) {
                    String dimensionsStr = matcher.group(1);
                    L.i("Branding dimensions: " + dimensionsStr);
                    String[] dimensions = dimensionsStr.split(",");
                    try {
                        dimension1 = new Dimension(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]));
                        dimension2 = new Dimension(Integer.parseInt(dimensions[2]), Integer.parseInt(dimensions[3]));
                    } catch (Exception e) {
                        L.bug("Invalid branding dimension: " + matcher.group(), e);
                    }
                }

                matcher = RegexPatterns.BRANDING_WAKELOCK_ENABLED.matcher(brandingHtml);
                if (matcher.find()) {
                    String wakelockEnabledStr = matcher.group(1);
                    wakelockEnabled = "true".equalsIgnoreCase(wakelockEnabledStr);
                }

                final List<String> externalUrlPatterns = new ArrayList<String>();
                matcher = RegexPatterns.BRANDING_EXTERNAL_URLS.matcher(brandingHtml);
                while (matcher.find()) {
                    externalUrlPatterns.add(matcher.group(1));
                }


                matcher = RegexPatterns.BRANDING_DISPLAY_TYPE.matcher(brandingHtml);
                if (matcher.find()) {
                    String type = matcher.group(1);
                    if (type.toLowerCase().equals("native")) {
                        displayType = DisplayType.NATIVE;
                    }
                }

                FileOutputStream fos = new FileOutputStream(brandingFile);
                try {
                    fos.write(brandingHtml.getBytes("UTF8"));
                } finally {
                    fos.close();
                }
                if (contentType != null && AttachmentViewerActivity.CONTENT_TYPE_PDF.equalsIgnoreCase(contentType)) {
                    brandingFile = new File(tmpBrandingDir, "embed.pdf");
                }
                return new BrandingResult(tmpBrandingDir, brandingFile, watermarkFile, backgroundColor, menuItemColor,
                        scheme, showHeader, dimension1, dimension2, contentType, orientation, wakelockEnabled,
                        externalUrlPatterns, displayType, avatarPath, logoPath, message, senderName);
            } finally {
                brandingBos.close();
            }
        } catch (IOException e) {
            L.e(e);
            throw new BrandingFailureException("Error copying cached branded file to private space", e);
        } catch (NoSuchAlgorithmException e) {
            L.e(e);
            throw new BrandingFailureException("Cannot validate ", e);
        }
    }

    private void extractJSEmbedding(final JSEmbeddingItemTO packet) throws BrandingFailureException,
            NoSuchAlgorithmException, FileNotFoundException, IOException {
        File brandingCache = getJSEmbeddingPacketFile(packet.name);
        if (!brandingCache.exists())
            throw new BrandingFailureException("Javascript package not found!");

        File jsRootDir = getJSEmbeddingRootDirectory();
        if (!(jsRootDir.exists() || jsRootDir.mkdir()))
            throw new BrandingFailureException("Could not create private javascript dir!");

        File jsPacketDir = getJSEmbeddingPacketDirectory(packet.name);
        if (jsPacketDir.exists() && !SystemUtils.deleteDir(jsPacketDir))
            throw new BrandingFailureException("Could not delete existing javascript dir");
        if (!jsPacketDir.mkdir())
            throw new BrandingFailureException("Could not create javascript dir");

        MessageDigest digester = MessageDigest.getInstance("SHA256");
        DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(brandingCache)),
                digester);
        try {
            ZipInputStream zis = new ZipInputStream(dis);
            try {
                byte data[] = new byte[BUFFER_SIZE];
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    L.d("Extracting: " + entry);
                    int count = 0;
                    if (entry.isDirectory()) {
                        L.d("Skipping javascript dir " + entry.getName());
                        continue;
                    }
                    File destination = new File(jsPacketDir, entry.getName());
                    destination.getParentFile().mkdirs();
                    final OutputStream fos = new BufferedOutputStream(new FileOutputStream(destination), BUFFER_SIZE);
                    try {
                        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                            fos.write(data, 0, count);
                        }
                    } finally {
                        fos.close();
                    }
                }
                while (dis.read(data) >= 0)
                    ;
            } finally {
                zis.close();
            }
        } finally {
            dis.close();
        }
    }

    public void validateStartFlowReady(final BrandedItem item, final StartFlowRequest flow) {
        T.BIZZ();
        boolean ready = false;
        synchronized (mFileLock) {
            try {
                final File localFlowContentFile = getLocalFlowContentFile(flow.thread_key);
                List<String> content;
                try {
                    content = IOUtils.readAllLinesFromFile(localFlowContentFile);
                } catch (IOException e) {
                    content = new ArrayList<String>(0);
                }

                if (content.contains(item.brandingKey)) {
                    content.remove(item.brandingKey);
                    try {
                        IOUtils.writeToFile(localFlowContentFile, content);
                    } catch (IOException e) {
                        L.w("Error while writing local flow .content file. Launching flow...", e);
                        ready = true;
                    }
                }

                ready = content.size() == 0;
            } catch (BrandingFailureException e) {
                L.w("Error while checking if local flow was ready. Launching flow...", e);
                ready = true;
            }
        }

        if (ready) {
            final MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                public void safeRun() {
                    T.UI();
                    plugin.startLocalFlow(flow);
                }
            });
        }
    }

    public void cleanupBranding(String brandingKey) {
        T.dontCare();
        L.d("Cleanup branding " + brandingKey);
        File parentDir = getBrandingDirectory();
        if (!parentDir.exists())
            return;
        File dir = getBrandingDirectory(brandingKey, parentDir);
        if (!dir.exists())
            return;
        SystemUtils.deleteDir(dir);
    }

    public void cleanupJSEmbeddingPacket(String name) {
        T.dontCare();
        try {
            L.d("Cleanup javascript packet: " + name);
            File parentDir = getJSEmbeddingRootDirectory();
            if (!parentDir.exists())
                return;
            File dir = getJSEmbeddingPacketDirectory(name);
            if (!dir.exists())
                return;
            SystemUtils.deleteDir(dir);
        } catch (BrandingFailureException ex) {
            L.bug("Failed to cleanup JSEmbedding tmp download file with name: " + name, ex);
        }
    }

    private void cleanupJSEmbeddingTmpDownloadFile(String name) {
        T.dontCare();
        try {
            L.d("Cleanup javascript download packet: " + name);
            File parentDir = getJSEmbeddingRootDirectory();
            if (!parentDir.exists())
                return;
            File tmpFile = getJSEmbeddingPacketFile(name);
            if (!tmpFile.exists())
                return;
            tmpFile.delete();
        } catch (BrandingFailureException ex) {
            L.bug("Failed to cleanup JSEmbedding tmp download file with name: " + name, ex);
        }
    }

    private File getBrandingDirectory(String brandingKey, File brandingDir) {
        T.dontCare();
        return new File(brandingDir, brandingKey);
    }

    private File getBrandingDirectory() {
        T.dontCare();
        return new File(mContext.getCacheDir(), "branding");
    }

    public void initialize(ConfigurationProvider cfgProvider, MainService mainService) {
        T.UI();
        mCfgProvider = cfgProvider;
        mMainService = mainService;
        mContext = mainService;

        List<BrandedItem> copy = new ArrayList<BrandingMgr.BrandedItem>();
        Collections.copy(mQueue, copy);
        for (BrandedItem item : copy)
            if (item.status != BrandedItem.STATUS_TODO)
                dequeue(item, false);

        mDownloaderThread = new HandlerThread("rogerthat_branding_worker");
        mDownloaderThread.start();
        Looper looper = mDownloaderThread.getLooper();
        mDownloaderHandler = new Handler(looper);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.registerReceiver(mBroadcastReceiver, filter);
        initStorageSettings();

        mInitialized = true;

        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mMainService.getPluginDBUpdates(BrandingMgr.class).contains(MUST_DELETE_ATTACHMENTS_INTENT)) {
                    synchronized (mLock) {
                        IOUtils.deleteRecursive(getAttachmentsRootDirectory());
                    }
                    mMainService.clearPluginDBUpdate(BrandingMgr.class, MUST_DELETE_ATTACHMENTS_INTENT);
                }
            }
        });
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                final Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                try {
                    downloadCompleted(downloadId);
                } catch (BrandingFailureException e) {
                    L.e(e);
                }
                return null;
            } else {
                if (mInitialized)
                    initStorageSettings();
                return new String[]{intent.getAction()};
            }
        }
    };

    private ParcelFileDescriptor getDownloadedFile(final Long downloadId) throws DownloadNotCompletedException {
        final DownloadManager dwnlMgr = getDownloadManager();
        final Cursor cursor = dwnlMgr.query(new Query().setFilterById(downloadId));
        try {
            if (!cursor.moveToFirst()) {
                L.w("Download with id " + downloadId + " not found!");
                return null;
            }

            final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:
                try {
                    return dwnlMgr.openDownloadedFile(downloadId);
                } catch (FileNotFoundException e) {
                    return null;
                }
            case DownloadManager.STATUS_FAILED:
                return null;
            default: // Not completed
                L.w("Unexpected DownloadManager.STATUS: " + status);
                throw new BrandingMgr.DownloadNotCompletedException();
            }
        } finally {
            cursor.close();
        }
    }

    private void downloadCompleted(final Long downloadId) throws BrandingFailureException {
        final ParcelFileDescriptor pfd;
        try {
            pfd = getDownloadedFile(downloadId);
        } catch (DownloadNotCompletedException e) {
            return;
        }

        try {
            final BrandedItem item;
            synchronized (mLock) {
                item = mDownloadMgrQueue.get(downloadId);
            }

            if (item == null) {
                return;
            }

            boolean success;
            if (pfd == null) {
                success = false;
            } else {
                try {
                    storeDownloadedBranding(pfd, item);
                    success = true;
                } catch (Exception e) {
                    success = false;
                }
            }

            dequeue(item, !success);
        } finally {
            getDownloadManager().remove(downloadId);
        }
    }

    private File getDestinationFile(BrandedItem item) throws BrandingFailureException {
        if (item.type == BrandedItem.TYPE_JS_EMBEDDING_PACKET) {
            final JSEmbeddingItemTO packet = (JSEmbeddingItemTO) item.object;
            return getJSEmbeddingPacketFile(packet.name);
        } else if (item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT) {
            final StartFlowRequest flow = (StartFlowRequest) item.object;
            return getAttachmentFile(flow.thread_key, attachmentDownloadUrlHash(item.brandingKey));
        } else if (item.type == BrandedItem.TYPE_ATTACHMENT) {
            final AttachmentDownload attachment = (AttachmentDownload) item.object;
            return getAttachmentFile(attachment);
        } else if (item.type == BrandedItem.TYPE_APP_ASSET) {
            final UpdateAppAssetRequestTO assetRequestTO = (UpdateAppAssetRequestTO) item.object;
            return getAssetFile(mMainService, assetRequestTO.kind);
        } else {
            return getBrandingFile(item.brandingKey);
        }
    }

    private void storeDownloadedBranding(File tmpFile, BrandedItem item) throws BrandingFailureException, IOException {
        final File dstFile = getDestinationFile(item);

        if (!tmpFile.renameTo(dstFile)) {
            IOUtils.copyFile(tmpFile, dstFile);
            tmpFile.delete();
        }
    }

    private void storeDownloadedBranding(ParcelFileDescriptor pfd, BrandedItem item) throws BrandingFailureException, IOException {
        final File dstFile = getDestinationFile(item);


        InputStream is = new FileInputStream(pfd.getFileDescriptor());
        if (is != null) {
            try {
                OutputStream out = new FileOutputStream(dstFile);
                try {
                    IOUtils.copy(is, out, 1024);
                } finally {
                    out.close();
                }
            } finally {
                is.close();
            }
        }
    }

    private final SafeRunnable mQueueProcessor = new SafeRunnable() {

        private void copyAndVerify(BrandedItem item, InputStream input, FileOutputStream output) throws IOException,
                BrandingFailureException {
            T.dontCare();
            String brandingKey = item.brandingKey;

            MessageDigest digester;
            try {
                digester = MessageDigest.getInstance("SHA256");
            } catch (NoSuchAlgorithmException e) {
                throw new BrandingFailureException("Cannot validate SHA256", e);
            }

            DigestInputStream dis = new DigestInputStream(input, digester);
            try {
                if (item.type == BrandedItem.TYPE_JS_EMBEDDING_PACKET
                        || item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT
                        || item.type == BrandedItem.TYPE_ATTACHMENT
                        || item.type == BrandedItem.TYPE_APP_ASSET) {
                    IOUtils.copy(dis, output, BUFFER_SIZE);
                } else {
                    try {
                        SecurityUtils.encryptAES(getEncryptionKey(), ENCRYPTION_IV, dis, output);
                    } catch (Exception e) {
                        throw new BrandingFailureException("Failed to encrypt branding " + brandingKey, e);
                    }
                }
            } finally {
                dis.close();
            }

            if (item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT || item.type == BrandedItem.TYPE_ATTACHMENT
                    || item.type == BrandedItem.TYPE_APP_ASSET) {
            } else {
                String hexDigest = com.mobicage.rogerthat.util.TextUtils.toHex(digester.digest());
                if (!getCleanBrandingKey(brandingKey).equals(hexDigest))
                    throw new BrandingFailureException(
                            "SHA256 digest could not be validated against branding key\nExpected " + brandingKey + "\nGot "
                                    + hexDigest);
            }
        }

        private void download(BrandedItem item) {
            T.dontCare();

            String url;
            if (item.type == BrandedItem.TYPE_JS_EMBEDDING_PACKET) {
                JSEmbeddingItemTO packet = (JSEmbeddingItemTO) item.object;
                url = CloudConstants.JS_EMBEDDING_URL_PREFIX + packet.name;
            } else if (item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT
                    || item.type == BrandedItem.TYPE_ATTACHMENT
                    || item.type == BrandedItem.TYPE_APP_ASSET) {
                url = item.brandingKey;
            } else {
                url = CloudConstants.BRANDING_URL_PREFIX + item.brandingKey;
            }
            L.d("Downloading branding: " + url);
            boolean success;
            try {
                final HttpClient httpClient = HTTPUtil.getHttpClient(60000, 0);
                // allow redirects
                HttpClientParams.setRedirecting(httpClient.getParams(), true);
                final HttpGet httpGet = new HttpGet(url);

                if (item.type == BrandedItem.TYPE_JS_EMBEDDING_PACKET) {
                    Credentials credentials = mMainService.getCredentials();
                    if (credentials != null) {
                        httpGet.addHeader("X-MCTracker-User",
                                Base64.encodeBytes(credentials.getUsername().getBytes(), Base64.DONT_BREAK_LINES));
                        httpGet.addHeader("X-MCTracker-Pass",
                                Base64.encodeBytes(credentials.getPassword().getBytes(), Base64.DONT_BREAK_LINES));
                    } else {
                        L.bug("Failed to download JS Embedding packet, Credentials were NULL");
                        throw new Exception("Failed to download JS Embedding packet, Credentials were NULL");
                    }
                }

                final HttpResponse response = httpClient.execute(httpGet);
                final int statusCode = response.getStatusLine().getStatusCode();
                final InputStream stream = response.getEntity().getContent();

                if (statusCode != HttpStatus.SC_OK) {
                    // We need to consume the whole entity
                    byte data[] = new byte[BUFFER_SIZE];
                    while (stream.read(data) != -1)
                        ;
                    throw new Exception("Received unexpected statusCode: " + statusCode);
                }

                try {
                    final File tmpFile = new File(getBrandingRootDirectory(), ".tmp_download_file");
                    if (tmpFile.exists()) {
                        if (!tmpFile.delete())
                            throw new BrandingFailureException("Could not cleanup tmp download file");
                    }

                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    try {
                        copyAndVerify(item, stream, fos);
                    } finally {
                        fos.close();
                    }

                    storeDownloadedBranding(tmpFile, item);

                } finally {
                    stream.close();
                }
                success = true;
            } catch (Exception e) {
                String errorMessage = "Failed to download branding file " + item.brandingKey;
                if (item.type == BrandedItem.TYPE_MESSAGE) {
                    MessageTO msg = (MessageTO) item.object;
                    errorMessage += " for message: " + msg.key;
                } else if (item.type == BrandedItem.TYPE_FRIEND) {
                    FriendTO friend = (FriendTO) item.object;
                    // Caution: this Friend object only has an email property populated
                    errorMessage += " for friend: " + friend.email;
                } else if (item.type == BrandedItem.TYPE_JS_EMBEDDING_PACKET) {
                    JSEmbeddingItemTO packet = (JSEmbeddingItemTO) item.object;
                    errorMessage += " for JSEmbedding: " + packet.name;
                } else if (item.type == BrandedItem.TYPE_LOCAL_FLOW_ATTACHMENT
                        || item.type == BrandedItem.TYPE_LOCAL_FLOW_BRANDING) {
                    StartFlowRequest flow = (StartFlowRequest) item.object;
                    errorMessage += " for service '" + flow.service + "' and start flow hash: " + flow.static_flow_hash;
                } else if (item.type == BrandedItem.TYPE_ATTACHMENT) {
                    AttachmentDownload attachment = (AttachmentDownload) item.object;
                    errorMessage += " for attachment " + attachment.download_url + " in message "
                            + attachment.messageKey;
                } else if (item.type == BrandedItem.TYPE_APP_ASSET) {
                    UpdateAppAssetRequestTO appAssetRequestTO = (UpdateAppAssetRequestTO) item.object;
                    errorMessage += " for app asset of kind " + appAssetRequestTO.kind + " in message ";
                }

                if (e instanceof IOException) {
                    L.e(errorMessage, e);
                } else {
                    L.bug(errorMessage, e);
                }
                success = false;
            }
            dequeue(item, !success);
            return;
        }

        /**
         * Get first brandedItem with status=STATUS_TODO
         */
        private BrandedItem getNextBrandedItemToDownload() {
            synchronized (mLock) {
                for (BrandedItem item : mQueue) {
                    if (item.status == BrandedItem.STATUS_TODO) {
                        return item;
                    }
                }

                return null;
            }
        }

        private boolean shouldDequeueItem(BrandedItem item) {
            try {
                return item.brandingKey == null || isBrandingAvailable(item.brandingKey)
                        || item.status != BrandedItem.STATUS_TODO;
            } catch (BrandingFailureException e) {
                L.e(e);
                return true;
            }
        }

        @Override
        protected void safeRun() throws Exception {
            T.dontCare();
            while (mQueue.size() > 0 && mExternalStorageWriteable) {
                BrandedItem item = getNextBrandedItemToDownload();
                if (item == null)
                    break;
                if (shouldDequeueItem(item)) {
                    dequeue(item, false);
                    continue;
                }
                if (!mExternalStorageWriteable)
                    break;
                download(item);
            }
        }
    };

    private File getOldBrandingFile(String branding) throws BrandingFailureException {
        File dir = getBrandingRootDirectory();
        return new File(dir, branding);
    }

    private File getBrandingFile(String branding) throws BrandingFailureException {
        T.dontCare();
        File dir = getBrandingRootDirectory();
        return new File(dir, branding + ".branding");
    }

    public File getBrandingRootDirectory() throws BrandingFailureException {
        T.dontCare();
        File file = IOUtils.getFilesDirectory(mMainService);
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        file = new File(file, "brandings");
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private File getJSEmbeddingPacketFile(String packet) throws BrandingFailureException {
        T.dontCare();
        File dir = getJSEmbeddingRootDirectory();
        return new File(dir, packet + ".tmp");
    }

    private File getJSEmbeddingUnpackDirectory(File brandingDir, String packet) throws BrandingFailureException {
        File file = new File(brandingDir, packet);
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private File getJSEmbeddingPacketDirectory(String packet) throws BrandingFailureException {
        T.dontCare();
        File dir = getJSEmbeddingRootDirectory();
        File file = new File(dir, packet);
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private File getJSEmbeddingRootDirectory() throws BrandingFailureException {
        T.dontCare();
        File file = new File(mMainService.getFilesDir(), "javascript");
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private static File getAssetsRootDirectory(Context context) throws BrandingFailureException {
        T.dontCare();
        File directory = new File(context.getFilesDir(), "assets");
        IOUtils.createDirIfNotExistsBranding(context, directory);
        return directory;
    }

    private File getAttachmentsRootDirectory() throws BrandingFailureException {
        T.dontCare();
        File file = IOUtils.getFilesDirectory(mMainService);
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        file = new File(file, "attachments");
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private File getAttachmentsThreadDirectory(String threadKey) throws BrandingFailureException {
        T.dontCare();
        File dir = getAttachmentsRootDirectory();
        File file = new File(dir, threadKey);
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private File getAttachmentsDirectory(String threadKey, String messageKey) throws BrandingFailureException {
        T.dontCare();
        File dir = getAttachmentsThreadDirectory(threadKey);
        File file = new File(dir, messageKey);
        IOUtils.createDirIfNotExistsBranding(mMainService, file);
        return file;
    }

    private String attachmentDownloadUrlHash(String downloadUrl) {
        return SecurityUtils.sha256(downloadUrl);
    }

    public File getAttachmentFile(AttachmentDownload attachment) throws BrandingFailureException {
        T.dontCare();
        return getAttachmentFile(attachment.threadKey, attachment.messageKey,
                attachmentDownloadUrlHash(attachment.download_url));
    }

    private File getAttachmentFile(String threadKey, String messageKey, String attachmentUrlHash)
            throws BrandingFailureException {
        T.dontCare();
        File dir = getAttachmentsDirectory(threadKey, messageKey);
        return new File(dir, attachmentUrlHash);
    }

    private File getAttachmentFile(String threadKey, String attachmentUrlHash) throws BrandingFailureException {
        T.dontCare();
        File dir = getAttachmentsThreadDirectory(threadKey);
        return new File(dir, attachmentUrlHash);
    }

    public static File getAssetFile(Context context, String kind) throws BrandingFailureException {
        File dir = getAssetsRootDirectory(context);
        return new File(dir, kind);
    }

    private File getLocalFlowContentFile(String threadKey) throws BrandingFailureException {
        T.dontCare();
        return new File(getAttachmentsThreadDirectory(threadKey), ".content");
    }

    protected void save() {
        T.dontCare();
        synchronized (mLock) {
            L.d("BrandingMgr items in queue: " + mQueue.size());
            String serializedMgr;
            try {
                serializedMgr = Base64.encodeBytes(Pickler.getPickleFromObject(this));
            } catch (PickleException e) {
                L.bug(e);
                return;
            }
            Configuration cfg = new Configuration();
            cfg.put(CONFIG_QUEUE, serializedMgr);
            mCfgProvider.updateConfigurationNow(CONFIGKEY, cfg);
        }
    }

    private void initStorageSettings() {
        T.UI();
        if (IOUtils.shouldCheckExternalStorageAvailable()) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mExternalStorageAvailable = mExternalStorageWriteable = true;
                if (mQueue.size() > 0)
                    mDownloaderHandler.post(mQueueProcessor);
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
                if (mQueue.size() > 0)
                    mDownloaderHandler.post(mQueueProcessor);
            } else {
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            if (mQueue.size() > 0)
                mDownloaderHandler.post(mQueueProcessor);
        }
    }

    private static Object safeDeserialize(String s) {
        byte[] b = Base64.decode(s);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(bais);
            try {
                return ois.readObject();
            } finally {
                ois.close();
            }
        } catch (Throwable t) {
            L.bug(t);
        }
        return null;
    }

    private static String safeSerialize(Map<String, Object> jsonMap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                new ObjectOutputStream(baos).writeObject(jsonMap);
                return Base64.encodeBytes(baos.toByteArray());
            } finally {
                baos.close();
            }
        } catch (Throwable t) {
            return L.getStackTraceString(t);
        }
    }

    private String serialize(BrandedItem item) {
        Map<String, Object> jsonMap = item.toJSONMap();
        try {
            return JSONValue.toJSONString(jsonMap);
        } catch (StackOverflowError e) { // Hunting down issue #55
            List<Object> memLocations = new ArrayList<>();
            Stack<Object> breadcrumb = new Stack<>();
            if (checkObjectForCircularReferences(memLocations, breadcrumb, jsonMap)) {
                throw new Error("Found circular reference while serializing BrandedItem!\n\nbreadcrumb = "
                        + breadcrumb + "\n\njsonMap b64 = " + safeSerialize(jsonMap) + "\n\nQueue was: " +
                        getSerializedQueue(mMainService.getConfigurationProvider()));
            }
            throw new Error("Expected to find circular reference in " + safeSerialize(jsonMap) + "\n\nQueue was: " +
                    getSerializedQueue(mMainService.getConfigurationProvider()));
        }
    }

    private boolean checkObjectForCircularReferences(List<Object> memLocations, Stack<Object> breadcrumb, Object
            value) {
        if (memLocations.contains(value)) {
            return true;
        }

        if (value instanceof Map) {
            memLocations.add(value);
            if (checkMapForCircularReferences(memLocations, breadcrumb, (Map) value)) {
                return true;
            }
            memLocations.remove(value);
        } else if (value instanceof List) {
            memLocations.add(value);
            if (checkListForCircularReferences(memLocations, breadcrumb, (List<Object>) value)) {
                return true;
            }
            memLocations.remove(value);
        }

        return false;
    }

    private boolean checkListForCircularReferences(List<Object> memLocations, Stack<Object> breadcrumb, List<Object>
            jsonArray) {

        for (int i = 0; i < jsonArray.size(); i++) {
            breadcrumb.push(i);

            if (checkObjectForCircularReferences(memLocations, breadcrumb, jsonArray.get(i))) {
                return true;
            }

            breadcrumb.pop();
        }
        return false;
    }

    private boolean checkMapForCircularReferences(List<Object> memLocations, Stack<Object> breadcrumb, Map<String,
            Object> jsonMap) {

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            breadcrumb.push(entry.getKey());

            if (checkObjectForCircularReferences(memLocations, breadcrumb, entry.getValue())) {
                return true;
            }

            breadcrumb.pop();
        }
        return false;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        out.writeInt(mQueue.size());
        for (BrandedItem item : mQueue) {
            out.writeUTF(serialize(item));
        }
        out.writeInt(mDownloadMgrQueue.size());
        for (BrandedItem item : mDownloadMgrQueue.values()) {
            out.writeUTF(serialize(item));
        }
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        if (version < 4) {
            deserializeStashedLegacyManager(version, in);
        } else {
            // Read branding queue
            int queueSize = in.readInt();
            for (int i = 0; i < queueSize; i++) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parse(in.readUTF());
                try {
                    mQueue.add(new BrandedItem(jsonMap));
                } catch (IncompleteMessageException e) {
                    L.bug(e);
                }
            }

            if (version < 6) {
                readLegacyStashedMemberStatusUpdates(in);
                if (version >= 5) {
                    readLegacyStashedLockRequests(in);
                }
            }

            if (version >= 7) {
                int downloadQueueSize = in.readInt();
                for (int i = 0; i < downloadQueueSize; i++) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parse(in.readUTF());
                    try {
                        final BrandedItem item = new BrandedItem(jsonMap);
                        mDownloadMgrQueue.put(item.downloadId, item);
                    } catch (IncompleteMessageException e) {
                        L.bug(e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readLegacyStashedMemberStatusUpdates(DataInput in) throws IOException, PickleException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            BrandedItem item = getMessageFromQueue(key);
            int updatesCount = in.readInt();
            for (int j = 0; j < updatesCount; j++) {
                item.calls.add(createRpcCall("com.mobicage.capi.messaging.updateMessageMemberStatus",
                        (Map<String, Object>) JSONValue.parse(in.readUTF())));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readLegacyStashedLockRequests(DataInput in) throws IOException, PickleException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            BrandedItem item = getMessageFromQueue(key);
            item.calls.add(createRpcCall("com.mobicage.capi.messaging.messageLocked",
                    (Map<String, Object>) JSONValue.parse(in.readUTF())));
        }
    }

    @SuppressWarnings("unchecked")
    private void deserializeStashedLegacyManager(int version, DataInput in) throws IOException, PickleException {
        int msgQueueSize = in.readInt();
        for (int i = 0; i < msgQueueSize; i++) {
            try {
                Message message = new Message((Map<String, Object>) JSONValue.parse(in.readUTF()));
                mQueue.add(new BrandedItem(message));
            } catch (IncompleteMessageException e) {
                L.bug(e);
            }
        }

        if (version == 3) {
            readLegacyStashedMemberStatusUpdates(in);
        }

        if (version >= 2) {
            int friendQueueSize = in.readInt();
            for (int i = 0; i < friendQueueSize; i++) {
                try {
                    FriendTO friend = new FriendTO((Map<String, Object>) JSONValue.parse(in.readUTF()));
                    mQueue.add(new BrandedItem(BrandedItem.TYPE_FRIEND, friend, friend.descriptionBranding));
                } catch (IncompleteMessageException e) {
                    L.bug(e);
                }
            }
        }
    }

    @Override
    public int getPickleClassVersion() {
        return 7;
    }

    public static int calculateHeight(BrandingResult br, int width) {
        return calculateHeight(br.dimension1, br.dimension2, width);
    }

    public static int calculateHeight(Dimension dimension1, Dimension dimension2, int width) {
        if (dimension1 == null || dimension2 == null) {
            return 0;
        }

        int w0 = dimension1.width;
        int h0 = dimension1.height;
        int w1 = dimension2.width;
        int h1 = dimension2.height;

        if (w1 == w0) {
            // prevent division by zero
            return 0;
        }

        int height = h0 + (h1 - h0) * (width - w0) / (w1 - w0);
        L.d("Calculated branding height: " + height);
        return height;
    }

}
