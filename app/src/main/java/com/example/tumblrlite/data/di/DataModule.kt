package com.example.tumblrlite.data.di

import com.example.tumblrlite.data.repository.FakeFeedRepository
import com.example.tumblrlite.domain.repository.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindFeedRepository(repository: FakeFeedRepository): FeedRepository
}
