package com.example.mydocumentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class HomeActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var btnDocuments: Button
    private lateinit var btnPrivate: Button
    private lateinit var btnScanner: Button
    private lateinit var btnBackup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adView = findViewById(R.id.adView)
        btnDocuments = findViewById(R.id.btnDocuments)
        btnPrivate = findViewById(R.id.btnPrivate)
        btnScanner = findViewById(R.id.btnScanner)
        btnBackup = findViewById(R.id.btnBackup)

        // AdMob Banner Ad Load
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        btnDocuments.setOnClickListener {
            startActivity(Intent(this, DocumentActivity::class.java))
        }

        btnPrivate.setOnClickListener {
            startActivity(Intent(this, PrivateDocumentActivity::class.java))
        }

        btnScanner.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        btnBackup.setOnClickListener {
            startActivity(Intent(this, BackupActivity::class.java))
        }
    }
}
