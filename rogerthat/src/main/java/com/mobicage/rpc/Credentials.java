/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.rpc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.util.pickle.Pickle;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickleable;
import com.mobicage.rogerthat.util.system.T;

public class Credentials implements Pickleable {

    // Pickled details
    private final String username;
    private final String password;

    // Non pickled details
    private final String xmppAccount;
    private final String xmppServiceName;

    public Credentials(final String pUsername, final String pPassword) {
        T.dontCare();
        username = pUsername;
        password = pPassword;
        final String[] parts = username.split("@");
        if (parts.length == 2) {
            xmppAccount = parts[0];
            xmppServiceName = parts[1];
        } else {
            throw new IllegalArgumentException("Illegal arguments for Credentials constructor");
        }
    }

    public Credentials(final Pickle pickle) throws IOException {
        this(pickle.dataInput.readUTF(), pickle.dataInput.readUTF());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getXmppAccount() {
        return xmppAccount;
    }

    public String getXmppServiceName() {
        return xmppServiceName;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(password);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
    }

    @Override
    public int getPickleClassVersion() {
        return 1;
    }

}