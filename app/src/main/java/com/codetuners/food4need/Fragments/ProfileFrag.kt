package com.codetuners.food4need.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.Models.USER
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileFrag : Fragment(){

    private var _ProfileBinding:FragmentProfileBinding?=null

    private val ProfileBinding get() = _ProfileBinding!!

    private val user:FirebaseUser?=null

    private lateinit var sp:SharedPreferences
    private lateinit var ed:SharedPreferences.Editor

    var username:String=""
    var role:String=""
    var phoneno:String=""
    var email:String=""
    var hotelname:String=""
    var photourl:String=""
    var isemailverified:Boolean=false
    var uid:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _ProfileBinding=FragmentProfileBinding.inflate(inflater,container,false)

        sp = activity?.getSharedPreferences(getString(R.string.PREFFILENAME), Context.MODE_PRIVATE)!!


        get_Prof_Data_FromDB()

        showtickicon()


        ProfileBinding.profEditFab.setOnClickListener {

            (context as FragmentActivity).findViewById<TextView>(R.id.toolbar_textview).text="Edit Profile"

            val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
            transaction.replace(R.id.FragFrame,EditProfileFrag())
            transaction.commit()
        }

        username= sp.getString("userinfo_name",username).toString()
        email= sp.getString("userinfo_email",email).toString()
        photourl= sp.getString("userinfo_photo",photourl).toString()

        uid= sp.getString("userinfo_uid",uid).toString()
        isemailverified=sp.getBoolean("userinfo_isemverified",isemailverified)
        //role to be added in db
        //phone to be added in db

        if ( email !="" ){
            ProfileBinding.profEmail.text=email
            ProfileBinding.profUsername.text=username
        }

        context?.MahiLongToast("$username  $email $photourl $uid $isemailverified")

        return ProfileBinding.root
    }

    private fun showtickicon() {
        when(isemailverified){
            true->{
                ProfileBinding.profTickIcon.visibility=View.VISIBLE
                ProfileBinding.profBtVerify.visibility=View.GONE
            }
            false->{
                ProfileBinding.profTickIcon.visibility=View.GONE
                ProfileBinding.profBtVerify.visibility=View.VISIBLE
            }
        }
    }

    private fun get_Prof_Data_FromDB() {
        val profdbref=Firebase.database.getReference("USERS_INFO").orderByChild("UserID").equalTo(uid)

        profdbref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

//                val child=snapshot.children

                /*child.forEach {
                    val user=it.getValue(USER::class.java)!!

                    username=user.Name
                    role=user.role
                    photourl=user.Image
                    email=user.email
                    phoneno=user.phone
                    hotelname=user.hotel_organ_name


                }*/



                for(s in snapshot.children){
                    s.getValue(USER::class.java)
                }
                UpdateUI(username,role,photourl,email,phoneno,hotelname)
            }

            override fun onCancelled(error: DatabaseError) {
                context!!.MahiLongToast(error.message)
            }

        })

    }
    private fun UpdateUI(username: String, role: String, photourl: String, email: String, phoneno: String, hotelname: String) {

        ProfileBinding.profUsername.text=username
        ProfileBinding.profRoletype.text=role
        ProfileBinding.profEmail.text=email
        ProfileBinding.profPhoneno.text=phoneno
        ProfileBinding.profHotelname.text=hotelname

        Glide.with(context as Context)
            .load(photourl)
            .placeholder(R.drawable.logo)
            .into(ProfileBinding.profilePhoto)
    }

    /*override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.prof_edit_fab->{
                context!!.MahiLongToast("fab Clicked")
                val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
                transaction.replace(R.id.FragFrame,EditProfileFrag())
                transaction.commit()
            }
        }
    }*/
}


