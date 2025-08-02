package com.example.chan

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Thread(
    val no: Long,
    val sub: String?,
) : Parcelable