package com.anjali.viewpager.activity

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.anjali.viewpager.R
import com.anjali.viewpager.SendCameraPicActivity
import com.anjali.viewpager.glide.GlideApp
import com.anjali.viewpager.model.ImageMessage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import util.FireBaseUtil
import util.StorageUtil
import java.util.*

class FullScreenActivity : AppCompatActivity() {

    lateinit var fullScreenImageView: ImageView
    lateinit var send: FloatingActionButton
    lateinit var ok:FloatingActionButton
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = resources.getString(R.string.send_message_via_full_screen_activity)

        fullScreenImageView = findViewById(R.id.fullScreenImageView)
        send = findViewById(R.id.send_via_full_screen_activity)
        ok=findViewById(R.id.ok_via_full_screen_activity)

        val selectedImagePathFromCamera = intent.getStringExtra("selectedImagePathFromCamera")

        val selectedImagePath = intent.getStringExtra("selectedImagePath")
        val toUserId = intent.getStringExtra("toUserId")
        val currentUserFname = intent.getStringExtra("currentChannelId")
        val selectedImageBytes = intent.getByteArrayExtra("selectedImageBytes")
        val currentChannelId = intent.getStringExtra("currentChannelId")
        val currentUserProfilePath = intent.getStringExtra("currentUserProfilePath")


        if (selectedImagePath != null) {

            GlideApp.with(this).load(selectedImagePath).placeholder(R.drawable.facebookmessenger)
                .into(fullScreenImageView)
            send.visibility = View.VISIBLE
            ok.visibility = View.GONE

        }

        if(selectedImagePathFromCamera != null){
            GlideApp.with(this).load(selectedImagePathFromCamera).placeholder(R.drawable.facebookmessenger)
                .into(fullScreenImageView)
            ok.visibility = View.VISIBLE
            send.visibility = View.GONE

        }

        ok.setOnClickListener{
             val intent= Intent(this,SendCameraPicActivity::class.java)
            startActivity(intent)
        }

        send.setOnClickListener {

            if ((toUserId != null) && (currentUserFname != null) && (currentUserProfilePath != null) && (selectedImageBytes != null) && (currentChannelId != null)) {

            StorageUtil.uploadMessagePhoto(selectedImageBytes) { imagePath ->  //this is where image is stored inside firebase storage
                val messageToSend = ImageMessage(
                    imagePath,
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser?.uid!!,
                    toUserId,
                    currentUserFname,
                    currentUserProfilePath
                )

                FireBaseUtil.sendMessage(messageToSend, currentChannelId)
                toast("image send successfully")
                val intent = Intent(this,ChatActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
            else{
                toast("error occured")
            }
        }
    }
}