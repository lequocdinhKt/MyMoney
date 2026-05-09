package com.example.mymoney.common

/**
 * Sealed class mô tả ba trạng thái vòng đời dữ liệu của mọi màn hình.
 *
 * Cách dùng trong ViewModel:
 * ```kotlin
 * class MyViewModel : BaseViewModel<MyUiState>() {
 *     init { collectFlow(repo.data()) { MyUiState(it) } }
 * }
 * ```
 *
 * Cách dùng trong Composable:
 * ```kotlin
 * UiStateContainer(
 *     uiState  = uiState,
 *     skeleton = { MySkeletonScreen() },
 *     content  = { data -> MyContent(data) }
 * )
 * ```
 */
sealed class UiState<out T> {

    /** Đang tải – skeleton shimmer được hiển thị */
    data object Loading : UiState<Nothing>()

    /** Tải thành công, chứa [data] để render UI */
    data class Success<out T>(val data: T) : UiState<T>()

    /** Tải thất bại, chứa [message] và [throwable] tuỳ chọn */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}

// ── Extension properties ──

@Suppress("unused")
val <T> UiState<T>.isLoading: Boolean get() = this is UiState.Loading
@Suppress("unused")
val <T> UiState<T>.isSuccess: Boolean get() = this is UiState.Success
@Suppress("unused")
val <T> UiState<T>.isError:   Boolean get() = this is UiState.Error

/** Trả về data nếu Success, null nếu Loading/Error */
@Suppress("unused")
fun <T> UiState<T>.getOrNull(): T? = (this as? UiState.Success)?.data

/** Trả về error message nếu Error, null nếu không phải */
@Suppress("unused")
fun <T> UiState<T>.errorOrNull(): String? = (this as? UiState.Error)?.message


