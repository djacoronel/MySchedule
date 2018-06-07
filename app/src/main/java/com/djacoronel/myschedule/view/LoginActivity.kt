package com.djacoronel.myschedule.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.djacoronel.myschedule.R
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        webview.loadUrl("https://myuste.ust.edu.ph/student/")
        webview.settings.javaScriptEnabled = true
        webview.webViewClient = LoginWebViewClient()
        setupAds()
    }


    fun returnCookie(cookie: String){
        val returnIntent = Intent()
        returnIntent.putExtra("cookie", cookie)

        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }


    inner class LoginWebViewClient : WebViewClient() {
        var cookie = ""

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.contains("studentcontrol")) {
                this@LoginActivity.returnCookie(cookie)
                return true
            }
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            cookie = CookieManager.getInstance().getCookie(url)
            Log.d("COOKIES!!", "All the cookies in a string:$cookie")
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }

    private fun setupAds(){
        val adRequest = AdRequest.Builder()
                .addTestDevice("CEA54CA528FB019B75536189748EAF7E")
                .addTestDevice("2F42DCE5AF01E77FB3B1748FFD2BFB08")
                .addTestDevice("4CCC112819318A806ADC4807B6A0C444")
                .build()

        adView.loadAd(adRequest)
    }

    public override fun onPause() {
        if (adView != null) {
            adView.pause()
        }
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (adView != null) {
            adView.resume()
        }
    }

    public override fun onDestroy() {
        if (adView != null) {
            adView.destroy()
        }
        super.onDestroy()
    }

}


