package com.anjali.viewpager.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService() {

    //here we receive messages from firestore cloud messaging

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.notification != null){

            //show notification only if we r not online or can say tha when we r not currently in chat channel from which incoming message is sent

            Log.d("FCM",remoteMessage.data.toString())
        }
        super.onMessageReceived(remoteMessage)
    }
}