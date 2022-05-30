package org.faktorips.devtools.stdbuilder.xtend.enumtype.template

import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumAttribute
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*

class CommonEnumTypeTmpl {

    def package static messageHelperVar (XEnumType it) '''
        «IF messageHelperNeeded»
        /**
         * @generated
         */
        private static final «MessagesHelper» «varNameMessageHelper» = new «MessagesHelper»(«unqualifiedClassName».class.getName(), «unqualifiedClassName».class.getClassLoader(), «defaultLocale»);
        «ENDIF»
    '''

    def package static fields (XEnumType it) '''
        «FOR it : allAttributesWithField»
            «field»
        «ENDFOR»
    '''

    def private static field (XEnumAttribute it) '''
        /**
        «getAnnotations(ELEMENT_JAVA_DOC)»
        «IF !getAnnotations(ELEMENT_JAVA_DOC).nullOrEmpty» *«ENDIF»
         * @generated
         */
        «getAnnotations(DEPRECATION)»
        private final «datatypeNameForConstructor» «field(memberVarName)»;
    '''

    def package static getters (XEnumType it) '''
        «FOR it : allAttributesWithoutLiteralName» «getter» «ENDFOR»
    '''

    def private static getter (XEnumAttribute it) '''
       /**
        *«IF multilingual»«localizedJDoc("GETTER_MULTILINGUAL", name, descriptionForJDoc)»«ELSE»«localizedJDoc("GETTER", name, descriptionForJDoc)»«ENDIF»
       «getAnnotations(ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(ENUM_ATTRIBUTE_GETTER)»
        «IF inherited || !isDeclaredIn(enumType)»@Override«ENDIF»
        public «datatypeName» «IF multilingual»«method(methodNameGetter, Locale, "locale")»«ELSE»«method(methodNameGetter)»«ENDIF» {
            «IF multilingual»
                «IF enumType.extensible»
                    return «memberVarName».get(locale);
                «ELSE»
                    return «enumType.varNameMessageHelper».getMessage("«name»_" + «enumType.identifierAttribute.methodNameGetter»(), locale);
                «ENDIF»
            «ELSE»
                return «memberVarName»;
            «ENDIF»
        }
    '''

    def package static fieldInitializations(boolean hasIndex, XEnumType it) '''
        «IF hasIndex»
            this.«varNameIndex» = «varNameIndex»;
        «ENDIF»
        «FOR it : allAttributesWithField»
            this.«memberVarName» = «memberVarName»;
        «ENDFOR»
    '''

    def package static toString (XEnumType it) '''
        /**
         * {@inheritDoc}
         *
         * @generated
         */
        @Override
        public String toString() {
            return "«name»: " + «identifierAttribute.memberVarName» + '(' + «IF displayNameAttribute.multilingual»«displayNameAttribute.methodNameGetter»(«defaultLocale»)«ELSE»«displayNameAttribute.memberVarName»«ENDIF» + ')';
        }
    '''

}