package com.wander.mono.di

import javax.inject.Qualifier

/** The process-wide scope the shared, callback-driven flows of `core` live in. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope
