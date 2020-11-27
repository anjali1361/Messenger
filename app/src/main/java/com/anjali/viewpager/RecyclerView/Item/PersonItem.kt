package com.anjali.viewpager.RecyclerView.Item

import android.content.Context
import com.anjali.viewpager.R
import com.anjali.viewpager.glide.GlideApp
import com.anjali.viewpager.model.user
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_profile.view.*
import kotlinx.android.synthetic.main.chat_single_row.view.*
import util.StorageUtil

class PersonItem(val person:user,val userId:String,private val context: Context): Item<GroupieViewHolder>(){
    override fun getLayout(): Int  =  R.layout.chat_single_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.accountHolder.text = person.fname
        viewHolder.itemView.bio_recycler_view.text = person.bio

        if(person.profilePicturePath != null){
            GlideApp.with(context).load(StorageUtil.pathToReference(person.profilePicturePath)).placeholder(R.drawable.ic_baseline_wallpaper_24).into(viewHolder.itemView.profile_image)
        }


    }
}