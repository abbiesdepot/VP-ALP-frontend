package com.abbie.alpvp.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun getUserId(token: String): Int {
        try {
            val split = token.split(".")
            if (split.size < 2) return -1
            val body = String(Base64.decode(split[1], Base64.URL_SAFE))
            val json = JSONObject(body)
            return json.optInt("id", -1)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    fun getUsername(token: String): String {
        try {
            val split = token.split(".")
            if (split.size < 2) return "User"
            val body = String(Base64.decode(split[1], Base64.URL_SAFE))
            val json = JSONObject(body)
            return json.optString("username", "User")
        } catch (e: Exception) {
            return "User"
        }
    }
}