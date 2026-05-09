package com.example.mymoney.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.common.UiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel cơ sở tích hợp sẵn [UiState].
 * Subclass khai báo kiểu dữ liệu **T** (thường là data class UiState của màn hình).
 *
 * ## Cách dùng – collect Flow:
 * ```kotlin
 * class SavingViewModel(repo: SavingRepository) : BaseViewModel<SavingUiState>() {
 *     init {
 *         collectFlow(repo.getSavings()) { list ->
 *             SavingUiState(items = list)
 *         }
 *     }
 * }
 * ```
 *
 * ## Cách dùng – suspend call một lần:
 * ```kotlin
 * class DetailViewModel(repo: DetailRepository, id: Long) : BaseViewModel<DetailUiState>() {
 *     init {
 *         launchWithState { DetailUiState(item = repo.getById(id)) }
 *     }
 * }
 * ```
 *
 * ### Lưu ý với HomeViewModel:
 * HomeViewModel dùng combine/flatMapLatest phức tạp nên **không** extend BaseViewModel.
 * Dùng overload `UiStateContainer(isLoading = ...)` để tích hợp từng bước.
 */
@Suppress("unused")
abstract class BaseViewModel<T> : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<T>>(UiState.Loading)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    // ── Setters ──

    protected fun setLoading() {
        _uiState.value = UiState.Loading
    }

    protected fun setSuccess(data: T) {
        _uiState.value = UiState.Success(data)
    }

    protected fun setError(message: String, throwable: Throwable? = null) {
        _uiState.value = UiState.Error(message, throwable)
    }

    // ── Builders ──

    /**
     * Collect một cold [Flow] và tự động map:
     *  - Trước emit đầu → [UiState.Loading]
     *  - Mỗi emit mới   → [UiState.Success] (qua [transform])
     *  - Khi exception  → [UiState.Error]
     *
     * @param flow       Flow nguồn (thường từ Repository)
     * @param dispatcher CoroutineDispatcher (mặc định IO)
     * @param transform  Chuyển raw data thành T (UiState data class của màn hình)
     */
    protected fun <R> collectFlow(
        flow: Flow<R>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        transform: (R) -> T
    ) {
        flow
            .onStart { setLoading() }
            .map    { transform(it) }
            .onEach { setSuccess(it) }
            .catch  { setError(it.message ?: "Đã xảy ra lỗi không xác định", it) }
            .flowOn (dispatcher)
            .launchIn(viewModelScope)
    }

    /**
     * Chạy một suspend block và tự động wrapper Loading → Success / Error.
     * Dùng cho single-shot call (không phải Flow).
     *
     * @param dispatcher CoroutineDispatcher (mặc định IO)
     * @param block      Suspend lambda trả về T
     */
    protected fun launchWithState(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ) {
        viewModelScope.launch(dispatcher) {
            setLoading()
            runCatching { block() }
                .onSuccess { setSuccess(it) }
                .onFailure { setError(it.message ?: "Đã xảy ra lỗi không xác định", it) }
        }
    }
}


