package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

class AiRepository {
    private val primaryModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val fastFallbackModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash-lite")
    }

    fun streamPrompt(prompt: String): Flow<String> = flow {
        val sb = StringBuilder()
        var primaryTimedOut = false

        try {
            withTimeout(30_000L) {
                primaryModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        sb.append(piece)
                        emit(sb.toString())
                    }
                }
            }
            if (sb.isNotBlank()) return@flow
        } catch (e: TimeoutCancellationException) {
            if (sb.isNotBlank()) {
                Log.w(TAG, "Primary stream partial timeout; returning collected output")
                return@flow
            }
            primaryTimedOut = true
            Log.w(TAG, "Primary stream timed out at 30s; falling back to fast model", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (sb.isNotBlank()) return@flow
            Log.w(TAG, "Primary stream failed; attempting fast fallback", e)
        }

        // Fallback to unary call on gemini-3.5-flash-lite (avoids SSE streaming stalls)
        try {
            val response = withTimeout(30_000L) {
                fastFallbackModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                emit(text)
            } else {
                throw IllegalStateException("PGLOB AI returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            val msg = if (primaryTimedOut) {
                "PGLOB AI timed out after 60 seconds. Check your connection and try again."
            } else {
                "PGLOB AI request timed out. Please try again."
            }
            Log.e(TAG, "AI generation timed out", e)
            throw IllegalStateException(msg, e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI generation failed", e)
            throw IllegalStateException(e.localizedMessage ?: "PGLOB AI request failed.", e)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generateOnce(prompt: String): String {
        try {
            val response = withTimeout(30_000L) {
                primaryModel.generateContent(prompt)
            }
            response.text?.takeIf { it.isNotBlank() }?.let { return it }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Primary unary generation timed out; trying fast fallback", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Primary unary generation failed; trying fast fallback", e)
        }

        return try {
            val fallback = withTimeout(30_000L) {
                fastFallbackModel.generateContent(prompt)
            }
            fallback.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("PGLOB AI returned an empty response.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI unary generation timed out", e)
            throw IllegalStateException("PGLOB AI timed out. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI unary generation failed", e)
            throw IllegalStateException(e.localizedMessage ?: "PGLOB AI request failed.", e)
        }
    }

    private companion object {
        const val TAG = "AiRepository"
    }
}
