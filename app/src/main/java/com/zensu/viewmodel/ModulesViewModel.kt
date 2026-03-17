package com.zensu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zensu.detector.RootDetector
import com.zensu.model.Module
import com.zensu.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModulesViewModel @Inject constructor() : ViewModel() {
    
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private var allModules: List<Module> = emptyList()
    
    init {
        loadModules()
    }
    
    private fun loadModules() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val rootState = RootDetector.detectRoot()
            
            if (rootState.isRooted) {
                allModules = ModuleRepository.getModules(rootState.rootType)
                filterModules()
            }
            
            _isLoading.value = false
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterModules()
    }
    
    private fun filterModules() {
        val query = _searchQuery.value.lowercase()
        
        _modules.value = if (query.isEmpty()) {
            allModules
        } else {
            allModules.filter {
                it.name.lowercase().contains(query) ||
                it.author.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }
    }
    
    fun toggleModule(module: Module) {
        viewModelScope.launch {
            val newState = !module.enabled
            val success = ModuleRepository.toggleModule(module, newState)
            
            if (success) {
                allModules = allModules.map {
                    if (it.id == module.id) it.copy(enabled = newState) else it
                }
                filterModules()
            }
        }
    }
    
    fun refresh() {
        loadModules()
    }
}
