package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.naming.BuilderAspect
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.XMethod
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType

import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAttributeExtensionTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.GenerateValueSetTypeRule
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter

class PolicyCmptAttributeTmpl {

  def package static constantForPropertyName(XPolicyAttribute it) '''
    «IF !overwrite»
      /**
       * «localizedJDoc("FIELD_PROPERTY_NAME", name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
      public static final String «field(constantNamePropertyName)» = "«name»";
    «ENDIF»
  '''

  def package static constantForValueSet(XPolicyAttribute it) '''
    «IF generateConstantForValueSet»
      /**
       * «localizedJDoc(getJavadocKey("FIELD_MAX"), name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
      public static final «valueSetJavaClassName» «field(constantNameValueSet)» = «valuesetCode»;
    «ENDIF»
  '''
  
    def package static constantForDefaultValue(XPolicyAttribute it) '''
    «IF generateField»
      /**
       * «localizedJDoc("FIELD_DEFAULTVALUE_CONSTANT", name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT)»
      public static final «javaClassName» «field(constantNameDefaultValue)» = «defaultValueCode»;
    «ENDIF»
  '''

  def package static constantField(XPolicyAttribute it) '''
    «IF constant»
      /**
       * «localizedJDoc("FIELD_ATTRIBUTE_CONSTANT", name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotationsForPublishedInterface(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface())»
      public static final «javaClassName» «field(fieldName)» = «defaultValueCode»;
    «ENDIF»
  '''

  def package static memberField(XPolicyAttribute it) '''
    «IF generateField»
      /**
       * «localizedJDoc("FIELD_ATTRIBUTE_VALUE", name)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD)»
      «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD)»
      private «javaClassName» «field(fieldName)» = «constantNameDefaultValue»;
    «ENDIF»
  '''

  def package static abstractGetter(XPolicyAttribute it) '''
    /**
     * «inheritDocOrJavaDocIf(genInterface, "METHOD_GETVALUE", name, descriptionForJDoc)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @generated
     */
     «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface())»
     «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overwrite)»
    public abstract «javaClassName» «method(methodNameGetter)»;
  '''

  def package static abstractSetter(XPolicyAttribute it) '''
    «IF isGenerateSetter()»
      /**
       * «inheritDocOrJavaDocIf(genInterface, "METHOD_SETVALUE", name, descriptionForJDoc)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
       «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_SETTER, genInterface())»
       «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overwrite)»
      public abstract void «method(methodNameSetter, javaClassName, "newValue")»;
    «ENDIF»
  '''

  def private static changesDatatype(XPolicyAttribute it) {
    if(datatype instanceof EnumTypeDatatypeAdapter && overwrittenAttribute.datatype instanceof EnumTypeDatatypeAdapter) {
      return (datatype as EnumTypeDatatypeAdapter).enumType != (overwrittenAttribute.datatype as EnumTypeDatatypeAdapter).enumType
    }
    return datatype != overwrittenAttribute.datatype;
  }

  def package static getter(XPolicyAttribute it) '''
    «IF isGenerateGetter(genInterface())»
      /**
       * «inheritDocOrJavaDocIf(genInterface(), "METHOD_GETVALUE", name, descriptionForJDoc)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * «IF !genInterface()&&!generateField&&(generateDefaultForOnTheFlyDerivedAttribute||!overwrite)»@restrainedmodifiable«ELSE»@generated«ENDIF»
       */
      «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface())»
      «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overwrite)»
      public «javaClassName» «method(methodNameGetter)»
      «IF genInterface()»;«ELSE» {
          «IF generateField»
            return «IF constant»«fieldName»«ELSE»«getReferenceOrSafeCopyIfNecessary(fieldName)»«ENDIF»;
          «ELSE»
            «IF generateDefaultForOnTheFlyDerivedAttribute»
              // begin-user-code
              return «defaultValueCode»;
              // end-user-code
            «ELSEIF overwrite»
              return «IF changesDatatype»(«javaClassName») «ENDIF»super.«methodNameGetter»();
            «ELSE»
              «IF changingOverTime»
                «getPolicyCmptNode.productCmptGenerationNode.implClassName» productCmpt = «getPropertyValueContainer(false)»;
              «ELSE»
                «getPolicyCmptNode.productCmptNode.implClassName» productCmpt = «getPropertyValueContainer(false)»;
              «ENDIF»
              // begin-user-code
              if (productCmpt == null) {
                  «IF datatype.isPrimitive»
                    return «datatype.defaultValue»;
                  «ELSE»
                    return null;
                  «ENDIF»
              }
              «IF !formulaSignature.parameters.isEmpty»
                «localizedComment("COMMENT_DERIVED_ATTRIBUTE_METHOD_CALL")»
              «ENDIF»
              «formulaParameterDeclaration(formulaSignature)»
              return productCmpt.«formulaCall(formulaSignature)»;
              // end-user-code
            «ENDIF»
          «ENDIF»
      }
      «ENDIF»
    «ENDIF»
  '''

