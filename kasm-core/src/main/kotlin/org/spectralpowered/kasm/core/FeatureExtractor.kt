package org.spectralpowered.kasm.core

class FeatureExtractor(val group: ClassGroup) {

    fun process() {
        /*
         * Process Stage: A
         */
        group.forEachClass { cls ->
            this.processA(cls)
        }
    }

    fun processA(cls: Class) {
        /*
         * Set the class parent / children
         */
        if(cls.superName != null && !cls.hasParent()) {
            cls.parent = group.findOrCreate(cls.superName)
            cls.parent.children.add(cls)
        }

        /*
         * Set class interfaces and implementers
         */
        cls.interfaceNames.map { group.findOrCreate(it) }.forEach { interf ->
            if(cls.interfaces.add(interf)) interf.implementers.add(cls)
        }
    }

}