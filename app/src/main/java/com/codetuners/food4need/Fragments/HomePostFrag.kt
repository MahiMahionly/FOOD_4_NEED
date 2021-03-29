package com.codetuners.food4need.Fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.codetuners.food4need.Adapters.PostsAdapter
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.Models.POST
import com.codetuners.food4need.database.foodpostdatabse
import com.codetuners.food4need.database.postEntity
import com.codetuners.food4need.databinding.FragmentHomePostBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomePostFrag : Fragment() {
    private  var _homePostBinding:FragmentHomePostBinding?=null
    private val homePostBinding  get() = _homePostBinding!!

    private lateinit var postlist:ArrayList<POST>
    private lateinit var postlayoutmanager:LinearLayoutManager
    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
         _homePostBinding= FragmentHomePostBinding.inflate(inflater, container, false)
         val view=homePostBinding.root

        postlist= arrayListOf()

        homePostBinding.progressLay.tvLoading.text="Fetching Posts"
        homePostBinding.progressLay.progressContent.visibility=View.VISIBLE
        homePostBinding.PostRecyclerView.visibility=View.GONE

        postlayoutmanager= LinearLayoutManager(context)
        postlayoutmanager.stackFromEnd=true
        postlayoutmanager.reverseLayout=true
        homePostBinding.PostRecyclerView.layoutManager=postlayoutmanager

        //activity!!.actionBar!!.title="HOME PAGE"

        //(activity as? AppCompatActivity)?.supportActionBar?.title="HOME PAGE"

        //postlist= arrayListOf()

      loadposts(activity as Context)
        return view
    }

    private fun loadposts(context: Context) {
        val posts_db=Firebase.database.getReference("POSTS")

        posts_db.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                postlist.clear()
                val child= snapshot.children

                child.forEach {
                    val post = it.getValue(POST::class.java)!!
                    postlist.add(post)

                    homePostBinding.progressLay.progressContent.visibility=View.GONE
                    homePostBinding.PostRecyclerView.visibility=View.VISIBLE
                }

                postsAdapter = PostsAdapter(context, postlist)
                homePostBinding.PostRecyclerView.adapter = postsAdapter

                //context.MahiLongToast("$postlist")
               /* for (ds in snapshot.children) {
                    val post = snapshot.getValue(USER::class.java)!!
                    postlist.add(post)
                    context.MahiLongToast("$postlist")
                }*/

            }

            override fun onCancelled(error: DatabaseError) {
                context.MahiLongToast("$error")
            }

        })


    }


    class DBasyncTasks(val context: Context,private val foodentity:postEntity,val mode:Int):AsyncTask<Void,Void,Boolean>(){
        private val db= Room.databaseBuilder(context,foodpostdatabse::class.java,"food 4 need DB").fallbackToDestructiveMigration().build()

        override fun doInBackground(vararg p0: Void?): Boolean {
            when(mode){
                1->{
                    val favpost:postEntity?=db.foodDAO().getpostbyId(foodentity.pid)
                    db.close()
                    return favpost!=null
                }

                2->{
                    db.foodDAO().insertpost(foodentity)
                    db.close()
                    return true
                }

                3->{
                    db.foodDAO().deletepost(foodentity)
                    db.close()
                    return true
                }
            }
            return false
        }

    }
}


/*
private fun loadposts(context: Context) {
    val posts_db=Firebase.database.getReference("POSTS")
    posts_db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            postlist.clear()
            for (ds in snapshot.children) {
                val post = snapshot.getValue(POST::class.java)!!
                postlist.add(post)
                context.MahiLongToast("$postlist")
            }
            postsAdapter = PostsAdapter(context, postlist)
            homePostBinding.PostRecyclerView.adapter = postsAdapter
        }
        override fun onCancelled(error: DatabaseError) {
            context.MahiLongToast("$error")
        }
    })
}*/
