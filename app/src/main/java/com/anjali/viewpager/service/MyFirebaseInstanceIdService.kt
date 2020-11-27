package com.anjali.viewpager.service

import com.anjali.viewpager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import util.FireBaseUtil
import java.lang.NullPointerException

class MyFirebaseInstanceIdService: FirebaseInstanceIdService() {

    //firebse messaging uses token to identify devices,hence we need to remember it and which user uses the device using firestore database

    //called whenever new token is generated and when the user just open the app nd not really signed in,we want to add token to firestore but we can do it only when we r signed in
    override fun onTokenRefresh() {
        val newRegistrationToken = FirebaseInstanceId.getInstance().token

        if(FirebaseAuth.getInstance().currentUser != null){
            addTokenToFireStore(newRegistrationToken!!)
        }
        super.onTokenRefresh()
    }

    //creating companion object
    companion object {
        fun addTokenToFireStore(newRegistrationToken: String?) {
            if (newRegistrationToken == null) throw NullPointerException(R.string.FCM_token_is_null.toString())

            FireBaseUtil.getFCMRrgistrationTokens {tokens ->
                if(tokens.contains(newRegistrationToken))
                    return@getFCMRrgistrationTokens//newRegistrationTokens not be duplicated

                tokens.add(newRegistrationToken)
                FireBaseUtil.setFCMRegistrationTokens(tokens)



            }


        }
    }
}

