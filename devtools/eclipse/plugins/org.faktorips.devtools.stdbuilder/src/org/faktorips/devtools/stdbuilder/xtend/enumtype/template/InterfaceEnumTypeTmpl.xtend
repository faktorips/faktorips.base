package org.faktorips.devtools.stdbuilder.xtend.enumtype.template

import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumAttribute
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType

import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import org.faktorips.devtools.stdbuilder.xtend.template.CommonDefinitions

class InterfaceEnumTypeTmpl {

    def package static String body(XEnumType it) '''
        /**
        «IF described»
         *  «description»
         *
        «ENDIF»
         «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(ENUM_CLASS)»
        public interface «name» «CommonDefinitions::extendedInterfaces(it)»{
            «FOR it : declaredAttributesWithoutLiteralName» «getterMethod» «ENDFOR»
        }
    '''

    def private static getterMethod(XEnumAttribute it) '''
        /**
        «IF multilingual»
            *«localizedJDoc("GETTER_MULTILINGUAL", name, descriptionForJDoc)»
        «ELSE»
            *«localizedJDoc("GETTER", name, descriptionForJDoc)»
        «ENDIF»
       «getAnnotations(ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(ENUM_ATTRIBUTE_GETTER)»
        public «datatypeName» «IF multilingual»«method(methodNameGetter, Locale, "locale")»«ELSE»«method(methodNameGetter)»«ENDIF»;
    '''

}
