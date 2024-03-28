package com.test.application.mapp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.test.application.mapp.models.ModelImp
import com.test.application.mapp.presenters.MapPresenter
import com.test.application.mapp.ui.compose_files.MapScreen
import com.test.application.mapp.views.ViewMap
import java.io.File

class MainActivity : ComponentActivity(), ViewMap.View {
    private lateinit var presenter: MapPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = MapPresenter( this, ModelImp())

        setContent {
            MapScreen(presenter)
        }
    }

    override fun showToastText(text: String) {
        Toast.makeText(AppContext.context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }

}