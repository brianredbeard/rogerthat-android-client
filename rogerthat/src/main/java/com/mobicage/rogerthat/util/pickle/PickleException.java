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

package com.mobicage.rogerthat.util.pickle;

import com.mobicage.rogerthat.util.system.T;

public class PickleException extends Exception {

    private static final long serialVersionUID = -8555125939032183363L;

    public PickleException(String errorMsg) {
        super(errorMsg);
        T.dontCare();
    }

    public PickleException(Exception e) {
        super(e);
        T.dontCare();
    }

}
