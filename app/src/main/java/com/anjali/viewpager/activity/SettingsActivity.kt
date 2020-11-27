package com.anjali.viewpager.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anjali.viewpager.AppConstants
import com.anjali.viewpager.Helper.LocaleForAllActivity
import com.anjali.viewpager.Helper.SetThemeForAllActivity
import com.anjali.viewpager.R
import com.anjali.viewpager.glide.GlideApp
import com.anjali.viewpager.model.ImageMessage
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast
import util.FireBaseUtil
import util.StorageUtil
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.properties.Delegates

class SettingsActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var profile_pic_setting: CircleImageView
    lateinit var name_setting: TextView
    lateinit var bio_setting: TextView
    lateinit var delete_relative: RelativeLayout
    lateinit var change_relative: RelativeLayout
    lateinit var wall_relative: RelativeLayout
    lateinit var theme_relative: RelativeLayout
    lateinit var font_relative: RelativeLayout
    lateinit var lang_relative: RelativeLayout
    lateinit var setting:ScrollView

    lateinit var progressBar: ProgressBar
    var mode: Switch? = null


//    var user:user? = null

    var localeForAll = LocaleForAllActivity(this)

    var themeForAllActivity = SetThemeForAllActivity(this)

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {

        themeForAllActivity.checkNightModeActivated()
        super.onCreate(savedInstanceState)

        localeForAll.loadLocale()

        setContentView(R.layout.activity_settings)

        setting = findViewById(R.id.setting)

        mode = findViewById(R.id.mode)

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = resources.getString(R.string.setting)

        profile_pic_setting = findViewById(R.id.profile_pic_setting)
        name_setting = findViewById(R.id.name_setting)
        bio_setting = findViewById(R.id.bio_setting)
        delete_relative = findViewById(R.id.delete_relative)
        change_relative = findViewById(R.id.change_relative)
        wall_relative = findViewById(R.id.wall_relative)
        theme_relative = findViewById(R.id.theme_relative)
        font_relative = findViewById(R.id.font_relative)
        lang_relative = findViewById(R.id.lang_relative)
        progressBar = findViewById(R.id.progressBar)

        FireBaseUtil.getCurrentUser {

            name_setting.setText(it.fname)
            bio_setting.setText(it.bio)
            GlideApp.with(this).load(StorageUtil.pathToReference(it.profilePicturePath!!))
                .placeholder(R.drawable.facebookmessenger).into(profile_pic_setting)

        }



        lang_relative.setOnClickListener {

            localeForAll.setAppLanguage()

        }

        mode?.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->

            themeForAllActivity.setThemeApp(isChecked, mode!!)
            themeForAllActivity.checkNightModeActivated()


        }

        delete_relative.setOnClickListener {

            deleteFromAuth()
            deleteFromFirestore()


        }
        wall_relative.setOnClickListener {


        }


    }




//    fun openSource() {
//
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
//        startActivityForResult(
//            Intent.createChooser(intent, "Select Picture"),
//            0
//        )
//    }
//
//
//    var selectedImagePath: Uri? = null//immutable variable
//
//    @SuppressLint("CommitPrefEdits")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
//            selectedImagePath = data.data
//
//            val selectedImageBmp = MediaStore.Images.Media.getBitmap(
//                contentResolver,
//                selectedImagePath
//            )//to get bitmap of selected image from content resolver
//
//            //converting bitmap to byteArray
//
//            val outputStream = ByteArrayOutputStream()
//
//            selectedImageBmp.compress(
//                Bitmap.CompressFormat.JPEG,
//                90,
//                outputStream
//            )//compressing selected image bitmap to bitmap format and take the output in output stream
//
//            val selectedImageBytes =
//
//                outputStream.toByteArray()//getting actual bytes of compressed image
//
//
//            val intent = Intent(this, ChatActivity::class.java)
//            intent.putExtra("selectedImagePathWallPaper", selectedImagePath.toString())
//            startActivity(intent)
//
////            StorageUtil.uploadMessagePhoto(selectedImageBytes) { imagePath ->  //this is where image is stored inside firebase storage
////                val messageToSend = ImageMessage(
////                    imagePath,
////                    Calendar.getInstance().time,
////                    FirebaseAuth.getInstance().currentUser?.uid!!,
////                    toUserId,
////                    currentUser.fname,
////                    currentUser.profilePicturePath!!
////                )
////
////                FireBaseUtil.sendMessage(messageToSend, currentChannelId)
////
////
////                val intent = Intent(this, FullScreenActivity::class.java)
////                intent.putExtra("selectedImagePath", selectedImagePath.toString())
////                startActivity(intent)
////
////
////            }
//
//        }

    private fun deleteFromFirestore() {
        FireBaseUtil.deleteUserFromFirestore()
    }

    private fun deleteFromAuth() {
        val dialog = AlertDialog.Builder(this).setTitle(R.string.r_u_sure)
            .setMessage(R.string.delete_acc_mess)
            .setPositiveButton(R.string.delete_acc) { dialogInterface: DialogInterface, i: Int ->

                progressBar.visibility = View.VISIBLE

                FireBaseUtil.mAuth.currentUser?.delete()?.addOnCompleteListener {

                    if (it.isSuccessful) {

                        progressBar.visibility = View.GONE

                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()


                        toast(R.string.user_acc_deleted)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(baseContext, it.exception?.message, Toast.LENGTH_SHORT)
                            .show()

                    }

                }

            }.setNegativeButton(R.string.Cancel) { dialogInterface: DialogInterface, i: Int ->

                dialogInterface.dismiss()

            }
        dialog.create()
        dialog.show()
    }


//    private fun setAppLanguage() {
//            val dialog = AlertDialog.Builder(this).setTitle(R.string.app_language)
//            val alertDialog = dialog.show()
//
//            dialog.setPositiveButton(R.string.english) { text, listener ->
//
//                localeForAll.setLocale("en")
//                Toast.makeText(
//                    baseContext,
//                    R.string.default_lang_set,
//                    Toast.LENGTH_SHORT
//                ).show()
//                ActivityCompat.finishAffinity(this)
//
//                alertDialog.dismiss()
//
//
//            }
//
//
//            dialog.setNegativeButton(R.string.hindi) { text, listener ->
//
//                localeForAll.setLocale("hi")
//                Toast.makeText(
//                    baseContext,
//                    R.string.default_lang_set,
//                    Toast.LENGTH_SHORT
//                ).show()
//                ActivityCompat.finishAffinity(this)
//
//                alertDialog.dismiss()
//
//            }
//            dialog.create()
//            dialog.show()
//
//        }
    }


