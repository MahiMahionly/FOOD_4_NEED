package com.codetuners.food4need.Fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.FragmentAboutBinding

class AboutFrag : Fragment(), View.OnClickListener {

    private var _aboutbinding: FragmentAboutBinding? = null
    private val aboutbinding get() = _aboutbinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _aboutbinding = FragmentAboutBinding.inflate(layoutInflater, container, false)

        return aboutbinding.root
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.teleflw -> {
                opentelegram()
            }
            R.id.ytflw -> {
                openyoutube()
            }
            R.id.fbflw -> {
                openfacebook()
            }
            R.id.instaflw -> {
                openinstagram()
            }
        }
    }

    private fun openyoutube() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val id = "R44sVI4ipOY"
            intent.data = Uri.parse("vnd.youtube:" + id)
            startActivity(intent)
        } catch (exp: ActivityNotFoundException) {
            val intent1 = Intent(Intent.ACTION_VIEW)
            val id = "R44sVI4ipOY"
            intent1.data = Uri.parse("https://www.youtube.com/watch?v=" + id)
            startActivity(intent1)
        }
    }

    private fun opentelegram() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://t.me/Mahimahionly")
            intent.setPackage("org.telegram.messenger")
            startActivity(intent)
        } catch (exp: ActivityNotFoundException) {
            Toast.makeText(activity, "Telegram not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openfacebook() {
        try {
            context?.packageManager?.getPackageInfo(
                "com.facebook.katana",
                PackageManager.GET_META_DATA
            )
            val FBID = "Mahimahionly"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("fb://page/" + FBID)
            intent.setPackage("com.facebook.katana")
            startActivity(intent)
        } catch (exp: ActivityNotFoundException) {
            Toast.makeText(activity, "Facebook not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openinstagram() {
        try {
            val InstaID = "mahi_the__wolverine/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://instagram.com/_u/" + InstaID)
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        } catch (exp: ActivityNotFoundException) {
            Toast.makeText(activity, "Instagram not installed", Toast.LENGTH_SHORT).show()
        }
    }

}