package com.codetuners.food4need.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.codetuners.food4need.Fragments.*
import com.codetuners.food4need.MahiLongToast
import com.codetuners.food4need.MahiShortToast
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.ActivityHomeScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_web.*

class HomeScreenActivity : AppCompatActivity(), View.OnClickListener {

    private var user: FirebaseUser? = null
    private lateinit var homeScreenBinding: ActivityHomeScreenBinding
    private val Mauth = Firebase.auth
    private lateinit var sp: SharedPreferences
    private lateinit var ed: SharedPreferences.Editor
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeScreenBinding = ActivityHomeScreenBinding.inflate(layoutInflater)
        val view = homeScreenBinding.root
        setContentView(view)

        setuptoolbar()

        openpostfrag(HomePostFrag(),"HOME PAGE")


        homeScreenBinding.fab.setOnClickListener(this)

        sp = this.getSharedPreferences(getString(R.string.PREFFILENAME), Context.MODE_PRIVATE)
        ed = sp.edit()

        setSupportActionBar(homeScreenBinding.bottomAppBar)

        user = Firebase.auth.currentUser

        if (user != null) {

            val email = user!!.email
            val photo = user!!.photoUrl
            val uid = user!!.uid
            val emailverified = user!!.isEmailVerified
            val name = user!!.displayName

            ed.putString("userinfo_email",email).apply()
            ed.putString("userinfo_photo",email).apply()
            ed.putString("userinfo_uid",uid).apply()
            ed.putBoolean("userinfo_isemverified",emailverified).apply()
            ed.putString("userinfo_name",name).apply()

            MahiLongToast("NAME: $name , EMAIL: $email , PHOTO: $photo , UID : $uid , VERIFIED : $emailverified")

        }

    }

    override fun onStart() {
        super.onStart()

        authStateListener=object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                if (p0.currentUser!=null){
                    MahiShortToast("Log in to Continue")
                }
            }
        }

        Mauth.addAuthStateListener { authStateListener }
        if (Mauth.currentUser==null){
            MahiShortToast("Log in to Continue")
            startActivity(Intent(this,LoginActivity::class.java))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Mauth.removeAuthStateListener { authStateListener }
    }

    private fun setuptoolbar() {
        val toolbar:TextView= homeScreenBinding.toolbarTextview
        toolbar.text="HOME PAGE"
    }

    private fun openpostfrag(frag: Fragment,title:String) {
        homeScreenBinding.toolbarTextview.text=title
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
        transaction.replace(R.id.FragFrame, frag)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {

        when(supportFragmentManager.findFragmentById(R.id.FragFrame)){
            is HomePostFrag->{
                showconfirmalert()
            }
            is FavouritesFrag->{
                openpostfrag(HomePostFrag(),"HOME PAGE")
            }
            is WebFrag->{
                if (webview.canGoBack()){
                    webview.goBack()
                }else{
                    openpostfrag(HomePostFrag(),"HOME PAGE")
                }
            }
            is AboutFrag->{
                openpostfrag(HomePostFrag(),"HOME PAGE")
            }
            is ProfileFrag->{
                openpostfrag(HomePostFrag(),"HOME PAGE")
            }
            is EditProfileFrag->{
                openpostfrag(ProfileFrag(),"Profile")
            }
            else-> super.onBackPressed()
        }
    }

    private fun showconfirmalert() {
        val logoutdialog = AlertDialog.Builder(this)
        logoutdialog.setTitle("ARE YOU SURE ?")
        logoutdialog.setMessage("Do You Want To LOGOUT / EXIT ?")
        logoutdialog.setCancelable(false)
        logoutdialog.setNeutralButton("NOTHING") { dialogInterface, which ->
            //nothing
        }
        logoutdialog.setNegativeButton("LOGOUT") { dialogInterface, which ->
            Mauth.signOut()
            ed.clear().commit()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        logoutdialog.setPositiveButton("EXIT") { dialogInterface, which ->
            finishAffinity()
        }
        logoutdialog.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottom_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomSheetDialogFragment()
                bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            }
            R.id.favourites_menu -> {
                openpostfrag(FavouritesFrag(),"FAVOURITES PAGE")
            }
            R.id.webview_menu -> {
               openpostfrag(WebFrag(),"Webview  ")
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab -> {
                startActivity(Intent(this, AddPostActivity::class.java))
            }
        }

    }


}