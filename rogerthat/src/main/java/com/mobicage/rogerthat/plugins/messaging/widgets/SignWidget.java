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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.models.properties.profiles.PublicKeyTO;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.security.SecurityPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.forms.SignWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitSignFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitSignFormResponseTO;

import org.jivesoftware.smack.util.Base64;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SignWidget extends Widget {

    private Button mSignBtn;
    private View mSignResultView;
    private SignWidgetResultTO mResult;
    private String mKeyAlgorithm;
    private String mKeyName;
    private Long mKeyIndex;
    private String mCaption;
    private String mPublicKey;

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

        Map<String, Object> result = (Map<String, Object>) mWidgetMap.get("value");
        if (result != null) {
            try {
                mResult = new SignWidgetResultTO(result);
            } catch (IncompleteMessageException e) {
                L.bug(e);
            }
        }
        if (mResult != null) {
            mSignBtn.setVisibility(View.GONE);
            mSignResultView.setVisibility(View.VISIBLE);
        }
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        return widget.get("value") != null ? context.getString(R.string.signed) : null;
    }

    @Override
    public void putValue() {
        mWidgetMap.put("value", mResult == null ? null : mResult.toJSONMap());
    }

    @Override
    public SignWidgetResultTO getWidgetResult() {
        return mResult;
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

    private byte[] getPayload() throws Exception {
        final String payload = (String) mWidgetMap.get("payload");
        if (payload == null) {
            return null;
        }
        return SecurityUtils.getPayload(mKeyAlgorithm, Base64.decode(payload));
    }

    private void sign() {
        try {
            mPublicKey = SecurityUtils.getPublicKeyString(mActivity.getMainService(), mKeyAlgorithm, mKeyName, mKeyIndex);
        } catch (Exception e) {
            L.bug("Error while getting public key", e);
            UIUtils.showErrorPleaseRetryDialog(mActivity);
            return;
        }
        if (mPublicKey == null) {
            UIUtils.showLongToast(mActivity, R.string.key_not_found);
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

        /* Signatures array contains 2 Strings:
          0. payload == null ? null : sign(the hash of the payload)
          1. sign(the hash of the message + the hash of the payload + the hash of all the attachments)
         */
        final String[] signatures = new String[2];
        final byte[] payload;
        try {
            payload = getPayload();
        } catch (Exception e) {
            L.d("Failed to get payload", e);
            UIUtils.showErrorPleaseRetryDialog(mActivity);
            return;
        }
        final MainService.SecurityCallback<String> signMessageCallback = new MainService.SecurityCallback<String>() {
            @Override
            public void onSuccess(String result) {
                signatures[1] = result;

                mResult = new SignWidgetResultTO();
                mResult.payload_signature = signatures[0];
                mResult.total_signature = signatures[1];
                mResult.public_key = new PublicKeyTO();
                mResult.public_key.algorithm = mKeyAlgorithm;
                mResult.public_key.index = SecurityPlugin.getIndexString(mKeyIndex);
                mResult.public_key.name = mKeyName;
                mResult.public_key.public_key = mPublicKey;

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
        final MainService.SecurityCallback<String> signPayloadCallback = new MainService.SecurityCallback<String>() {
            @Override
            public void onSuccess(String result) {
                signatures[0] = result;

                final List<byte[]> hashes = new ArrayList<>(mMessage.attachments.length + 2);
                try {
                    hashes.add(SecurityUtils.getPayload(mKeyAlgorithm, mMessage.message));
                } catch (Exception e) {
                    L.d("Failed to get message hash", e);
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                    return;
                }
                if (payload != null) {
                    hashes.add(payload);
                }

                if (mMessage.attachments.length != 0) {
                    for (AttachmentTO attachment : mMessage.attachments) {
                        final File attachmentFile;
                        try {
                            attachmentFile = messagingPlugin.attachmentFile(mMessage, attachment);
                        } catch (IOException e) {
                            UIUtils.showErrorPleaseRetryDialog(mActivity);
                            return;
                        }
                        try {
                            hashes.add(SecurityUtils.getPayload(mKeyAlgorithm, attachmentFile));
                        } catch (Exception e) {
                            L.d("Failed to get attachment hash", e);
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
                final byte[] hash;
                try {
                    hash = SecurityUtils.getPayload(mKeyAlgorithm, hashes.toArray(new byte[hashes.size()][]));
                } catch (Exception e) {
                    L.d("Failed to get attachments hash", e);
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                    return;
                }
                L.i("Combined hash: " + TextUtils.toHex(hash));
                mActivity.getMainService().sign(mKeyAlgorithm, mKeyName, mKeyIndex, mCaption, hash, payload == null, signMessageCallback);
            }

            @Override
            public void onError(String code, String errorMessage) {
                if (!"user_cancelled_pin_input".equals(code)) {
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                }
             }
        };

        if (payload != null) {
            // Sign the payload
            mActivity.getMainService().sign(mKeyAlgorithm, mKeyName, mKeyIndex, mCaption, payload, true, signPayloadCallback);
        } else {
            signPayloadCallback.onSuccess(null);
        }
    }
}
