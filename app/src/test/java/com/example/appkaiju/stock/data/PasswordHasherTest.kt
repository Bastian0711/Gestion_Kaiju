package com.example.appkaiju.stock.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotContain

class PasswordHasherTest : StringSpec({

    "el hash tiene el formato sal:hash en hexadecimal y no contiene la clave (RNF-03)" {
        val hash = PasswordHasher.hashear("Admin123")
        hash shouldMatch Regex("^[0-9a-f]{32}:[0-9a-f]{64}$")
        hash shouldNotContain "Admin123"
    }

    "verifica la clave correcta y rechaza una incorrecta" {
        val hash = PasswordHasher.hashear("Admin123")
        PasswordHasher.verificar("Admin123", hash) shouldBe true
        PasswordHasher.verificar("admin123", hash) shouldBe false
    }

    "dos hashes de la misma clave son distintos por la sal aleatoria" {
        PasswordHasher.hashear("Clave123") shouldNotBe PasswordHasher.hashear("Clave123")
    }

    "un hash guardado con formato inválido no se acepta" {
        PasswordHasher.verificar("Admin123", "sin-separador") shouldBe false
    }
})