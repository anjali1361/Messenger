package com.anjali.viewpager.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.anjali.viewpager.BuildConfig
import com.anjali.viewpager.Helper.LocaleForAllActivity
import com.anjali.viewpager.Helper.SetThemeForAllActivity
import com.anjali.viewpager.R
import com.anjali.viewpager.glide.GlideApp
import com.anjali.viewpager.model.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.w3c.dom.Text
import util.FireBaseUtil
import util.StorageUtil
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var email_profile: EditText
    lateinit var name_profile: EditText
    lateinit var bio_profile: EditText
    lateinit var phone_profile: EditText
    lateinit var edit: ImageView
    lateinit var share: ImageView
    lateinit var logout: ImageView
    lateinit var profile_pic: ImageView
    lateinit var next: ImageView
    lateinit var text_next: TextView
    lateinit var edit_text: TextView


    var PERMISSION_CODE = 1000
    var localeForAll = LocaleForAllActivity(this)
    var themeForAllActivity = SetThemeForAllActivity(this)
//    var currentDayNightMode:Int? = null

    private var selectedImageBytes: ByteArray? =
        null//keeping track of selected image as all images are byte array
    private var pictureJustChanged = false

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        themeForAllActivity.checkNightModeActivated()
        super.onCreate(savedInstanceState)
        localeForAll.loadLocale()
        setContentView(R.layout.activity_profile)


//        currentDayNightMode =  AppCompatDelegate.getDefaultNightMode()

        toolbar = findViewById(R.id.toolbar)
        email_profile = findViewById(R.id.email_profile)
        name_profile = findViewById(R.id.name_profile)
        phone_profile = findViewById(R.id.phone_profile)
        edit = findViewById(R.id.edit)
        share = findViewById(R.id.share)
        logout = findViewById(R.id.logout)
        profile_pic = findViewById(R.id.profile_pic)
        bio_profile = findViewById(R.id.bio_profile)
        next = findViewById(R.id.next)
        text_next = findViewById(R.id.text_next)
        edit_text = findViewById(R.id.edit_text)

//        FireBaseUtil.currentUserDocRef.addSnapshotListener(this) { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
//
//            if (documentSnapshot != null) {
//                if((documentSnapshot.getString("fname") == "")){
//
//                    next.visibility = View.VISIBLE
//                    text_next.visibility = View.VISIBLE
//
////                   startActivity(intentFor<MainActivity>().newTask().clearTask()) }
//            }else if(!(documentSnapshot.getString("fname") == "") && !(edit_text.text == "profile")){
//
//                    next.visibility = View.VISIBLE
//                    text_next.visibility = View.VISIBLE
//
//                }
//
//                }
//        }




            setSupportActionBar(toolbar)
            supportActionBar?.setTitle(R.string.Save_Info)

            profile_pic.setOnClickListener {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    ) {
                        //permission not enabled ,request it
                        val permission = arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )

                        //show popup to request permission
                        requestPermissions(permission, PERMISSION_CODE)
                    } else {
                        //permission already granted
                        openSource()

                    }
                } else {
                    // system os < marshmallow
                    openSource()

                }


            }

            edit.setOnClickListener {
                if (selectedImageBytes != null) {//to get the property member isInitialized we have to use this two colons as a syntax

                    //update profile picture inside firebase storage

                    StorageUtil.uploadProfilePhoto(selectedImageBytes!!) { imagePath ->
                        FireBaseUtil.updateCurrentUser(
                            name_profile.text.toString(),
                            bio_profile.text.toString(),
                            email_profile.text.toString(),
                            phone_profile.text.toString(),
                            imagePath
                        )
                        Toast.makeText(this, "Info saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    FireBaseUtil.updateCurrentUser(
                        name_profile.text.toString(),
                        bio_profile.text.toString(),
                        email_profile.text.toString(),
                        phone_profile.text.toString(),
                        null
                    )

                    Toast.makeText(
                        this,
                        R.string.your_update_saved_successfully,
                        Toast.LENGTH_SHORT
                    ).show()


                }
            }

            logout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()

            }

            share.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "SHARE DEMO")
                val message =
                    "https://play.google.com/store/apps/details?=" + BuildConfig.APPLICATION_ID + "\n\n"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey ,there is my first app check it out and respond me with some feedback " + message
                )
                startActivity(Intent.createChooser(intent, R.string.share_using.toString()))
            }

            next.setOnClickListener {
                startActivity(intentFor<MainActivity>().newTask().clearTask())
                edit_text.setText("profile")
            }


        }

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onRequestPermissionsResult(

                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                //this method is called when the user presses Allow or Deny from permission request popup
                when (requestCode) {

                    PERMISSION_CODE -> {
                        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                            //permission from popup is granted
                            openSource()
                        } else {
                            //permission from popup is denied
                            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT)
                                .show()

                        }
                    }
                }

            }

            @SuppressLint("CommitPrefEdits")
            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

                    // start picker to get image for cropping and then use the image in cropping activity

                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);

                }
                //to get the cropped image
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        val selectedImagePath = result.uri
                        val selectedImageBmp =
                            MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)

                        val outputStream = ByteArrayOutputStream()
                        selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        selectedImageBytes = outputStream.toByteArray()


                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        val error = result.error
                    }

                    GlideApp.with(this).load(selectedImageBytes).into(profile_pic)
                    pictureJustChanged = true

                    super.onActivityResult(requestCode, resultCode, data)
                }
            }

            override fun onStart() {
                //loading user from firestore,it will take some time
                FireBaseUtil.getCurrentUser { user ->
                    name_profile.setText(user.fname)
                    email_profile.setText(user.email)
                    bio_profile.setText(user.bio)
                    phone_profile.setText(user.phone)
                    if (!pictureJustChanged && user.profilePicturePath != null) {

                        GlideApp.with(this)
                            .load(StorageUtil.pathToReference(user.profilePicturePath))
                            .placeholder(R.drawable.facebookmessenger).into(profile_pic)


                    }

//
//            if(currentDayNightMode != AppCompatDelegate.getDefaultNightMode()){
//                    recreate()
//                }

                }


                super.onStart()


            }


            @RequiresApi(Build.VERSION_CODES.KITKAT)
            fun openSource() {


                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                startActivityForResult(
                    Intent.createChooser(intent, R.string.Select_Picture.toString()),
                    0
                )
            }

        }

