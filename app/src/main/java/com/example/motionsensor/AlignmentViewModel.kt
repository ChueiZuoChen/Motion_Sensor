package com.example.motionsensor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlignmentViewModel : ViewModel() {
    private val _state = MutableLiveData(State.AlignmentStart)
    val state: LiveData<State>
        get() = _state

    fun setState(state: State) {
        _state.value = state
    }
}