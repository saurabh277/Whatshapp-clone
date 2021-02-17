package com.example.whatsappclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.R
import com.example.whatsappclone.adapter.UserViewHolder
import com.example.whatsappclone.model.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_chats.*
import java.lang.Exception

class PeopleFragment : Fragment() {

    private lateinit var mAdapter:FirestorePagingAdapter<User,UserViewHolder>
    private val auth by lazy{
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name",Query.Direction.DESCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupAdapter()
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    private fun setupAdapter() {
        // Init Paging Configuration
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false) //empty value before the things actually loaded
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()
        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database, config, User::class.java)
            .build()

        // Instantiate Paging Adapter
        mAdapter=object :FirestorePagingAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
               val view=layoutInflater.inflate(R.layout.list_item,parent,false)
                return UserViewHolder(view)
                    }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, user: User) {
               //Bind to viewholder
               holder.bind(user)
                  }

            override fun onError(e: Exception) {
                super.onError(e)
            }

                override fun onLoadingStateChanged(state: LoadingState) {
                    when (state) {
                        LoadingState.LOADING_INITIAL -> {
                        }

                        LoadingState.LOADING_MORE -> {
                        }

                        LoadingState.LOADED -> {
                        }

                        LoadingState.ERROR -> {
                            Toast.makeText(
                                requireContext(),
                                "Error Occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        LoadingState.FINISHED -> {
                        }
                    }
                }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager=LinearLayoutManager(requireContext())
            adapter=mAdapter
        }

    }


}
