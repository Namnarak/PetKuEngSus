package com.petkuengsus.petkuengsus.util

import java.io.File

object DummyFile : File("") {
    override fun exists(): Boolean = false
}
