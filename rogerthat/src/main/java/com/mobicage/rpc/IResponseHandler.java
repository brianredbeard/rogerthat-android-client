/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rpc;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.pickle.Pickleable;

// XXX: setService is an ugly hack - remove from interface
// but keep FinishRegistrationResponseHandler working

public interface IResponseHandler<T> extends Pickleable {

    void handle(IResponse<T> response);

    void setService(MainService pService);

    String getFunction();

    void setFunction(String function);

}
