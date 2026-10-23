package com.example.appkaiju.stock.data

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object PasswordHasher {
    private const val ITERACIONES = 10_000
    private const val LARGO_CLAVE = 256

    fun hashear(clave: String): String {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${aHex(sal)}:${aHex(derivar(clave, sal))}"
    }

    fun verificar(clave: String, guardado: String): Boolean {
        val partes = guardado.split(":")
        if (partes.size != 2) return false
        val sal = deHex(partes[0])
        return aHex(derivar(clave, sal)) == partes[1]
    }

    private fun derivar(clave: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), sal, ITERACIONES, LARGO_CLAVE)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun aHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    private fun deHex(texto: String): ByteArray =
        ByteArray(texto.length / 2) { texto.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}