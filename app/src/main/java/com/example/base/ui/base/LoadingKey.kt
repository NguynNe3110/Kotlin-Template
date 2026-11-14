package com.example.base.ui.base

@JvmInline
value class LoadingKey(val value: String) {
    companion object {
        val Initial = LoadingKey("initial")
        val Refresh = LoadingKey("refresh")
        val Submit = LoadingKey("submit")
        val Pagination = LoadingKey("pagination")
    }
}