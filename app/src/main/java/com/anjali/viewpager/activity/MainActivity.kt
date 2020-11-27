package com.anjali.viewpager.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.anjali.viewpager.Helper.LocaleForAllActivity
import com.anjali.viewpager.Helper.SetThemeForAllActivity
import com.anjali.viewpager.PagerViewAdapter
import com.anjali.viewpager.R
import com.anjali.viewpager.fragment.Camera
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var camera:ImageView
    lateinit var chat:TextView
    lateinit var contact:TextView
    lateinit var status:TextView
    lateinit var viewPager: ViewPager
    lateinit var toolbar:Toolbar
    lateinit var pagerViewAdapter: PagerViewAdapter

    var localeForAll = LocaleForAllActivity(this)
    var themeForAllActivity = SetThemeForAllActivity(this)
//    var currentDayNightMode:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeForAllActivity.checkNightModeActivated()
        super.onCreate(savedInstanceState)
        localeForAll.loadLocale()
        setContentView(R.layout.activity_main)

        camera = findViewById(R.id.camera)
        contact = findViewById(R.id.contact)
        chat = findViewById(R.id.chat)
        status = findViewById(R.id.status)
        viewPager = findViewById(R.id.fragment_container)

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)

        pagerViewAdapter =
            PagerViewAdapter(supportFragmentManager)
        viewPager.adapter = pagerViewAdapter

        camera.setOnClickListener {
//            viewPager.setCurrentItem(3)
            take_picture()
        }
        chat.setOnClickListener {
            viewPager.setCurrentItem(0)
        }
        status.setOnClickListener {
            viewPager.setCurrentItem(1)
        }
        contact.setOnClickListener {
            viewPager.setCurrentItem(2)
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPageSelected(position: Int) {
                onChangeTab(position)
            }

        })
    }

    private fun take_picture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//starts the camera on clicking the icon
        startActivityForResult(intent,1)
    }

 var selectedImagePath: Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            val bitmap:Bitmap = data?.extras?.get("data") as Bitmap//bitmap is used to represent binary data

            selectedImagePath = data.data

//            val selectedImageBmp = MediaStore.Images.Media.getBitmap(
//                contentResolver,
//                selectedImagePath
//            )
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
//                outputStream.toByteArray()

            Log.d("MainActivity", bitmap.toString())

            val intent = Intent(this, FullScreenActivity::class.java)
//            intent.putExtra("selectedImagePathFromCamera", selectedImagePath)
            startActivity(intent)

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onChangeTab(position: Int) {
        if (position==0){
//            camera.setTextSize(25F)
//            camera.setTextColor(getColor(R.color.bright_color))

            chat.setTextSize(25F)
            chat.setTextColor(getColor(R.color.bright_color))

            status.setTextSize(20F)
            status.setTextColor(getColor(R.color.light_color))

            contact.setTextSize(20F)
           contact.setTextColor(getColor(R.color.light_color))
        }
        if (position==1){
//            camera.setTextSize(20F)
//            camera.setTextColor(getColor(R.color.light_color))

            chat.setTextSize(20F)
            chat.setTextColor(getColor(R.color.light_color))

            status.setTextSize(25F)
            status.setTextColor(getColor(R.color.bright_color))

            contact.setTextSize(20F)
            contact.setTextColor(getColor(R.color.light_color))
        }
        if (position==2){
//            camera.setTextSize(20F)
//            camera.setTextColor(getColor(R.color.light_color))

            chat.setTextSize(20F)
            chat.setTextColor(getColor(R.color.light_color))

            status.setTextSize(20F)
            status.setTextColor(getColor(R.color.light_color))

            contact.setTextSize(25F)
            contact.setTextColor(getColor(R.color.bright_color))
        }
//        if (position==3){
////            camera.setTextSize(20F)
////            camera.setTextColor(getColor(R.color.light_color))
//
//            chat.setTextSize(20F)
//            chat.setTextColor(getColor(R.color.light_color))
//
//            status.setTextSize(20F)
//            status.setTextColor(getColor(R.color.light_color))
//
//            contact.setTextSize(20F)
//            contact.setTextColor(getColor(R.color.light_color))
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when( item.itemId) {

            R.id.search ->{

            }
            R.id.profile ->{

                val intent = Intent(this,ProfileActivity::class.java)
                startActivity(intent)

            }
            R.id.signout ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()

            }
            R.id.setting ->{

                val intent = Intent(this, SettingsActivity::class.java)

                startActivity(intent)

            }

            R.id.group ->{

            }
            R.id.view_group ->{

            }



        }
        return super.onOptionsItemSelected(item)
    }



}