  def package static setter(XPolicyAttribute it) '''
    «IF generateSetter && !(genInterface() && isDerived)»
      /**
       * «inheritDocOrJavaDocIf(genInterface()||isDerived, "METHOD_SETVALUE", name, descriptionForJDoc)»«IF generateChangeSupport && (!generatePublishedInterfaces || genInterface())» «inheritDocOrJavaDocIf(genInterface(), "METHOD_SETVALUE_LISTENERS", name, descriptionForJDoc)»«ENDIF»
       «IF overwriteAbstract»
         *
         * «localizedJDoc("OVERWRITTEN_ABSTRACT_SETTER_PARAM", javaClassName, addImport(ClassCastException))»
         *
         * «localizedJDoc("OVERWRITTEN_ABSTRACT_SETTER_THROWS", addImport(ClassCastException), javaClassName)»
       «ENDIF»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_SETTER, genInterface()||isDerived)»
      «IF !(isDerived && (!overwrite || overwriteAbstract))»«overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overwrite && !attributeTypeChangedByOverwrite)»«ENDIF»
      public void «IF overwriteAbstract»«method(methodNameSetter, overwrittenAttribute.javaClassName, "newValue")»«ELSE»«method(methodNameSetter, javaClassName, "newValue")»«ENDIF»
      «IF genInterface()»;«ELSE» {
          «PropertyChangeSupportTmpl.storeOldValue(it)»
          «IF generateSetterInternal»
            «methodNameSetterInternal»(«IF overwriteAbstract»(«javaClassName»)«ENDIF»newValue);
          «ELSE»
            this.«fieldName» = «IF overwriteAbstract»(«javaClassName»)«ENDIF»«getReferenceOrSafeCopyIfNecessary("newValue")»;
          «ENDIF»
          «PropertyChangeSupportTmpl.notify(it)»
      }
      «ENDIF»
    «ENDIF»
  '''

  def package static setterInternal(XPolicyAttribute it) '''
    «IF generateSetterInternal»
      /**
       * «localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
      «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       *
       * @generated
       */
      «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
      protected final void «method(methodNameSetterInternal, javaClassName, "newValue")»
      {
          this.«fieldName» = «getReferenceOrSafeCopyIfNecessary("newValue")»;
      }
    «ENDIF»
  '''

