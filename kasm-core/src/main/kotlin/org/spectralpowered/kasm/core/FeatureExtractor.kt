package org.spectralpowered.kasm.core

class FeatureExtractor(val group: ClassGroup) {

    fun process() {
        /*
         * Add a virtual class 'java/lang/Object' to the group.
         */
        group.findOrCreate("java/lang/Object")


        /*
         * Process Stage: A
         */
        group.forEachClass { cls ->
            this.processA(cls)
        }
    }

    fun processA(cls: Class) {
        /*
         * Add methods to class method map
         */
        for(i in cls.methodNodes.indices) {
            val node = cls.methodNodes[i]

            if(cls.findMethod(node.name, node.desc) == null) {
                val m = Method(group, cls, node)
                node.accept(m)
                cls.methods[m.toString()] = m
            }
        }

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