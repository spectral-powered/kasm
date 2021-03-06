package org.spectralpowered.kasm.core

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.spectralpowered.kasm.util.JarUtil
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.BiPredicate
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

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

    internal val featureExtractor = FeatureExtractor(this)

    fun addClass(cls: Class): Class {
        return this.classes.putIfAbsent(cls.toString(), cls) ?: cls
    }

    fun addSharedClass(cls: Class): Class {
        return this.sharedClasses.putIfAbsent(cls.toString(), cls) ?: cls
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
        val type = Type.getObjectType(name)

        /*
         * Check if the name is a shared class.
         */
        var ret = this.sharedClasses[name]
        if(ret != null) return ret

        /*
         * Check if the name is an array class.
         */
        if(type.sort == Type.ARRAY) {
            val elementId = type.elementType.internalName

            if(elementId.isEmpty()) {
                throw IllegalArgumentException("Invalid array class signature: $name")
            }

            val elementClass = this.findOrCreate(elementId)
            val cls = Class(this, Type.getObjectType(name), elementClass)

            ret = addSharedClass(cls)

            if(ret == cls) {
                ret.parent = this.findOrCreate("java/lang/Object")
                ret.parent.children.add(ret)
            }
        } else {
            ret = findMissingClass(name)
        }

        return ret
    }

    private fun findMissingClass(name: String): Class {
        if(name.length > 1) {
            var file: Path? = null

            if(this.classes[name] == null) {
                val url = ClassLoader.getSystemResource("$name.class")

                if(url != null) {
                    val uri = url.toURI()
                    var ret = Paths.get(uri)

                    if(uri.scheme == "jrt" && !Files.exists(ret)) {
                       ret = Paths.get(URI(uri.scheme, uri.userInfo, uri.host, uri.port, "/modules".plus(uri.path), uri.query, uri.fragment))
                    }

                    file = ret
                }
            }

            if(file != null) {
                val cls = readClass(file)
                val ret = this.addSharedClass(cls)

                if(ret == cls) {
                    featureExtractor.processA(ret)
                }

                return ret
            }
        }

        val ret = Class(this, Type.getObjectType(name))
        this.addSharedClass(ret)

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

    fun writeToJar(path: Path) {
        if(Files.exists(path)) {
            path.toFile().delete()
        }

        Files.newOutputStream(path).use { writer ->
            val jos = JarOutputStream(writer)

            this.classes.values.forEach { cls ->
                jos.putNextEntry(JarEntry(cls.name + ".class"))
                jos.write(cls.toBytecode())
                jos.closeEntry()
            }

            jos.close()
        }
    }

    fun writeToDirectory(path: Path) {
        if(!Files.isDirectory(path)) {
            throw IOException("Provided path is not a directory.")
        }

        this.classes.values.forEach { cls ->
            val fileName = cls.name + ".class"
            val filePath = path.resolve(fileName)

            Files.newOutputStream(filePath).use { writer ->
                writer.write(cls.toBytecode())
            }
        }
    }

}