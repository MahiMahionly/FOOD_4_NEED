package com.codetuners.food4need.Fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.codetuners.food4need.R
import com.codetuners.food4need.databinding.FragmentWebBinding

class WebFrag : Fragment() {
    private var _webbinding: FragmentWebBinding? = null
    private val webBinding get() = _webbinding!!
    private var searchhint:String?=""
    private var mainurl:String?=""
    var key:String?=""
    //val bundle=this.arguments
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _webbinding = FragmentWebBinding.inflate(layoutInflater, container, false)
        val view = webBinding.root

        key=arguments?.getString("key")
        searchhint=arguments?.getString("search_hint")
        mainurl= arguments?.getString("mainurl").toString()

        if (key.equals("corono")){
            webBinding.progressLay.tvLoading.text="Geeting corono Updates..."
            mainurl="https://www.worldometers.info/coronavirus/"
            searchhint=""
        }

        if (searchhint==null){
            mainurl="www.google.com"
            searchhint=""
        }else{
            when(key){
                "corono"->{
                    webBinding.progressLay.tvLoading.text="Geeting corono Updates..."
                    mainurl="https://www.worldometers.info/coronavirus/"
                    searchhint=""
                }
                else->{
                    mainurl="https://www.google.com/search?q="
                }
            }

        }

        webBinding.webview.webViewClient = WebViewClient()
        webBinding.webview.settings.javaScriptEnabled = true
        webBinding.webview.settings.setSupportZoom(true)

        webBinding.webview.loadUrl("$mainurl$searchhint")

        val webViewClient:WebViewClient=object :WebViewClient(){
           override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
               super.onPageStarted(view, url, favicon)
               webBinding.progressLay.tvLoading.text="Fetching Webview"
               webBinding.progressLay.progressContent.visibility=View.VISIBLE
               webBinding.webview.visibility=View.GONE
           }

           override fun onPageFinished(view: WebView?, url: String?) {
               super.onPageFinished(view, url)
               webBinding.progressLay.progressContent.visibility=View.GONE
               webBinding.webview.visibility=View.VISIBLE
           }

       }

        webBinding.webview.webViewClient=webViewClient
        webBinding.webview.settings.defaultTextEncodingName="utf-8"
       // Inflate the layout for this fragment
        return view
    }
}