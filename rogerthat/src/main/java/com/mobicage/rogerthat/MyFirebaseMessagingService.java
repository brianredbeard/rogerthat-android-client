package com.mobicage.rogerthat;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mobicage.rogerthat.util.logging.L;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        L.d("MyFirebaseMessagingService.onMessageReceived: " + remoteMessage.getFrom());
        Intent service = new Intent(this, MainService.class);
        service.putExtra(MainService.START_INTENT_FCM, true);
        startService(service);
    }
}
