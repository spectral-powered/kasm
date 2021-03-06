package org.spectralpowered.kasm.core

import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode

/**
 * A Java Class
 */
class Class(val group: ClassGroup) : ClassNode(ASM9) {

    lateinit var parent: Class

    val children = mutableSetOf<Class>()

    val interfaces = mutableSetOf<Class>()

    val implementers = mutableSetOf<Class>()

    val innerClasses = mutableSetOf<Class>()

    val type: Type get() = Type.getObjectType(this.name)

    var elementClass: Class? = null

    /**
     * Unknown Class Type
     */
    constructor(group: ClassGroup, name: String) : this(group) {
        this.name = name
    }

    /**
     * Array Class Type
     */
    constructor(group: ClassGroup, name: String, elementClass: Class) : this(group) {
        this.name = name
        this.elementClass = elementClass
    }

    val methods = mutableMapOf<String, Method>()

    fun hasParent(): Boolean = ::parent.isInitialized

    fun isShared(): Boolean = group.findSharedClass(this.name)?.let { true } ?: false

    fun isInput(): Boolean = group.findClass(this.name)?.let { true } ?: false

    fun isArray(): Boolean = this.elementClass != null

    fun findMethod(name: String, desc: String): Method? {
        return this.methods["${this.name}.$name$desc"]
    }

    override fun toString(): String {
        return this.name
    }
}