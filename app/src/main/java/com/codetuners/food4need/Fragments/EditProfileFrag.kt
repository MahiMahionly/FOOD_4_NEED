package com.codetuners.food4need.Fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextClock
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.codetuners.food4need.Activity.AddPostActivity
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.MahiShortToast
import com.codetuners.food4need.Models.USER
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.FragmentEditProfileBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_web.*
import java.io.ByteArrayOutputStream

class EditProfileFrag : Fragment(),View.OnClickListener {
    private var _editprofbinding:FragmentEditProfileBinding?=null
    private val editProfileBinding get() = _editprofbinding!!
    private lateinit var sp:SharedPreferences
    private lateinit var ed:SharedPreferences.Editor
    private var Image_URI: Uri? =null
    var mail:String=""
    var photourl:String=""
    var isemailverified:Boolean=false
    var uid:String=""
    var role:String=""
    var hotelorganname:String=""
    var phoneno:String=""
    var name:String=""

    private lateinit var data:ByteArray
    companion object{
        const val CAMERA_REQUEST_CODE = 101
        const val STORAGE_REQUEST_CODE = 201

        val camerapermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val storagepermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        const val IMAGE_PICK_CAMERA_CODE = 301
        const val IMAGE_PICK_GALLERY_CODE = 401
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _editprofbinding=FragmentEditProfileBinding.inflate(layoutInflater,container,false)
        // Inflate the layout for this fragment


        sp= activity?.getSharedPreferences(getString(R.string.PREFFILENAME),Context.MODE_PRIVATE)!!
        ed=sp.edit()

        mail=sp.getString("userinfo_email",mail).toString()
        photourl=sp.getString("userinfo_photo",photourl).toString()
        isemailverified=sp.getBoolean("userinfo_isemverified",isemailverified)
        uid=sp.getString("userinfo_uid",uid).toString()

        editProfileBinding.editDummyEmail.text = mail

        editProfileBinding.doneBt.setOnClickListener(this)
        editProfileBinding.btVerify.setOnClickListener(this)
        editProfileBinding.uploadProfilePhoto.setOnClickListener(this)

        name=editProfileBinding.etEdpName.text.toString()

        val selectedoption:Int=editProfileBinding.radiogroup.checkedRadioButtonId
        role = activity?.findViewById<RadioButton>(selectedoption).toString()

        hotelorganname=editProfileBinding.etEdpHotelOrganName.text.toString()
        phoneno=editProfileBinding.etEdpPhone.text.toString()

        showtickicon()

        //if email is verified show tick icon

        return editProfileBinding.root
    }

    private fun showtickicon() {
        when(isemailverified){
            true->{
                editProfileBinding.tickIcon.visibility=View.VISIBLE
                editProfileBinding.btVerify.visibility=View.GONE
            }
            false->{
                editProfileBinding.tickIcon.visibility=View.GONE
                editProfileBinding.btVerify.visibility=View.VISIBLE
            }
        }
    }



    private fun movetoprofile() {
        val profile=ProfileFrag()
        val transaction=(context as FragmentActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.FragFrame,profile)
        transaction.commit()
    }

