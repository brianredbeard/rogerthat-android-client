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
package com.mobicage.rogerthat.plugins.trackme;

public class BeaconProximity {
    /**
     * Less than half a meter away
     */
    public static final int PROXIMITY_IMMEDIATE = 1;
    /**
     * More than half a meter away, but less than four meters away
     */
    public static final int PROXIMITY_NEAR = 2;
    /**
     * More than four meters away
     */
    public static final int PROXIMITY_FAR = 3;
    /**
     * No distance estimate was possible due to a bad RSSI value or measured TX power
     */
    public static final int PROXIMITY_UNKNOWN = 0;

    public String uuid;
    public int major;
    public int minor;
    public String tag;
    public int proximity;

    // Copied from IBeacon.calculateProximity of the RadiusNetwork 0.x library
    public static int calculateProximity(double distance) {
        if (distance < 0) {
            return PROXIMITY_UNKNOWN;
        }
        if (distance < 0.5) {
            return PROXIMITY_IMMEDIATE;
        }
        // forums say 3.0 is the near/far threshold, but it looks to be based on experience that this is 4.0
        if (distance <= 4.0) {
            return PROXIMITY_NEAR;
        }
        // if it is > 4.0 meters, call it far
        return PROXIMITY_FAR;
    }

}
