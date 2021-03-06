package org.spectralpowered.kasm.core

import org.objectweb.asm.Type

class Signature(private val owner: Method) {

    private val group: ClassGroup get() = owner.group
    private val type get() = owner.type

    var returnType = group.findOrCreate(type.returnType.internalName)
        set(value) {
            field = value
        }

    var arguments = type.argumentTypes.map { group.findOrCreate(it.internalName) }.toList()
        set(value) {
            field = value
        }

    private fun update() {
        owner.desc = this.toString()
    }

    override fun toString(): String {
        return Type.getMethodDescriptor(returnType.type, *arguments.map { it.type }.toTypedArray())
    }
}