package com.ishant.musicify.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.ishant.musicify.data.entities.Song
import com.ishant.musicify.other.Constants.SONG_COLLECTION
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION) // It is name of our collection in firestore database

    // This function will return list of meta data of all songs in our firestore database.
    // Metadata will also have song URL and song thumbnail URL.
    suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch(e: Exception) {
            emptyList()
        }
    }
}