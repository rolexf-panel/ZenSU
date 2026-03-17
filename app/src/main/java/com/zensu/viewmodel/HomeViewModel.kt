package com.zensu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zensu.detector.RootDetector
import com.zensu.model.Module
import com.zensu.model.RootState
import com.zensu.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    
    private val _rootState = MutableStateFlow(RootState())
    val rootState: StateFlow<RootState> = _rootState.asStateFlow()
    
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    private val _isRequestingRoot = MutableStateFlow(false)
    val isRequestingRoot: StateFlow<Boolean> = _isRequestingRoot.asStateFlow()
    
    val moduleCount: Int
        get() = _modules.value.size
    
    init {
        checkRootAccess()
    }
    
    fun checkRootAccess() {
        viewModelScope.launch {
            _rootState.value = RootDetector.detectRoot()
            
            if (_rootState.value.isRooted) {
                _modules.value = ModuleRepository.getModules(_rootState.value.rootType)
            }
        }
    }
    
    fun requestRoot() {
        viewModelScope.launch {
            _isRequestingRoot.value = true
            
            // Try to get root access - this will trigger the root prompt
            RootDetector.requestRootAccess()
            
            // Wait a bit and check again
            delay(2000)
            
            _rootState.value = RootDetector.detectRoot()
            
            if (_rootState.value.isRooted) {
                _modules.value = ModuleRepository.getModules(_rootState.value.rootType)
            }
            
            _isRequestingRoot.value = false
        }
    }
    
    fun refresh() {
        checkRootAccess()
    }
}
