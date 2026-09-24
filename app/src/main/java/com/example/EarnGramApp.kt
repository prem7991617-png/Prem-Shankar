package com.example

import android.app.Application
import com.example.data.local.EarnGramDatabase
import com.example.data.repository.EarnGramRepository

class EarnGramApp : Application() {
    lateinit var database: EarnGramDatabase
        private set

    lateinit var repository: EarnGramRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = EarnGramDatabase.getDatabase(this)
        repository = EarnGramRepository(database, this)
    }

    companion object {
        lateinit var instance: EarnGramApp
            private set
    }
}
