package com.anjali.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.anjali.viewpager.fragment.Contact
import com.anjali.viewpager.fragment.Chat
import com.anjali.viewpager.fragment.Status

class PagerViewAdapter(fm:FragmentManager):FragmentPagerAdapter(fm) {
    lateinit var fragment: Fragment

    override fun getItem(position: Int): Fragment {

        when{
            position==0 -> fragment =
                Chat() //inheritance of class Cameraby declaring it open class
            position==1 -> fragment = Status()
            position==2 -> fragment = Contact()

        }

        return fragment

    }

    override fun getCount(): Int {
    return 3
    }


}
