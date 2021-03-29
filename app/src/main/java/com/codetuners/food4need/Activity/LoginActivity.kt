package com.codetuners.food4need.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.MahiShortToast
import com.codetuners.food4need.Models.USER
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var loginBinding: ActivityLoginBinding
    lateinit var Mauth: FirebaseAuth
    private var usernow:FirebaseUser?=null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

     var name :String?    =""
     var demail :String?  =""
     var uid    :String?  =""
     var phone  :String?  =""
     var imageurl =""
     var role:String?=""
     var hotel_organ_name:String?=""

     var logbool:Boolean=false

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)
        sharedPreferences =
            this.getSharedPreferences(getString(R.string.PREFFILENAME), Context.MODE_PRIVATE)
        editor=sharedPreferences.edit()

        Mauth = Firebase.auth
        logbool=sharedPreferences.getBoolean("logbool",false)

        loginBinding.btLogin.setOnClickListener(this)
        loginBinding.btSignup.setOnClickListener(this)
        loginBinding.tvResetpwd.setOnClickListener(this)
        loginBinding.tvVerifymailid.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_login -> {
                MakeSignIn(
                    loginBinding.etEmail.text.toString(),
                    loginBinding.etPassword.text.toString()
                )
            }
            R.id.bt_signup -> {
                CreateUser(
                    loginBinding.etEmail.text.toString(),
                    loginBinding.etPassword.text.toString()
                )
            }

            R.id.tv_resetpwd -> {
                showresetpwddialog("reset_dg")
                //Toast.makeText(this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.tv_verifymailid -> {
                showresetpwddialog("verify_dg")
            }
        }
    }

    private fun showresetpwddialog(mode: String) {
        val alertdialog = AlertDialog.Builder(this)
        val lview = this.layoutInflater.inflate(R.layout.reset_pwd_dialog, null)
        alertdialog.setView(lview)
        val title = lview.findViewById<TextView>(R.id.tv_alert_dialog_title)

        if (mode.equals("reset_dg")) {
            title.text = "RESET YOUR PASSWORD"
        }

        if (mode.equals("verify_dg")) {
            title.text = "VERIFY YOUR MAIL ID"
        }

        val et_resetmail = lview.findViewById<EditText>(R.id.et_resetpwd_email)

        val bt_cancel = lview.findViewById<Button>(R.id.bt_cancel_dialog)
        val bt_proceed = lview.findViewById<Button>(R.id.btt_proceed_dialog)

        val remail = et_resetmail.text

        bt_proceed.setOnClickListener {
            when (mode) {
                "reset_dg" -> {
                    sendresetpwdlink(remail)

                }
                "verify_dg" -> {
                    verifyemail(remail)

                }

            }
        }
        val dialog = alertdialog.create()
        dialog.show()
        bt_cancel.setOnClickListener {
            dialog.dismiss()
            //nothing
        }
    }

    private fun verifyemail(remail: Editable?) {
        Mauth.currentUser!!.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                MahiShortToast("Check Your Mail Now")
            }
        }.addOnFailureListener { e ->
            MahiLongToast(e.message.toString())
        }
    }

    private fun sendresetpwdlink(resetemail: Editable) {
        Mauth.sendPasswordResetEmail(resetemail.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
               MahiShortToast("Link Sent Successfully..Check Your Mail")
            }
        }.addOnFailureListener { e ->
            MahiLongToast(e.message.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        usernow = Firebase.auth.currentUser
        //the user is logged in
        if (usernow!=null){
            uid= usernow!!.uid
            demail= usernow!!.email
            name= usernow!!.displayName
            phone=usernow!!.phoneNumber
            imageurl= usernow!!.photoUrl.toString()
        }

        MahiLongToast("$uid $demail $name $phone $imageurl")
    }


    private fun MakeSignIn(email: String, Password: String) {
        if (!validatedetails()) {
            return
        }

        showProgressBar("Fetching Your Existing Account")

        Mauth.signInWithEmailAndPassword(email, Password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                MahiShortToast("Login Successful")
                //MahiLongToast("${Firebase.auth.currentUser!!.uid}  ::::  ${Firebase.auth.currentUser!!.email}")
                store_userdetails()
                editor.putBoolean("logbool",true).commit()
                hideProgressBar()
            }
        }.addOnFailureListener { e ->
            MahiLongToast(e.message.toString())
            hideProgressBar()
        }
    }


    private fun CreateUser(email: String, Password: String) {

        if (!validatedetails()) {
            return
        }

        showProgressBar("Account Creatinng in Progress")

        //MahiLongToast("${Firebase.auth.currentUser!!.uid}  ::::  ${Firebase.auth.currentUser!!.email}")

        Mauth.createUserWithEmailAndPassword(email, Password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                MahiShortToast("Account Created Successfully")
                startActivity(Intent(this, HomeScreenActivity::class.java))
                store_userdetails()
                hideProgressBar()
            }
        }.addOnFailureListener { e ->
            MahiLongToast(e.message.toString())
            hideProgressBar()
        }


    }

    private fun store_userdetails(
    ) {
        val detailsmap=USER(imageurl,name.toString(),role.toString(),hotel_organ_name.toString(),phone.toString(),Firebase.auth.currentUser!!.email.toString(),Firebase.auth.currentUser!!.uid)
//        val detailsmap = hashMapOf(
//            "UserID" to Firebase.auth.currentUser!!.uid,
//            "Name" to name,
//            "email" to Firebase.auth.currentUser!!.email,
//            "phone" to phone,
//            "Image" to imageurl
//        )

        val userinfodb = Firebase.database.getReference("USERS_INFO").child(Firebase.auth.currentUser!!.uid)
        userinfodb.setValue(detailsmap).addOnSuccessListener {
            startActivity(Intent(this, HomeScreenActivity::class.java))
            MahiShortToast("You Can Visit Your Profile Now")
        }.addOnFailureListener { e ->
            MahiLongToast(e.message.toString())
        }
    }

    private fun hideProgressBar() {
        loginBinding.lyProgresslayout.visibility = View.GONE

        loginBinding.btLogin.visibility = View.VISIBLE
        loginBinding.btSignup.visibility = View.VISIBLE
        loginBinding.tvResetpwd.visibility = View.VISIBLE
        loginBinding.tvVerifymailid.visibility = View.VISIBLE
    }

    private fun showProgressBar(message: String) {
        loginBinding.tvProgressText.text = message
        loginBinding.lyProgresslayout.visibility = View.VISIBLE

        loginBinding.tvResetpwd.visibility = View.GONE
        loginBinding.btSignup.visibility = View.GONE
        loginBinding.btLogin.visibility = View.GONE
        loginBinding.tvVerifymailid.visibility = View.GONE
    }

    fun validatedetails(): Boolean {
        var valid = true

        val emailc = loginBinding.etEmail.text.toString()
        val pwdc = loginBinding.etPassword.text.toString()

        if (TextUtils.isEmpty(emailc)) {
            valid = false
            loginBinding.etEmail.error = "Email Required"
        } else {
            //fine
            loginBinding.etEmail.error = null
        }
        if (TextUtils.isEmpty(pwdc)) {
            valid = false
            loginBinding.etPassword.error = "Password Is Must"
        } else {
            //ok
            loginBinding.etPassword.error = null
        }
        return valid
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
