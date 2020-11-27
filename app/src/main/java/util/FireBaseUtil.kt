package util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.anjali.viewpager.Helper.LocaleForAllActivity
import com.anjali.viewpager.R
import com.anjali.viewpager.RecyclerView.Item.PersonItem
import com.anjali.viewpager.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import org.jetbrains.anko.toast
import java.lang.NullPointerException

object FireBaseUtil {

  val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }//instantiated only when we need it

   val mAuth:FirebaseAuth by lazy { FirebaseAuth.getInstance() } //variable of share...

//     val currentUserCollectionRef: DocumentReference
//        get() = firestoreInstance.document(
//            "users/${FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException(
//                "UID is null"
//            )}"
//        )


   val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException(
                "UID is null"
            )}"
        )

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit){

        //getting reference to user database nd after success listener provide us with DocumentSnapshot!

        currentUserDocRef.get().addOnSuccessListener {

            if(!it.exists()){
                val newUser = user(FirebaseAuth.getInstance().currentUser?.displayName ?: "","","","",null,
                    mutableListOf())
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else{
                onComplete()
            }

        }

    }

    fun deleteUserFromFirestore(){
        currentUserDocRef.delete().addOnSuccessListener {
            Log.d("FireBaseUtil", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("FireBaseUtil", "Error deleting document", e)
        }
    }

    fun updateCurrentUser(fname:String = "",bio:String = "",email:String = "",phone:String="",profilePicturePath:String?=null){
        val userFieldMap = mutableMapOf<String,Any>()//need this as we may have to update some or all fields at a time

        if(fname.isNotBlank()) userFieldMap["fname"] = fname
        if(bio.isNotBlank()) userFieldMap["bio"] = bio
        if(email.isNotBlank()) userFieldMap["email"] = email
        if(phone.isNotBlank()) userFieldMap["phone"] = phone
        if(profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath

        currentUserDocRef.update(userFieldMap)

    }

    fun getCurrentUser(onComplete: (user) -> Unit){

//        val sharedPreferences =getSharedPreferences("Settings", Activity.MODE_PRIVATE)
//        val lang = sharedPreferences.getString("my_lang", "")
//        mAuth.setLanguageCode("hi")
        currentUserDocRef.get().addOnSuccessListener {

            if(!(it.get("fname")?.equals(""))!!) {
                onComplete(it.toObject(user::class.java)!!)
            }

        }

    }


    fun addUsersListener(context: Context,onListen:(List<Item<GroupieViewHolder>>) -> Unit):ListenerRegistration{
        return firestoreInstance.collection("users").addSnapshotListener{querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
           if(exception != null){
               Log.e("Firestore","Users listener error.",exception)
               return@addSnapshotListener
           }

            val items = mutableListOf<Item<GroupieViewHolder>>()
            querySnapshot?.documents?.forEach {

                if(it.id != FirebaseAuth.getInstance().currentUser?.uid){
                    items.add(PersonItem(it.toObject(user::class.java)!!,it.id,context))
                }

            }
            onListen(items)

        }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()


    val chatChannelCollectionRef =
        firestoreInstance.collection("chatChannels")//here all the chat channels is stored along with messages it contain

    fun getOrCreateChatChannel(otherUserId: String, onComplete: (channelId: String) -> Unit) {//higher order fun onComplete is called automatically once when the task(getting or creating chat channel) completes,which takes one arg channelId and return unit

        currentUserDocRef.collection("engagedChatChannels").document(otherUserId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    // => we r currently chatting to some user
                    onComplete(it["channelId"] as @kotlin.ParameterName(name = "channelId") String)
                    return@addOnSuccessListener
                }
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val newChannel = chatChannelCollectionRef.document()//accessing collection without it is being actually created in firebse
                newChannel.set(
                    ChatChannel(
                        mutableListOf(currentUserId, otherUserId)
                    )
                )

                //saving channel id in users who will chat together
                currentUserDocRef.collection("engagedChatChannels").document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels").document(currentUserId!!).set(
                    mapOf("channelId" to newChannel.id)//getting channelId
                )

                onComplete(newChannel.id)
            }

    }

    //for listening messages inside a channel id

    fun addChatMessageListener(
        channelId: String,
        context: Context,
        onListen: (List<Item<GroupieViewHolder>>) -> Unit
    ): ListenerRegistration {

        return chatChannelCollectionRef.document(channelId).collection("messages").orderBy("time")
            .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->

                if(firebaseFirestoreException != null){
                    Log.e("Firestore","ChatChannelListener error",firebaseFirestoreException)
                }

                val items = mutableListOf<Item<GroupieViewHolder>>()
                querySnapshot?.documents?.forEach {//it here is the message sent
                    if (it["type"] == MessageType.TEXT)
                        items.add(
                            TextMessageItem(
                                it.toObject(
                                    TextMessage::class.java
                                )!!, context
                            )
                        )
                    else {
                        items.add(
                            ImageMessageItem(
                                it.toObject(ImageMessage::class.java)!!,
                                context
                            )


                        )
                    }
                    return@forEach
                }
                onListen(items)
            }
    }

    fun sendMessage(message: Message, channelId: String){
        chatChannelCollectionRef.document(channelId).collection("messages").add(message)
    }

    fun deleteMessage(channelId: String){

        chatChannelCollectionRef.document(channelId).collection("messages")
            .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->

                if(firebaseFirestoreException != null){
                    Log.e("Firestore","ChatChannelListener error",firebaseFirestoreException)
                }
               //'it' here is the message sent
                querySnapshot?.forEach {
                  chatChannelCollectionRef.document("${channelId}/messages/${it.id}").delete().addOnSuccessListener {
                      Log.d("Firebase","deleted")
                  }.addOnFailureListener{
                      Log.d("Firebase","error happen"+it)
                  }
//                    chatChannelCollectionRef.document(channelId).delete()
//                    val ref =it.id
//                    Log.d("Firebase", ref)

                }

            }

    }

    //region FCM

    fun getFCMRrgistrationTokens(onComplete: (tokens:MutableList<String>) -> Unit){

        currentUserDocRef.get().addOnSuccessListener {
            //creating user from document snapshot

            val user = it.toObject(user::class.java)!!
            //first add new parameter to user class i.e. registrationToken
            onComplete(user.registrationTokens)

        }
    }

    fun setFCMRegistrationTokens(registrationTokens:MutableList<String>){
        currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
    }


    //endregion FCM
}