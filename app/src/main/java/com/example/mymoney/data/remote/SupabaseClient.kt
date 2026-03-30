package com.example.mymoney.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton Supabase client – dùng chung cho toàn bộ app.
 *
 * Tại sao dùng object?
 *  • Chỉ tạo **một** kết nối duy nhất → tiết kiệm tài nguyên.
 *  • Truy cập ở bất kỳ đâu qua `SupabaseClient.client`.
 *
 * Cách dùng:
 * ```
 * val result = SupabaseClient.client.postgrest["table_name"]
 *     .select()
 *     .decodeList<MyModel>()
 * ```
 */
object SupabaseClient {

    // TODO: Thay bằng URL và Key thật của project Supabase
    private const val SUPABASE_URL = "https://cilhdctuvhpdqodfevla.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNpbGhkY3R1dmhwZHFvZGZldmxhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4ODQ3NDcsImV4cCI6MjA5MDQ2MDc0N30.hkJxcFDrH761VM8lCZEPsraEADRM6cKzpL8i0sLhbDY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // Module truy vấn database (PostgREST)
        install(Postgrest)
        // Module xác thực người dùng (Auth)
        install(Auth)
    }
}
