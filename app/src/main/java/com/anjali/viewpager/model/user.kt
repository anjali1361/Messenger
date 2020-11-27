package com.anjali.viewpager.model

data class user(val fname:String,val bio:String, val email:String,val phone:String, val profilePicturePath:String?,val registrationTokens:MutableList<String>) {
    constructor():this("","","","",null, mutableListOf())
}