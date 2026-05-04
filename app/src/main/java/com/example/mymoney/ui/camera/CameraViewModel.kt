package com.example.mymoney.ui.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.data.remote.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CameraViewModel(
    private val context: Context,
    private val walletId: Long
) : ViewModel() {

    private val TAG = "CameraViewModel"

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val _photoTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val photoTransactions: StateFlow<List<TransactionEntity>> = _photoTransactions

    init {
        viewModelScope.launch {
            val userId = SettingPreferences(context).currentUserId.first() ?: return@launch
            AppDatabase.getInstance(context)
                .transactionDao()
                .observePhotoTransactions(userId)
                .collect { _photoTransactions.value = it }
        }
    }

    fun savePhoto(photoUri: Uri, amount: Double = 0.0) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val prefs = SettingPreferences(context)
                val userId = prefs.currentUserId.first() ?: run {
                    _saveState.value = SaveState.Error("Chưa đăng nhập")
                    return@launch
                }

                // 1. Copy ảnh từ cacheDir sang filesDir (persistent)
                val photoFile = File(photoUri.path ?: run {
                    _saveState.value = SaveState.Error("Không đọc được ảnh")
                    return@launch
                })
                val destFile = File(context.filesDir, "tximg_${System.currentTimeMillis()}.jpg")
                withContext(Dispatchers.IO) { photoFile.copyTo(destFile, overwrite = true) }
                val localPath = destFile.absolutePath

                val db = AppDatabase.getInstance(context)
                val now = System.currentTimeMillis()

                // 2. Lưu vào Room với local image_path
                val entity = TransactionEntity(
                    userId          = userId,
                    walletId        = walletId,
                    amount          = amount,
                    type            = "expense",
                    note            = "Ảnh chi tiêu",
                    categoryName    = "Khác",
                    transactionDate = now,
                    createdAt       = now,
                    updatedAt       = now,
                    isDeleted       = false,
                    syncStatus      = SyncStatus.PENDING_INSERT,
                    imagePath       = localPath
                )
                val localId = db.transactionDao().insert(entity)
                Log.d(TAG, "Saved photo transaction to Room: id=$localId, path=$localPath")

                // 3. Trừ số dư ví (expense)
                if (amount > 0.0) {
                    val wallet = db.walletDao().getWalletById(walletId)
                    if (wallet != null) {
                        val newBalance = wallet.balance - amount
                        db.walletDao().update(
                            wallet.copy(
                                balance    = newBalance,
                                updatedAt  = now,
                                syncStatus = SyncStatus.PENDING_UPDATE
                            )
                        )
                        Log.d(TAG, "Wallet balance: ${wallet.balance} → $newBalance")
                    }
                }

                // 4. Upload lên Supabase Storage (non-fatal)
                launch {
                    try {
                        val storagePath = "transactions/$userId/${destFile.name}"
                        val bytes = withContext(Dispatchers.IO) { destFile.readBytes() }
                        SupabaseClient.client.storage.from("transaction-images").upload(storagePath, bytes)
                        val publicUrl = SupabaseClient.client.storage.from("transaction-images").publicUrl(storagePath)
                        db.transactionDao().updateImagePath(localId, publicUrl)
                        Log.d(TAG, "Uploaded to Supabase Storage: $publicUrl")
                    } catch (e: Exception) {
                        Log.w(TAG, "Storage upload failed (non-fatal): ${e.message}")
                    }
                }

                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e(TAG, "savePhoto failed: ${e.message}", e)
                _saveState.value = SaveState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /** Xóa giao dịch ảnh và phục hồi số dư ví */
    fun deletePhotoTransaction(txId: Long, txWalletId: Long, amount: Double, type: String, imagePath: String?) {
        viewModelScope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                // Phục hồi số dư
                val wallet = db.walletDao().getWalletById(txWalletId)
                if (wallet != null && amount > 0.0) {
                    val restoredBalance = if (type == "expense") wallet.balance + amount
                                         else wallet.balance - amount
                    db.walletDao().update(
                        wallet.copy(
                            balance    = restoredBalance,
                            updatedAt  = System.currentTimeMillis(),
                            syncStatus = SyncStatus.PENDING_UPDATE
                        )
                    )
                    Log.d(TAG, "Restored wallet balance: ${wallet.balance} → $restoredBalance")
                }
                // Soft delete giao dịch
                db.transactionDao().softDelete(txId)
                // Xóa file ảnh local
                imagePath?.let { withContext(Dispatchers.IO) { File(it).delete() } }
                Log.d(TAG, "Deleted photo transaction $txId")
            } catch (e: Exception) {
                Log.e(TAG, "deletePhotoTransaction failed: ${e.message}")
            }
        }
    }

    /** Lưu ảnh vào album máy (DCIM/MyMoney) */
    fun savePhotoToAlbum(imagePath: String?) {
        imagePath ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (!file.exists()) return@launch
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MyMoney")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@launch
                resolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                Log.d(TAG, "Saved to album: ${file.name}")
            } catch (e: Exception) {
                Log.e(TAG, "savePhotoToAlbum failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }

    companion object {
        fun factory(context: Context, walletId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CameraViewModel(context.applicationContext, walletId) as T
        }
    }
}
