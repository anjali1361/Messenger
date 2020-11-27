package com.anjali.viewpager.model

import android.annotation.SuppressLint
import android.content.Context
import com.anjali.viewpager.R
import com.anjali.viewpager.glide.GlideApp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.item_image_message.view.*
import util.StorageUtil

class ImageMessageItem(val message:ImageMessage,val context: Context):MessageItem(message) {

    var requestOptions  = RequestOptions()

    @SuppressLint("CheckResult")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        super.bind(viewHolder, position)
        GlideApp.with(context).load(StorageUtil.pathToReference(message.imagePath)).placeholder(R.drawable.facebookmessenger).into(viewHolder.itemView.sending_image)



    }
    override fun getLayout() = R.layout.item_image_message

    override fun isSameAs(other: Item<*>): Boolean {

        if (other !is ImageMessageItem)
            return false
        if(this.message !=other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as ImageMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }




    }


