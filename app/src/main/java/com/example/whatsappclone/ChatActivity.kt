package com.example.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.whatsappclone.model.Inbox
import com.example.whatsappclone.model.Message
import com.example.whatsappclone.model.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val USER_ID = "userId"
const val USER_THUMB_IMAGE = "thumbImage"
const val USER_NAME = "userName"

class ChatActivity : AppCompatActivity() {

    private val friendId: String by lazy {
        intent.getStringExtra(USER_ID)
    }
    private val name: String by lazy {
        intent.getStringExtra(USER_NAME)
    }
    private val image: String by lazy {
        intent.getStringExtra(USER_THUMB_IMAGE)
    }
    private val mCurrentUid: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    lateinit var currentUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
             currentUser=it.toObject(User::class.java)!!
            }

        nameTv.text =name
        Picasso.get().load(image).into(userImgView)
        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if(it.isNotEmpty()){ //if edit text is not empty
                    sendMessage(it.toString())
                    it.clear() //clear the msg from edit text
                }
            }
        }
    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key //get a unique id
        checkNotNull(id) { "Cannot be null" }
        val msgMap = Message(msg, mCurrentUid, id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {
            Log.i("CHATS","completed")
        }.addOnFailureListener {
            Log.i("CHATS",it.localizedMessage)
        }

        //update lastmsg in inbox
        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {
     val inboxMap = Inbox(
         message.msg,
         friendId,
         name,
         image,
         count=0
     )
     getInbox(mCurrentUid,friendId).setValue(inboxMap).addOnSuccessListener {
         getInbox(friendId,mCurrentUid).addListenerForSingleValueEvent(object:ValueEventListener{
            //to get the last value
             override fun onCancelled(error: DatabaseError) {
                   }

             override fun onDataChange(snapshot: DataSnapshot) {
             //current value of inbox of friend and mcurrent uid
              val value =snapshot.getValue(Inbox::class.java)

                 inboxMap.apply {
                     from =message.senderId
                     name=currentUser.name
                     image=currentUser.thumbImage
                     count=1
                 }
                 //if there is previous chat and msg is not read
                 if (value?.from == message.senderId) {
                     inboxMap.count = value.count + 1
                 }
              getInbox(friendId,mCurrentUid).setValue(inboxMap)

             }

         })

     }
    }

    //this function is used to set the value as 0
    private fun markasRead(){
     getInbox(friendId,mCurrentUid).child("count").setValue(0)
    }

    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")


    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }
}
