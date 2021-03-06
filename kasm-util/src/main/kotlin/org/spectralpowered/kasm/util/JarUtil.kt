package org.spectralpowered.kasm.util

import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object JarUtil {

    fun iterate(path: Path, handler: (Path) -> Unit) {
        val fs: FileSystem = FileSystems.newFileSystem(URI("jar:${path.toUri()}"), mutableMapOf<String, Any>())

        try {
            Files.walkFileTree(fs.getPath("/"), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    if(file.toString().endsWith(".class")) {
                        handler(file)
                    }

                    return FileVisitResult.CONTINUE
                }
            })
        } catch(e : Exception) {
            throw e
        } finally {
            fs.close()
        }
    }

}