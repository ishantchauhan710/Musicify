package com.ishant.musicify.data.entities

// This class specifies a song. The vairable names in the constructor are same as the field names in firestore database collection.
// You must assign these variables a default value if you want to use this class with firebase firestore.
data class Song (
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    val imageUrl: String = ""
)