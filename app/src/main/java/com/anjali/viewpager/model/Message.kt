package com.anjali.viewpager.model

import java.util.*

object MessageType{
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

interface Message {
    //common fields between class textmessage and interface
    val time:Date
    val senderId:String
    val recipientId:String
    val senderName:String
    val type:String
    val sender_profile_picture_path:String
}