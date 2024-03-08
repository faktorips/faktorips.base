package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType
import org.faktorips.devtools.model.builder.xmodel.XMethod

import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class MethodsTmpl{

//TODO Sadly there are some minor differences between product methods and policy methods. Maybe we want to join these templates and generate more equal code

def package static method (XMethod it) '''
    «IF published || !genInterface()»
        /**
        «IF published && !genInterface()»
         *«inheritDocOrText(description)»
        «ELSEIF description.length > 0»
         *«description»
        «ENDIF»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overrides)»
        «getModifier(genInterface())» «javaClassName» «method(methodName, methodParameters)» «IF abstract || genInterface()»
            ;
        «ELSE»
            {
                // TODO implement model method.
                throw new RuntimeException("Not implemented yet!");
            }
        «ENDIF»
    «ENDIF»
'''

}