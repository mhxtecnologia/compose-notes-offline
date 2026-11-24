package com.mhxtecnologia.notes.core.data.di

import com.mhxtecnologia.notes.core.data.NotesRepository
import com.mhxtecnologia.notes.core.data.OfflineFirstNotesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindsNotesRepository(repository: OfflineFirstNotesRepository): NotesRepository
}

/** O relógio é injetado para que os testes controlem as datas das notas. */
@Module
@InstallIn(SingletonComponent::class)
internal object ClockModule {

    @Provides
    fun provideClock(): Clock = Clock.systemUTC()
}
