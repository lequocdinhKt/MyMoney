package com.example.mymoney.ui.other

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.theme.MyMoneyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBugScreen(
    onBackClick: () -> Unit = {}
) {
    var bugDescription by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gửi lỗi cho chúng tôi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Chúng tôi rất tiếc vì bạn gặp phải sự cố. Vui lòng mô tả chi tiết lỗi bên dưới để chúng tôi có thể hỗ trợ bạn tốt nhất.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bugDescription,
                onValueChange = { bugDescription = it },
                label = { Text("Mô tả lỗi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                placeholder = { Text("Ví dụ: App bị văng khi tôi nhấn vào nút Thêm giao dịch...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text("Thông tin liên hệ (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email hoặc số điện thoại") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (bugDescription.isNotBlank()) {
                        val deviceInfo = "Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}"
                        val emailBody = "Mô tả lỗi:\n$bugDescription\n\nThông tin liên hệ: $contactInfo\n\n---\n$deviceInfo"
                        
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@mymoney.app")) // Placeholder email
                            putExtra(Intent.EXTRA_SUBJECT, "[MyMoney] Báo cáo lỗi từ người dùng")
                            putExtra(Intent.EXTRA_TEXT, emailBody)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            // Fallback if no email client
                            context.startActivity(Intent.createChooser(intent, "Chọn ứng dụng email"))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bugDescription.isNotBlank()
            ) {
                Text("Gửi báo cáo")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportBugScreenPreview() {
    MyMoneyTheme {
        ReportBugScreen()
    }
}