    /*fun onRadioButtonClicked(view: View){
        if (view is RadioButton){
            val checked = view.isChecked
            when(view.id){
                R.id.donar->{
                    if (checked){
                        //value is donar
                    }
                }
                R.id.receiver->{
                    if (checked){
                        //value is receiver
                    }
                }
            }
        }

    }*/

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.done_bt->{
                store_updates(name,role,hotelorganname,phoneno,mail,photourl,uid)
                //pass changes to db
                //val selectedoption:Int=editProfileBinding.radiogroup.checkedRadioButtonId
                //val checkedbutton = activity?.findViewById<RadioButton>(selectedoption)
            //context?.MahiLongToast("${checkedbutton?.text} is selected")

            }
            R.id.upload_profile_photo->{
                //show cam dialogue
                showgal_cam_dialog()
            }
            R.id.tv_verifymailid->{
                //sent verification
            }
        }

    }

    private fun store_updates(
        name: String,
        role: String,
        hotelorganname: String,
        phoneno: String,
        mail: String,
        photourl: String,
        uid: String
    ) {

        if (!validateinputs()){
            return
        }

        context?.MahiShortToast("Profile is Updating")

        val timestamp="Prof"+name+"_"+uid
        val profileimage=editProfileBinding.uploadProfilePhoto.drawable

        if (profileimage!=null){
            val bitmap=profileimage.toBitmap()
            val baos=ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
            data=baos.toByteArray()
        }

        val profstoragereference=Firebase.storage.reference.child("Prof_Images$timestamp")

        profstoragereference.putBytes(data).addOnSuccessListener {  snap->

            val uritask=snap.storage.downloadUrl

            while (!uritask.isSuccessful){

            }

            val downloadurl=uritask.result.toString()

            if (uritask.isSuccessful){

                val user= USER(downloadurl,name,role,hotelorganname,phoneno,mail,uid)

                val userinfodb=Firebase.database.getReference("USERS_INFO").child(uid)
                userinfodb.setValue(user).addOnSuccessListener { s->
                    movetoprofile()
                    ed.putBoolean("first_visit",false)
                    context!!.MahiShortToast("Profile Updated Successfully")
                }.addOnFailureListener { e->
                    context!!.MahiLongToast(e.message.toString())
                } }
        }.addOnFailureListener {e->
            context!!.MahiLongToast(e.message.toString())
        }

        val udb=Firebase.database




    }

    private fun showgal_cam_dialog() {
        val options = arrayOf("Camera", "Gallery")
        val choosegalcamdg = AlertDialog.Builder(context as Context)
        choosegalcamdg.setTitle("Choose Image From")
        choosegalcamdg.setItems(options) { dialogInterface, position ->
            when (position) {
                0 -> {
                    if (!checkCameraPermisions()) {
                        requestCameraPermissions()
                    } else {
                        PickFromCamera()
                    }
                    context?.MahiShortToast("Camera Clicked")
                }
                1 -> {
                    if (!checkstoragepermission()) {
                        requirestoragepermission()
                    } else {
                        PickFromGallery()
                    }
                    context?.MahiShortToast("Gallery clicked")
                }
            }
        }
        choosegalcamdg.create().show()

    }

    private fun checkstoragepermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            (context as Context),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
        return result
    }

    private fun requirestoragepermission() {
        ActivityCompat.requestPermissions((context as Activity),
            storagepermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkCameraPermisions(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            (context as Context),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
        val result2 = ContextCompat.checkSelfPermission(
            (context as Context),
            android.Manifest.permission.CAMERA
        ) == (PackageManager.PERMISSION_GRANTED)
        return result1 && result2
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions((context as Activity),
            camerapermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun PickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE)
    }

    private fun PickFromCamera() {
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick")
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr")
        Image_URI = context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Image_URI)
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK){
            when(requestCode){
                IMAGE_PICK_GALLERY_CODE->{
                    if (data != null) {
                        Image_URI = data.data!!
                    }
                    editProfileBinding.uploadProfilePhoto.setImageURI(Image_URI)
                }
                IMAGE_PICK_CAMERA_CODE->{
                    editProfileBinding.uploadProfilePhoto.setImageURI(Image_URI)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AddPostActivity.CAMERA_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val cam_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storage_accepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cam_accepted && storage_accepted) {
                        PickFromCamera()
                    } else {
                        context!!.MahiShortToast("Camera & Storage Permissions Are Necessary")
                    }
                }
            }
            AddPostActivity.STORAGE_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val storage_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storage_accepted) {
                        PickFromGallery()
                    } else {
                        context!!.MahiShortToast("Storage Permission Is Mandatory")
                    }
                }

            }
        }

    }

    fun validateinputs():Boolean{
        var valid=true
        val name=editProfileBinding.etEdpName.text.toString()
        val checkrdbtn=editProfileBinding.radiogroup.checkedRadioButtonId
        val btn=activity?.findViewById<RadioButton>(checkrdbtn)
        val hname=editProfileBinding.etEdpHotelOrganName.text.toString()
        val phone=editProfileBinding.etEdpPhone.text.toString()
        if (TextUtils.isEmpty(name)){
            valid=false
        }else if (btn==null){
            valid=false
        }else if(TextUtils.isEmpty(hname)){
            valid=false
        }else valid = !TextUtils.isEmpty(phone)
        return valid
    }
}