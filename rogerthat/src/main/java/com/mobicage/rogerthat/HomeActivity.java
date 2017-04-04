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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryListActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

import java.util.ArrayList;

public class HomeActivity extends AbstractHomeActivity {

    @Override
    ItemDef[] getItemDefs() {
        return new ItemDef[]{};
    }
}
