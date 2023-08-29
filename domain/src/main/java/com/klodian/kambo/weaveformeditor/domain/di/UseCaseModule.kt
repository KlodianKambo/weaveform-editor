package com.klodian.kambo.weaveformeditor.domain.di


import com.klodian.kambo.weaveformeditor.domain.GetWeaveFrequencyList
import com.klodian.kambo.weaveformeditor.domain.GetWeaveFrequencyListUseCase
import com.klodian.kambo.weaveformeditor.domain.SaveNewSound
import com.klodian.kambo.weaveformeditor.domain.SaveNewSoundUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UseCaseModule {

    @Binds
    abstract fun bindGetWeaveFrequencyList(impl: GetWeaveFrequencyListUseCase): GetWeaveFrequencyList


    @Binds
    abstract fun bindSaveNewSound(impl: SaveNewSoundUseCase): SaveNewSound

}