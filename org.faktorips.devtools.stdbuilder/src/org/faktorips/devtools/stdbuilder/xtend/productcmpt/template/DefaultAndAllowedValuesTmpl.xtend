package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductClass
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.GenerateValueSetTypeRule
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.AllowedValuesForAttributeRule

class DefaultAndAllowedValuesTmpl {

    def package static defaultAndAllowedValuesFields (XPolicyAttribute it) '''
        «IF generateGetAllowedValuesForAndGetDefaultValue»
            «defaultField»
            «allowedValueSetField»
        «ENDIF»
    '''

    def private static defaultField (XPolicyAttribute it) '''
        /**
         * «localizedJDoc("FIELD_DEFAULTVALUE", name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(DEPRECATION)»
        private «javaClassName» «field(fieldNameDefaultValue)» = «defaultValueCode»;
    '''

    def private static allowedValueSetField (XPolicyAttribute it) '''
        /**
         * «localizedJDoc(getJavadocKey("FIELD"), name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(DEPRECATION)»
        private «valueSetJavaClassName» «field(fieldNameValueSet)»«IF generateConstantForValueSet» = «IF generatePublishedInterfaces && published»«policyCmptNode.interfaceName»«ELSE»«policyCmptNode.implClassName»«ENDIF».«constantNameValueSet»«ENDIF»;
    '''

    def package static getterAndSetter (XPolicyAttribute it) '''
        «IF generateGetAllowedValuesForAndGetDefaultValue»
            «getterDefaultValue»
            «setterDefaultValue»
            «FOR rule : AllowedValuesForAttributeRule.getGenerateValueSetTypeRulesFor(it)»
                «getterAllowedValues(it, rule)»
            «ENDFOR»
            «setterAllowedValues»
        «ENDIF»
    '''

    def package static allowedValuesMethodForNotOverriddenAttributesButDifferentUnifyValueSetSettings(XProductClass it, XPolicyAttribute attributeSuperType, GenerateValueSetType valueSetMethods) '''
        /**
         * «localizedText("OVERRIDE_UNIFY_METHODS_JAVADOC")»
         *«IF attributeSuperType.isDeprecatedGetAllowedValuesMethodForNotOverrideAttributesButDifferentUnifyValueSetSettings(valueSetMethods)» @deprecated «localizedText("DEPRECATED_UNIFY_METHODS_JAVADOC")»«ENDIF»
         * @generated
         */
        «overrideAnnotationForPublishedMethodImplementation»
        «IF attributeSuperType.isDeprecatedGetAllowedValuesMethodForNotOverrideAttributesButDifferentUnifyValueSetSettings(valueSetMethods)»@Deprecated«ENDIF»
        public «attributeSuperType.valueSetJavaClassName» «method(attributeSuperType.getMethodNameGetAllowedValuesFor(valueSetMethods), attributeSuperType.getAllowedValuesMethodParameterSignature(valueSetMethods))» 
        «IF genInterface() || isAbstract()»;«ELSE» {
          return super.«attributeSuperType.getMethodNameGetAllowedValuesFor(valueSetMethods.inverse)»(«attributeSuperType.allowedValuesMethodParameter(valueSetMethods, valueSetMethods.inverse)»);
        }
        «ENDIF»
    '''

