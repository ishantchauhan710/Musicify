package com.ishant.musicify.other

// This class will tell us if we have handled a particular event such as a song
// open keyword means we can inherit from this class
open class Event <out T>(private val data: T) {
    var hasBeenHandled = false
    private set // We can read this variable from anywhere but we can only change it's value within this class

    fun getContentIfNotHandled(): T? {
        return if(hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    // We can call this function whenever we want to see the data of a particular event class's object
    fun peekContent() = data

}