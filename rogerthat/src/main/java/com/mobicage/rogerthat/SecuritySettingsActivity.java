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

package com.mobicage.rogerthat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.security.PublicKeyInfo;
import com.mobicage.rogerthat.plugins.security.SecurityPlugin;

import java.util.List;

public class SecuritySettingsActivity extends ServiceBoundActivity {

    public static final int REQUEST_IMPORT_KEY = 1;

    private List<PublicKeyInfo> mPublicKeys;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.security);
        setContentView(R.layout.security_settings);
    }

    @Override
    protected void onServiceBound() {
        mListView = (ListView) findViewById(R.id.keys_list);
        refreshList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PublicKeyInfo publicKey = mPublicKeys.get(position);
                final Intent intent = new Intent(SecuritySettingsActivity.this, SecurityKeyActivity.class);
                intent.putExtra(SecurityKeyActivity.KEY_NAME, publicKey.name);
                intent.putExtra(SecurityKeyActivity.KEY_ALGORITHM, publicKey.algorithm);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onServiceUnbound() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.security_menu, menu);
        addIconToMenuItem(menu, R.id.import_key, FontAwesome.Icon.faw_download);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.import_key:
                Intent intent = new Intent(this, ImportSecurityKeyActivity.class);
                startActivityForResult(intent, REQUEST_IMPORT_KEY);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMPORT_KEY) {
            refreshList();
        }
    }

    private void refreshList() {
        mPublicKeys = mService.getPlugin(SecurityPlugin.class).getStore().listPublicKeys();
        mListView.setAdapter(new ArrayAdapter<PublicKeyInfo>(this, R.layout.security_key_item, mPublicKeys) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                final View view;
                if (convertView == null) {
                    final LayoutInflater inflater = LayoutInflater.from(SecuritySettingsActivity.this);
                    view = inflater.inflate(R.layout.security_key_item, parent, false);
                } else {
                    view = convertView;
                }

                final PublicKeyInfo publicKey = getItem(position);
                if (publicKey != null) {
                    ((TextView) view.findViewById(R.id.title)).setText(publicKey.name);
                    ((TextView) view.findViewById(R.id.subtitle)).setText(publicKey.algorithm);
                }

                return view;
            }
        });
    }
}
