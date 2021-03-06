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

package com.mobicage.rogerthat.util.geo;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

public class GeoLocationListener implements LocationListener {

    private final GeoLocationReceiver mReceiver;

    public GeoLocationListener(GeoLocationReceiver pReceiver) {
        T.UI();
        mReceiver = pReceiver;
    }

    @Override
    public void onLocationChanged(Location location) {
        T.UI();
        L.d("GeoLocationListener onLocationChanged: " + location);
        if (location == null) {
            mReceiver.onLocationError();
        } else {
            mReceiver.onLocationReceived(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        T.UI();
        L.d("GeoLocationListener onProviderDisabled()");
        mReceiver.onLocationError();
    }

    @Override
    public void onProviderEnabled(String provider) {
        T.UI();
        L.d("GeoLocationListener onProviderEnabled()");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        T.UI();
        L.d("GeoLocationListener onStatusChanged() " + provider + " / " + status);
    }

}
