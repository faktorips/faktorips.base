package org.faktorips.devtools.stdbuilder.xpand.template

import org.faktorips.devtools.stdbuilder.xpand.model.ImportStatement
import org.faktorips.devtools.stdbuilder.xpand.model.XClass
import org.faktorips.devtools.core.builder.naming.BuilderAspect
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext

class CommonDefinitionsTmpl {
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
