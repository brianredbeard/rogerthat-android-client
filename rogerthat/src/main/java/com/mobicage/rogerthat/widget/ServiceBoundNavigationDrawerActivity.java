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
package com.mobicage.rogerthat.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.ServiceFriendsActivity;
import com.mobicage.rogerthat.SettingsActivity;
import com.mobicage.rogerthat.UserFriendsActivity;
import com.mobicage.rogerthat.plugins.history.HistoryListActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;


public abstract class ServiceBoundNavigationDrawerActivity extends ServiceBoundActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected CustomDrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.homescreen_news);

        LinearLayout item = (LinearLayout )findViewById(R.id.linear_layout);
        View child = getLayoutInflater().inflate(layoutResID, null);
        item.addView(child);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Open navigation drawer automatically when the app start.
        drawer.openDrawer(GravityCompat.START);

        android.support.design.widget.NavigationView navigationView = (android.support.design.widget.NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Change color of the icons.
        navigationView.setItemIconTintList(null);

    }

    public void onOptionNavigationViewToolbarSelected(View v) {
        Intent intent;
        switch (v.getId()) {
            // Hide the navigation view
            case R.id.nav_hamburger:
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.ic_footer_01:
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.ic_footer_02:
                break;
            case R.id.ic_footer_03:
                break;
            case R.id.ic_footer_04:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        CustomDrawerLayout drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
////        noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_menu_01:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, MessagingActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_menu_02:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, ServiceFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_menu_03:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, UserFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_menu_04:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, ScanTabActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_menu_05:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_menu_06:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_menu_07:
                intent = new Intent(ServiceBoundNavigationDrawerActivity.this, HistoryListActivity.class);
                startActivity(intent);
                break;
        }
        CustomDrawerLayout drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
