package com.anjali.viewpager.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anjali.viewpager.AppConstants
import com.anjali.viewpager.Helper.LocaleForAllActivity
import com.anjali.viewpager.Helper.SetThemeForAllActivity
import com.anjali.viewpager.R
import com.anjali.viewpager.glide.GlideApp
import com.anjali.viewpager.model.TextMessage
import com.anjali.viewpager.model.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sinch.android.rtc.Sinch
import com.sinch.android.rtc.SinchClient
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.Section
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import util.FireBaseUtil
import util.StorageUtil
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.util.*

class ChatActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var to_chat_image: CircleImageView
    lateinit var to_chat_name: TextView

    private var shouldInitRecyclerView = true
    private lateinit var messageSection: Section//from groupie

    private lateinit var currentChannelId: String
    private lateinit var currentUser: user//for notification

    private lateinit var toUserId: String
    private lateinit var toUserProfilepicturepath: String
    private lateinit var toUserName: String
    private lateinit var toUserPhone: String

    lateinit var sending_image: ImageView
    lateinit var recyclerListChatLog: RecyclerView
    lateinit var sharedPreferences: SharedPreferences
    var defaultColor = 0

    var localeForAll = LocaleForAllActivity(this)
    var themeForAllActivity = SetThemeForAllActivity(this)
