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
package com.mobicage.rogerthat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.trackme.MapDetailActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetLocationActivity extends ServiceBoundActivity {

    private final static int TURNING_ON_GPS = 1;
    private final static int LOCATION_REQUEST_CODE = 2;

    private final static int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Button mGetCurrentLocationButton;
    private EditText mAddress;
    private Button mCalculate;
    private CheckBox mUseGPS;
    private LocationManager mLocationManager;
    private Location mLocation;
    private String mLocationProviderUpdates = null;
    private ProgressDialog mProgressDialog;
    private long mLocationQueryStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case TURNING_ON_GPS:
            if (mLocationManager != null)
                mUseGPS.setChecked(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            break;
        case LOCATION_REQUEST_CODE:
            if (data != null && data.getBooleanExtra(MapDetailActivity.VERIFIED, false)) {
                Intent intent = getIntent();
                intent.putExtra(MapDetailActivity.LATITUDE, mLocation.getLatitude());
                intent.putExtra(MapDetailActivity.LONGITUDE, mLocation.getLongitude());
                setResult(RESULT_OK, intent);
                finish();
            }
            break;
        }
    }


    @Override
    protected void onServiceBound() {
        T.UI();
        setContentView(R.layout.get_location);
        setTitle(R.string.get_location);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.updating_location));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                T.UI();
                if (mLocationManager != null) {
                    try {
                        mLocationManager.removeUpdates(mLocationListener);
                    } catch (SecurityException e) {
                        L.bug(e); // Should never happen
                    }
                }
            }
        });
        mProgressDialog.setMax(10000);

        mUseGPS = (CheckBox) findViewById(R.id.use_gps_provider);
        mGetCurrentLocationButton = (Button) findViewById(R.id.get_current_location);
        mAddress = (EditText) findViewById(R.id.address);
        mCalculate = (Button) findViewById(R.id.calculate);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager == null) {
            mGetCurrentLocationButton.setEnabled(false);
        } else {
            mUseGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        new AlertDialog.Builder(GetLocationActivity.this).setMessage(R.string.gps_is_not_enabled)
                            .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, TURNING_ON_GPS);
                                }
                            }).setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int which) {
                                    mUseGPS.setChecked(false);
                                }
                            }).create().show();
                    }
                }
            });

            mGetCurrentLocationButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    T.UI();
                    if (mService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        getMyLocation();
                    } else {
                        ActivityCompat.requestPermissions(GetLocationActivity.this, new String[] {Manifest.permission
                                .ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
            });
        }

        mCalculate.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final ProgressDialog pd = new ProgressDialog(GetLocationActivity.this);
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setMessage(getString(R.string.updating_location));
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
                final String addressText = mAddress.getText().toString();
                mService.postOnIOHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        Geocoder geoCoder = new Geocoder(GetLocationActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geoCoder.getFromLocationName(addressText, 5);
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                final Location location = new Location("");
                                location.setLatitude(address.getLatitude());
                                location.setLongitude(address.getLongitude());
                                mService.postOnUIHandler(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        pd.dismiss();
                                        mLocation = location;
                                        showMap();
                                    }
                                });
                                return;
                            }
                        } catch (IOException e) {
                            L.d("Failed to geo code address " + addressText, e);
                        }
                        mService.postOnUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                pd.dismiss();
                                UIUtils.showLongToast(GetLocationActivity.this,
                                    getString(R.string.failed_to_lookup_address));
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getMyLocation() {
        mLocationProviderUpdates = mUseGPS.isChecked() ? LocationManager.GPS_PROVIDER
            : LocationManager.NETWORK_PROVIDER;
        mLocation = null;
        try {
            mLocationManager.requestLocationUpdates(mLocationProviderUpdates, 0, 0, mLocationListener);
        } catch (SecurityException e) {
            L.bug(e); // Should never happen
        }
        mLocationQueryStart = System.currentTimeMillis();
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
        mService.postDelayedOnUIHandler(mUpdateProgress, 100);
    }

    @Override
    protected void onServiceUnbound() {
    }

    private SafeRunnable mUpdateProgress = new SafeRunnable() {
        @Override
        protected void safeRun() throws Exception {
            T.UI();
            if (mLocationProviderUpdates == null)
                return;
            int progress = (int) (System.currentTimeMillis() - mLocationQueryStart) / 4;
            if (progress > 10000) {
                if (mLocationManager != null) {
                    try {
                        mLocationManager.removeUpdates(mLocationListener);
                    } catch (SecurityException e) {
                        L.bug(e);
                    }
                }
                mProgressDialog.dismiss();
                UIUtils.showLongToast(GetLocationActivity.this, getString(R.string.failed_to_get_updated_location));
            } else {
                mProgressDialog.setProgress(progress);
                mService.postDelayedOnUIHandler(mUpdateProgress, 100);
            }

        }
    };

    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // do nothing
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider))
                mUseGPS.setEnabled(true);
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider))
                mUseGPS.setEnabled(false);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocationProviderUpdates = null;
            mLocation = location;
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException e) {
                L.bug(e); // Should never happen
            }
            mProgressDialog.dismiss();
            showMap();
        }
    };

    private void showMap() {
        Intent intent = new Intent(this, MapDetailActivity.class);
        intent.putExtra(MapDetailActivity.LATITUDE, new Float((Double) mLocation.getLatitude()).floatValue());
        intent.putExtra(MapDetailActivity.LONGITUDE, new Float((Double)mLocation.getLongitude()).floatValue());
        intent.putExtra(MapDetailActivity.VERIFY, true);
        startActivityForResult(intent, LOCATION_REQUEST_CODE);
    }

}
