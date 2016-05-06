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

package com.mobicage.rogerthat.util.pickle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

// Format of pickled data
//
// int     pickle version (currently 1)
// String  class name
// int     class version
// << pickled class content >>
//

public class Pickler {

    private final static int PICKLE_VERSION = 1;

    public static Pickleable createObjectFromPickle(byte[] pickledObject) throws PickleException {
        T.dontCare();
        try {
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(pickledObject));
            int version = is.readInt();
            if (version != PICKLE_VERSION) {
                // No idea how to unpickle this.
                throw new PickleException("Cannot unpickle version " + version);
            }
            String klazz = is.readUTF();
            int klazzVersion = is.readInt();
            Class<?> klazzObject = Class.forName(klazz);
            try {
                Constructor<?> constructor = klazzObject.getConstructor(Pickle.class);
                return (Pickleable) constructor.newInstance(new Pickle(klazzVersion, is));
            } catch (NoSuchMethodException e) {
                Pickleable obj = (Pickleable) klazzObject.newInstance();
                obj.readFromPickle(klazzVersion, is);
                return obj;
            }
        } catch (Exception e) {
            L.d(e);
            throw new PickleException(e);
        }
    }

    public static byte[] getPickleFromObject(Pickleable obj) throws PickleException {
        T.dontCare();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(PICKLE_VERSION);
            dos.writeUTF(obj.getClass().getName());
            dos.writeInt(obj.getPickleClassVersion());
            obj.writePickle(dos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new PickleException(e);
        }
    }

}