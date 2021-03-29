package com.codetuners.food4need.Adapters

import android.animation.Animator
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codetuners.food4need.Activity.AddPostActivity
import com.codetuners.food4need.Fragments.HomePostFrag
import com.codetuners.food4need.Fragments.WebFrag
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.MahiShortToast
import com.codetuners.food4need.Models.POST
import com.codetuners.food4need.R
import com.codetuners.food4need.database.postEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import xyz.hanks.library.bang.SmallBangView
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

class PostsAdapter(val context: Context, val postslist: ArrayList<POST>) :
    RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    val myuid=Firebase.auth.currentUser!!.uid


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postslist[position]

        holder.d_R_Name.text = post.UserName
        holder.location.text = post.PLocation
        holder.postDesc.text = post.PDescription
        holder.postTitle.text = post.PTitle


        val postobj = postEntity(
            post.PID,
            post.PDescription,
            post.PImage,
            post.PLocation,
            post.PMobileNumber,
            post.PTitle,
            post.PostedTime,
            post.UserDP,
            post.UserEmail,
            post.UserID,
            post.UserName
        )

        val favimg = ContextCompat.getDrawable(context, R.drawable.fullheart_icon)
        val notfavimg = ContextCompat.getDrawable(context, R.drawable.heart_icon)

        val checkfav = HomePostFrag.DBasyncTasks(context, postobj, 1).execute()
        val isFav = checkfav.get()

        holder.bangviewlike.isSelected = isFav

        holder.d_R_Name.setOnClickListener {
            val webFrag=WebFrag()
            val args=Bundle()
            args.putString("search_hint",post.UserName)
            webFrag.arguments=args
            (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.FragFrame,webFrag).addToBackStack("web").commit()
            (context as AppCompatActivity).findViewById<TextView>(R.id.toolbar_textview).text="Search View"
        }

        holder.likebtn.setOnClickListener {
            //save to liked list
            //show little heart blast icon

            if (!HomePostFrag.DBasyncTasks(context, postobj, 1).execute().get()) {

                val addfav = HomePostFrag.DBasyncTasks(context, postobj, 2).execute()
                val result = addfav.get()

                if (result) {
                    //holder.likeimg.setImageDrawable(favimg)
                    holder.bangviewlike.isSelected = true
                    holder.bangviewlike.likeAnimation(object : Animator.AnimatorListener {
                        override fun onAnimationStart(p0: Animator?) {

                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            context.MahiShortToast("Added To Favourites")
                        }

                        override fun onAnimationCancel(p0: Animator?) {

                        }

                        override fun onAnimationRepeat(p0: Animator?) {

                        }

                    }
                    )

                } else {
                    context.MahiShortToast("Couldn't Add To Favourites")
                }
            } else {
                val removefav = HomePostFrag.DBasyncTasks(context, postobj, 3).execute()
                val result = removefav.get()

                if (result) {
                    context.MahiShortToast("Removed From Favourites")
                    //holder.likeimg.setImageDrawable(notfavimg)
                    holder.bangviewlike.isSelected = false
                } else {
                    context.MahiShortToast("Didn't Remove From Favourites")
                }
            }
        }

        holder.callbtn.setOnClickListener {
            val callintent = Intent(Intent.ACTION_DIAL)
            callintent.data = Uri.parse("tel:" + post.PMobileNumber)
            context.startActivity(callintent)
            //pass intent to dialer
            //post.Pmob
        }

        holder.sharebtn.setOnClickListener {
            //pass intent to share
            //context.MahiShortToast("Share Button Clicked")

            val bitmapdrawable = holder.postImage.drawable

            if (bitmapdrawable == null) {

                sharetextonly(post.PTitle, post.PDescription)

            } else {

                val bitmap: Bitmap = bitmapdrawable.toBitmap()

                shareImageandText(post.PTitle, post.PDescription, bitmap)
            }

        }

        if (post.PImage == "NoImage") {
            holder.postcardview.visibility = View.GONE
        } else {
            //set image using glide
            Glide.with(context).load(post.PImage).placeholder(R.drawable.logo)
                .into(holder.postImage)
        }

        //set post time
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = post.PostedTime.toLong()
        val ptime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar)
        holder.postTime.text = ptime

        holder.itemView.setOnLongClickListener {

            if (checkownerofpost(post.UserID,myuid)){
                showbottomdialog(post.PID,post.PImage,postobj)
            }else{
                context.MahiShortToast("You Can't Edit / Delete Other User Posts")
            }
            return@setOnLongClickListener true
        }
    }

    private fun checkownerofpost(uid: String, myuid: String): Boolean {

        return uid.equals(myuid)
    }

    private fun showbottomdialog(pid: String, pImage: String,Post:postEntity) {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null)

        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(view)

        val editbtn = bottomSheetDialog.findViewById<TextView>(R.id.edit_btn_btmsheet)
        val deletebtn = bottomSheetDialog.findViewById<TextView>(R.id.delete_btn_btmsheet)

        editbtn?.setOnClickListener {
            passintentforedit(pid)
            bottomSheetDialog.dismiss()
        }
        deletebtn?.setOnClickListener {
            beginDelete(pid,pImage,Post)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()

    }

    private fun passintentforedit(pid: String) {
        val editintent=Intent(context,AddPostActivity::class.java)
        editintent.putExtra("key","EditPost")
        editintent.putExtra("editPostID",pid)
        context.startActivity(editintent)
    }

    private fun removepost_in_room(post: postEntity):Boolean {
        val remove =HomePostFrag.DBasyncTasks(context,post,3).execute().get()

        return remove
    }

    private fun beginDelete(pid: String, pImage: String,Post: postEntity) {

        if (!removepost_in_room(Post)){
            context.MahiLongToast("Couldn't Delete From Favourites")
            return
        }

        if (pImage.equals("NoImage")){
            deletewithoutimagepost(pid)
        }else{
            deletepostwithimage(pid,pImage)
        }
    }

    private fun deletepostwithimage(pid: String, pImage: String) {
        val pd=ProgressDialog(context)
        pd.setMessage("Deleting in Process..")

        pd.show()

        val picstorageref=Firebase.storage.getReferenceFromUrl(pImage)

        picstorageref.delete().addOnSuccessListener {
            //Image Deleted Now Delete Database

            //val fquery=Firebase.database.getReference("POSTS").orderByChild("PID").orderByValue().equalTo(pid)

            val fquery=Firebase.database.getReference("POSTS").child(pid)

            fquery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val child=snapshot.children
                         child.forEach { it->
                        it.ref.removeValue()

                    }
                    notifyDataSetChanged()
                    context.MahiShortToast("Deleted Successfully")
                    pd.dismiss()

                    /*for (child in snapshot.children) {
                        child.ref.removeValue()
                    }*/

                }

                override fun onCancelled(error: DatabaseError) {
                    context.MahiLongToast(error.message)
                }

            })

        }.addOnFailureListener { e->
            context.MahiShortToast(e.message.toString())
        }

    }

    private fun deletewithoutimagepost(pid: String) {
        val pd=ProgressDialog(context)
        pd.setMessage("Deleting in Process..")
        pd.show()

        //val fquery=Firebase.database.getReference("POSTS").orderByChild("PID").equalTo(Pid)
        val fquery=Firebase.database.getReference("POSTS").child(pid)

        fquery.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for(c in snapshot.children){
                    c.ref.removeValue()
                }
                context.MahiShortToast("Deleted Successfully")
                notifyDataSetChanged()
                pd.dismiss()

             /*  val child=snapshot.children
                 child.forEach { it->
                     it.ref.removeValue()
                 }*/
            }
            override fun onCancelled(error: DatabaseError) {
                context.MahiLongToast(error.message)
            }

        })
    }


    private fun shareImageandText(pTitle: String, pDescription: String, bitmap: Bitmap) {

        val sharebody = pTitle + "\n" + pDescription

        val uri: Uri = saveImageToShare(bitmap)

        val sintent = Intent(Intent.ACTION_SEND)
        sintent.putExtra(Intent.EXTRA_STREAM, uri)
        sintent.putExtra(Intent.EXTRA_TEXT, sharebody)
        sintent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT")
        sintent.type = "image/png"
        context.startActivity(Intent.createChooser(sintent, "Share $pTitle with Image Via"))
    }

    private fun saveImageToShare(bitmap: Bitmap): Uri {

        val imagefolder: File = File(context.cacheDir, "images")

        var uri: Uri? = null

        try {
            imagefolder.mkdirs()
            val file: File = File(imagefolder, "shared_image.png")
            val stream: FileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(context, "com.codetuners.food4need.fileprovider", file)
        } catch (e: Exception) {
            context.MahiLongToast(e.message!!)
        }

        return uri!!
    }

    private fun sharetextonly(pTitle: String, pDescription: String) {

        val sharebody = pTitle + "\n" + pDescription

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT")
        intent.putExtra(Intent.EXTRA_TEXT, sharebody)
        context.startActivity(Intent.createChooser(intent, "Share $pTitle Via"))

    }

    override fun getItemCount(): Int {
        if (postslist.isEmpty()) {
            context.MahiLongToast("NO POSTS")
        }
        return postslist.size
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        val bangviewlike: SmallBangView = view.findViewById(R.id.likebangview)
        val postcardview: CardView = view.findViewById(R.id.postimagecardview)
    }
}