  def package static allowedValuesMethod(XPolicyAttribute it, GenerateValueSetTypeRule rule) '''
    «IF generateGetAllowedValuesForAndGetDefaultValue»
      /**
       «IF isOverwritingValueSetWithMoreConcreteType(rule.fromMethod) && !isValueSetDerived»
       * «inheritDoc»
       «ELSE»
       * «inheritDocOrJavaDocIf(genInterface(), getJavadocKey("METHOD_GET"), name, descriptionForJDoc)»
       «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
       «ENDIF»
       *«IF isGetAllowedValuesMethodDeprecated(rule)» @deprecated «localizedText("DEPRECATED_UNIFY_METHODS_JAVADOC")»«ENDIF»
       *«IF isValueSetDerived»@restrainedmodifiable«ELSE»@generated«ENDIF»
       */
      «IF isPublishedInterfaceModifierRelevant(rule)»
         «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES, genInterface())»
      «ELSE»
          «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
      «ENDIF»
      «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, isConditionForOverrideAnnotation(rule))»
      «IF isGetAllowedValuesMethodDeprecated(rule)»@Deprecated«ENDIF»
      public «IF isAbstract()»abstract «valueSetJavaClassNameWithWildcard»«ELSE»«valueSetJavaClassName»«ENDIF» «method(getMethodNameGetAllowedValuesFor(rule.fromMethod), getAllowedValuesMethodParameterSignature(rule.fromMethod))»
      «IF genInterface() || isAbstract()»;«ELSE» {
          «IF productRelevant && !rule.isDelegate»
            return «getPropertyValueContainer(published)».«getMethodNameGetAllowedValuesFor(rule.fromMethod)»(«allowedValuesMethodParameter(rule.fromMethod, rule.fromMethod)»);
          «ELSEIF isValueSetDerived»
            // begin-user-code
            «IF rule.fromMethod.generateUnified && generateBothMethodsToGetAllowedValues»
              return «getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_BY_TYPE)»(null);
            «ELSEIF overwritingAttributeWithDifferentValueSetTypeAndGenerateValueSetType && !genInterface»
              return super.«overwrittenAttribute.getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_BY_TYPE)»(«overwrittenAttribute.allowedValuesMethodParameter(rule.fromMethod, GenerateValueSetType.GENERATE_BY_TYPE)»);
            «ELSEIF overwritingValueSetWithDerived && !genInterface && !rule.isDelegate»
              return super.«overwrittenAttribute.getMethodNameGetAllowedValuesFor(rule.fromMethod)»(«overwrittenAttribute.allowedValuesMethodParameter(rule.fromMethod, rule.fromMethod)»);
            «ELSEIF overwritingValueSetWithDerived && !genInterface && rule.isDelegate»
              return super.«overwrittenAttribute.getMethodNameGetAllowedValuesFor(rule.fromMethod.inverse)»(«overwrittenAttribute.allowedValuesMethodParameter(rule.fromMethod, rule.fromMethod.inverse)»);
            «ELSE»
              return «valuesetCode»;
            «ENDIF»
            // end-user-code
          «ELSE»
              «IF rule.isDelegate»
                return «getMethodNameGetAllowedValuesFor(rule.fromMethod.inverse)»(«allowedValuesMethodParameter(rule.fromMethod, rule.fromMethod.inverse)»);
              «ELSE»
                return «IF overwrite»«typeName».«ENDIF»«constantNameValueSet»;
              «ENDIF»
          «ENDIF»
          }
      «ENDIF»
    «ENDIF»
  '''
 
  def package static allowedValuesMethodWithMoreConcreteTypeForByType(XPolicyAttribute it) '''
  
    «IF generateAllowedValuesMethodWithMoreConcreteTypeForByType && generateGetAllowedValuesForAndGetDefaultValue && overwritingValueSetWithMoreConcreteTypeForByType»
        /**
         * «inheritDoc»
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        @Override
        public «valueSetJavaClassName» «method(overwrittenAttribute.getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_BY_TYPE), IValidationContext(), "context")»
        «IF genInterface()»;«ELSE» {
          return «getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_BY_TYPE)»(context);
        }
        «ENDIF»
    «ENDIF»    
    ''' 

   def package static allowedValuesMethodWithMoreConcreteTypeForByTypeWithBothTypeParent(XPolicyAttribute it) '''
     «IF generateAllowedValuesMethodWithMoreConcreteTypeForByTypeWithBothTypeParent && generateGetAllowedValuesForAndGetDefaultValue && isOverwritingValueSetWithMoreConcreteTypeForByTypeWithBothTypeParent»
        /**
         * «inheritDoc»
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        @Override
        public «valueSetJavaClassName» «method(overwrittenAttribute.getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_UNIFIED))»
        «IF genInterface()»;«ELSE» {
          return «getMethodNameGetAllowedValuesFor(GenerateValueSetType.GENERATE_BY_TYPE)»(null);
        }
        «ENDIF»
    «ENDIF»
    '''

