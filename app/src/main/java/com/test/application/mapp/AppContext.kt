package com.test.application.mapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class AppContext : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}