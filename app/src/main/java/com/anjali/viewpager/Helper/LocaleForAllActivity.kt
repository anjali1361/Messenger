package com.anjali.viewpager.Helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.anjali.viewpager.R
import java.util.*

class LocaleForAllActivity(var baseContext: Context) {


    @SuppressLint("CommitPrefEdits")
    fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)


        val sharedPreferences =
            baseContext.getSharedPreferences("Language_Settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("my_lang", lang)
        editor.apply()


    }

    fun loadLocale() {
        val sharedPreferences =
            baseContext.getSharedPreferences("Language_Settings", Activity.MODE_PRIVATE)
        val lang = sharedPreferences.getString("my_lang", "")
        if (lang != null) {
            setLocale(lang)
        }

    }

    fun setAppLanguage() {
        val dialog = AlertDialog.Builder(baseContext).setTitle(R.string.app_language)
        val alertDialog = dialog.show()

        dialog.setPositiveButton(R.string.english) { text, listener ->

            setLocale("en")
            Toast.makeText(
                baseContext,
                R.string.default_lang_set,
                Toast.LENGTH_SHORT
            ).show()
            ActivityCompat.finishAffinity(baseContext as Activity)

            alertDialog.dismiss()


        }

        dialog.setNegativeButton(R.string.hindi) { text, listener ->

          setLocale("hi")
            Toast.makeText(
                baseContext,
                R.string.default_lang_set,
                Toast.LENGTH_SHORT
            ).show()
            ActivityCompat.finishAffinity(baseContext as Activity)

            alertDialog.dismiss()

        }
        dialog.create()
        dialog.show()
    }
}