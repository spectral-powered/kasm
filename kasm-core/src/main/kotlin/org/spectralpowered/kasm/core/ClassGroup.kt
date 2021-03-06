package org.spectralpowered.kasm.core

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.spectralpowered.kasm.util.JarUtil
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ClassGroup {

    val classes = mutableMapOf<String, Class>()

    val sharedClasses = mutableMapOf<String, Class>()

    val classPathIndex = mutableMapOf<String, Path>()

    fun addJar(file: Path) {
        JarUtil.iterate(file) { entry ->
            this.addClass(entry)
        }
    }

    fun addClass(file: Path): Class {
        val cls = readClass(file)

        classPathIndex.putIfAbsent(cls.toString(), file)
        classes.putIfAbsent(cls.toString(), cls)

        return cls
    }

    fun addSharedClass(file: Path): Class {
        val cls = readClass(file)
        val prev = this.sharedClasses.putIfAbsent(cls.type.internalName, cls)

        if(prev != null) return prev

        return cls
    }

    fun findSharedClass(type: Type): Class? = this.sharedClasses[type.internalName]

    fun findOrCreateClass(type: Type): Class {
        var ret = this.findSharedClass(type)
        if(ret != null) return ret

        ret = this.findMissingClass(type)

        return ret
    }

    fun findMissingClass(type: Type): Class {
        if(type.descriptor.length > 1) {
            val name = type.internalName
            var file = this.classPathIndex[name]

            if(file == null) {
                val url = ClassLoader.getSystemResource("$name.class")

                if(url != null) {
                    file = this.resolvePath(url)
                }
            }

            if(file != null) {
                return addSharedClass(file)
            }
        }

        val ret = Class(this, type)

        this.sharedClasses.putIfAbsent(ret.type.descriptor, ret)

        return ret
    }

    private fun resolvePath(url: URL): Path {
        var uri = url.toURI()
        var ret: Path = Paths.get(uri)

        if(uri.scheme == "jrt" && !Files.exists(ret)) {
            ret = Paths.get(URI(uri.scheme, uri.userInfo, uri.host, uri.port, "/modules".plus(uri.path), uri.query, uri.fragment))
        }

        return ret
    }

    private fun readClass(file: Path): Class {
        val reader = ClassReader(Files.readAllBytes(file))
        val cls = Class(this, file.toUri())

        reader.accept(cls, ClassReader.EXPAND_FRAMES)

        return cls

    }

    companion object {

        fun addSuperClass(target: Class, name: String) {
            target.parent = target.group.findOrCreateClass(Type.getObjectType(name))
            target.parent.children.add(target)
        }

    }
}