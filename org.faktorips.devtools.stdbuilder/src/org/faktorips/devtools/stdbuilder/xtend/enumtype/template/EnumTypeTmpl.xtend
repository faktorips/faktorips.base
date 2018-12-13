package org.faktorips.devtools.stdbuilder.xtend.enumtype.template

import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType

class EnumTypeTmpl {

    def static String body(XEnumType it) '''
        «IF abstract»
            «InterfaceEnumTypeTmpl::body(it)»
        «ELSEIF extensible»
            «ClassEnumTypeTmpl::body(it)»
        «ELSE»
            «EnumEnumTypeTmpl::body(it)»
        «ENDIF»
    '''
}
