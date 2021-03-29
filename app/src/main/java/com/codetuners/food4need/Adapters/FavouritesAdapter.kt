package com.codetuners.food4need.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codetuners.food4need.Fragments.HomePostFrag
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.MahiShortToast
import com.codetuners.food4need.R
import com.codetuners.food4need.database.postEntity
import java.util.*

class FavouritesAdapter(val context: Context,val favlist:List<postEntity>): RecyclerView.Adapter<FavouritesAdapter.FavViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_post,parent,false)
        return FavViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val favpost=favlist[position]

        holder.postTitle.text=favpost.pTitle
        holder.postDesc.text=favpost.pdesc
        holder.d_R_Name.text=favpost.username
        holder.location.text=favpost.pLoc

        val favobj=postEntity(
            favpost.pid,
            favpost.pdesc,
            favpost.pImage,
            favpost.pLoc,
            favpost.pMob,
            favpost.pTitle,
            favpost.postedtime,
            favpost.userdap,
            favpost.useremail,
            favpost.userid,
            favpost.username
        )

        val favimg = ContextCompat.getDrawable(context, R.drawable.fullheart_icon)
        val notfavimg = ContextCompat.getDrawable(context, R.drawable.heart_icon)

        val checkFav=HomePostFrag.DBasyncTasks(context,favobj,1).execute()
        val isFav=checkFav.get()

        if (isFav){
            holder.likeimg.setImageDrawable(favimg)
        }
        else{
            holder.likeimg.setImageDrawable(notfavimg)
        }

        holder.likebtn.setOnClickListener {
            if (!HomePostFrag.DBasyncTasks(context,favobj,1).execute().get()){

                val addFav=HomePostFrag.DBasyncTasks(context,favobj,2).execute()
                val result=addFav.get()

                if (result){
                    context.MahiShortToast("Added to Favourites")
                    holder.likeimg.setImageDrawable(favimg)
                }
                else{
                    context.MahiShortToast("Couldn't Add to Favourites")
                }
            }else{

                val removeFav=HomePostFrag.DBasyncTasks(context,favobj,3).execute()
                val result=removeFav.get()

                if (result){
                    context.MahiShortToast("Removed From Favourites")
                    holder.likeimg.setImageDrawable(notfavimg)
                }else{
                    context.MahiShortToast("Didn't Remove from favourites")
                }
            }
        }
        holder.sharebtn.setOnClickListener {
            context.MahiShortToast("Share Button Cliked")
        }
        holder.callbtn.setOnClickListener {
            val callintent = Intent(Intent.ACTION_DIAL)
            callintent.data = Uri.parse("tel:" + favpost.pMob)
            context.startActivity(callintent)
        }

        if (favpost.pImage == "NoImage") {
            holder.postImage.visibility = View.GONE
        } else {
            //set image using glide
            Glide.with(context).load(favpost.pImage).placeholder(R.drawable.logo)
                .into(holder.postImage)
        }

        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = favpost.postedtime.toLong()
        val ptime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar)
        holder.postTime.text = ptime
    }

    override fun getItemCount(): Int {
        if (favlist.isEmpty()) {
            context.MahiLongToast("There Is No Favourites")
        }
        return favlist.size
    }

    class FavViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val postTitle: TextView = view.findViewById(R.id.tv_PTitle)
        val postDesc: TextView = view.findViewById(R.id.tv_Pdesc)
        val d_R_Name: TextView = view.findViewById(R.id.tv_donar_name)
        val location: TextView = view.findViewById(R.id.tv_location)
        val postImage: ImageView = view.findViewById(R.id.Img_PostImagerecycler)
        val likebtn: CardView = view.findViewById(R.id.likebtn)
        val likeimg: ImageView = view.findViewById(R.id.likeIMG)
        val callbtn: CardView = view.findViewById(R.id.callbtn)
        val sharebtn: CardView = view.findViewById(R.id.sharebtn)
        val postTime: TextView = view.findViewById(R.id.postedtime)

    }
}