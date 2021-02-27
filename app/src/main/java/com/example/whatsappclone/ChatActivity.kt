package com.example.whatsappclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.adapter.ChatAdapter
import com.example.whatsappclone.model.*
import com.example.whatsappclone.utils.KeyboardVisibilityUtil
import com.example.whatsappclone.utils.isSameDayAs

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private val messages = mutableListOf<ChatEvent>()
    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()

    lateinit var chatAdapter:ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        keyboardVisibilityHelper = KeyboardVisibilityUtil(rootView) {
            msgRv.scrollToPosition(mutableItems.size - 1)
        }

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser=it.toObject(User::class.java)!!
            }
        chatAdapter = ChatAdapter(messages,mCurrentUid)
        msgRv.apply {
            layoutManager=LinearLayoutManager(this@ChatActivity)
            adapter=chatAdapter
        }

        nameTv.text =name
        Picasso.get().load(image).into(userImgView)
        val emojiPopup=EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }

        swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                swipeToLoad.isRefreshing =false
            }
        }

        listenToMessage()
        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if(it.isNotEmpty()){ //if edit text is not empty
                    sendMessage(it.toString())
                    it.clear() //clear the msg from edit text
                }
            }
        }
        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }
        updateReadCount()
    }
    private fun updateReadCount() {
        getInbox(mCurrentUid, friendId).child("count").setValue(0)
    }

    private fun listenToMessage() {
        getMessages((friendId))
            .orderByKey() //because we want msg in some order

            //to listen all changes occuring in  children of this message
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    //curent msg added
                    val msg=snapshot.getValue(Message::class.java)
                    //now we need to update this msg
                    addMessage(msg)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })
    }


    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId).child(id).updateChildren(mapOf("liked" to status))
    }


    private fun addMessage(msg: Message?) {
        val eventBefore =messages.lastOrNull() //it will give us last value or null if no vakue is there
        if (msg != null) {
            if(eventBefore !=null && !eventBefore.sentAt.isSameDayAs(msg.sentAt) || eventBefore ==null){
                //we just add items in list if current msg date is same as last sent msg
                //else add dateheader
                messages.add(
                    DateHeader(
                        msg.sentAt,context=this
                    )
                )
            }
        }
        if (msg != null) {
            messages.add(msg)
        }
        chatAdapter.notifyItemInserted(messages.size -1)
        msgRv.scrollToPosition(messages.size -1)
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
    override fun onResume() {
        super.onResume()
        rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(USER_ID, id)
            intent.putExtra(USER_NAME, name)
            intent.putExtra(USER_THUMB_IMAGE, image)

            return intent
        }
    }
}
