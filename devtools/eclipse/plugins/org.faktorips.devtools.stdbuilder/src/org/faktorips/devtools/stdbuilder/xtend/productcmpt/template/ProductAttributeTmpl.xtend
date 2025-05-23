package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAttribute

import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAttributeExtensionTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType

class ProductAttributeTmpl {

    def package static constantForPropertyName (XProductAttribute it) '''
            /**
             *«localizedJDoc("FIELD_PROPERTY_NAME", name)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
            public static final String «field(constantNamePropertyName)» = "«name»";
    '''

  def package static constantForValueSet(XProductAttribute it) '''
    «IF !abstract»
      /**
       *«localizedJDoc(getJavadocKey("FIELD_MAX"), name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES)»
      public static final «getValueSetJavaClassName(GenerateValueSetType.GENERATE_BY_TYPE)» «field(constantNameValueSet)» = «valuesetCode»;
    «ENDIF»
  '''
  
    def package static constantForDefaultValue(XProductAttribute it) '''
    «IF !abstract»
      /**
       *«localizedJDoc("FIELD_DEFAULTVALUE_CONSTANT", name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT)»
      public static final «javaClassName» «field(constantNameDefaultValue)» = «defaultValueCode»;
    «ENDIF»
  '''

    def package static memberField (XProductAttribute it) '''
            /**
             *«localizedJDoc("FIELD_VALUE", name.toFirstUpper)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotations(PRODUCT_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD)»
            private «javaClassName» «field(fieldName)» = «constantNameDefaultValue»;
    '''

    def package static abstractGetter (XProductAttribute it) '''
            /**
             *«inheritDocOrJavaDocIf(genInterface, "METHOD_GETVALUE", name, descriptionForJDoc)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
             «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface)»
             «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overwrite)»
            public abstract «returnType» «method(methodNameGetter)»;
    '''

    def package static getterSetter (XProductAttribute it) '''
        «getter»
        «setter»
        «internalSetter»
    '''

    def package static setDefaultValue (XProductAttribute it) '''
        «IF callSetDefaultValue»
            «methodNameSetterInternal»(«defaultValueCode»);
        «ENDIF»
    '''

    def package static getter (XProductAttribute it) '''
            /**
             *«inheritDocOrJavaDocIf(genInterface, "METHOD_GETVALUE", name, descriptionForJDoc)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface)»
            «IF (!genInterface && generatePublishedInterfaces) || overwrite»@Override«ENDIF»
            public «returnType» «method(methodNameGetter)»
            «IF genInterface»;«ELSE»
            {
                return «getReferenceOrSafeCopyIfNecessary(fieldName)»;
            }
            «ENDIF»
            «IF multilingual»
                «multilingualGetter»
            «ENDIF»
    '''

    def package static getterWithSuper (XProductAttribute it) '''
            /**
             *«inheritDocOrJavaDocIf(genInterface, "METHOD_GETVALUE", name, descriptionForJDoc)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface)»
            @Override
            public «returnType» «method(methodNameGetter)»
            «IF genInterface»;«ELSE»
            {
                return super.«method(methodNameGetter)»;
            }
            «ENDIF»
    '''

    def private static multilingualGetter (XProductAttribute it) '''
        /**
         *«inheritDocOrJavaDocIf(genInterface, "METHOD_GETVALUE_MULTILINGUAL", name, descriptionForJDoc)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overwrite)»
        «IF multiValue»
            public «List_("String")» «method(methodNameGetter, Locale, "locale")»
            «IF genInterface»;«ELSE»
            {
                «List_("String")» result = new «ArrayList»();
                for («DefaultInternationalString» internationalString : «fieldName») {
                    result.add(internationalString.get(locale));
                }
                return result;
            }
            «ENDIF»
        «ELSE»
            public String «method(methodNameGetter, Locale, "locale")»
            «IF genInterface»;«ELSE»
            {
                return «fieldName».get(locale);
            }
            «ENDIF»
        «ENDIF»
    '''

    def private static setter (XProductAttribute it) '''
            /**
             *«localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_SETTER, genInterface)»
            public void «method(methodNameSetter, javaClassName, "newValue")»{
                «checkRepositoryModifyable»
                «methodNameSetterInternal»(«getReferenceOrSafeCopyIfNecessary("newValue")»);
            }
    '''

    def private static internalSetter (XProductAttribute it) '''
        «IF (!genInterface)»
            /**
             *«localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
            protected final void «method(methodNameSetterInternal, javaClassName, "newValue")»{
                this.«fieldName» = newValue;
            }
        «ENDIF»
    '''

    def package static initFromXmlMethodCall (XProductAttribute it) '''
            «methodNameDoInitFromXml»(configMap);
    '''

    def package static initFromXmlMethod (XProductAttribute it) '''
            /**
             * @generated
             */
            private void «method(methodNameDoInitFromXml, Map("String", Element), "configMap")» {
                Element configElement = configMap.get(«constantNamePropertyName»);
                if (configElement != null) {
                «IF multiValue»
                    «initMultiValueAttributeFromXml»
                «ELSE»
                    «xmlValueType» value = «ValueToXmlHelper».«getFromElement("configElement", XML_TAG_VALUE)»;
                       this.«fieldName» = «getNewInstanceFromExpression("value", "getRepository()")»;
                «ENDIF»
                }
            }
    '''

    def private static initMultiValueAttributeFromXml (XProductAttribute it) '''
        «IF multiValueDirectXmlHandling»
            «javaClassName» valueList = «MultiValueXmlHelper».«getMultiValueFromXML("configElement")»;
        «ELSE»
            «javaClassName» valueList = «newMultiValueInstance»;
            «val singleVal = singleValueOfMultiValueAttribute»
                «List_("String")» stringList = «MultiValueXmlHelper».«getValuesFromXML("configElement")»;
                for (String stringValue : stringList) {
                    «singleVal.javaClassName» convertedValue = «singleVal.getNewInstanceFromExpression("stringValue", "getRepository()")»;
                    valueList.add(convertedValue);
                }
        «ENDIF»
        this.«fieldName» = valueList;
    '''

    def package static writeAttributeToXmlMethodCall (XProductAttribute it) '''
            «methodNameWriteToXml»(element);
    '''

    def package static writeAttributeToXmlMethod (XProductAttribute it) '''
            /**
             * @generated
             */
            private void «method(methodNameWriteToXml, Element, "element")» {
                Element attributeElement = «ValueToXmlHelper».«deleteExistingElementAndCreateNewElement("element", XML_TAG_ATTRIBUTE_VALUE, constantNamePropertyName)»;
                «IF multiValue»
                    «IF multiValueDirectXmlHandling»
                        «MultiValueXmlHelper».«addMultiValueToElement("attributeElement", "this." + fieldName)»;
                    «ELSE»
                        «val singleVal = singleValueOfMultiValueAttribute»
                        «List_("String")» stringList«singleVal.fieldName.toFirstUpper» = new «ArrayList»();
                        for («singleVal.javaClassName» value : this.«singleVal.fieldName») {
                            String stringValue = «singleVal.getToStringExpression("value")»;
                            stringList«singleVal.fieldName.toFirstUpper».add(stringValue);
                        }
                        «MultiValueXmlHelper».«addValuesToElement("attributeElement", "stringList" + fieldName.toFirstUpper)»;
                    «ENDIF»
                «ELSE»
                       «ValueToXmlHelper».«addToElement(getToStringExpression(fieldName), "attributeElement", XML_TAG_VALUE)»;
                «ENDIF»
                element.appendChild(attributeElement);
            }
    '''
}