  def package static allowedValuesMethodForNotOverriddenAttributesButDifferentUnifyValueSetSettings(XPolicyCmptClass it, XPolicyAttribute attributeSuperType, GenerateValueSetType valueSetMethods) '''
        /**
         * «localizedText("OVERRIDE_UNIFY_METHODS_JAVADOC")»
         *«IF attributeSuperType.isDeprecatedGetAllowedValuesMethodForNotOverrideAttributesButDifferentUnifyValueSetSettings(valueSetMethods)» @deprecated «localizedText("DEPRECATED_UNIFY_METHODS_JAVADOC")»«ENDIF»
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        «overrideAnnotationForPublishedMethodImplementation»
        «IF !(valueSetMethods.generateByType && generateBothMethodsToGetAllowedValues)»
            «attributeSuperType.getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES, genInterface())»
        «ENDIF»
        «IF attributeSuperType.isDeprecatedGetAllowedValuesMethodForNotOverrideAttributesButDifferentUnifyValueSetSettings(valueSetMethods)»@Deprecated«ENDIF»
        public «attributeSuperType.valueSetJavaClassName» «method(attributeSuperType.getMethodNameGetAllowedValuesFor(valueSetMethods), attributeSuperType.getAllowedValuesMethodParameterSignature(valueSetMethods))» 
        «IF genInterface() || isAbstract()»;«ELSE» {
          return super.«attributeSuperType.getMethodNameGetAllowedValuesFor(valueSetMethods.inverse)»(«attributeSuperType.allowedValuesMethodParameter(valueSetMethods, valueSetMethods.inverse)»);
        }
        «ENDIF»
  '''

  def package static initConfigurableAttribute(XPolicyAttribute it) '''
    «IF generateInitWithProductData»
      «methodNameSetterInternalIfGenerateChangeSupport»(«getPropertyValueContainer(published)».«methodNameGetDefaultValue»());
    «ENDIF»
  '''

  def package static initFromXmlMethodCall(XPolicyAttribute it) '''
    «IF (!overwrite || overwriteAbstract && !overwrittenAttribute.generateInitPropertiesFromXML) && generateInitPropertiesFromXML && !datatype.abstract»
      «IF datatypeExtensibleEnum»
        «methodNameDoInitFromXml»(propMap, productRepository);
      «ELSE»
        «methodNameDoInitFromXml»(propMap);
      «ENDIF»
    «ENDIF»
  '''

  def package static initFromXmlMethod(XPolicyAttribute it) '''
    «IF generateInitPropertiesFromXML && (!overwrite || overwriteAbstract) && !datatype.abstract»
      /**
       * @generated
       */
      «IF datatypeExtensibleEnum»
        private void «method(methodNameDoInitFromXml, Map("String", "String"),"propMap", "IRuntimeRepository", "productRepository")»{
      «ELSE»
        private void «method(methodNameDoInitFromXml, Map("String", "String"),"propMap")»{
      «ENDIF»
          if (propMap.containsKey(«constantNamePropertyName»)) {
              this.«fieldName» = «getNewInstanceFromExpression("propMap.get(" + constantNamePropertyName + ")", "productRepository")»;
          }
      }
    «ENDIF»
  '''

  def private static formulaParameterDeclaration(XMethod it) '''
    «FOR parameter : parameters»
      «parameter.javaClassName» «parameter.name» = «parameter.nullExpression»;
    «ENDFOR»
  '''

  def private static formulaCall(XMethod it) '''
    «methodName»(«FOR parameter : parameters SEPARATOR ","»«parameter.name»«ENDFOR»)
  '''

  def package static genericValidation(XPolicyAttribute it) '''
      «IF policyCmptNode.ipsObjectPartContainer.generateValidatorClass»
      ml.add(«GenericRelevanceValidation».of(get«policyCmptNode.implClassName»(), «policyCmptNode.implClassName».class, «addStaticImport(policyCmptNode.getQualifiedName(BuilderAspect.INTERFACE),constantNamePropertyName)», context));
      «ELSE»
      ml.add(«GenericRelevanceValidation».of(this, «policyCmptNode.implClassName».class, «constantNamePropertyName», context));
      «ENDIF»
  '''

}
