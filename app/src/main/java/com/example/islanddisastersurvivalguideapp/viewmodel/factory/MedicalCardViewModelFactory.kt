package com.example.islanddisastersurvivalguideapp.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.islanddisastersurvivalguideapp.data.repository.MedicalCardRepository
import com.example.islanddisastersurvivalguideapp.viewmodel.MedicalCardViewModel

class MedicalCardViewModelFactory(
    private val repository: MedicalCardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicalCardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}