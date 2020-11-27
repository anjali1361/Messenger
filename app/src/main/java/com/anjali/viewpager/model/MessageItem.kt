package com.anjali.viewpager.model

import android.view.Gravity
import com.anjali.viewpager.R
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.item_image_message.view.*
import kotlinx.android.synthetic.main.item_text_messages.view.*
import kotlinx.android.synthetic.main.item_text_messages.view.message_root
import kotlinx.android.synthetic.main.item_text_messages.view.sending_message_time
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent
import java.text.SimpleDateFormat

abstract class MessageItem(private val message: Message):Item<GroupieViewHolder>() {//abstract class to share the commom fun bet textmessageitem and imagemessageitem class

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
       setTimeText(viewHolder)
        setMessageRootGravity(viewHolder)
    }

    private fun setTimeText(viewHolder: GroupieViewHolder){
        val dateFormat = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT,
            SimpleDateFormat.SHORT)
        viewHolder.itemView.sending_message_time.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravity(viewHolder: GroupieViewHolder){
        if(message.senderId == FirebaseAuth.getInstance().currentUser?.uid){
            viewHolder.itemView.message_root.apply {
                backgroundResource =
                    R.drawable.rect_around_primary_color

                //Redefining frame layout parameters

                val lParams  = android.widget.FrameLayout.LayoutParams(
                    wrapContent,
                    wrapContent, Gravity.END)
                this.layoutParams = lParams
            }

        }
        else{
            viewHolder.itemView.message_root.apply {
                backgroundResource =
                    R.drawable.rect_around_white_color
                val lParams = android.widget.FrameLayout.LayoutParams(
                    wrapContent, wrapContent,
                    Gravity.START)
                this.layoutParams = lParams
            }
        }
    }

    private fun onImageTouch(viewHolder: GroupieViewHolder){

        if(message.type == MessageType.IMAGE){
            viewHolder.itemView.sending_image.setOnClickListener {

            }
        }

    }

}