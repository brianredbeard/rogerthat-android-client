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

package com.mobicage.rogerthat.util.system;

import android.os.AsyncTask;

import com.mobicage.rogerthat.util.logging.L;

public abstract class SafeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected abstract Result safeDoInBackground(Params... params);

    protected void safeOnPostExecute(Result result) {
    }

    protected void safeOnCancelled(Result result) {
    }

    protected void safeOnProgressUpdate(Progress... values) {
    }

    protected void safeOnPreExecute() {
    }

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return safeDoInBackground(params);
        } catch (Throwable t) {
            L.bug(t);
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        T.UI();
        try {
            safeOnPreExecute();
        } catch (Throwable t) {
            L.bug(t);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        T.UI();
        try {
            safeOnPostExecute(result);
        } catch (Throwable t) {
            L.bug(t);
        }
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        try {
            safeOnProgressUpdate(values);
        } catch (Throwable t) {
            L.bug(t);
        }
    }

    @Override
    protected void onCancelled(Result result) {
        try {
            safeOnCancelled(result);
        } catch (Throwable t) {
            L.bug(t);
        }
    }
}
