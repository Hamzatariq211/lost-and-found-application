package com.hamzatariq.lost_and_found_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamzatariq.lost_and_found_application.api.Post
import com.hamzatariq.lost_and_found_application.api.PostData
import com.hamzatariq.lost_and_found_application.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _postState = MutableLiveData<PostState>(PostState.Idle)
    val postState: LiveData<PostState> = _postState

    private val _postsListState = MutableLiveData<PostsListState>(PostsListState.Idle)
    val postsListState: LiveData<PostsListState> = _postsListState

    sealed class PostState {
        object Idle : PostState()
        object Loading : PostState()
        data class Success(val message: String, val postData: PostData) : PostState()
        data class Error(val message: String) : PostState()
    }

    sealed class PostsListState {
        object Idle : PostsListState()
        object Loading : PostsListState()
        data class Success(val posts: List<Post>) : PostsListState()
        data class Error(val message: String) : PostsListState()
    }

    fun createPost(
        itemName: String,
        itemDescription: String,
        location: String,
        itemType: String,
        imageBase64: String?
    ) {
        viewModelScope.launch {
            _postState.value = PostState.Loading

            when (val result = repository.createPost(itemName, itemDescription, location, itemType, imageBase64)) {
                is PostRepository.PostResult.Success -> {
                    _postState.value = PostState.Success("Post created successfully!", result.postData)
                }
                is PostRepository.PostResult.Error -> {
                    _postState.value = PostState.Error(result.message)
                }
                is PostRepository.PostResult.Loading -> {
                    _postState.value = PostState.Loading
                }
            }
        }
    }

    fun getPosts(itemType: String? = null, searchQuery: String? = null) {
        viewModelScope.launch {
            _postsListState.value = PostsListState.Loading

            when (val result = repository.getPosts(itemType, searchQuery)) {
                is PostRepository.PostsListResult.Success -> {
                    _postsListState.value = PostsListState.Success(result.posts)
                }
                is PostRepository.PostsListResult.Error -> {
                    _postsListState.value = PostsListState.Error(result.message)
                }
                is PostRepository.PostsListResult.Loading -> {
                    _postsListState.value = PostsListState.Loading
                }
            }
        }
    }

    fun resetState() {
        _postState.value = PostState.Idle
    }

    fun resetPostsListState() {
        _postsListState.value = PostsListState.Idle
    }
}
