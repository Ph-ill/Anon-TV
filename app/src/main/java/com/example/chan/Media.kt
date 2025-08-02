package com.example.chan

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Media(
    val tim: Long,
    val filename: String,
    val ext: String
) : Parcelable