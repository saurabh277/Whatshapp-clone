package com.example.whatsappclone.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappclone.fragments.ChatsFragment
import com.example.whatsappclone.fragments.PeopleFragment

class ScreenSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int =2 //only two tabs

    override fun createFragment(position: Int): Fragment = when(position){
       0 -> ChatsFragment()
       else -> PeopleFragment()
    }

}
