package com.example.chan

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Thread(
    val no: Long,
    val sub: String? = null,
    val com: String? = null,
    val semantic_url: String? = null,
    val replies: Int? = 0,
    val tim: Long? = null,
) : Parcelable