package com.anjali.viewpager.Helper

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import com.anjali.viewpager.R

class SetThemeForAllActivity(var baseContext: Context) {


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  fun setThemeApp(isChecked:Boolean,mode:Switch) {
        if (isChecked) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            mode.isChecked = true
            saveNightModeState(nightMode = true)


        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            mode.isChecked = false
            saveNightModeState(nightMode = false)


        }
    }
        fun saveNightModeState(nightMode: Boolean) {
            if(nightMode) {
                val sharedPreferences =
                    baseContext.getSharedPreferences("Theme_Settings", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("saved theme", true)
                editor.apply()
            }
            else{
                val sharedPreferences =
                    baseContext.getSharedPreferences("Theme_Settings", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("saved theme", false)
                editor.apply()

            }


            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            fun checkNightModeActivated() {

                val sharedPreferences = baseContext.getSharedPreferences("Theme_Settings", Activity.MODE_PRIVATE)
                val result = sharedPreferences.getBoolean("saved theme", false)

                if (result){

                    baseContext.setTheme(R.style.DarkTheme)
                }
                else{
                 baseContext.setTheme(R.style.AppTheme)
                }
            }

        }

