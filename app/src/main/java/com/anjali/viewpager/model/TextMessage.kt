package com.anjali.viewpager.model

import java.util.*

class TextMessage(val text:String, override val time: Date, override val senderId:String,
                  override val recipientId: String,
                  override val senderName: String,
                  override val sender_profile_picture_path: String,
                  override val type:String = MessageType.TEXT
):
    Message {

    constructor():this("",Date(0),"","","","")
}