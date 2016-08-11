package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.forms.SubmitSignFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitSignFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO;

import org.jivesoftware.smack.util.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SignWidget extends Widget {

    private Button mSignBtn;
    private View mSignResultView;
    private List<String> mResult;
    private MessageDigest mSha256Digester;
    private String mCaption;

    public SignWidget(Context context) {
        super(context);
    }

    public SignWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        try {
            mSha256Digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            mSha256Digester = null;
        }

        mSignResultView = findViewById(R.id.sign_result);
        mSignBtn = (Button) findViewById(R.id.sign_btn);
        mSignBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                sign();
            }
        });

        mCaption = (String) mWidgetMap.get("caption");
        if (mCaption == null) {
            mCaption = mActivity.getString(R.string.enter_pin_to_sign);
        }

        mResult = (List<String>) mWidgetMap.get("values");
        if (mResult != null) {
            mSignBtn.setVisibility(View.GONE);
            mSignResultView.setVisibility(View.VISIBLE);
        }
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        return widget.get("values") != null ? context.getString(R.string.signed) : null;
    }

    @Override
    public void putValue() {
        mWidgetMap.put("values", mResult == null ? null : mResult);
    }

    @Override
    public UnicodeListWidgetResultTO getWidgetResult() {
        UnicodeListWidgetResultTO result = new UnicodeListWidgetResultTO();
        final List<String> values = (List<String>) mWidgetMap.get("values");
        result.values = values.toArray(new String[values.size()]);
        return result;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        if (Message.POSITIVE.equals(buttonId)) {
            if (mResult == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setCancelable(true);
                builder.setMessage(mActivity.getString(R.string.sign_first));
                builder.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(mSignBtn.getText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        sign();
                    }
                });
                builder.show();

                return false;
            }
        }
        return true;
    }

    @Override
    public void submit(String buttonId, long timestamp) throws Exception {
        final SubmitSignFormRequestTO request = new SubmitSignFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                    "com.mobicage.api.messaging.submitSignForm", mActivity, mParentView);
        } else {
            Rpc.submitSignForm(new ResponseHandler<SubmitSignFormResponseTO>(), request);
        }
    }

    private byte[] getPayloadHash() {
        final String payloadStr = (String) mWidgetMap.get("payload");
        return payloadStr == null ? null : sha256(Base64.decode(payloadStr));
    }

    private void sign() {
        if (mSha256Digester == null) {
            UIUtils.showLongToast(mActivity, R.string.feature_not_supported);
            return;
        }

        final MessagingPlugin messagingPlugin = mActivity.getMainService().getPlugin(MessagingPlugin.class);
        for (AttachmentTO attachment : mMessage.attachments) {
            try {
                if (!messagingPlugin.attachmentFile(mMessage, attachment).exists()) {
                    UIUtils.showAlertDialog(mActivity, R.string.activity_error, R.string
                            .open_attachments_before_signing);
                    return;
                }
            } catch (IOException e) {
                UIUtils.showErrorPleaseRetryDialog(mActivity);
                return;
            }
        }

        /** Signatures array contains 2 Strings:
         * 0. payload == null ? null : sign(the hash of the payload)
         * 1. sign(the hash of the message + the hash of the payload + the hash of all the attachments)
         */
        final String[] signatures = new String[2];
        final byte[] payloadHash = getPayloadHash();
        final MainService.SecurityCallback<byte[]> signMessageCallback = new MainService.SecurityCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                signatures[1] = result == null ? null : Base64.encodeBytes(result);

                mResult = Arrays.asList(signatures);
                mSignBtn.setVisibility(View.GONE);
                mSignResultView.setVisibility(View.VISIBLE);
                mActivity.excecutePositiveButtonClick();
            }

            @Override
            public void onError(Exception e) {
                if (!(e instanceof MainService.PinCancelledException)) {
                    L.bug(e);
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                }
            }
        };
        final MainService.SecurityCallback<byte[]> signPayloadCallback = new MainService.SecurityCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                signatures[0] = (payloadHash == null || result == null) ? null : Base64.encodeBytes(result);

                final List<byte[]> hashes = new ArrayList<>(mMessage.attachments.length + 2);
                hashes.add(sha256(mMessage.message));
                if (payloadHash != null) {
                    hashes.add(payloadHash);
                }

                if (mMessage.attachments.length != 0) {
                    for (AttachmentTO attachment : mMessage.attachments) {
                        mSha256Digester.reset();
                        try {
                            final File attachmentFile = messagingPlugin.attachmentFile(mMessage, attachment);
                            hashes.add(IOUtils.digest(mSha256Digester, attachmentFile));
                        } catch (IOException e) {
                            UIUtils.showErrorPleaseRetryDialog(mActivity);
                            return;
                        }
                    }
                }

                if (CloudConstants.DEBUG_LOGGING) {
                    for (int i = 0; i < hashes.size(); i++) {
                        L.d("Partial hash " + i + ": " + TextUtils.toHex(hashes.get(i)));
                    }
                }

                final byte[] hash = sha256(hashes.toArray(new byte[hashes.size()][]));
                L.i("Combined hash: " + TextUtils.toHex(hash));
                mActivity.getMainService().sign(mCaption, hash, payloadHash == null, signMessageCallback);
            }

            @Override
            public void onError(Exception e) {
                if (!(e instanceof MainService.PinCancelledException)) {
                    L.bug(e);
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                }
            }
        };

        if (payloadHash != null) {
            // Sign the payload
            mActivity.getMainService().sign(mCaption, payloadHash, true, signPayloadCallback);
        } else {
            signPayloadCallback.onSuccess(null);
        }
    }

    private byte[] sha256(byte[]... data) {
        T.UI();
        mSha256Digester.reset();
        for (byte[] bytes : data) {
            mSha256Digester.update(bytes);
        }
        return mSha256Digester.digest();
    }

    private byte[] sha256(String data) {
        return sha256(data.getBytes(StandardCharsets.UTF_8));
    }
}
