package com.ishant.musicify.other

// It is a class we will use to get the current status of our song. A song can be loaded (SUCCESS), Not loaded (ERROR) or loading (LOADING)
// You can use this class with any datatype or any class as we have used generic type T
// The out keyword in <out T> means it can even take the parent class of T
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        // We will call these functions whenever we need to define a particular state for a song
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)
        fun <T> error(message: String, data: T?) = Resource(Status.ERROR,data,message)
        fun <T> loading(data: T?) = Resource(Status.LOADING,data,null)
    }
}

// Here are the three enum class states of our song
enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}