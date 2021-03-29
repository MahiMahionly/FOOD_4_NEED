package com.codetuners.food4need.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.codetuners.food4need.Activity.LoginActivity
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.FragmentBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _bottomsheetBinding:FragmentBottomsheetBinding?= null
    private val bottomsheetBinding get() = _bottomsheetBinding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bottomsheetBinding = FragmentBottomsheetBinding.inflate(layoutInflater,container,false)

        return bottomsheetBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bottomsheetBinding.NavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.Profile_menuit -> {
                    dismiss()

                    openFrag(ProfileFrag(),"Profile")
                    //context?.MYtoast("PROFILE IS CLICKED")
                    //(context as AppCompatActivity).findViewById<TextView>(R.id.toolbar_textview).text="PROFILE"
                    //(context as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.FragFrame,ProfileFrag()).addToBackStack("profile").commit()
                }
                R.id.stopcorono_menuit -> {
                    dismiss()
                    val webfrag=WebFrag()
                    val args=Bundle()
                    args.putString("mainurl","https://www.worldometers.info/coronavirus/")
                    args.putString("key","corono")
                    webfrag.arguments=args

                    openFrag(WebFrag(),"Corono Updates")

                    (context as AppCompatActivity).findViewById<TextView>(R.id.toolbar_textview).text="Corono Updates"

                   (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.FragFrame,webfrag).addToBackStack(null).commit()

                }
                R.id.share_menuit -> {
                    dismiss()
                    //context?.MYtoast("Share is Clicked")
                    val sendIntent:Intent=Intent().apply {
                        action=Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT,"Get the FOOD 4 NEED to provide the food for the need ones")
                        type="text/plain"

                    }
                    val shareintent=Intent.createChooser(sendIntent,null)
                    startActivity(shareintent)
                   //NavigationView.isFocusable=false
                    //NavigationView.isEnabled=false
                }
                R.id.about_menuit -> {
                    dismiss()
                    context?.MYtoast("ABOUT IS CLICKED")
                }
                R.id.logout_menuit -> {
                    dismiss()
                    val user = Firebase.auth
                    user.signOut()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    //context?.MYtoast("LOGOUT IS CLICKED")
                }
            }
            true
        }
    }


    fun Context.MYtoast(message: CharSequence) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 600)
        toast.show()
    }

    fun openFrag(frag:Fragment,title:String){
        val transaction= activity?.supportFragmentManager?.beginTransaction()
        transaction!!.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
        transaction.replace(R.id.FragFrame,frag)
        transaction.commit()
        activity!!.findViewById<TextView>(R.id.toolbar_textview).text=title
    }
}