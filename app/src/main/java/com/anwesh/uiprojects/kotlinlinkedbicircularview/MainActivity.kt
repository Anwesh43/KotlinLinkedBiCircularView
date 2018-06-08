package com.anwesh.uiprojects.kotlinlinkedbicircularview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedbicircularview.LinkedBiCircularView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedBiCircularView.create(this)
    }
}
