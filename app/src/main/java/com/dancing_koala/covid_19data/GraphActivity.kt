package com.dancing_koala.covid_19data

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GraphActivity : AppCompatActivity() {

    private val graphFragment by lazy {
        GraphFragment.newInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, graphFragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        graphFragment.onActivityResult(requestCode, resultCode, data)
    }
}