package org.faktorips.devtools.stdbuilder.xpand.enumtype.template

import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumAttribute
import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumType
import org.faktorips.devtools.stdbuilder.xpand.template.CommonDefinitionsTmpl

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xpand.template.ClassNamesTmpl.*

class InterfaceEnumTypeTmpl {

    def package static String body(XEnumType it) '''
        /**
        «IF described»
         *  «description»
         *
        «ENDIF»
         «getAnnotations(ELEMENT_JAVA_DOC)»
         * @generated
         */
        «getAnnotations(ENUM_CLASS)»
        public interface «name» «CommonDefinitionsTmpl::extendedInterfaces(it)»{
            «FOR it : declaredAttributesWithoutLiteralName» «getterMethod» «ENDFOR»
        }
    '''

    def private static getterMethod(XEnumAttribute it) '''
        /**
        «IF multilingual»
            * «localizedJDoc("GETTER_MULTILINGUAL", name, descriptionForJDoc)»
        «ELSE»
            * «localizedJDoc("GETTER", name, descriptionForJDoc)»
        «ENDIF»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        «getAnnotations(ENUM_ATTRIBUTE_GETTER)»
        public «datatypeName» «IF multilingual»«method(methodNameGetter, Locale, "locale")»«ELSE»«method(methodNameGetter)»«ENDIF»;
    '''

}