//    var currentDayNightMode:Int? = null

    var request_call = 1//to identify permission request

    private lateinit var store: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    private lateinit var messagesListenerRegistration: ListenerRegistration


    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {

        themeForAllActivity.checkNightModeActivated()

        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("Wallpaper", Context.MODE_PRIVATE)

        localeForAll.loadLocale()
        setContentView(R.layout.activity_chat)

//        defaultColor = ContextCompat.getColor(this,R.attr.bgc_other_activity)

        store = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        toUserName = intent.getStringExtra(AppConstants.USER_NAME)!!
        toUserId = intent.getStringExtra(AppConstants.USER_ID)!!
        toUserProfilepicturepath = intent.getStringExtra(AppConstants.PROFILE_PICTURE_PATH)!!

        val documentReference = store.collection("users").document(toUserId)
        documentReference.get().addOnSuccessListener {
            toUserPhone = it.getString("phone")!!
        }



        to_chat_image = findViewById(R.id.to_chat_image)
        to_chat_name = findViewById(R.id.to_chat_name)
        toolbar = findViewById(R.id.toolbar)
        recyclerListChatLog = findViewById(R.id.recyclerListChatLog)

        sending_image = findViewById(R.id.send_image)

        FireBaseUtil.getCurrentUser {
            //getting current user to use in notification
            currentUser = it
        }

        setSupportActionBar(toolbar)
        actionBarIcon()

        if (toUserId.isNotBlank()) {

            FireBaseUtil.getOrCreateChatChannel(toUserId) { channelId ->

                currentChannelId = channelId

                messagesListenerRegistration =
                    FireBaseUtil.addChatMessageListener(channelId, this, this::updateRecyclerView)

                send.setOnClickListener {
                    val message = writeMessage.text.toString()

                    if (message.isNotEmpty()) {

                        val messageToSend = TextMessage(
                            message,
                            Calendar.getInstance().time,
                            FirebaseAuth.getInstance().currentUser?.uid!!,
                            toUserId, currentUser.fname, currentUser.profilePicturePath!!
                        )
                        //removing text from edit text as now it is send
                        writeMessage.setText("")
                        FireBaseUtil.sendMessage(messageToSend, channelId)
                    } else {
                        //toast using ancho library
                        toast(R.string.select_first_to_send)
                    }
                }

                send_image.setOnClickListener {
                    //sending images

                    openSource()


                }

            }
        }

    }

    fun updateRecyclerView(messages: List<Item<GroupieViewHolder>>) {
        fun init() {
            recyclerListChatLog.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    messageSection =
                        Section(messages)//contains all the messages by default which contains a list of items
                    this.add(messageSection)
                }
            }

            shouldInitRecyclerView = false
        }

        fun updateItems() = messageSection.update(messages)//updates all of the messages

        if (shouldInitRecyclerView) init()
        else updateItems()

        recyclerListChatLog.scrollToPosition(recyclerListChatLog.adapter!!.itemCount - 1)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun openSource() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            0
        )
    }


    var selectedImagePath: Uri? = null//immutable variable

    @SuppressLint("CommitPrefEdits")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImagePath = data.data

            val selectedImageBmp = MediaStore.Images.Media.getBitmap(
                contentResolver,
                selectedImagePath
            )//to get bitmap of selected image from content resolver

            //converting bitmap to byteArray

            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                outputStream
            )//compressing selected image bitmap to bitmap format and take the output in output stream

            val selectedImageBytes =
                outputStream.toByteArray()//getting actual bytes of compressed image



            val intent = Intent(this, FullScreenActivity::class.java)
            intent.putExtra("selectedImagePath", selectedImagePath.toString())
            intent.putExtra("toUserId", toUserId)
            intent.putExtra("currentUserFname", currentUser.fname)
            intent.putExtra("currentChannelId", currentChannelId)
            intent.putExtra("selectedImageBytes", selectedImageBytes)
            intent.putExtra("currentUserProfilePath", currentUser.profilePicturePath)
            startActivity(intent)


        }
    }

    private fun onClickImage() {
        val fullScreenIntent = Intent(this, ChatActivity::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_video_call, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.chat_call -> {
                chat_call()
            }
            R.id.video_call -> {
                makeVideoCallViaSinch()
            }
            R.id.wall_paper -> {

                openColorPicker()

            }
            R.id.add_to_contact -> {
                add_to_contact()
            }
            R.id.clear_chat -> {
                clear_chat()
            }

            R.id.delete_contact -> {

            }


        }
        return super.onOptionsItemSelected(item)
    }


    private fun clear_chat() {
        if (currentChannelId.isNotBlank()) {

                FireBaseUtil.deleteMessage(currentChannelId)
                toast("messages deleted permanently")


        }
    }

        private fun add_to_contact() {

            // Creates a new Intent to insert a contact
            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                // Sets the MIME type to match the Contacts Provider
                type = ContactsContract.RawContacts.CONTENT_TYPE
            }

            intent.apply {
                // Inserts an email address
                putExtra(ContactsContract.Intents.Insert.EMAIL, toUserName)
                /*
                 * In this example, sets the email type to be a work email.
                 * You can set other email types as necessary.
                 */
                putExtra(
                    ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                )
                // Inserts a phone number
                putExtra(ContactsContract.Intents.Insert.PHONE, toUserPhone)
                /*
                 * In this example, sets the phone type to be a work phone.
                 * You can set other phone types as necessary.
                 */
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                )
            }

            startActivity(intent)

        }

        private fun makeVideoCallViaSinch() {
            toast("video calling function")
        }

        private fun makeCallViaSich() {
            //sich client setup

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    request_call
                )
            } else {

                //setting up sinch client
                val context = this.applicationContext
                val sinchClient: SinchClient = Sinch.getSinchClientBuilder().context(context)
                    .applicationKey("12c3dbde-971c-4737-baff-1788c86c38f9")
                    .applicationSecret("Pmr7t2cynUuTOhPN2dWeRQ==")
                    .environmentHost("clientapi.sinch.com")
                    .userId("161046")
                    .build()

                sinchClient.setSupportCalling(true)
                sinchClient.start()

                val callClient = sinchClient.callClient
                callClient.callPhoneNumber("7256028138")
            }


        }

        private fun chat_call() {

            val phone = toUserPhone
            if (phone.trim().length > 0) {
                //to check wheteher the device has neccessary permission or not for mking call

                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.CALL_PHONE),
                        request_call
                    )
                } else {
                    val dial = "tel:" + phone
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse(dial))
                    startActivity(intent)
                }
            } else {
                toast("phone no. not valid")
            }


        }


        private fun openColorPicker() {
            val colorPicker = AmbilWarnaDialog(this@ChatActivity, defaultColor, object :
                AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {

                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {

                    defaultColor = color
                    recyclerListChatLog.setBackgroundColor(defaultColor)
                    sharedPreferences.edit().putInt("bgColor", defaultColor).apply()


                }

            })
            colorPicker.show()

        }

        fun actionBarIcon() {

            FireBaseUtil.getCurrentUser {
                //getting current user to use in notification
                currentUser = it

                to_chat_name.setText(toUserName)
                GlideApp.with(this).load(StorageUtil.pathToReference(toUserProfilepicturepath))
                    .placeholder(R.drawable.facebookmessenger).into(to_chat_image)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }

        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            if (requestCode == request_call) {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //  makeCallViaSich()
                    chat_call()
                } else {
                    toast("permission denied")
                }
            }
        }

        override fun onStart() {
            super.onStart()
            val bgColor = sharedPreferences.getInt("bgColor", R.attr.bgc_chat_activity)
            recyclerListChatLog.setBackgroundColor(bgColor)
        }

    }






