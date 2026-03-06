// app/src/main/java/com/example/smartfit/di/AppGraph.kt
package com.example.smartfit.di

import android.content.Context
import com.example.smartfit.data.local.SmartFitDatabase
import com.example.smartfit.data.remote.gemini.GeminiApi
import com.example.smartfit.data.repository.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface AppGraph {
    val userRepo: UserRepository
    val activityRepo: ActivityRepository
    val foodRepo: FoodRepository
    val prefsRepo: PrefsRepository
    val tipsRepo: TipsRepository
}

private const val GEMINI_BASE_URL =
    "https://generativelanguage.googleapis.com/v1beta/"   // ✅ 必须带 v1beta/ + 结尾斜杠

class AppContainer(context: Context) : AppGraph {

    private val db: SmartFitDatabase by lazy {
        SmartFitDatabase.getDatabase(context)
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // 带日志的 OkHttpClient
    private val geminiClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }



    private val geminiApi: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(geminiClient) // 如果有 logging client 就用它
            .build()
            .create(GeminiApi::class.java)
    }


    override val userRepo: UserRepository by lazy {
        UserRepositoryImpl(db.userDao())
    }

    override val activityRepo: ActivityRepository by lazy {
        ActivityRepository(db.activityDao())
    }

    override val foodRepo: FoodRepository by lazy {
        FoodRepository(db.foodLogDao())
    }

    override val prefsRepo: PrefsRepository by lazy {
        PrefsRepository(context.applicationContext)
    }

    override val tipsRepo: TipsRepository by lazy {
        TipsRepository(
            dao = db.tipsDao(),
            gemini = geminiApi,
            foodRepo = foodRepo,
            activityRepo = activityRepo
        )
    }
}
