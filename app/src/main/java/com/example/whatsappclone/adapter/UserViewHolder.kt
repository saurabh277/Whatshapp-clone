package com.example.whatsappclone.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_sign_up.view.*
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.userImgView

class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

    fun bind(user: User)=with(itemView){
        countTv.isVisible=false
        timeTv.isVisible=false
        titleTv.text=user.name
        subtitleTv.text=user.status

        Picasso.get().load(user.thumbImage)
            .placeholder(R.drawable.defaultavatar)
            .error(R.drawable.defaultavatar)
            .into(userImgView)


    }
}