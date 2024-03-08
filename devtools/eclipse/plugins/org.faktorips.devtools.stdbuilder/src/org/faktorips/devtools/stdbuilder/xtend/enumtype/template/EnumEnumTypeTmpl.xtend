package org.faktorips.devtools.stdbuilder.xtend.enumtype.template

import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumAttribute
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumValue

import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.enumtype.template.CommonEnumTypeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import org.faktorips.devtools.stdbuilder.xtend.template.CommonDefinitions

class EnumEnumTypeTmpl {

    def package static String body(XEnumType it) '''

        /**
        «IF described»
            *  «description»
        «ENDIF»
        «getAnnotations(ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(ENUM_CLASS)»
        public enum «name» «CommonDefinitions::implementedInterfaces(it)»{

            «enumValues(it)»

            «messageHelperVar»
            «staticIdMap»
            «initKeyMaps»

            «fields»
            «constructors»

            «FOR attribute : allUniqueAttributesWithoutLiteralName» «methodGetValueBy(attribute)» «ENDFOR»
            «FOR attribute : allUniqueAttributesWithoutLiteralName» «methodGetExistingValueBy(attribute)» «ENDFOR»
            «FOR attribute : allUniqueAttributesWithoutLiteralName» «methodIsValueBy(attribute)» «ENDFOR»


            «getters(it)»
            «toString(it)»
        }
    '''

    def private static enumValues(XEnumType it) '''
        «FOR it : enumValues SEPARATOR ",\n"»
            «enumValue»
        «ENDFOR»
        ;
    '''

    def private static enumValue(XEnumValue it) '''
        /**
        * @generated
        */
        «literalNameAttributeValue.field(memberVarNameLiteralNameAttribute)»(
            «FOR it : enumAttributeValuesWithField SEPARATOR ", "»
                «memberVariableValue»
            «ENDFOR»
        )
    '''

    def private static staticIdMap(XEnumType it) '''
        /**
        *«localizedJDoc("ID_MAP")»
        *
        * @generated
        */
        «val keyClassName = identifierAttribute.datatypeNameUseWrappers»
        private static final «Map(keyClassName, unqualifiedClassName)» «varNameIdMap»;
    '''

    def private static initKeyMaps(XEnumType it) '''
        /**
        *«localizedJDoc("STATIC")»
        *
        * @generated
        */
        static{
            «varNameIdMap» = new «HashMap»();
            for(«name» value : values()){
                «varNameIdMap».put(value.«identifierAttribute.memberVarName», value);
            }
        }
    '''

    def private static constructors(XEnumType it) '''
        /**
        *«localizedJDoc("CONSTRUCTOR", name)»
        *
        * @generated
        */
        private «method(name, constructorParameters)»{
            «fieldInitializations(false, it)»
        }
    '''

    def private static methodGetValueBy(XEnumType enumType, XEnumAttribute it) '''
        /**
        *«localizedJDoc("METHOD_GET_VALUE_BY_XXX", memberVarName)»
        *
        * @generated
        */
        public static final «enumType.name» «IF multilingual»«method(methodNameGetValueBy, datatypeName, memberVarName, Locale, "locale")»«ELSE»«method(methodNameGetValueBy, datatypeName, memberVarName)»«ENDIF»{
            «IF identifier»
                return «enumType.varNameIdMap».get(«memberVarName»);
            «ELSE»
                for(«enumType.name» currentValue : values()){
                    if(
                    currentValue.
                    «IF multilingual»
                        «methodNameGetter»(locale).equals(«memberVarName»)
                    «ELSE»
                        «memberVarName» «equals(memberVarName,it)»
                    «ENDIF»
                    ){
                        return currentValue;
                    }
                }
                return null;
            «ENDIF»
        }
    '''
    
     def private static methodGetExistingValueBy(XEnumType enumType, XEnumAttribute it) '''
        /**
        *«localizedJDoc("METHOD_GET_EXISTING_VALUE_BY_XXX", memberVarName)»
        *
        * @throws IllegalArgumentException «localizedText("METHOD_GET_EXISTING_VALUE_BY_XXX_EXCEPTION_JAVADOC")»
        *
        * @generated
        */
        public static final «enumType.name» «IF multilingual»«method(methodNameGetExistingValueBy, datatypeName, memberVarName, Locale, "locale")»«ELSE»«method(methodNameGetExistingValueBy, datatypeName, memberVarName)»«ENDIF»{
            «IF identifier»
                if(«enumType.varNameIdMap».containsKey(«memberVarName»)) {
                    return «enumType.varNameIdMap».get(«memberVarName»);
                } else {
                    throw new IllegalArgumentException("No enum value with «memberVarName» " + «memberVarName»);
                }
            «ELSE»
                for(«enumType.name» currentValue : values()){
                    if(
                    currentValue.
                    «IF multilingual»
                        «methodNameGetter»(locale).equals(«memberVarName»)
                    «ELSE»
                        «memberVarName» «equals(memberVarName,it)»
                    «ENDIF»
                    ){
                        return currentValue;
                    }
                }
                throw new IllegalArgumentException("No enum value with «memberVarName» " + «memberVarName»);
            «ENDIF»
        }
    '''

    def private static equals(String varName, XEnumAttribute it) '''
        «IF datatype.primitive»==«varName»«ELSE».equals(«varName»)«ENDIF»
    '''

    def private static methodIsValueBy(XEnumType enumType, XEnumAttribute it) '''
        /**
        *«localizedJDoc("METHOD_IS_VALUE_BY_XXX")»
        *
        * @generated
        */
        public static final boolean «IF multilingual»«method(methodNameIsValueBy, datatypeName, memberVarName, Locale, "locale")»«ELSE»«method(methodNameIsValueBy, datatypeName, memberVarName)»«ENDIF»{
            «IF multilingual»
                return «methodNameGetValueBy»(«memberVarName», locale) != null;
            «ELSE»
                return «methodNameGetValueBy»(«memberVarName») != null;
            «ENDIF»
        }
    '''

}
