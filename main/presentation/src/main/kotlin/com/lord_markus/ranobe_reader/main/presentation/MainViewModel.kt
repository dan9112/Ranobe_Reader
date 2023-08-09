package com.lord_markus.ranobe_reader.main.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    private val mainRepository: MainRepository
) : ViewModel()
