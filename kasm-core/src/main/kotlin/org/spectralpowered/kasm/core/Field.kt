package org.spectralpowered.kasm.core

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldNode

class Field(
    val group: ClassGroup,
    val owner: Class,
    access: Int,
    name: String,
    desc: String,
    genericSignature: String?,
    value: Any?
) : FieldNode(ASM9, access, name, desc, genericSignature, value) {

    constructor(group: ClassGroup, owner: Class, node: FieldNode) : this(group, owner, node.access, node.name, node.desc, node.genericSignature, node.value)

    val type get() = Type.getType(this.desc)

    var signature = group.findOrCreate(this.type.internalName)

    override fun accept(visitor: ClassVisitor) {
        val fieldVisitor = visitor.visitField(access, name, signature.type.descriptor, genericSignature, value)
                ?: return

        fieldVisitor.visitEnd()
    }

    override fun toString(): String {
        return "$owner.$name"
    }
}