package com.codetuners.food4need.Activity

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.MahiShortToast
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.ActivityAddPostBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class AddPostActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val CAMERA_REQUEST_CODE = 100
        const val STORAGE_REQUEST_CODE = 200
        val camerapermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val storagepermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        const val IMAGE_PICK_CAMERA_CODE = 300
        const val IMAGE_PICK_GALLERY_CODE = 400
    }

    private lateinit var addpostbinding: ActivityAddPostBinding
    private var Image_URI: Uri? =null
    private val users_db = Firebase.database.getReference("USERS-INFO")
    private var Username: String=""
    private var UserEmail: String=""
    private var UserDPUri: String=""
    private var UserID: String=""
    private lateinit var pd:ProgressDialog

    private var editPtitle=""
    private var editPDescr=""
    private var editPLoc=""
    private var editPMob=""
    private var editImageurl=""
    var isupdatekey=""
    var editpostid=""
    private lateinit var data:ByteArray

    override fun onStart() {
        super.onStart()
        UserID= Firebase.auth.currentUser?.uid.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addpostbinding = ActivityAddPostBinding.inflate(layoutInflater)
        val view = addpostbinding.root
        setContentView(view)

        val intent=intent

        isupdatekey = intent.getStringExtra("key").toString()
        editpostid= intent.getStringExtra("editPostID").toString()


        if (isupdatekey == "EditPost"){
            //do edit process
            addpostbinding.btPostnow.text="UPDATE NOW"
            //MahiShortToast("$isupdatekey $editpostid")
            loadpostdata_to_edit(editpostid)
        }


        addpostbinding.btPostnow.setOnClickListener(this)
        addpostbinding.ImgPostphoto.setOnClickListener(this)


    }

    private fun loadpostdata_to_edit(editpostid: String) {

        val editdb=Firebase.database.getReference("POSTS").orderByChild("PID").equalTo(editpostid)

        editdb.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val child=snapshot.children
                child.forEach {

                    editPtitle=""+it.child("PTitle").value
                    editPDescr=""+it.child("PDescription").value
                    editPLoc=""+it.child("PLocation").value
                    editPMob=""+it.child("PMobileNumber").value
                    editImageurl=""+it.child("PImage").value


                    addpostbinding.etTitle.setText(editPtitle)
                    addpostbinding.etPostdescription.setText(editPDescr)
                    addpostbinding.etLocation.setText(editPLoc)
                    addpostbinding.etPhonenumber.setText(editPMob)

                    if (editImageurl != "NoImage"){
                        try {
                            Glide.with(this@AddPostActivity).load(editImageurl).placeholder(R.drawable.logo).into(addpostbinding.ImgPostphoto)
                        }catch (e:Exception){
                            MahiLongToast(e.message.toString())
                        }
                    }else{
                       MahiShortToast("There is no Image For this post")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                MahiLongToast(error.message)
            }

        })


    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onClick(v: View?) {

        when (v!!.id) {
            R.id.bt_postnow -> {
                submitposttodb()
            }
            R.id.Img_postphoto -> {
                showgal_cam_dialog()
            }
        }
    }


    private fun BeginUpdatePost(
        editpostid: String,
        editPtitle: String,
        editPDescr: String,
        editPLoc: String,
        editPMob: String,
        editImageurl: String,
        editImageurl1: String
    ) {

        if (!validateEditpostinputs(editPtitle,editPDescr,editPLoc,editPMob)){
            return
        }

        if (!validatepostinputs()){
            return
        }

        when {
            editImageurl != "NoImage" -> {
                updatepostwithimage(
                    editpostid,
                    editPtitle,
                    editPDescr,
                    editPLoc,
                    editPMob,
                    editImageurl,
                    UserID,
                    Username,
                    UserEmail,
                    UserDPUri
                )
                return
            }
            addpostbinding.ImgPostphoto.drawable!=null -> {
                updatepostnowimage(
                    editpostid,
                    editPtitle,
                    editPDescr,
                    editPLoc,
                    editPMob,
                    editImageurl,
                    UserID,
                    Username,
                    UserEmail,
                    UserDPUri
                )
                return
            }
            else -> {
                updatepostwithoutimage(
                    editpostid,
                    editPtitle,
                    editPDescr,
                    editPLoc,
                    editPMob,
                    UserID,
                    Username,
                    UserEmail,
                    UserDPUri
                )
                return
            }
        }
    }

    private fun updatepostwithoutimage(
        editpostid: String,
        editPtitle: String,
        editPDescr: String,
        editPLoc: String,
        editPMob: String,
        userID: String,
        username: String,
        userEmail: String,
        userDPUri: String
    ) {

        val update_post_withImage_map = hashMapOf<String,Any>(
            "PDescription" to editPDescr,
            "PID" to editpostid,
            "PImage" to "NoImage",
            "PLocation" to editPLoc,
            "PMobileNumber" to editPMob,
            "PTitle" to editPtitle,
            "PostedTime" to System.currentTimeMillis().toString(),
            "UserDP" to userDPUri,
            "UserEmail" to userEmail,
            "UserID" to userID,
            "UserName" to username,
        )

        val update_posts_db=Firebase.database.getReference("POSTS").child(editpostid)
        update_posts_db.updateChildren(update_post_withImage_map).addOnSuccessListener {
            MahiLongToast("$editPtitle is Updated...")
            hideprogressdialog()
            finish()
        }.addOnFailureListener { e->
            MahiLongToast(e.message.toString())
            hideprogressdialog()
        }

    }


    private fun updatepostnowimage(
        editpostid: String,
        editPtitle: String,
        editPDescr: String,
        editPLoc: String,
        editPMob: String,
        editImageurl: String,
        userID: String,
        username: String,
        userEmail: String,
        userDPUri: String
    ) {

        val timestamp=System.currentTimeMillis()
        val filepathname= "POSTS/Post_$timestamp"

        val postimg=addpostbinding.ImgPostphoto.drawable

        if (postimg!=null){
            val bitmap = postimg.toBitmap()
            val baos=ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
            data= baos.toByteArray()
        }

        val updatestorageref =Firebase.storage.reference.child(filepathname)
        updatestorageref.putBytes(data).addOnSuccessListener {snap->
            val uriTask=snap.storage.downloadUrl

            while(!uriTask.isSuccessful){

            }
            val updatedownloadurl=uriTask.result.toString()

            if (uriTask.isSuccessful){

                val update_post_withImage_map = hashMapOf<String,Any>(
                    "PDescription" to editPDescr,
                    "PID" to editpostid,
                    "PImage" to updatedownloadurl,
                    "PLocation" to editPLoc,
                    "PMobileNumber" to editPMob,
                    "PTitle" to editPtitle,
                    "PostedTime" to System.currentTimeMillis().toString(),
                    "UserDP" to userDPUri,
                    "UserEmail" to userEmail,
                    "UserID" to userID,
                    "UserName" to username,
                )

                val update_posts_db=Firebase.database.getReference("POSTS").child(editpostid)
                update_posts_db.updateChildren(update_post_withImage_map).addOnSuccessListener {
                    MahiLongToast("$editPtitle is Updated...")
                    hideprogressdialog()
                    finish()
                }.addOnFailureListener { e->
                    MahiLongToast(e.message.toString())
                    hideprogressdialog()
                }
            }
        }.addOnFailureListener { e->
            MahiLongToast(e.message.toString())
            hideprogressdialog()
        }

    }


    private fun updatepostwithimage(
        editpostid: String,
        editPtitle: String,
        editPDescr: String,
        editPLoc: String,
        editPMob: String,
        editImageurl: String,
        userID: String,
        username: String,
        userEmail: String,
        userDPUri: String
    ) {

        showprogressdialog("Updating $editPtitle")
        val mpictureref=Firebase.storage.getReferenceFromUrl(editImageurl)
        mpictureref.delete().addOnSuccessListener {

            val timestamp=System.currentTimeMillis()
            val filepathname= "POSTS/Post_$timestamp"

            val postimg=addpostbinding.ImgPostphoto.drawable

            if (postimg!=null){
                val bitmap = postimg.toBitmap()
                val baos=ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
                data= baos.toByteArray()
            }

            val updatestorageref =Firebase.storage.reference.child(filepathname)
            updatestorageref.putBytes(data).addOnSuccessListener {snap->
                val uriTask=snap.storage.downloadUrl

                while(!uriTask.isSuccessful){

                }
                val updatedownloadurl=uriTask.result.toString()

                if (uriTask.isSuccessful){

                    val update_post_withImage_map = hashMapOf<String,Any>(
                        "PDescription" to editPDescr,
                        "PID" to editpostid,
                        "PImage" to updatedownloadurl,
                        "PLocation" to editPLoc,
                        "PMobileNumber" to editPMob,
                        "PTitle" to editPtitle,
                        "PostedTime" to System.currentTimeMillis().toString(),
                        "UserDP" to userDPUri,
                        "UserEmail" to userEmail,
                        "UserID" to userID,
                        "UserName" to username,
                    )

                    val update_posts_db=Firebase.database.getReference("POSTS").child(editpostid)
                        update_posts_db.updateChildren(update_post_withImage_map).addOnSuccessListener {
                        MahiLongToast("$editPtitle is Updated...")
                            hideprogressdialog()
                            finish()
                    }.addOnFailureListener { e->
                        MahiLongToast(e.message.toString())
                            hideprogressdialog()
                    }
                }
            }.addOnFailureListener { e->
                MahiLongToast(e.message.toString())
                hideprogressdialog()
            }

        }.addOnFailureListener {e->
            MahiLongToast(e.message.toString())
            hideprogressdialog()
        }

    }

    private fun validateEditpostinputs(
        editPtitle: String,
        editPDescr: String,
        editPLoc: String,
        editPMob: String
    ): Boolean {

        MahiLongToast("$editPtitle $editPDescr $editPLoc $editPMob")

        val validupdateinfo: Boolean
        val title=addpostbinding.etTitle.text.toString()
        val desc=addpostbinding.etPostdescription.text.toString()
        val loc=addpostbinding.etLocation.text.toString()
        val mobno=addpostbinding.etPhonenumber.text.toString()

        when {
            title.equals(editPtitle) -> {
                MahiShortToast("Modify Title to update")
                validupdateinfo=false
            }
            desc.equals(editPDescr) -> {
                MahiShortToast("Modify Description to update")
                validupdateinfo=false
            }
            loc.equals(editPLoc) -> {
                MahiShortToast("Modify Location To Update")
                validupdateinfo=false
            }
            mobno.equals(editPMob) -> {
                MahiShortToast("Modify Mobile Number To Update")
                validupdateinfo=false
            }
            else -> {
                validupdateinfo=true
            }
        }
        return validupdateinfo
    }

    private fun showgal_cam_dialog() {
        val options = arrayOf("Camera", "Gallery")
        val choosegalcamdg = AlertDialog.Builder(this)
        choosegalcamdg.setTitle("Choose Image From")
        choosegalcamdg.setItems(options) { dialogInterface, position ->
            when (position) {
                0 -> {
                    if (!checkCameraPermisions()) {
                        requestCameraPermissions()
                    } else {
                        PickFromCamera()
                    }
                    MahiShortToast("Camera Clicked")
                }
                1 -> {
                    if (!checkstoragepermission()) {
                        requirestoragepermission()
                    } else {
                        PickFromGallery()
                    }
                    MahiShortToast("Gallery clicked")
                }
            }
        }
        choosegalcamdg.create().show()

    }

    private fun PickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun PickFromCamera() {
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick")
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr")
        Image_URI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Image_URI)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun submitposttodb() {

        if (!validatepostinputs()){
            return
        }

        if (isupdatekey == "EditPost"){
            BeginUpdatePost(editpostid,editpostid,editPtitle,editPDescr,editPLoc,editPMob,editImageurl)
        }

        if (Image_URI==null){
            showprogressdialog("Publishing Your Food Without Image")
        }else{
            showprogressdialog("Publishing Your Food Details")
        }

        val title = addpostbinding.etTitle.text
        val description = addpostbinding.etPostdescription.text
        val location = addpostbinding.etLocation.text
        val mobno = addpostbinding.etPhonenumber.text
        val time = System.currentTimeMillis()

        val query = users_db.orderByChild("email").equalTo(Firebase.auth.currentUser?.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    UserEmail = "" + ds.child("email").value
                    Username  = "" + ds.child("Name").value
                    UserDPUri = "" + ds.child("Image").value
                }
            }
            override fun onCancelled(error: DatabaseError) {
                MahiShortToast("Datbase Error Occured")
            }
        })

        if (Image_URI==null){
            PostwithoutImage(UserID,Username,UserEmail,UserDPUri,title.toString(),description.toString(),location.toString(),mobno.toString(),time.toString(),"NoImage")
        }else{
            PostWithImage(UserID,Username,UserEmail,UserDPUri,title.toString(),description.toString(),location.toString(),mobno.toString(),time.toString(),Image_URI.toString())
        }

        //MahiLongToast("Title : $title Desc : $description Loc : $location Mob : $mobno Time : $time")

    }

    private fun PostwithoutImage(ID:String,Uname:String,Uemail:String,Udpurl:String,Title:String,Desc:String,Location:String,MobNo:String,Time:String,Image:String) {

        val timestamp=System.currentTimeMillis().toString()+"_"+ID

        val post_NOImage_map = hashMapOf(
            "PDescription" to Desc,
            "PID" to timestamp,
            "PImage" to "NoImage",
            "PLocation" to Location,
            "PMobileNumber" to MobNo,
            "PTitle" to Title,
            "PostedTime" to Time,
            "UserDP" to Udpurl,
            "UserEmail" to Uemail,
            "UserID" to ID,
            "UserName" to Uname
            )
        val postNoimgdb=Firebase.database.getReference("POSTS").child(timestamp)

        postNoimgdb.setValue(post_NOImage_map).addOnSuccessListener {
            MahiShortToast("Published Successfully Without Image")
            pd.dismiss()
            finish()
        }.addOnFailureListener{e->
            MahiLongToast(e.message.toString())
            pd.dismiss()
        }
    }


    private fun PostWithImage(UID:String, Uname:String, Uemail:String, Udpurl:String, Title:String, Desc:String, Location:String, MobNo:String, Time:String, Image:String) {

        val timestamp=System.currentTimeMillis().toString()+"_"+UID
        val postimg=addpostbinding.ImgPostphoto.drawable

        if (postimg!=null){
            val bitmap = postimg.toBitmap()
            val baos=ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
            data= baos.toByteArray()
        }

        val storageref =Firebase.storage.reference.child("POSTS/post$timestamp")

        storageref.putBytes(data).addOnSuccessListener { snap->

            val uriTask=snap.storage.downloadUrl

            while(!uriTask.isSuccessful){

            }

            val downloadurl=uriTask.result.toString()

            if (uriTask.isSuccessful){

                val post_withImage_map = hashMapOf(
                    "PDescription" to Desc,
                    "PID" to timestamp,
                    "PImage" to downloadurl,
                    "PLocation" to Location,
                    "PMobileNumber" to MobNo,
                    "PTitle" to Title,
                    "PostedTime" to Time,
                    "UserDP" to Udpurl,
                    "UserEmail" to Uemail,
                    "UserID" to UID,
                    "UserName" to Uname,
                )

                val postdb=Firebase.database.getReference("POSTS").child(timestamp)

                postdb.setValue(post_withImage_map).addOnSuccessListener {
                    MahiShortToast("$Title Posted Successfully...")
                    pd.dismiss()
                    finish()
                }.addOnFailureListener{e->
                    MahiLongToast(e.message.toString())
                    pd.dismiss()
                }
            }

        }.addOnFailureListener { e->
            MahiLongToast(e.message.toString())
            pd.dismiss()
        }
    }

    private fun checkstoragepermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
        return result
    }

    private fun requirestoragepermission() {
        ActivityCompat.requestPermissions(this, storagepermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermisions(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
        val result2 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == (PackageManager.PERMISSION_GRANTED)
        return result1 && result2
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, camerapermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val cam_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storage_accepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cam_accepted && storage_accepted) {
                        PickFromCamera()
                    } else {
                        MahiShortToast("Camera & Storage Permissions Are Necessary")
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val storage_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storage_accepted) {
                        PickFromGallery()
                    } else {
                        MahiShortToast("Storage Permission Is Mandatory")
                    }
                }

            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_GALLERY_CODE -> {
                    if (data != null) {
                        Image_URI = data.data!!
                    }
                    //MahiShortToast("$Image_URI for gallery")
                    addpostbinding.ImgPostphoto.setImageURI(Image_URI)
                }
                IMAGE_PICK_CAMERA_CODE -> {
                    addpostbinding.ImgPostphoto.setImageURI(Image_URI)
                    //MahiShortToast("$Image_URI for camera")
                }
            }
        }

    }

    private fun validatepostinputs():Boolean{
        var validInfo=true
        val title=addpostbinding.etTitle.text
        val desc=addpostbinding.etPostdescription.text
        val loc=addpostbinding.etLocation.text
        val mobno=addpostbinding.etPhonenumber.text
        if (TextUtils.isEmpty(title)){
            MahiShortToast("Enter Title")
            validInfo=false
        }else if (TextUtils.isEmpty(desc)){
            MahiShortToast("Enter Description")
            validInfo=false
        }else if(TextUtils.isEmpty(loc)){
            MahiShortToast("Enter Location")
            validInfo=false
        }else if(TextUtils.isEmpty(mobno)){
            MahiShortToast("Provide Your Number For Contact")
            validInfo=false
        }else{
            validInfo=true
        }

        return validInfo
    }

    private fun showprogressdialog(message: String){
       pd= ProgressDialog(this)
        pd.setMessage(message)
        pd.setCancelable(false)
        pd.show()
    }
    private fun hideprogressdialog(){
        pd.dismiss()
    }
}