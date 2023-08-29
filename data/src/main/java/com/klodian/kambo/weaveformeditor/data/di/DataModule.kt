package com.klodian.kambo.weaveformeditor.data.di

import android.os.Environment
import com.klodian.kambo.weaveformeditor.data.FileRepositoryImpl
import com.klodian.kambo.weaveformeditor.domain.repositories.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    internal fun provideFileRepository(
        @IoDispatcher coroutineDispatcher: CoroutineDispatcher
    ): FileRepository {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return FileRepositoryImpl(downloadsFolder, coroutineDispatcher)
    }

}