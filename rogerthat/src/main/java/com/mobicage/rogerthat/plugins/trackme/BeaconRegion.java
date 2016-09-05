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
package com.mobicage.rogerthat.plugins.trackme;

import org.altbeacon.beacon.Identifier;

import com.mobicage.to.beacon.BeaconRegionTO;

public class BeaconRegion extends BeaconRegionTO {

    public static String getUniqueRegionId(BeaconRegionTO br) {
        return br.uuid + "|" + getMajor(br) + "|" + getMinor(br);
    }

    public static Integer getMajor(BeaconRegionTO br) {
        return br.has_major ? Long.valueOf(br.major).intValue() : null;
    }

    public static Integer getMinor(BeaconRegionTO br) {
        return br.has_minor ? Long.valueOf(br.minor).intValue() : null;
    }

    public static Identifier getId1(BeaconRegionTO br) {
        return Identifier.parse(br.uuid);
    }

    public static Identifier getId2(BeaconRegionTO br) {
        return br.has_major ? Identifier.fromInt(Long.valueOf(br.major).intValue()) : null;
    }

    public static Identifier getId3(BeaconRegionTO br) {
        return br.has_minor ? Identifier.fromInt(Long.valueOf(br.minor).intValue()) : null;
    }

    public String getUniqueRegionId() {
        return getUniqueRegionId(this);
    }

    public String getUUID() {
        return uuid;
    }

    public Integer getMajor() {
        return getMajor(this);
    }

    public Integer getMinor() {
        return getMinor(this);
    }

    public Identifier getId1() {
        return getId1(this);
    }

    public Identifier getId2() {
        return getId2(this);
    }

    public Identifier getId3() {
        return getId3(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (has_major ? 1231 : 1237);
        result = prime * result + (has_minor ? 1231 : 1237);
        result = prime * result + (int) (major ^ (major >>> 32));
        result = prime * result + (int) (minor ^ (minor >>> 32));
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BeaconRegionTO other = (BeaconRegionTO) obj;
        if (has_major != other.has_major)
            return false;
        if (has_minor != other.has_minor)
            return false;
        if (major != other.major)
            return false;
        if (minor != other.minor)
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

}
