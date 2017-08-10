/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */

package com.mobicage.rogerthat.plugins.messaging.widgets;

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
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
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
    private String mKeyAlgorithm;
    private String mKeyName;
    private Long mKeyIndex;
    private String mCaption;

    public SignWidget(Context context) {
        super(context);
    }

    public SignWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        mSignResultView = findViewById(R.id.sign_result);
        mSignBtn = (Button) findViewById(R.id.sign_btn);
        mSignBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                sign();
            }
        });

        mKeyAlgorithm = (String) mWidgetMap.get("algorithm");
        mKeyName = (String) mWidgetMap.get("key_name");
        String index = (String) mWidgetMap.get("index");
        mKeyIndex = index == null ? null : Long.getLong(index);

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
                String message = mActivity.getString(R.string.sign_first);
                String positiveCaption = (String) mSignBtn.getText();
                String negativeCaption = mActivity.getString(R.string.cancel);
                SafeDialogClick onPositiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        sign();
                    }
                };
                UIUtils.showDialog(mActivity, null, message, positiveCaption, onPositiveClick, negativeCaption, null);
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
        return payloadStr == null ? null : SecurityUtils.sha256Digest(Base64.decode(payloadStr));
    }

    private void sign() {
        if (!SecurityUtils.hasKey(mActivity.getMainService(), "public", mKeyAlgorithm, mKeyName, mKeyIndex)) {
            UIUtils.showLongToast(mActivity, R.string.key_not_found);
            return;
        }

        try {
            MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            UIUtils.showLongToast(mActivity, R.string.feature_not_supported);
            return;
        }

        final MessagingPlugin messagingPlugin = mActivity.getMainService().getPlugin(MessagingPlugin.class);
        for (AttachmentTO attachment : mMessage.attachments) {
            try {
                if (!messagingPlugin.attachmentFile(mMessage, attachment).exists()) {
                    UIUtils.showDialog(mActivity, R.string.activity_error, R.string
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
            public void onError(String code, String errorMessage) {
                if (!"user_cancelled_pin_input".equals(code)) {
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                }
            }
        };
        final MainService.SecurityCallback<byte[]> signPayloadCallback = new MainService.SecurityCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                signatures[0] = (payloadHash == null || result == null) ? null : Base64.encodeBytes(result);

                final List<byte[]> hashes = new ArrayList<>(mMessage.attachments.length + 2);
                hashes.add(SecurityUtils.sha256Digest(mMessage.message));
                if (payloadHash != null) {
                    hashes.add(payloadHash);
                }

                if (mMessage.attachments.length != 0) {
                    for (AttachmentTO attachment : mMessage.attachments) {
                        try {
                            final File attachmentFile = messagingPlugin.attachmentFile(mMessage, attachment);
                            hashes.add(SecurityUtils.sha256Digest(attachmentFile));
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

                final byte[] hash = SecurityUtils.sha256Digest(hashes.toArray(new byte[hashes.size()][]));
                L.i("Combined hash: " + TextUtils.toHex(hash));
                mActivity.getMainService().sign(mKeyAlgorithm, mKeyName, mKeyIndex, mCaption, hash, payloadHash == null, signMessageCallback);
            }

            @Override
            public void onError(String code, String errorMessage) {
                if (!"user_cancelled_pin_input".equals(code)) {
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                }
             }
        };

        if (payloadHash != null) {
            // Sign the payload
            mActivity.getMainService().sign(mKeyAlgorithm, mKeyName, mKeyIndex, mCaption, payloadHash, true, signPayloadCallback);
        } else {
            signPayloadCallback.onSuccess(null);
        }
    }
}
