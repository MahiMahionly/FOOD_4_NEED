package com.codetuners.food4need.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.FragmentAboutBinding

class AboutFrag : Fragment() {

    private var _aboutbinding:FragmentAboutBinding?=null
    private val aboutbinding get() = _aboutbinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _aboutbinding=FragmentAboutBinding.inflate(layoutInflater,container,false)




        return aboutbinding.root
    }

}