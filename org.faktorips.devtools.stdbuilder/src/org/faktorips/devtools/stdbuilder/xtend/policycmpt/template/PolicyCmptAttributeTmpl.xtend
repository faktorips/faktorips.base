package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.XMethod
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute

import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAttributeExtensionTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class PolicyCmptAttributeTmpl {
def package static constantForPropertyName (XPolicyAttribute it) '''
    «IF !overwrite»
        /**
         * «localizedJDoc("FIELD_PROPERTY_NAME", name)»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        public static final String «field(constantNamePropertyName)» = "«name»";
    «ENDIF»
'''

def package static constantForValueSet (XPolicyAttribute it) '''
    «IF generateConstantForValueSet»
        /**
         * «localizedJDoc(getJavadocKey("FIELD_MAX"), name)»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        public static final «valueSetJavaClassName» «field(constantNameValueSet)» = «valuesetCode»;
    «ENDIF»
'''

def package static constantField (XPolicyAttribute it) '''
    «IF constant»
            /**
             * «localizedJDoc("FIELD_ATTRIBUTE_CONSTANT", name)»
             * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
             * @generated
             */
            «getAnnotationsForPublishedInterface(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface())»
            public static final «javaClassName» «field(fieldName)» = «defaultValueCode»;
    «ENDIF»
'''

def package static memberField (XPolicyAttribute it) '''
    «IF generateField»
        /**
         * «localizedJDoc("FIELD_ATTRIBUTE_VALUE", name)»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD)»
        «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD)»
        private «javaClassName» «field(fieldName)» = «defaultValueCode»;
    «ENDIF»
'''

def package static getter (XPolicyAttribute it) '''
    «IF isGenerateGetter(genInterface())»
        /**
         * «inheritDocOrJavaDocIf(genInterface(), "METHOD_GETVALUE", name, descriptionForJDoc)»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, genInterface())»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overwrite)»
        public «javaClassName» «method(methodNameGetter)»
        «IF genInterface()»;«ELSE»
        {
            «IF generateField»
                return «IF constant»«fieldName»«ELSE»«getReferenceOrSafeCopyIfNecessary(fieldName)»«ENDIF»;
            «ELSE»
                «IF generateDefaultForOnTheFlyDerivedAttribute»
                    return «defaultValueCode»;
                «ELSEIF !generatePublishedInterfaces && overwrite»
                    return super.«methodNameGetter»();
                «ELSE»
                    «IF !formulaSignature.parameters.isEmpty»
                        «localizedComment("COMMENT_DERIVED_ATTRIBUTE_METHOD_CALL")»
                    «ENDIF»
                    «formulaParameterDeclaration(formulaSignature)»
                    return «getPropertyValueContainer(false)».«formulaCall(formulaSignature)»;
                «ENDIF»
            «ENDIF»
        }
        «ENDIF»
    «ENDIF»
'''


def package static setter (XPolicyAttribute it) '''
    «IF generateSetter»
        /**
         * «inheritDocOrJavaDocIf(genInterface(), "METHOD_SETVALUE", name, descriptionForJDoc)»«IF generateChangeSupport && (!generatePublishedInterfaces || genInterface())» «inheritDocOrJavaDocIf(genInterface(), "METHOD_SETVALUE_LISTENERS", name, descriptionForJDoc)»«ENDIF»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_SETTER, genInterface())»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overwrite && !attributeTypeChangedByOverwrite)»
        public void «method(methodNameSetter, javaClassName, "newValue")»
        «IF genInterface()»;«ELSE»
        {
            «PropertyChangeSupportTmpl.storeOldValue(it)»
            «IF generateSetterInternal»
                «methodNameSetterInternal»(newValue);
            «ELSE»
                this.«fieldName» = «getReferenceOrSafeCopyIfNecessary("newValue")»;
            «ENDIF»
            «PropertyChangeSupportTmpl.notify(it)»
        }
        «ENDIF»
    «ENDIF»
'''

def package static setterInternal (XPolicyAttribute it) '''
    «IF generateSetterInternal»
        /**
         * «localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        protected final void «method(methodNameSetterInternal, javaClassName, "newValue")»
        {
            this.«fieldName» = «getReferenceOrSafeCopyIfNecessary("newValue")»;
        }
    «ENDIF»
'''

def package static allowedValuesMethod (XPolicyAttribute it) '''
    «IF generateGetAllowedValuesForAndGetDefaultValue»
        /**
         * «inheritDocOrJavaDocIf(genInterface(), getJavadocKey("METHOD_GET"), name, descriptionForJDoc)»
         * «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         * @generated
         */
        «getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES, genInterface())»
        «overrideAnnotationForPublishedMethodOrIf(!genInterface() && published, overrideGetAllowedValuesFor)»
        public «valueSetJavaClassName» «method(methodNameGetAllowedValuesFor, IValidationContext(), "context")»
        «IF genInterface()»;«ELSE»
        {
            «IF productRelevant»
              «IF overwritingValueSetWithMoreConcreteType»
                return «castFromTo(overwrittenAttribute.valueSetJavaClassName,valueSetJavaClassName)» «getPropertyValueContainer(published)».«overwrittenAttribute.methodNameGetAllowedValuesFor»(context);
              «ELSE»
                return «getPropertyValueContainer(published)».«methodNameGetAllowedValuesFor»(context);
              «ENDIF»
            «ELSE»
                return «IF overwrite»«typeName».«ENDIF»«constantNameValueSet»;
            «ENDIF»
        }
        «ENDIF»
        «IF overwritingValueSetWithMoreConcreteType»
          /**
           * «inheritDoc»
           * @generated
           */
          @Override
          public «valueSetJavaClassName» «method(overwrittenAttribute.methodNameGetAllowedValuesFor, IValidationContext(), "context")» {
            return «methodNameGetAllowedValuesFor»(context);
          }
        «ENDIF»
    «ENDIF»
'''

protected def static boolean isOverwritingValueSetWithMoreConcreteType(XPolicyAttribute it) {
  overwrite && overwrittenAttribute.generateGetAllowedValuesForAndGetDefaultValue && !overwrittenAttribute.methodNameGetAllowedValuesFor.equals(methodNameGetAllowedValuesFor) && !genInterface
}

def package static initConfigurableAttribute (XPolicyAttribute it) '''
    «IF generateInitWithProductData»
        «methodNameSetterInternalIfGenerateChangeSupport»(«getPropertyValueContainer(published)».«methodNameGetDefaultValue»());
    «ENDIF»
'''

def package static initFromXmlMethodCall (XPolicyAttribute it) '''
    «IF !overwrite && generateInitPropertiesFromXML»
        «IF datatypeExtensibleEnum»
            «methodNameDoInitFromXml»(propMap, productRepository);
        «ELSE»
            «methodNameDoInitFromXml»(propMap);
        «ENDIF»
    «ENDIF»
'''

def package static initFromXmlMethod (XPolicyAttribute it) '''
    «IF !overwrite && generateInitPropertiesFromXML»
        /**
         * @generated
         */
         «IF datatypeExtensibleEnum»
            private void «method(methodNameDoInitFromXml, Map("String", "String"),"propMap", "IRuntimeRepository", "productRepository")» {
        «ELSE»
            private void «method(methodNameDoInitFromXml, Map("String", "String"),"propMap")» {
        «ENDIF»
            if (propMap.containsKey(«constantNamePropertyName»)) {
                this.«fieldName» = «getNewInstanceFromExpression("propMap.get(" + constantNamePropertyName + ")", "productRepository")»;
            }
        }
    «ENDIF»
'''

def private static formulaParameterDeclaration (XMethod it) '''
    «FOR parameter : parameters»
        «parameter.javaClassName» «parameter.name» = «parameter.nullExpression»;
    «ENDFOR»
'''

def private static formulaCall (XMethod it) '''
    «methodName»(«FOR parameter : parameters SEPARATOR  ","»«parameter.name»«ENDFOR»)
'''

}
