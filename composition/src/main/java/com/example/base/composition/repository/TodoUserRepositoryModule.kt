package com.example.base.composition.repository

import com.example.base.data.api.ApiErrorParser
import com.example.base.data.api.DataErrorMapper
import com.example.base.data.api.JsonPlaceholderService
import com.example.base.data.datasource.InMemoryTodoDataSource
import com.example.base.data.datasource.TodoDataSource
import com.example.base.data.repository.TodoRepositoryImpl
import com.example.base.data.repository.UserRepositoryImpl
import com.example.base.domain.repository.TodoRepository
import com.example.base.domain.repository.UserRepository
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TodoUserRepositoryModule {

    @Provides @Singleton
    fun errorParser(gson: Gson): ApiErrorParser = ApiErrorParser(gson)

    @Provides @Singleton
    fun dataErrorMapper(parser: ApiErrorParser): DataErrorMapper = DataErrorMapper(parser)

    @Provides
    fun errorMapper(mapper: DataErrorMapper): ErrorMapper = mapper

    @Provides @Singleton
    fun todoSource(): TodoDataSource = InMemoryTodoDataSource()

    @Provides @Singleton
    fun todos(source: TodoDataSource, logger: AppLogger): TodoRepository = TodoRepositoryImpl(source, logger)

    @Provides @Singleton
    fun users(api: JsonPlaceholderService, mapper: DataErrorMapper, logger: AppLogger): UserRepository =
        UserRepositoryImpl(api, mapper, logger)
}
