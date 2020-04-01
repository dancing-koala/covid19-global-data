package com.dancing_koala.covid_19data

import android.app.Application
import androidx.room.Room
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiService
import com.dancing_koala.covid_19data.persistence.AppDatabase
import com.dancing_koala.covid_19data.persistence.CacheDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class App : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        val appDb = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "covid19")
            .fallbackToDestructiveMigration()
            .build()

        bind<AppDatabase>() with provider { appDb }
        bind<CacheDao>() with provider { appDb.cacheDao() }
        bind<LmaoNinjaApiService>() with singleton { LmaoNinjaApiService() }
        bind<LmaoNinjaApiDataRepository>() with provider {
            LmaoNinjaApiDataRepository(kodein)
        }
    }
}