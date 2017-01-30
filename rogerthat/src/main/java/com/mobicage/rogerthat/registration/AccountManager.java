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
package com.mobicage.rogerthat.registration;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.mobicage.rogerthat.util.logging.L;

public class AccountManager {

    protected Context mContext;

    public class Account {
        public String name;
        public String type;
    }

    public AccountManager(Activity context) {
        mContext = context;
    }

    public List<Account> getAccounts() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.GET_ACCOUNTS) != PackageManager
                .PERMISSION_GRANTED) {
            L.w("GET_ACCOUNTS permission not granted!");
            return new ArrayList<>(0);
        }

        android.accounts.AccountManager accountManager = (android.accounts.AccountManager) mContext
            .getSystemService(Context.ACCOUNT_SERVICE);

        android.accounts.Account[] accounts = accountManager.getAccounts();
        List<Account> result = new ArrayList<>(accounts.length);
        for (android.accounts.Account account_ : accounts) {
            Account account = new Account();
            account.name = account_.name;
            account.type = account_.type;
            result.add(account);
        }
        return result;
    }
}
