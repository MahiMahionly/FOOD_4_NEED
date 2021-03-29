package com.codetuners.food4need.Fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.codetuners.food4need.Adapters.FavouritesAdapter
import com.codetuners.food4need.Models.POST
import com.codetuners.food4need.R
import com.codetuners.food4need.database.foodpostdatabse
import com.codetuners.food4need.database.postEntity
import com.codetuners.food4need.databinding.FragmentFavouritesBinding


class FavouritesFrag : Fragment() {

    private var _favouritesbinding:FragmentFavouritesBinding?=null
    private val favouritesBinding get() = _favouritesbinding!!

    private lateinit var favlist:List<postEntity>
    private lateinit var favouritesAdapter: FavouritesAdapter
    private lateinit var favouritesmanager:LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _favouritesbinding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val view=favouritesBinding.root

        favouritesBinding.progressLay.tvLoading.text="Getting Favourites"
        favouritesBinding.progressLay.progressContent.visibility=View.VISIBLE
        favouritesBinding.favouritesRecyclerview.visibility=View.GONE

        favouritesmanager=LinearLayoutManager(context)
        favouritesmanager.stackFromEnd=true
        favouritesmanager.reverseLayout=true

        favouritesBinding.favouritesRecyclerview.layoutManager=favouritesmanager

        loadfavposts(activity as Context)

        return view
    }

    private fun loadfavposts(context: Context) {
        favlist=RetrievFav(context).execute().get()

        favouritesAdapter=FavouritesAdapter(context,favlist)
        favouritesBinding.favouritesRecyclerview.adapter=favouritesAdapter
        favouritesBinding.progressLay.progressContent.visibility=View.GONE
        favouritesBinding.favouritesRecyclerview.visibility=View.VISIBLE
    }

    class RetrievFav(val context: Context):AsyncTask<Void,Void,List<postEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<postEntity> {
            val db=Room.databaseBuilder(context,foodpostdatabse::class.java,"food 4 need DB").build()

            return db.foodDAO().getallfavposts()
        }
    }
}