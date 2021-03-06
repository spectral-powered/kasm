package org.spectralpowered.kasm.core

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
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

    val type: Type get() = Type.getObjectType(this.name)

    var elementClass: Class? = null

    /**
     * Unknown Class Type
     */
    constructor(group: ClassGroup, type: Type) : this(group) {
        this.name = type.internalName
    }

    /**
     * Array Class Type
     */
    constructor(group: ClassGroup, type: Type, elementClass: Class) : this(group) {
        this.name = type.elementType.internalName
        this.elementClass = elementClass
    }

    val methods = mutableMapOf<String, Method>()

    val fields = mutableMapOf<String, Field>()

    fun hasParent(): Boolean = ::parent.isInitialized

    fun isShared(): Boolean = group.findSharedClass(this.name)?.let { true } ?: false

    fun isInput(): Boolean = group.findClass(this.name)?.let { true } ?: false

    fun isArray(): Boolean = this.elementClass != null

    fun findMethod(name: String, desc: String): Method? {
        return this.methods["${this.name}.$name$desc"]
    }

    fun findField(name: String): Field? {
        return this.fields["${this.name}.$name"]
    }

    override fun accept(visitor: ClassVisitor) {
        val interfacesArray = this.interfaces.map { it.name }.toTypedArray()

        visitor.visit(version, access, name, genericSignature, parent.name, interfacesArray)

        if(sourceFile != null || sourceDebug != null) {
            visitor.visitSource(sourceFile, sourceDebug)
        }

        if(outerClassName != null) {
            visitor.visitOuterClass(outerClassName, outerMethodName, outerMethodDesc)
        }

        for(i in innerClasses.indices) {
            innerClasses[i].accept(visitor)
        }

        fields.values.forEach { field ->
            field.accept(visitor)
        }

        methods.values.forEach { method ->
            method.accept(visitor)
        }

        visitor.visitEnd()
    }

    fun toBytecode(): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        this.accept(writer)
        return writer.toByteArray()
    }

    override fun toString(): String {
        return if(this.isArray()) {
            "${this.name}[]"
        } else {
            this.name
        }
    }
}