package com.example.appfoco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfoco.data.AppDatabase
import com.example.appfoco.data.AppRepository
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val repository = AppRepository(dao, application)

    val rules: StateFlow<List<ForbiddenRuleEntity>> = repository.rulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRule(name: String, description: String?) {
        viewModelScope.launch {
            repository.insertRule(ForbiddenRuleEntity(name = name, description = description))
        }
    }

    fun updateRule(rule: ForbiddenRuleEntity) {
        viewModelScope.launch {
            repository.updateRule(rule)
        }
    }

    fun deleteRule(rule: ForbiddenRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun triggerRuleBreak(ruleName: String, mode: String) {
        // Rules handling
    }
}
