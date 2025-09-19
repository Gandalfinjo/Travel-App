package com.example.travelapp.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.repositories.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {
    fun addPhoto(photo: Photo) = viewModelScope.launch {
        photoRepository.addPhoto(photo)
    }

    fun getTripPhotos(tripId: Int): Flow<List<Photo>> =
        photoRepository.getTripPhotos(tripId)
}