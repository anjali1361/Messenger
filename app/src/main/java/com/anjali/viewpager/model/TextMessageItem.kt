package com.anjali.viewpager.model

import android.content.Context
import com.anjali.viewpager.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.item_text_messages.view.*

class TextMessageItem (val message: TextMessage, val context: Context): MessageItem(message) {
    override fun getLayout(): Int =
        R.layout.item_text_messages

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.sending_message.text = message.text
        super.bind(viewHolder, position)

    }

    override fun isSameAs(other:Item<*>): Boolean {

        if (other !is TextMessageItem)
            return false
        if(this.message != other.message)//if contents of the message is not same
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
       return isSameAs(other as TextMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }


}