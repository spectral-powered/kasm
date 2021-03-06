package org.spectralpowered.kasm.core

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.spectralpowered.kasm.util.JarUtil
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate

/**
 * A group of classes.
 *
 * This class represents a collection of java classes from a common classpath.
 *
 * @constructor Creates an empty [ClassGroup] collection.
 */
class ClassGroup {

    private val classes = mutableMapOf<String, Class>()
    private val sharedClasses = mutableMapOf<String, Class>()

    private val merged: MutableMap<String, Class> get() {
        return mutableMapOf<String, Class>().apply {
            this.putAll(classes)
            this.putAll(sharedClasses)
        }
    }

    private val featureExtractor = FeatureExtractor(this)

    fun addClass(cls: Class): Class {
        return this.classes.putIfAbsent(cls.name, cls) ?: cls
    }

    fun addSharedClass(cls: Class): Class {
        return this.sharedClasses.putIfAbsent(cls.name, cls) ?: cls
    }

    fun addClass(path: Path): Class = this.addClass(this.readClass(path))

    fun addSharedClass(path: Path): Class = this.addSharedClass(this.readClass(path))

    fun addJar(path: Path) {
        if(!path.toAbsolutePath().toString().endsWith(".jar")) {
            throw IOException("Provided path is not a JAR file.")
        }

        JarUtil.iterate(path) { entry ->
            this.addClass(entry)
        }
    }

    fun addDirectory(path: Path) {
        if(!Files.isDirectory(path)) {
            throw IOException("Provided path is not a directory.")
        }

        Files.find(
            path,
            999,
            BiPredicate { t, u -> u.isRegularFile && t.fileName.toString().matches(Regex(".*\\class")) }
        ).forEach { match ->
            this.addClass(match)
        }
    }

    internal fun readClass(path: Path): Class {
        val reader = ClassReader(Files.readAllBytes(path))
        val cls = Class(this)

        reader.accept(cls, ClassReader.EXPAND_FRAMES)

        return cls
    }

    fun findOrCreate(name: String): Class {
        var ret = this.classes[name]
        if(ret != null) return ret

        ret = this.sharedClasses[name]
        if(ret != null) return ret

        ret = findMissingClass(name)

        return ret
    }

    private fun findMissingClass(name: String): Class {
        val file: Path
        val url = ClassLoader.getSystemResource("$name.class")

        if(url != null) {
            val uri: URI

            try {
                uri = url.toURI()
                var ret = Paths.get(uri)

                if(uri.scheme == "jrt" && !Files.exists(ret)) {
                    ret = Paths.get(URI(uri.scheme, uri.userInfo, uri.host, uri.port, "/modules".plus(uri.path), uri.query, uri.fragment))
                }

                file = ret
            } catch(e : Exception) {
                throw e
            }
        } else {
            throw RuntimeException("Unable to load missing class: $name")
        }

        val cls = this.readClass(file)
        val ret = this.addSharedClass(cls)

        if(ret == cls) {
            featureExtractor.processA(ret)
        }

        return ret
    }

    fun findSharedClass(name: String): Class? = this.sharedClasses[name]

    fun findClass(name: String): Class? = this.classes[name]

    fun analyzeFeatures() {
        featureExtractor.process()
    }

    fun forEach(action: (Class) -> Unit) {
        merged.values.forEach(action)
    }

    fun forEachClass(action: (Class) -> Unit) {
        classes.values.forEach(action)
    }

    fun forEachSharedClass(action: (Class) -> Unit) {
        sharedClasses.values.forEach(action)
    }

}