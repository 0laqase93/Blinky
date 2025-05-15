package dam.tfg.blinky.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object JwtUtils {
    /**
     * Extract email from JWT token
     * 
     * @param token JWT token
     * @return email from token payload or null if extraction fails
     */
    fun extractEmailFromToken(token: String?): String? {
        if (token.isNullOrEmpty()) {
            return null
        }
        
        try {
            // Split the token into parts
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("JwtUtils", "Invalid token format")
                return null
            }
            
            // Decode the payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
            
            // Parse the JSON payload
            val jsonObject = JSONObject(decodedString)
            
            // Extract the email (assuming it's in the "sub" claim or "email" claim)
            return when {
                jsonObject.has("email") -> jsonObject.getString("email")
                jsonObject.has("sub") -> jsonObject.getString("sub")
                else -> null
            }
        } catch (e: Exception) {
            Log.e("JwtUtils", "Error extracting email from token", e)
            return null
        }
    }
}