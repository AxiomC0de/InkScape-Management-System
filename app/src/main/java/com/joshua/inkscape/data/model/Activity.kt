package com.joshua.inkscape.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Activity(
    val id: String = "",
    val details: String = "",
    val productId: String? = null,
    val productName: String? = null,
    val timestamp: String = "",
    val type: String = "",
    val user: String = ""
) 