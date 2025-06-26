package com.example.panicshield.di

import android.content.Context
import com.example.panicshield.data.remote.repository.ContactRepository
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.domain.usecase.ContactUseCase
import com.example.panicshield.domain.usecase.EmergencyUseCase
import com.example.panicshield.domain.usecase.LocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideEmergencyUseCase(
        emergencyRepository: EmergencyRepository
    ): EmergencyUseCase = EmergencyUseCase(emergencyRepository)

    @Provides
    @Singleton
    fun provideLocationUseCase(
        @ApplicationContext context: Context
    ): LocationUseCase = LocationUseCase(context)

    @Provides
    @Singleton
    fun provideContactUseCase(
        contactRepository: ContactRepository
    ): ContactUseCase = ContactUseCase(contactRepository)
}
