package org.faktorips.devtools.stdbuilder.xtend.enumtype.template

import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumValue

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.enumtype.template.CommonEnumTypeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import org.faktorips.devtools.stdbuilder.xtend.template.CommonDefinitions

class ClassEnumTypeTmpl {

    def static String body(XEnumType it) '''

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
        public final class «name» «CommonDefinitions::extendedInterfaces(it)» «CommonDefinitions::implementedInterfaces(it)»{

            «serialVersionUID»
            «messageHelperVar»
            «FOR it : enumValues» «enumValue» «ENDFOR»
            «valuesConstant»
            «indexField»
            «fields»
            «constructors»
            «getters»
            «toString(it)»
            «equals(it)»
            «hashCode(it)»
            «enumValueId»
            «compareTo»
        }
    '''

    def private static compareTo(XEnumType it) '''
        «IF generateMethodCompareTo»
            /**
             * @generated
             */
            @Override
            public int compareTo(«name» o) {
                return «varNameIndex» - o.«varNameIndex»;
            }
        «ENDIF»
    '''

    def private static indexField(XEnumType it) '''
        «IF indexFieldRequired»
            /**
             * @generated
             */
            private final int «varNameIndex»;
        «ENDIF»
    '''

    def private static enumValueId(XEnumType it) '''
        «IF identifierAttribute.isDeclaredIn(it)»
            /**
             * «localizedJDoc("METHOD_GET_ENUM_VALUE_BY_ID")»
             *
             * @generated
             */
            Object getEnumValueId() {
                return «identifierAttribute.memberVarName»;
            }
        «ENDIF»
    '''

    def private static serialVersionUID(XEnumType it) '''
        /**
        * «localizedJDoc("SERIALVERSIONUID")»
        *
        * @generated
        */
        public static final long serialVersionUID = 1L;
    '''

    def private static hashCode(XEnumType it) '''
        /**
         * {@inheritDoc}
         *
         * @generated
         */
        @Override
        public int hashCode() {
            «IF identifierAttribute.datatype.primitive»
                return «identifierAttribute.datatypeUseWrappers».hashCode(«methodNameGetIdentifierAttribute»());
            «ELSE»
                return «methodNameGetIdentifierAttribute»().hashCode();
            «ENDIF»
        }
    '''

    def private static constructors(XEnumType it) '''
        «protectedConstructor»
        «publicConstructor»
    '''

    def private static publicConstructor(XEnumType it) '''
        /**
        * «localizedJDoc("CONSTRUCTOR", name)»
        *
        * @generated
        */
        public «method(name, constructorParameters)»{
          «fieldInitializations(indexFieldRequired, it)»
          }
    '''

    def private static protectedConstructor(XEnumType it) '''
        /**
        * «localizedJDoc("PROTECTED_CONSTRUCTOR", name)»
        *
        * @generated
        */
        protected «method(name, stringConstructorParameters)»{
            «stringFieldInitializations»
        }
    '''

    def private static stringFieldInitializations(XEnumType it) '''
        this.«varNameIndex» = «varNameIndex»;
        «FOR it : allAttributesWithField»
            this.«memberVarName» = «memberVarAssignmentFromStringParameter»;
        «ENDFOR»
    '''

    def private static equals(XEnumType it) '''
        /**
         * {@inheritDoc}
         *
         * @generated
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof «name») {
                «IF identifierAttribute.datatype.primitive»
                    return this.«methodNameGetIdentifierAttribute»() == ((«name») obj).«methodNameGetIdentifierAttribute»();
                «ELSE»
                    return this.«methodNameGetIdentifierAttribute»().equals(((«name») obj).«methodNameGetIdentifierAttribute»());
                «ENDIF»
            }
            return false;
        }
    '''

    def private static valuesConstant(XEnumType it) '''
        «IF !enumValues.isEmpty»
            /**
            * «localizedJDoc("CONSTANT_VALUES")»
            *
            * @generated
            */
            public static final «List_(name)» VALUES = «ListUtil».unmodifiableList(
            «FOR it : enumValues SEPARATOR  ", "»
                «memberVarNameLiteralNameAttribute»
            «ENDFOR»
            );

        «ENDIF»
    '''

    def private static enumValue(XEnumValue it) '''
        /**«localizedJDoc("ENUMVALUE")»
        * @generated
        */
        public static final «enumType.name» «field(memberVarNameLiteralNameAttribute)» = new «enumType.name»(«index»,
            «FOR it : enumAttributeValuesWithField SEPARATOR ", "»
                «memberVariableValue»
            «ENDFOR»
        );
    '''

}
