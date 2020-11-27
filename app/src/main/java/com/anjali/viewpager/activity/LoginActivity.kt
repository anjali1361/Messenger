package com.anjali.viewpager.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.anjali.viewpager.Helper.LocaleForAllActivity
import com.anjali.viewpager.Helper.SetThemeForAllActivity
import com.anjali.viewpager.R
import com.anjali.viewpager.service.MyFirebaseInstanceIdService
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.hbb20.CountryCodePicker
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast
import util.FireBaseUtil
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    lateinit var ccp:CountryCodePicker
    lateinit var phone_register: EditText
    lateinit var toolbar:Toolbar

    lateinit var next: Button
    lateinit var otp:EditText
    lateinit var progressBar: ProgressBar

    lateinit var constraintPhone:ConstraintLayout


    lateinit var codeSent: String
    var verificationInProgress= false

    lateinit var mAuth: FirebaseAuth
    lateinit var Store:FirebaseFirestore

    var localeForAll = LocaleForAllActivity(this)
    var themeForAllActivity = SetThemeForAllActivity(this)
//    var currentDayNightMode:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeForAllActivity.checkNightModeActivated()
        super.onCreate(savedInstanceState)
        localeForAll.loadLocale()
        setContentView(R.layout.activity_login)
//        currentDayNightMode =  AppCompatDelegate.getDefaultNightMode()

        next = findViewById(R.id.next)
        ccp = findViewById(R.id.ccp)
        otp = findViewById(R.id.otp)
        phone_register = findViewById(R.id.phone_register)
        mAuth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progressBar)
        Store = FirebaseFirestore.getInstance()
        constraintPhone = findViewById(R.id.constraintPhone)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = resources.getString(R.string.login)

        if(mAuth.currentUser?.uid != null) {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        //if devices used to get otp is different then tht of device in which otp is entered to use
        next.setOnClickListener{

            if (!verificationInProgress) {

                if (!phone_register.text.toString()
                        .isEmpty() && phone_register.text.toString().length == 10
                ) {

                    val full_phone: String? =
                        "+" + ccp.selectedCountryCode + phone_register.text.toString()

                    progressBar.visibility = View.VISIBLE

                    sendVerificationCodeToUser(full_phone)

                    progressBar.visibility = View.GONE

                } else  toast(R.string.phone_no_is_not_valid)

            }else{

                val code = otp.text.toString()

                if(code.isEmpty() || code.length <6){
                    otp.setError(R.string.Wrong_OTP.toString())
                    otp.requestFocus()
                    return@setOnClickListener
                }

                else {

                    progressBar.visibility = View.VISIBLE
                    verifyCode(code)
                }

            }


        }

    }


    @SuppressLint("SetTextI18n")
    private fun sendVerificationCodeToUser(phone: String?) {

        //mthod for providing otp
        progressBar.visibility = View.GONE

        otp.visibility = View.VISIBLE

        next.setText(R.string.VERIFY)

        if (phone != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
               phone, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                callbacks
            )
        } // OnVerificationStateChangedCallbacks

        verificationInProgress =  true

    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        //fun below is called automatically after execution of onCodeSent() mthod
        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
           codeSent = p0

        }

        //responsible for automatic invocation if phone entered on the same device in which the otp is sent otherwise above mthod get called and get out of callbacks
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {

            val code = p0.smsCode//code provided by the user inside the edit text field(otp)

            if (code != null) {
                if (code.isNotEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    verifyCode(code)
                }
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
         toast( p0.message!!)
            Log.d("LoginActivity",p0.message!!)
        }

    }
    private fun verifyCode(codeByUser: String) {

        val credential = PhoneAuthProvider.getCredential(codeSent, codeByUser)

        signInUserByCredentials(credential)


    }

    fun signInUserByCredentials(credential: PhoneAuthCredential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                FireBaseUtil.initCurrentUserIfFirstTime {

                    toast(R.string.Login_Successfull)

                    FireBaseUtil.initCurrentUserIfFirstTime {

                        //intent using ancho lib

                        startActivity(intentFor<MainActivity>().newTask().clearTask())

                        //want to get token and add it to firestore when user signs in

                        val registrationToken = FirebaseInstanceId.getInstance().token
                        MyFirebaseInstanceIdService.addTokenToFireStore(registrationToken!!)


                    }


                }

            } else {

                    toast(task.exception?.message!!)//using ancho library
                    Log.d("LoginActivity",task.exception?.message!!)



            }


        }

    }

//    override fun onStart() {
//        super.onStart()
//
//     localeForAll.loadLocale()
//    }



}