    def private static getterDefaultValue (XPolicyAttribute it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, "METHOD_GET_DEFAULTVALUE", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT, genInterface)»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overrideGetDefaultValue && overwrittenAttribute.productRelevantInHierarchy)»
        public «IF isAbstract»abstract «ENDIF»«javaClassName» «method(methodNameGetDefaultValue)»
        «IF genInterface || isAbstract»;«ELSE»
        {
            return «fieldNameDefaultValue»;
        }
        «ENDIF»
    '''
    
    def private static setterDefaultValue (XPolicyAttribute it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, "METHOD_SET_DEFAULTVALUE", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT_SETTER, genInterface)»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overrideGetDefaultValue && overwrittenAttribute.productRelevantInHierarchy)»
        public «IF isAbstract»abstract «ENDIF»void «method(methodNameSetDefaultValue, javaClassName, fieldNameDefaultValue)»
        «IF genInterface || isAbstract»;«ELSE»
        {
            «checkRepositoryModifyable»
            this.«fieldNameDefaultValue» = «fieldNameDefaultValue»;
        }
        «ENDIF»
    '''

    def private static getterAllowedValues (XPolicyAttribute it, GenerateValueSetTypeRule rule) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, getJavadocKey("METHOD_GET"), name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *«IF isGetAllowedValuesMethodDeprecated(rule)» @deprecated «localizedText("DEPRECATED_UNIFY_METHODS_JAVADOC")»«ENDIF»
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES, genInterface)»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, isConditionForOverrideAnnotation(rule) && overwrittenAttribute.productRelevantInHierarchy)»
        «IF isGetAllowedValuesMethodDeprecated(rule)»@Deprecated«ENDIF»
        public «IF isAbstract»abstract «ENDIF»«valueSetJavaClassName» «method(getMethodNameGetAllowedValuesFor(rule.fromMethod), getAllowedValuesMethodParameterSignature(rule.fromMethod))»
        «IF genInterface || isAbstract»;«ELSE»
        {
            return «IF rule.fromMethod.generateUnified && generateBothMethodsToGetAllowedValues»«getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_BY_TYPE)»(«allowedValuesMethodParameter(rule.fromMethod, GenerateValueSetType.GENERATE_BY_TYPE)»)«ELSE»«fieldNameValueSet»«ENDIF»;
        }
        «ENDIF»
    '''

    def private static setterAllowedValues (XPolicyAttribute it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, "METHOD_SET_VALUESET", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES_SETTER, genInterface)»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overrideSetAllowedValuesFor && overwrittenAttribute.productRelevantInHierarchy)»
        public «IF isAbstract»abstract «ENDIF»void «method(methodNameSetAllowedValuesFor, ValueSet(javaClassUsedForValueSet), fieldNameValueSet)»
        «IF genInterface || isAbstract»;«ELSE»
        {
            «checkRepositoryModifyable»
            this.«fieldNameValueSet» = «castFromTo(ValueSet(javaClassUsedForValueSet),valueSetJavaClassName)»«fieldNameValueSet»;
        }
        «ENDIF»
    '''

    def package static initFromXmlMethodCall (XPolicyAttribute it) '''
        «IF generateGetAllowedValuesForAndGetDefaultValue»
            «methodNameDoInitFromXml»(configMap);
        «ENDIF»
    '''

    def package static initFromXmlMethod (XPolicyAttribute it) '''
        «IF generateGetAllowedValuesForAndGetDefaultValue»
            /**
             * @generated
             */
             private void «method(methodNameDoInitFromXml, Map("String", Element), "configMap")» {
             Element defaultValueElement = configMap.get(«CONFIGURED_DEFAULT_PREFIX»+«policyCmptNode.implClassName».«constantNamePropertyName»);
            if (defaultValueElement != null) {
                String value = «ValueToXmlHelper».«getValueFromElement("defaultValueElement")»;
                «fieldNameDefaultValue» = «getNewInstanceFromExpression("value", "getRepository()")»;
            }
            Element valueSetElement = configMap.get(«CONFIGURED_VALUE_SET_PREFIX»+«policyCmptNode.implClassName».«constantNamePropertyName»);
            if (valueSetElement != null) {
                «IF valueSetUnrestricted»
                    «IF ipsEnum»
                           «UnrestrictedValueSet("?")» unrestrictedValueSet = «ValueToXmlHelper()».«getUnrestrictedValueSet("valueSetElement", XML_TAG_VALUE_SET())»;
                           «fieldNameValueSet» = «newEnumValueSetInstance(getAllEnumValuesCode("getRepository()"), "unrestrictedValueSet.containsNull()")»;
                    «ELSE»
                        «fieldNameValueSet» = «ValueToXmlHelper()».«getUnrestrictedValueSet("valueSetElement", XML_TAG_VALUE_SET)»;
                    «ENDIF»
                «ENDIF»
                «IF valueSetStringLength»
                	«StringLengthValueSet» stringLengthValueSet = «ValueToXmlHelper».«getStringLengthValueSetFromElement("valueSetElement", XML_TAG_VALUE_SET)»;
                	if (stringLengthValueSet != null) {
                		«fieldNameValueSet» = stringLengthValueSet;
                		return;
                	}
                «ENDIF»
                «IF valueSetEnum || ((valueSetUnrestricted || valueSetStringLength) && enumValueSetSupported)»
                    «EnumValues» values = «ValueToXmlHelper».«getEnumValueSetFromElement("valueSetElement", XML_TAG_VALUE_SET)»;
                    if (values != null) {
                        «List_(javaClassUsedForValueSet)» enumValues = new «ArrayList»();
                        for (int i = 0; i < values.«getNumberOfValues()»; i++) {
                            enumValues.add(«getValueSetNewInstanceFromExpression("values.getValue(i)", "getRepository()")»);
                        }
                        «fieldNameValueSet» = «newEnumValueSetInstance("enumValues", "values
                                .containsNull()")»;
                    }
                «ENDIF»
                «IF valueSetRange || (valueSetUnrestricted && rangeSupported)»
                    «Range» range = «ValueToXmlHelper».«getRangeFromElement("valueSetElement", XML_TAG_VALUE_SET)»;
                    if (range != null) {
                        if (range.isEmpty()) {
                          «fieldNameValueSet» = «addImport(getValuesetDatatypeHelper.getRangeJavaClassName(false))».empty();
                        } else {
                          «fieldNameValueSet» = «getNewRangeExpression("range.getLower()", "range
                                .getUpper()", "range.getStep()", "range.containsNull()")»;
                        }
                    }
                «ENDIF»
            }
        }
         «ENDIF»
    '''

    def package static writeAttributeToXmlMethodCall (XPolicyAttribute it) '''
        «IF generateGetAllowedValuesForAndGetDefaultValue»
            «methodNameWriteToXml»(element);
        «ENDIF»
    '''

    def package static writeAttributeToXmlMethod (XPolicyAttribute it) '''
        «IF generateGetAllowedValuesForAndGetDefaultValue»
            /**
             * @generated
             */
            private void «method(methodNameWriteToXml, Element, "element")» {
                Element defaultValueElement = «ValueToXmlHelper».«addValueAndReturnElement(toStringExpression, "element", XML_TAG_CONFIGURED_DEFAULT)»;
                defaultValueElement.setAttribute(«XML_ATTRIBUTE_ATTRIBUTE», «policyCmptNode.implClassName».«constantNamePropertyName»);

                Element configuredValueSetElement = element.getOwnerDocument().createElement(«XML_TAG_CONFIGURED_VALUE_SET»);
                Element valueSetElement = element.getOwnerDocument().createElement(«XML_TAG_VALUE_SET»);
                «IF valueSetUnrestricted»
                    if («fieldNameValueSet» instanceof «UnrestrictedValueSet("?")») {
                        Element valueElement = element.getOwnerDocument().createElement(«XML_TAG_ALL_VALUES»);
                        valueElement.setAttribute(«XML_ATTRIBUTE_CONTAINS_NULL», Boolean.toString(«fieldNameValueSet».«containsNull»));
                        valueSetElement.appendChild(valueElement);
                    }
                «ENDIF»
                «IF valueSetUnrestricted && rangeSupported»
                    if («fieldNameValueSet» instanceof «qnameRange("?")») {
                        «qnameRange(javaClassQualifiedNameUsedForValueSet)» range = («qnameRange(javaClassQualifiedNameUsedForValueSet)»)«fieldNameValueSet»;
                        «writeRange("range", it)»
                    }
                «ELSEIF valueSetRange»
                        «writeRange(fieldNameValueSet, it)»
                «ENDIF»
                «IF valueSetUnrestricted && enumValueSetSupported»
                    if («fieldNameValueSet» instanceof «OrderedValueSet("?")») {
                        «writeEnumValueSet»
                    }
                «ELSEIF valueSetEnum»
                           «writeEnumValueSet»
                «ENDIF»
                configuredValueSetElement.setAttribute(«XML_ATTRIBUTE_ATTRIBUTE», «policyCmptNode.implClassName».«constantNamePropertyName»);
                configuredValueSetElement.appendChild(valueSetElement);
                element.appendChild(configuredValueSetElement);
            }
        «ENDIF»
    '''


    def private static writeRange(String rangeVar, XPolicyAttribute it) '''
        Element valueSetValuesElement = element.getOwnerDocument().createElement(«XML_TAG_RANGE»);
        valueSetValuesElement.setAttribute(«XML_ATTRIBUTE_CONTAINS_NULL», Boolean.toString(«fieldNameValueSet».«containsNull»));
        valueSetValuesElement.setAttribute(«XML_ATTRIBUTE_EMPTY», Boolean.toString(«fieldNameValueSet».«isEmpty»));
        «ValueToXmlHelper».«addValueToElement(getToStringExpression(rangeVar + ".getLowerBound()"), "valueSetValuesElement", XML_TAG_LOWER_BOUND)»;
        «ValueToXmlHelper».«addValueToElement(getToStringExpression(rangeVar + ".getUpperBound()"), "valueSetValuesElement", XML_TAG_UPPER_BOUND)»;
        «ValueToXmlHelper».«addValueToElement(getToStringExpression(rangeVar + ".getStep()"), "valueSetValuesElement", XML_TAG_STEP)»;
        valueSetElement.appendChild(valueSetValuesElement);
    '''

    def private static writeEnumValueSet (XPolicyAttribute it) '''
        Element valueSetValuesElement = element.getOwnerDocument().createElement(«XML_TAG_ENUM»);
        for («javaClassQualifiedName» value : «fieldNameValueSet».getValues(false)) {
            Element valueElement = element.getOwnerDocument().createElement(«XML_TAG_VALUE»);
            «ValueToXmlHelper».«addValueToElement(getToStringExpression("value"),"valueElement", XML_TAG_DATA)»;
            valueSetValuesElement.appendChild(valueElement);
        }
        valueSetElement.appendChild(valueSetValuesElement);
    '''

}