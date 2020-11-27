package com.anjali.viewpager.model

data class ChatChannel(val userIds: MutableList<String?>){
    constructor(): this(mutableListOf())
}