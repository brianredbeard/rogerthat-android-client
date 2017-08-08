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

package com.mobicage.capi.payment;

public interface IClientRpc {

    com.mobicage.to.payment.UpdatePaymentAssetResponseTO updatePaymentAsset(com.mobicage.to.payment.UpdatePaymentAssetRequestTO request) throws java.lang.Exception;

    com.mobicage.to.payment.UpdatePaymentAssetsResponseTO updatePaymentAssets(com.mobicage.to.payment.UpdatePaymentAssetsRequestTO request) throws java.lang.Exception;

    com.mobicage.to.payment.UpdatePaymentProviderResponseTO updatePaymentProvider(com.mobicage.to.payment.UpdatePaymentProviderRequestTO request) throws java.lang.Exception;

    com.mobicage.to.payment.UpdatePaymentProvidersResponseTO updatePaymentProviders(com.mobicage.to.payment.UpdatePaymentProvidersRequestTO request) throws java.lang.Exception;

    com.mobicage.to.payment.UpdatePaymentStatusResponseTO updatePaymentStatus(com.mobicage.to.payment.UpdatePaymentStatusRequestTO request) throws java.lang.Exception;

}
