package com.anjali.viewpager.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.anjali.viewpager.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.support.v4.intentFor

open class Camera: Fragment() {

    lateinit var cameraImage:ImageView
    lateinit var proceed:FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =inflater.inflate(R.layout.fragment_camera,null)

        return view
    }
}