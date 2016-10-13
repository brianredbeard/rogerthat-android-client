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

package com.mobicage.rogerthat.registration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rpc.config.AppConstants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectedBeaconActivity extends ServiceBoundActivity {

    public static final String EXTRA_DETECTED_BEACONS = "detected_beacons";
    public static final String EXTRA_AGE_AND_GENDER_SET = "age_and_gender_set";

    private FriendsPlugin mFriendsPlugin;
    private boolean mAgeGenderSet;
    private List<String> mInvites;
    private final Map<String, Bitmap> mAvatars = new HashMap<String, Bitmap>();

    @Override
    protected void onServiceBound() {
        setContentViewWithoutNavigationBar(R.layout.detected_beacons);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        Intent intent = getIntent();
        mAgeGenderSet = intent.getBooleanExtra(EXTRA_AGE_AND_GENDER_SET, true);
        displayDiscoverdBeacons(intent.getStringExtra(EXTRA_DETECTED_BEACONS));

        ((TextView) findViewById(R.id.detected_services_title)).setText(getString(R.string.detected_services_title,
            getString(R.string.app_name)));
    }

    private void displayDiscoverdBeacons(String jsonResult) {
        T.UI();
        mInvites = new ArrayList<String>();

        JSONArray discoveredBeaconsJSON = (JSONArray) JSONValue.parse(jsonResult);
        ArrayList<DetectedBeacon> discoveredBeacons = new ArrayList<DetectedBeacon>();
        for (Object discoveredBeacon : discoveredBeaconsJSON) {
            DetectedBeacon db = new DetectedBeacon();
            JSONObject db1 = (JSONObject) discoveredBeacon;
            db.name = (String) db1.get("name");
            db.friendEmail = (String) db1.get("friend_email");
            db.avatarUrl = (String) db1.get("avatar_url");
            discoveredBeacons.add(db);
            mInvites.add(db.friendEmail);
        }
        L.d("displayDiscoverdBeacons");

        DetectedBeaconsListAdapter matchesAdapter = new DetectedBeaconsListAdapter(discoveredBeacons);

        final Button btnSent = (Button) findViewById(R.id.sent_invites);
        btnSent.setEnabled(mInvites.size() > 0);
        btnSent.setOnClickListener(new SafeViewOnClickListener() {

            @Override
            public void safeOnClick(View v) {
                for (String email : mInvites) {
                    mFriendsPlugin.inviteFriend(email, null, null, false);
                }
                finish();
            }
        });

        final Button btnSkip = (Button) findViewById(R.id.skip_button);
        btnSkip.setOnClickListener(new SafeViewOnClickListener() {

            @Override
            public void safeOnClick(View v) {
                finish();
            }
        });

        ListView list = (ListView) findViewById(R.id.detected_beacons_list);
        list.setAdapter(matchesAdapter);
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(DetectedBeaconActivity.this, MainActivity.class);
        if (AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE && !mAgeGenderSet) {
            intent.setAction(MainActivity.ACTION_COMPLETE_PROFILE);
        } else {
            intent.setAction(MainActivity.ACTION_REGISTERED);
        }
        intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
        startActivity(intent);
        super.onStop();
    }

    private class DetectedBeaconsListAdapter extends BaseAdapter {

        private final List<DetectedBeacon> mDetectedBeacons;

        public DetectedBeaconsListAdapter(List<DetectedBeacon> detectedBeacons) {
            mDetectedBeacons = detectedBeacons;
        }

        @Override
        public int getCount() {
            T.UI();
            return mDetectedBeacons.size();
        }

        @Override
        public Object getItem(int position) {
            T.UI();
            return null;
        }

        @Override
        public long getItemId(int position) {
            T.UI();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewInfoHolder holder = null;
            if (v == null || v.getTag() == null) {
                v = getLayoutInflater().inflate(R.layout.detected_beacon, parent, false);
                holder = new ViewInfoHolder();
                holder.avatarView = (ImageView) v.findViewById(R.id.avatar);
                holder.friendName = (TextView) v.findViewById(R.id.friend_name);
                holder.checkBox = (CheckBox) v.findViewById(R.id.invite_checkbox);
            } else {
                holder = (ViewInfoHolder) v.getTag();
            }

            final DetectedBeacon db = mDetectedBeacons.get(position);
            holder.detectedBeacon = db;
            v.setTag(holder);

            setDetectedBeaconOnView(db, v, holder);
            return v;
        }
    }

    private class ViewInfoHolder {
        DetectedBeacon detectedBeacon;
        TextView friendName;
        ImageView avatarView;
        CheckBox checkBox;
    }

    private void setDetectedBeaconOnView(final DetectedBeacon db, final View v, final ViewInfoHolder holder) {
        ((ImageView) v.findViewById(R.id.avatar)).setImageBitmap(getAvatar(db));
        ((TextView) v.findViewById(R.id.friend_name)).setText(db.name);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mInvites.add(holder.detectedBeacon.friendEmail);
                } else {
                    mInvites.remove(holder.detectedBeacon.friendEmail);
                }
                final Button btnSent = (Button) findViewById(R.id.sent_invites);
                btnSent.setEnabled(mInvites.size() > 0);
            }
        });

        holder.checkBox.setChecked(mInvites != null && mInvites.contains(holder.detectedBeacon.friendEmail));
    }

    private Bitmap getAvatar(final DetectedBeacon db) {
        Bitmap avatar;
        if (mAvatars.containsKey(db.friendEmail)) {
            avatar = mAvatars.get(db.friendEmail);
        } else {
            avatar = null;
            L.d("Downloading avatar: " + db.avatarUrl);
            mAvatars.put(db.friendEmail, null);
            new SafeAsyncTask<Object, Object, Object>() {
                @Override
                protected Object safeDoInBackground(Object... params) {
                    try {
                        return BitmapFactory.decodeStream((InputStream) new URL((String) params[0]).getContent());
                    } catch (MalformedURLException e) {
                        L.bug("Could not download avatar: " + db.avatarUrl, e);
                    } catch (IOException e) {
                        L.bug("Could not download avatar: " + db.avatarUrl, e);
                    } catch (Exception e) {
                        L.bug("Could not download avatar: " + db.avatarUrl, e);
                    }
                    return null;
                };

                @Override
                protected void safeOnPostExecute(Object result) {
                    Bitmap bitmap = (Bitmap) result;
                    if (bitmap != null) {
                        mAvatars.put(db.friendEmail, bitmap);
                        ListView listView = (ListView) findViewById(R.id.detected_beacons_list);
                        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                protected void safeOnCancelled(Object result) {
                }

                @Override
                protected void safeOnProgressUpdate(Object... values) {
                }

                @Override
                protected void safeOnPreExecute() {
                };
            }.execute(db.avatarUrl);
        }
        if (avatar == null)
            avatar = mFriendsPlugin.getMissingFriendAvatarBitmap();
        else
            avatar = ImageHelper.getRoundedCornerAvatar(avatar);
        return avatar;
    }

}
