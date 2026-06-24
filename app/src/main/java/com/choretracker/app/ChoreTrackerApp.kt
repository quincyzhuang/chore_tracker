package com.choretracker.app

import android.app.Application
import com.choretracker.app.data.ChoreDatabase

class ChoreTrackerApp : Application() {

    lateinit var database: ChoreDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = ChoreDatabase.getInstance(this)
    }
}
