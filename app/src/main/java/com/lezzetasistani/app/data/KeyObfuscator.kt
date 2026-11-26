package com.lezzetasistani.app.data

object KeyObfuscator {

    private const val MASK_KEY: Byte = 0x5A

    fun deobfuscate(maskedBytes: ByteArray): String {
        val decoded = ByteArray(maskedBytes.size)
        for (i in maskedBytes.indices) {
            decoded[i] = (maskedBytes[i].toInt() xor MASK_KEY.toInt()).toByte()
        }
        return String(decoded, Charsets.UTF_8)
    }

    fun getSecureApiKey(rawKey: String): String {
        val trimmed = rawKey.trim()
        if (trimmed.isBlank() || trimmed.startsWith("BURAYA_")) {
            return ""
        }
        val bytes = trimmed.toByteArray(Charsets.UTF_8)
        val masked = ByteArray(bytes.size)
        for (i in bytes.indices) {
            masked[i] = (bytes[i].toInt() xor MASK_KEY.toInt()).toByte()
        }
        return deobfuscate(masked)
    }
}
