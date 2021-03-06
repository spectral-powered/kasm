package org.spectralpowered.kasm.util

import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object JarUtil {

    fun iterate(path: Path, handler: (Path) -> Unit) {
        val fs = FileSystems.newFileSystem(URI("jar:${path.toUri()}"), mutableMapOf<String, Any>())

        Files.walkFileTree(fs.getPath("/"), object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                if(file.toString().endsWith(".class")) {
                    handler(file)
                }

                return FileVisitResult.CONTINUE
            }
        })
    }

}