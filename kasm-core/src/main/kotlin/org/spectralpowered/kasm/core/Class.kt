package org.spectralpowered.kasm.core

import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import java.net.URI

open class Class(val group: ClassGroup, val uri: URI) : ClassNode(ASM9) {

    lateinit var parent: Class

    val children = mutableSetOf<Class>()

    val interfaces = mutableSetOf<Class>()

    val implementers = mutableSetOf<Class>()

    val innerClasses = mutableSetOf<Class>()

    val type: Type get() = Type.getObjectType(this.name)

    constructor(group: ClassGroup, type: Type) : this(group, URI("")) {
        this.name = type.internalName
    }

    fun hasParent(): Boolean = ::parent.isInitialized

    override fun toString(): String {
        return this.name
    }
}