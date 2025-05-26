package dam.tfg.blinky.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.dataclass.PersonalityResponseDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonalityViewModel : ViewModel() {
    private val _personalities = MutableStateFlow<List<PersonalityResponseDTO>>(emptyList())
    val personalities: StateFlow<List<PersonalityResponseDTO>> = _personalities

    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error = _error

    init {
        fetchPersonalities()
    }

    fun fetchPersonalities() {
        _isLoading.value = true
        _error.value = null

        val chatApiService = RetrofitClient.api

        chatApiService.getPersonalities().enqueue(object : Callback<List<PersonalityResponseDTO>> {
            override fun onResponse(
                call: Call<List<PersonalityResponseDTO>>,
                response: Response<List<PersonalityResponseDTO>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val personalitiesList = response.body() ?: emptyList()
                    _personalities.value = personalitiesList
                    Log.d("PersonalityViewModel", "Fetched ${personalitiesList.size} personalities")
                } else {
                    _error.value = "Error: ${response.code()} - ${response.message()}"
                    Log.e("PersonalityViewModel", "Error fetching personalities: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<PersonalityResponseDTO>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Network error: ${t.message}"
                Log.e("PersonalityViewModel", "Network error fetching personalities", t)
            }
        })
    }

    // Factory for creating the ViewModel
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PersonalityViewModel::class.java)) {
                return PersonalityViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
