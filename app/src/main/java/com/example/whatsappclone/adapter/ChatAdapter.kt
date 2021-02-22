package com.example.whatsappclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.model.ChatEvent
import com.example.whatsappclone.model.DateHeader
import com.example.whatsappclone.model.Message
import com.example.whatsappclone.utils.formatAsTime
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.*
import kotlinx.android.synthetic.main.list_item_date_header.view.*

class ChatAdapter (private val list:MutableList<ChatEvent>,private val mCurrentUid:String ):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //higher order function
        val inflate ={layout:Int ->
        LayoutInflater.from(parent.context).inflate(layout,parent,false)
        }
        return when(viewType) {
            TEXT_MESSAGE_RECIEVED ->{
             MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
            }
            TEXT_MESSAGE_SENT ->{
             MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
            }
            DATE_HEADER ->{
             DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else ->MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
        }
           }

    override fun getItemCount(): Int =list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      when(val item =list[position]){
            is DateHeader->{
                holder.itemView.textView.text =item.date
            }
            is Message->{
                holder.itemView.apply {
                    content.text=item.msg
                    time.text=item.sentAt.formatAsTime()

                }

            }
        }

         }

    //this tells which view we need to show
    override fun getItemViewType(position: Int): Int {
        return when(val event =list[position]){
            is Message ->{
                //if we have send msg to other
                if(event.senderId == mCurrentUid){
                    TEXT_MESSAGE_SENT
                }
                //if someone msg me
                else{
                    TEXT_MESSAGE_RECIEVED
                }
            }
            is DateHeader -> DATE_HEADER
            else -> UHNSUPPORTED
        }
    }

    class DateViewHolder(view: View):RecyclerView.ViewHolder(view)

    class MessageViewHolder(view:View):RecyclerView.ViewHolder(view)

    //companion objects are static variable(use outside the class also)
    companion object{
        private const val UHNSUPPORTED =-1
        private const val TEXT_MESSAGE_RECIEVED=0
        private const val TEXT_MESSAGE_SENT =1
        private const val DATE_HEADER =2
    }

}