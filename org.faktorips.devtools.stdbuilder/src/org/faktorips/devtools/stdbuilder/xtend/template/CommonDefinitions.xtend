package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.stdbuilder.xmodel.ImportStatement
import org.faktorips.devtools.stdbuilder.xmodel.XClass
import org.faktorips.devtools.core.builder.naming.BuilderAspect
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext

class CommonDefinitions {
    def static importBlock(ImportStatement it) '''
        import «qualifiedName»;
    '''

    def static implementedInterfaces(XClass it) '''
        «IF implementsInterface»
            implements «FOR interfaceDef  : implementedInterfaces SEPARATOR  ', '» «interfaceDef» «ENDFOR»
        «ENDIF»
    '''

    def static extendedInterfaces(XClass it) '''
        «IF extendsInterface»
            extends «FOR interfaceDef  : implementedInterfaces SEPARATOR  ", "» «interfaceDef» «ENDFOR»
        «ENDIF»
    '''

    def static String packageDef(XClass it, BuilderAspect aspect) {
        "package " + getPackageName(aspect) + ";\n";
    }

    def static String importBlock(GeneratorModelContext context) '''
        «FOR importStatement : context.importHandler.imports»
            «importStatement(importStatement)»
        «ENDFOR»
    '''

    def static importStatement(ImportStatement it) {
        "import " + qualifiedName + ";\n"
    }

}
