package com.example.whatsappclone

import com.google.firebase.firestore.FieldValue

data class User(
    val name:String,
    val imageUrl:String,
    val thumbImage:String,
    val uid:String,
    val deviceToken:String,//to send notification
    val status:String,
    val onlineStatus: FieldValue
) {
     /**Empty [Constructor] for firebase*/
    constructor():this("","","","","","","")
    //now we made actual constructor because at time of user creation we won't have all parameter like device token,online status,status
    constructor(name: String,imageUrl: String,thumbImage: String,uid: String):this(
        name,
        imageUrl,
        thumbImage,
        uid,
        "",
        "Hey there I aM using whatshapp",
        FieldValue.serverTimestamp() //we can use anything that is current time when you are creating account -this is calculated using firebasse end or we can also use system.currtimeinmillisec()
    )
}