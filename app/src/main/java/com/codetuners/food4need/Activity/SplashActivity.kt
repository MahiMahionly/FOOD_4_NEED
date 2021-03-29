package com.codetuners.food4need.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.codetuners.food4need.Fragments.EditProfileFrag
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.ActivityMainBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var splashbinding: ActivityMainBinding
    private lateinit var sp: SharedPreferences
    private lateinit var ed: SharedPreferences.Editor
    private var loguser: Boolean = false
    private var firstvisit:Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashbinding = ActivityMainBinding.inflate(layoutInflater)
        val view = splashbinding.root
        setContentView(view)

        sp=this.getSharedPreferences(getString(R.string.PREFFILENAME),Context.MODE_PRIVATE)

        loguser = sp.getBoolean("logbool", false)
        firstvisit=sp.getBoolean("first_visit",true)

        animate(splashbinding.imgLogo, splashbinding.txtAppName)

        Handler().postDelayed({

            /*if (firstvisit){
                val args=Bundle()
                args.putString("newuservisit","newuservisit")
                val editfrag=EditProfileFrag()
                editfrag.arguments=args
                val transaction = supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
                transaction.replace(R.id.FragFrame,editfrag)
            }else{
                if (loguser){
                    startActivity(Intent(this, HomeScreenActivity::class.java))
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                }

            }*/

            when (loguser) {
                true -> {
                    startActivity(Intent(this, HomeScreenActivity::class.java))
                }
                false -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
                              //checks if the user is logined or not
            //navigate to next screen based on that login page (or) home page

        }, 2500)
    }

    private fun animate(image: ImageView, name: TextView) {
        val animation1: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        image.startAnimation(animation1)
        val animation2: Animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        name.startAnimation(animation2)
    }
}