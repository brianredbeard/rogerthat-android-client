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

package com.mobicage.rogerthat.plugins.payment;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.payment.GetPaymentTransactionsResponseTO;

import org.json.simple.JSONValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class GetPaymentTransactionsResponseHandler extends ResponseHandler<GetPaymentTransactionsResponseTO> {

    private String mCallbackKey;

    public void setCallbackKey(String callbackKey) {
        mCallbackKey = callbackKey;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mCallbackKey);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mCallbackKey = in.readUTF();
    }

    @Override
    public void handle(final IResponse<GetPaymentTransactionsResponseTO> result) {
        T.BIZZ();
        GetPaymentTransactionsResponseTO response;
        try {
            response = result.getResponse();
        } catch (Exception e) {
            L.d("Get payment transactions api call failed", e);
            Intent intent = new Intent(PaymentPlugin.GET_PAYMENT_TRANSACTIONS_FAILED_INTENT);
            intent.putExtra("callback_key", mCallbackKey);
            mMainService.sendBroadcast(intent);
            return;
        }

        Intent intent = new Intent(PaymentPlugin.GET_PAYMENT_TRANSACTIONS_RESULT_INTENT);
        intent.putExtra("json", JSONValue.toJSONString(response.toJSONMap()));
        intent.putExtra("callback_key", mCallbackKey);
        mMainService.sendBroadcast(intent);
    }

}
