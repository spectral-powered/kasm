package org.spectralpowered.kasm.core

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

class Method(
    val group: ClassGroup,
    val owner: Class,
    access: Int,
    name: String,
    desc: String,
    genericSignature: String?,
    exceptions: Array<String>
) : MethodNode(ASM9, access, name, desc, genericSignature, exceptions) {

    constructor(group: ClassGroup, owner: Class, node: MethodNode) : this(group, owner, node.access, node.name, node.desc, node.genericSignature, node.exceptions.toTypedArray())

    val type get() = Type.getMethodType(this.desc)

    val isInitializer get() = this.name == INITIALIZER

    val isConstructor get() = this.name == CONSTRUCTOR

    var signature: Signature = Signature(this)

    override fun accept(visitor: ClassVisitor) {
        visitor.visitMethod(access, name, signature.toString(), genericSignature, exceptions.toTypedArray())?.apply {
            accept(this)
        }
    }

    override fun toString(): String {
        return "$owner.$name$desc"
    }

    companion object {
        private const val INITIALIZER = "<clinit>"
        private const val CONSTRUCTOR = "<init>"
    }
}