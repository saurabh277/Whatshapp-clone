package com.example.whatsappclone.model

import java.util.*

data class Inbox(
 val msg:String,
 val from:String,
 var name:String,
 var image:String,
 val time:Date = Date(),
var count:Int =0
){
    //empty constructor
    constructor():this("","","","",Date(),0)
}
