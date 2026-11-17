package com.example.base.composition.auth

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtectedHttpClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtectedRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicAuthApi

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtectedAuthApi
