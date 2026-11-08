package com.example.tumblrlite.data.di

import com.example.tumblrlite.BuildConfig
import com.example.tumblrlite.data.api.OAuth1Credentials
import com.example.tumblrlite.data.api.OAuth1Signer
import com.example.tumblrlite.data.api.TumblrApi
import com.example.tumblrlite.data.repository.RetrofitTumblrAccountRepository
import com.example.tumblrlite.data.repository.RetrofitFollowingFeedRepository
import com.example.tumblrlite.data.repository.RetrofitLikesFeedRepository
import com.example.tumblrlite.domain.repository.FollowingFeedRepository
import com.example.tumblrlite.domain.repository.LikesFeedRepository
import com.example.tumblrlite.domain.repository.TumblrAccountRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkProviders {
    @Provides @Singleton
    fun provideCredentials() = OAuth1Credentials(
        consumerKey = BuildConfig.TUMBLR_CONSUMER_KEY,
        consumerSecret = BuildConfig.TUMBLR_CONSUMER_SECRET,
        token = BuildConfig.TUMBLR_OAUTH_TOKEN,
        tokenSecret = BuildConfig.TUMBLR_OAUTH_TOKEN_SECRET,
    )

    @Provides @Singleton
    fun provideOAuth1Signer(credentials: OAuth1Credentials) = OAuth1Signer(credentials)

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Provides @Singleton
    fun provideOkHttpClient(signer: OAuth1Signer): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(signer)
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.tumblr.com/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideTumblrApi(retrofit: Retrofit): TumblrApi = retrofit.create(TumblrApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TumblrRepositoryBindings {
    @Binds @Singleton
    abstract fun bindTumblrAccountRepository(repository: RetrofitTumblrAccountRepository): TumblrAccountRepository

    @Binds @Singleton
    abstract fun bindFollowingFeedRepository(repository: RetrofitFollowingFeedRepository): FollowingFeedRepository

    @Binds @Singleton
    abstract fun bindLikesFeedRepository(repository: RetrofitLikesFeedRepository): LikesFeedRepository
}
