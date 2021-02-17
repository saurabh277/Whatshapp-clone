package com.example.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.adapter.ScreenSliderAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class  MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewPager.adapter= ScreenSliderAdapter(this)
        TabLayoutMediator(tabs,viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, pos ->
            when(pos){
                0->tab.text="Chats"
                1->tab.text="People"
            }
        }).attach()
    }
}
