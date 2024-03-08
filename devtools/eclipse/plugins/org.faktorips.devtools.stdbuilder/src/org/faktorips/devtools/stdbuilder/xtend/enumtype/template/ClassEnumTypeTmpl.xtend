package org.faktorips.devtools.stdbuilder.xtend.enumtype.template

import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumValue
import org.faktorips.devtools.stdbuilder.xtend.template.CommonDefinitions

import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.enumtype.template.CommonEnumTypeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.enumtype.template.EnumAttributeExtensionTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

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
            «repo»
            «constructors»
            «getters»
            «toString(it)»
            «equals(it)»
            «hashCode(it)»
            «enumValueId»
            «compareTo»
            «writeToXmlMethod»
            «serialization»
        }
    '''

    def private static repo(XEnumType it) '''
        /**
         * @generated
         */
        private final «IRuntimeRepository» «varnameProductRepository»;
        
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
             *«localizedJDoc("METHOD_GET_ENUM_VALUE_BY_ID")»
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
        *«localizedJDoc("SERIALVERSIONUID")»
        *
        * @generated
        */
        public static final long serialVersionUID = 2L;
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
        *«localizedJDoc("CONSTRUCTOR", name)»
        *
        * @generated
        */
        public «method(name, constructorParameters)»{
          «fieldInitializations(indexFieldRequired, it)»
          this.«varnameProductRepository» = null;
        }
    '''

    def private static protectedConstructor(XEnumType it) '''
        /**
        *«localizedJDoc("PROTECTED_CONSTRUCTOR", name)»
        *
        * @generated
        */
        protected «method(name, stringConstructorParameters)»{
            «stringFieldInitializations»
            this.«varnameProductRepository» = «varnameProductRepository»;
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
            *«localizedJDoc("CONSTANT_VALUES")»
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

    def private static serialization(XEnumType it) '''
        /**
         * @generated
         */
        private Object writeReplace() {
          return new SerializationProxy(«identifierAttribute.memberVarName», getRepositoryLookup());
        }
        
        /**
         * @generated
         */
        private «IRuntimeRepositoryLookup» getRepositoryLookup() {
          if («varnameProductRepository» != null) {
            «IRuntimeRepositoryLookup» runtimeRepositoryLookup = «varnameProductRepository».getRuntimeRepositoryLookup();
            if (runtimeRepositoryLookup == null) {
              throw new «IllegalStateException»(
                  "For serialization of «name» instances you need to set an «IRuntimeRepositoryLookup» in your runtime repository.");
            }
            return runtimeRepositoryLookup;
          } else {
            return null;
          }
        }
        
        /**
         * @generated
         */
        private void readObject(@«SuppressWarnings»("unused") «ObjectInputStream» s) throws «IOException» {
          throw new «InvalidObjectException»("SerializationProxy required");
        }
        
        /**
         * @generated
         */
        private static class SerializationProxy implements «Serializable» {
          private static final long serialVersionUID = 1L;
        
          private final «identifierAttribute.datatypeNameForConstructor» «identifierAttribute.memberVarName»;
          private final «IRuntimeRepositoryLookup» runtimeRepositoryLookup;
        
          /**
         * @generated
         */
          SerializationProxy(«identifierAttribute.datatypeNameForConstructor» «identifierAttribute.memberVarName», «IRuntimeRepositoryLookup» runtimeRepositoryLookup) {
            this.«identifierAttribute.memberVarName» = «identifierAttribute.memberVarName»;
            this.runtimeRepositoryLookup = runtimeRepositoryLookup;
          }
        
          /**
         * @generated
         */
          private Object readResolve() {
            return runtimeRepositoryLookup.getRuntimeRepository().getEnumValue(«name».class, «identifierAttribute.memberVarName»);
          }
        }
    '''
    def package static writeToXmlMethod(XEnumType it) '''
        «IF generateToXmlSupport»
            /**
             *«inheritDoc»
             *
             * @generated
             */
            @Override
            public void «writePropertiesToXml(Element + " element")» {
                «FOR attribute : it.allAttributesWithoutLiteralName»
                    «ValueToXmlHelper».«attribute.addToElement(attribute.getToStringExpression(attribute.memberVarName) , "element", IpsEnumToXmlWriter+".XML_ELEMENT_ENUMATTRIBUTEVALUE")»;
                «ENDFOR»
            }
        «ENDIF»
    '''

}
