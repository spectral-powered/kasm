package org.spectralpowered.kasm.core

import org.objectweb.asm.Type

object FeatureProcessor {

    fun process(group: ClassGroup) {
        /*
         * Process features Step A
         */
        group.classes.values.forEach { cls ->
            this.processA(cls)
        }
    }

    private fun processA(cls: Class) {
        /*
         * Set each class's parent or extended class isntance.
         */
        if(cls.superName != null && !cls.hasParent()) {
            ClassGroup.addSuperClass(cls, cls.superName)
        }

        /*
         * Set each class's interface class instance.
         */
        cls.interfaceNames.forEach { className ->
            val interf = cls.group.findOrCreateClass(Type.getObjectType(className))

            if(cls.interfaces.add(interf)) {
                interf.implementers.add(cls)
            }
        }
    }

}