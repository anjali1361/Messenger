package com.anjali.viewpager.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anjali.viewpager.AppConstants
import com.anjali.viewpager.R
import com.anjali.viewpager.RecyclerView.Item.PersonItem
import com.anjali.viewpager.activity.ChatActivity
import com.anjali.viewpager.activity.ProfileActivity
import com.anjali.viewpager.glide.GlideApp
import com.anjali.viewpager.model.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.fragment_chat.*
import org.jetbrains.anko.support.v4.startActivity
import util.ConnectionManager
import util.FireBaseUtil
import util.StorageUtil

open class Chat() : Fragment() {

    private lateinit var userListenerRegistration: ListenerRegistration//to add user to list of all people on creating new account and we always wnt updated list of user hence listener is used instead getter
    private var shouldInitRecyclerView = true
    private lateinit var peopleSection: Section//group adapter holds the section and this in turn holds these items(individual views)
    private lateinit var store:FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_chat, null)


        store = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        val documentReference = store.collection("users").document(userId!!)
//        localeForAll.loadLocale()

        userListenerRegistration =
            FireBaseUtil.addUsersListener(this.activity!!, this::updateRecyclerView)

        if (!ConnectionManager().checkConnectivity(activity as Context)) {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle(R.string.Error)
            dialog.setMessage(R.string.Internet_Connection_is_not_Found)
            dialog.setPositiveButton(R.string.Open_Settings) { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }

            dialog.setNegativeButton(R.string.Exit) { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

             documentReference.get().addOnSuccessListener {
                 val name = it.getString("fname")
                 if(name == ""){
                     val dialog1 = AlertDialog.Builder(activity as Context)
                     dialog1.setTitle(R.string.save_profile_info_first)
                     dialog1.setMessage(R.string.enter_some_info_to_chat)
                     dialog1.setPositiveButton(R.string.ok) { text, listener ->

                         startActivity<ProfileActivity>()
                     }

                     dialog1.setNegativeButton(R.string.Exit) { text, listener ->
                         ActivityCompat.finishAffinity(activity as Activity)
                     }
                     dialog1.create()
                     dialog1.show()
                 }
             }



            return view
        }

        override fun onDestroyView() {
            super.onDestroyView()
            FireBaseUtil.removeListener(userListenerRegistration)
            shouldInitRecyclerView = true
        }

        fun updateRecyclerView(item: List<Item<GroupieViewHolder>>) {

            fun init() {
                recyclerList.apply {
                    layoutManager = LinearLayoutManager(this@Chat.context)
                    adapter = GroupAdapter<GroupieViewHolder>().apply {
                        peopleSection = Section(item)
                        add(peopleSection)
                        setOnItemClickListener(onItemClick)
                    }
                }

                shouldInitRecyclerView = false

            }

            fun updateItems() = peopleSection.update(item)

            if (shouldInitRecyclerView) init()
            else updateItems()

        }

        private val onItemClick = OnItemClickListener { item: Item<GroupieViewHolder>, view: View ->
            if (item is PersonItem && (item.person.fname.isNotBlank())) {

                //starting activity using ancho library
                startActivity<ChatActivity>(
                    AppConstants.USER_NAME to item.person.fname,
                    AppConstants.USER_ID to item.userId,
                    AppConstants.PROFILE_PICTURE_PATH to item.person.profilePicturePath
//                    AppConstants.PHONE to item.person.phone

                )
            }

        }

    }

