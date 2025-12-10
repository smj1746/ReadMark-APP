// di/AppModule.kt
package com.readmark.di

import android.content.Context
import com.readmark.data.repository.DataManager
import com.readmark.data.repository.LMStudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDataManager(
        @ApplicationContext context: Context
    ): DataManager = DataManager(context)
    
    @Provides
    @Singleton
    fun provideLMStudioRepository(): LMStudioRepository = LMStudioRepository()
}