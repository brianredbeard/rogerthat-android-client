package com.mobicage.rogerthat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;

public abstract class ServiceBoundCursorListActivityNavigationView extends ServiceBoundCursorListActivity
        implements android.support.design.widget.NavigationView.OnNavigationItemSelectedListener {

    ImageButton ib_hamburger, ib_newspaper, ib_shopping_basket, ib_calendar, ib_credit_car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_navigation_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout item = (LinearLayout) findViewById(R.id.linear_layout);
        View child = getLayoutInflater().inflate(layoutResID, null);
        item.addView(child);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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

        //Change font
        ib_hamburger = (ImageButton) findViewById(R.id.nav_hamburger);
        ib_newspaper = (ImageButton) findViewById(R.id.nav_newspaper);
        ib_shopping_basket = (ImageButton) findViewById(R.id.nav_shopping_basket);
        ib_calendar = (ImageButton) findViewById(R.id.nav_calendar);
        ib_credit_car = (ImageButton) findViewById(R.id.nav_credit_card);
    }

    public void onOptionNavigationViewToolbarSelected(View v) {
        Intent intent;
        switch (v.getId()) {
            // Hide the navigation view
            case R.id.nav_hamburger:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                break;
            //Launch Newspaper Activity
            case R.id.nav_newspaper:
                intent = new Intent(this, MessagingActivity.class);
                startActivity(intent);
                break;
            //Launch Shopping Basket Activity
            case R.id.nav_shopping_basket:
//                intent = new Intent(NavigationView.this, TermsAndConditions.class);
//                startActivity(intent);
                break;
            //Launch Calendar Activity
            case R.id.nav_calendar:
                break;
            //Launch Credit Card Activity
            case R.id.nav_credit_card:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
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
            case R.id.nav_messages:
                intent = new Intent(this, MessagingActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_report_card:
//                intent = new Intent(NavigationView.this, ReportCard.class);
//                startActivity(intent);
                break;
            case R.id.nav_agenda:
                break;
            case R.id.nav_community_services:
//                intent = new Intent(NavigationView.this, CommunityServices.class);
//                startActivity(intent);
                break;
            case R.id.nav_merchants:
//                intent = new Intent(NavigationView.this, Merchants.class);
//                startActivity(intent);
                break;
            case R.id.nav_associations:
                break;
            case R.id.nav_scan:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}