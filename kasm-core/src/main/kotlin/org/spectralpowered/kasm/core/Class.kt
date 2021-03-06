package org.spectralpowered.kasm.core

import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode

class Class(val group: ClassGroup) : ClassNode(ASM9) {

    lateinit var parent: Class

    val children = mutableSetOf<Class>()

    val interfaces = mutableSetOf<Class>()

    val implementers = mutableSetOf<Class>()

    val innerClasses = mutableSetOf<Class>()

    val type: Type get() = Type.getObjectType(this.name)

    constructor(group: ClassGroup, name: String) : this(group) {
        this.name = name
    }

    fun hasParent(): Boolean = ::parent.isInitialized

    fun isShared(): Boolean = group.findSharedClass(this.name)?.let { true } ?: false

    fun isInput(): Boolean = group.findClass(this.name)?.let { true } ?: false

    override fun toString(): String {
        return this.name
    }
}