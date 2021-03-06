package org.spectralpowered.kasm.core

import java.nio.file.Paths

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val group = ClassGroup()
        group.addJar(Paths.get("newgamepack.jar"))
        group.analyzeFeatures()

        println("Boom")
    }
}