package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.model.builder.naming.BuilderAspect
import org.faktorips.devtools.stdbuilder.xmodel.ImportStatement
import org.faktorips.devtools.stdbuilder.xmodel.StaticImportStatement
import org.faktorips.devtools.stdbuilder.xmodel.XClass
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
        «val staticImports = context.importHandler.staticImports»
        «FOR importStatement : staticImports»
            «staticImportStatement(importStatement)»
        «ENDFOR»
        «IF !staticImports.empty»
        
        «ENDIF»
        «FOR importStatement : context.importHandler.imports»
            «importStatement(importStatement)»
        «ENDFOR»
    '''

    def static staticImportStatement(StaticImportStatement it) {
        "import static " + qualifiedName + "." + element + ";\n"
    }
    def static importStatement(ImportStatement it) {
        "import " + qualifiedName + ";\n"
    }

}
