package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.naming.BuilderAspect
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAttribute
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptGenerationClass
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XTableUsage
import org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.template.PolicyCmptCreateBuilderTmpl
import org.faktorips.devtools.stdbuilder.xtend.template.CommonDefinitions
import org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions
import org.faktorips.devtools.stdbuilder.xtend.template.DerivedUnionAssociationTmpl

import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.ValidationRuleTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*
import org.faktorips.devtools.model.builder.xmodel.policycmpt.AllowedValuesForAttributeRule

class PolicyCmptTmpl {

def static String body(XPolicyCmptClass it) '''
    
    /**
     «IF generatePublishedInterfaces»
         *«localizedJDoc("CLASS", getSimpleName(BuilderAspect.INTERFACE))»
     «ELSE»
         *«localizedJDocOrDescription("CLASS_NO_INTERFACE", name, description)»
     «ENDIF»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @generated
     */
     «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS)»
     «getAnnotationsForPublishedInterface(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS, genInterface())»
    public «CommonGeneratorExtensions::isAbstract(it)» class «implClassName» extends «superclassName» «CommonDefinitions.implementedInterfaces(it)»{

        «IF !generatePublishedInterfaces»
            «FOR association : associations» «PolicyCmptAssociationTmpl.constants(association)» «ENDFOR»
            «FOR it : validationRules» «constants» «ENDFOR»
        «ENDIF»

        «FOR it : attributesIncludingAbstract»
            «IF !published || !generatePublishedInterfaces»
                «PolicyCmptAttributeTmpl.constantForPropertyName(it)»
                «PolicyCmptAttributeTmpl.constantForValueSet(it)»
                «PolicyCmptAttributeTmpl.constantForDefaultValue(it)»
            «ENDIF»
        «ENDFOR»

        «FOR it : attributes»
            «IF !published || !generatePublishedInterfaces»
                «PolicyCmptAttributeTmpl.constantField(it)»
            «ENDIF»
        «ENDFOR»

        «IF generateSerializablePolicyCmptsSupport»
            /**
             * @generated
             */
            private static final long serialVersionUID = 1L;
        «ENDIF»

        «IF type.generateValidatorClass && !hasSupertype()»
            /**
             * @generated
             */
            private volatile «validatorClassName» validator;
        «ENDIF»

        «PropertyChangeSupportTmpl.fieldDefinition(it)»
    
        «FOR it : attributes»
            «IF !constant»
                «PolicyCmptAttributeTmpl.memberField(it)»
            «ENDIF»
        «ENDFOR»

        «IF firstConfigurableInHierarchy»
            /**
             *«localizedJDoc("PRODUCTCONFIGURATION_FIELD")»
             *
             * @generated
             */
            «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_PRODUCTCONFIGURATION_FIELD)»
            private «ProductConfiguration()» productConfiguration;
        «ENDIF»

        «FOR it : associations» «PolicyCmptAssociationTmpl.field(it)» «ENDFOR»

        «constructors»

        «FOR it : attributesIncludingAbstract»
            «IF generateAbstractMethods»
                «abstractGetter»
                «abstractSetter»
            «ENDIF»
        «ENDFOR»

        «IF generateConvenienceGetters»
            «FOR attributes : productAttributes» «getterForProductAttributes(it, attributes)» «ENDFOR»
        «ENDIF»



        «FOR it : attributesIncludingAbstract»

            «FOR rule : AllowedValuesForAttributeRule.getGenerateValueSetTypeRulesFor(it)»
                «allowedValuesMethod(rule)»
            «ENDFOR»
            «allowedValuesMethodWithMoreConcreteTypeForByType»
            «allowedValuesMethodWithMoreConcreteTypeForByTypeWithBothTypeParent»
            «IF !it.isAbstract»
                «getter»
                «setter»
                «setterInternal»
            «ENDIF»
        «ENDFOR»

        «FOR attributeSuperType : attributesFromSupertypeWhenDifferentUnifyValueSetSettingsFor(GenerateValueSetType.GENERATE_BY_TYPE)»
            «allowedValuesMethodForNotOverriddenAttributesButDifferentUnifyValueSetSettings(it, attributeSuperType, GenerateValueSetType.GENERATE_BY_TYPE)»
        «ENDFOR»

        «FOR attributeSuperType : attributesFromSupertypeWhenDifferentUnifyValueSetSettingsFor(GenerateValueSetType.GENERATE_UNIFIED)»
            «allowedValuesMethodForNotOverriddenAttributesButDifferentUnifyValueSetSettings(it, attributeSuperType, GenerateValueSetType.GENERATE_UNIFIED)»
        «ENDFOR»

        «FOR it : associations» «methods» «ENDFOR»

        «FOR derivedUnion : subsettedDerivedUnions» «DerivedUnionAssociationTmpl.methodsForDerivedUnion(it, derivedUnion)» «ENDFOR»

        «FOR it : methods» «MethodsTmpl.method(it)» «ENDFOR»

        «FOR table : productTables» «getterForTables(it, table)» «ENDFOR»

        /**
     *«localizedJDoc("METHOD_INITIALIZE")»
     *
     * @restrainedmodifiable
     */
     «overrideAnnotationIf(hasSupertype() || configured)»
     public void «initialize()» {
        «IF hasSupertype()»
            super.«initialize()»;
        «ENDIF»
        «IF configured»
            «initializeAttributes(methodNameGetProductCmptGeneration , true ,it)»
            «initializeAttributes(methodNameGetProductCmpt, false, it)»
        «ENDIF»
        // begin-user-code
        // end-user-code
        }

        «IF configured»
            «getAndSetProductComponent(productCmptNode)»
            «IF generateGenerationAccessMethods»
                «getAndSetProductComponentGeneration(productCmptGenerationNode)»
            «ENDIF»
            «generalMethodsForConfiguredPolicyCmpts(it)»
        «ENDIF»

        «generateCodeForDependentObject(it)»

        «FOR assocation : detailToMasterDerivedUnionAssociations» «DerivedUnionAssociationTmpl.getterForDetailToMaster(it, assocation)» «ENDFOR»

        «PropertyChangeSupportTmpl.generalMethods(it)»

        «generalMethods(it)»

        «IF !type.generateValidatorClass»
            «FOR it : validationRules»«validationRuleMethods»«ENDFOR»
        «ENDIF»

        «IF generatePolicyBuilder && !isAbstract»
            «PolicyCmptCreateBuilderTmpl.builder(policyBuilderModelNode)»
            «IF !generatePublishedInterfaces»
                «PolicyCmptCreateBuilderTmpl.with(true, policyBuilderModelNode)»
            «ENDIF»
        «ENDIF»
        }
'''

def private static initializeAttributes(String methodNameGetProductConfiguration, boolean changingOverTime, XPolicyCmptClass it) '''
    «IF isGenerateAttributeInitCode(changingOverTime)»
        if («methodNameGetProductConfiguration»() != null) {
             «IF hasSupertype()»
                «FOR it : getAttributesToInit(false, changingOverTime)»
                  «methodNameSetterInternalIfGenerateChangeSupport»(«defaultValueCode»);
                «ENDFOR»
            «ENDIF»
            «FOR it : getAttributesToInit(true, changingOverTime)»
                «initConfigurableAttribute»
            «ENDFOR»
        }
    «ENDIF»
'''

def private static constructors(XPolicyCmptClass it) '''
    /**
     *«localizedJDoc("CONSTRUCTOR", implClassName)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @generated
     */
    «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
    public «method(implClassName)» {
        super();
        «IF firstConfigurableInHierarchy»
            productConfiguration = new «ProductConfiguration()»();
        «ENDIF»
        «initializationForOverrideAttributes(it)»
    }
    «IF configured»
        /**
         *«localizedJDoc("CONSTRUCTOR", implClassName)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public «method(implClassName, productCmptClassName, "productCmpt")» {
            «IF firstConfigurableInHierarchy»
                super();
                productConfiguration = new «ProductConfiguration()»(productCmpt);
            «ELSE»
                super(productCmpt);
            «ENDIF»
            «initializationForOverrideAttributes(it)»
        }
    «ENDIF»
'''

def private static getterForProductAttributes(XPolicyCmptClass currentClass, XProductAttribute it) '''
    «IF generateInterfaceGetter»
         /**
         *«localizedJDoc("METHOD_GET_VALUE_IN_POLICY", name, descriptionForJDoc)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        «overrideAnnotationIf(overwrite && currentClass.hasSupertype())»
        «IF generatePublishedInterfaces»public«ELSE»protected«ENDIF» «javaClassName» «method(methodNameGetter)» {
            «IF changingOverTime»
                return «currentClass.methodNameGetProductCmptGeneration»().«methodNameGetter»();
            «ELSE»
                return «currentClass.methodNameGetProductCmpt»().«methodNameGetter»();
            «ENDIF»
        }
    «ENDIF»
'''

def private static getterForTables(XPolicyCmptClass policyClass, XTableUsage it) '''
    /**
     *«localizedJDoc("METHOD_GET_TABLE_IN_POLICY", name)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @generated
     */
    «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
    public «tableClassName» «method(methodNameGetter)» {
        «IF changingOverTime»
            «policyClass.productCmptGenerationNode.implClassName» productCmpt = «castToImplementation(policyClass.productCmptGenerationNode.implClassName)»«policyClass.methodNameGetProductCmptGeneration»();
        «ELSE»
            «policyClass.productCmptNode.implClassName» productCmpt = «castToImplementation(policyClass.productCmptNode.implClassName)»«policyClass.methodNameGetProductCmpt»();
        «ENDIF»
        if (productCmpt == null) {
            return null;
        }
        return productCmpt.«methodNameGetter»();
    }
'''

def private static initializationForOverrideAttributes(XPolicyCmptClass it) '''
    «FOR it : attributes»
        «IF generateDefaultInitialize»
            «methodNameSetterInternalIfGenerateChangeSupport»(«defaultValueCode»);
        «ENDIF»
    «ENDFOR»
'''

def package static getAndSetProductComponent(XProductCmptClass it) '''
    /**
     *«inheritDocOrJavaDocIf(genInterface(), "METHOD_GET_PRODUCTCMPT", name, policyCmptClass.name)»
     *
     * @generated
     */
    «overrideAnnotationForPublishedMethodImplementation()»
    public «interfaceName» «method(methodNameGetProductCmpt)»
     «IF genInterface()»
      ;
     «ELSE»
         {
             return («interfaceName») «getProductComponent()»;
         }
     «ENDIF»

    /**
     *«inheritDocOrJavaDocIf(genInterface(), "METHOD_SET_PRODUCTCMPT",  name, nameForVariable, "initPropertiesWithConfiguratedDefaults")»
     *
     * @generated
     */
     «overrideAnnotationForPublishedMethodImplementation()»
     public void «method(methodNameSetProductCmpt, interfaceName, nameForVariable, "boolean", "initPropertiesWithConfiguratedDefaults")»
     «IF genInterface()»;«ELSE»
         {
            «setProductComponent(nameForVariable)»;
            if (initPropertiesWithConfiguratedDefaults) {
                «initialize()»;
            }
         }
     «ENDIF»

     «IF !genInterface() && policyCmptClass.firstConfigurableInHierarchy»
         /**
          * {@inheritDoc}
          *
          * @generated
          */
          @Override
          public «IProductComponent()» «getProductComponent()» {
              return productConfiguration.«getProductComponent()»;
          }
         
             /**
              *«localizedJDoc("METHOD_SET_PRODUCT_COMPONENT")»
              *
              * @generated
              */
              @Override
              public void setProductComponent(IProductComponent productComponent) {
                  productConfiguration.setProductComponent(productComponent);
              }
     «ENDIF»
'''

def package static getAndSetProductComponentGeneration(XProductCmptGenerationClass it) '''
    /**
     *«inheritDocOrJavaDocIf(genInterface(), "METHOD_GET_PRODUCTCMPT_GENERATION", generationConceptNameSingular, policyCmptClass.name)»
     *
     * @generated
     */
     «overrideAnnotationForPublishedMethodImplementation()»
     public «interfaceName» «method(methodNameGetProductComponentGeneration)»
     «IF genInterface()»;«ELSE»
                 {
                     return («interfaceName») getProductCmptGeneration();
                 }
     «ENDIF»

     «IF !genInterface() && policyCmptClass.firstConfigurableInHierarchy»
         /**
          * {@inheritDoc}
          *
          * @generated
          */
         @Override
         public «IProductComponentGeneration()» «getProductCmptGeneration()» {
             return productConfiguration.getProductCmptGeneration(getEffectiveFromAsCalendar());
         }

             /**
             *«localizedJDoc("METHOD_SET_PRODUCT_COMPONENT_GENERATION")»
             *
             * @generated
             */
             «overrideAnnotationIf(policyCmptClass.hasConfiguredSupertype())»
             public void «setProductCmptGeneration("IProductComponentGeneration productComponentGeneration")» {
                 productConfiguration.«setProductCmptGeneration("productComponentGeneration")»;
             }
     «ENDIF»
'''

def private static generalMethodsForConfiguredPolicyCmpts(XPolicyCmptClass it) '''

    /**
     *«inheritDocOrJavaDocIf(!hasConfiguredSupertype(), "METHOD_EFFECTIVE_FROM_HAS_CHANGED")»
     *
     * @generated
     */
    «overrideAnnotationIf(hasConfiguredSupertype())»
    public void «effectiveFromHasChanged()» {
        «IF hasConfiguredSupertype()»
            super.«effectiveFromHasChanged()»;
        «ELSE»
            if («getEffectiveFromAsCalendar()» != null) {
                resetProductCmptGenerationAfterEffectiveFromHasChanged();
            }
        «ENDIF»
        «FOR it : associations» «PolicyCmptAssociationTmpl.delegateEffectiveFromHasChanged(it)» «ENDFOR»
    }
    
    «IF firstConfigurableInHierarchy»
        /**
         *«localizedJDoc("METHOD_RESET_PRODUCT_CMPT")»
         *
         * @generated
         */
        protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
           productConfiguration.resetProductCmptGeneration();
        }
    «ENDIF»
 
    «IF isGenerateGetEffectiveFromAsCalendar»
        «IF firstDependantConfiguredTypeInHierarchy»
            /**
             *«inheritDoc»
             *
             * @generated
             */
            @Override
            public «Calendar()» «getEffectiveFromAsCalendar()» {
                «IModelObject()» parent = «getParentModelObject()»;
                if (parent instanceof «IConfigurableModelObject()») {
                    return ((«IConfigurableModelObject()»)parent).«getEffectiveFromAsCalendar()»;
                }
                return null;
            }
        «ELSEIF aggregateRoot»
            /**
             *«inheritDoc»
             *
             * @generated
             */
            @Override
            public «Calendar()» «getEffectiveFromAsCalendar()» {
                   «IF hasConfiguredSupertype()»
                       return super.«getEffectiveFromAsCalendar()»;
            «ELSE»
                «localizedComment("METHOD_GET_EFFECTIVE_FROM_TODO_LINE1")»
                «localizedComment("METHOD_GET_EFFECTIVE_FROM_TODO_LINE2")»
                «localizedComment("METHOD_GET_EFFECTIVE_FROM_TODO_LINE3")»
                return null;
            «ENDIF»
            }
        «ENDIF»
    «ENDIF»
'''

def private static generateCodeForDependentObject(XPolicyCmptClass it) '''
    «IF generateGetParentModelObject»
        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public «IModelObject()» «getParentModelObject()» {
            «FOR it : associations»
                «IF implementedDetailToMasterAssociation»
                    if («fieldName» != null) {
                        return «fieldName»;
                    }
                «ENDIF»
            «ENDFOR»
            «IF supertypeGenerateGetParentModelObject»
                return super.«getParentModelObject()»;
            «ELSE»
                return null;
            «ENDIF»
        }
    «ENDIF»

'''

def private static generalMethods(XPolicyCmptClass it) '''

    «IF firstConfigurableInHierarchy»
        /**
        *«inheritDoc»
        *
        * @generated
        */
        @Override
        protected void initFromXml(Element objectEl,
                boolean initWithProductDefaultsBeforeReadingXmlData,
                «IRuntimeRepository()» productRepository,
                «IObjectReferenceStore()» store,
                «XmlCallback()» xmlCallback,
                String currPath) {
            productConfiguration.initFromXml(objectEl, productRepository);
            if (initWithProductDefaultsBeforeReadingXmlData) {
                initialize();
            }
            super.initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback,
                currPath);
        }
    «ENDIF»

    «IF generateInitPropertiesFromXML»
        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        protected void «initPropertiesFromXml(Map("String", "String")+" propMap", IRuntimeRepository()+" productRepository")» {
            super.«initPropertiesFromXml("propMap", "productRepository")»;
            «FOR it : attributesIncludingAbstract» «initFromXmlMethodCall» «ENDFOR»
        }
        «FOR it : attributesIncludingAbstract» «PolicyCmptAttributeTmpl.initFromXmlMethod(it)» «ENDFOR»
    «ENDIF»

    /**
     *«inheritDoc»
     *
     * @generated
     */
    @Override
    protected «AbstractModelObject()» «createChildFromXml(Element()+" childEl")» {
        «AbstractModelObject()» newChild = super.«createChildFromXml("childEl")»;
        if (newChild != null) {
            return newChild;
        }
        «FOR it : associations» «PolicyCmptAssociationTmpl.createTargetFromXmlMethodCall(it)» «ENDFOR»
        return null;
    }

    «FOR it : associations» «PolicyCmptAssociationTmpl.createTargetFromXmlMethod(it)» «ENDFOR»

    «IF generateMethodCreateUnresolvedReference»
        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        protected «IUnresolvedReference()» «createUnresolvedReference("Object objectId, String targetRole, String targetId")»
                throws Exception {
        «FOR it : associations» «PolicyCmptAssociationTmpl.createUnresolvedReference(it)» «ENDFOR»
        return super.createUnresolvedReference(objectId, targetRole, targetId);
        }
    «ENDIF»

    «IF generateDeltaSupport»
        «DeltaSupportTmpl.computeDeltaMethod(it)»
    «ENDIF»

    «IF generateCopySupport»
        «CopySupportTmpl.copyMethods(it)»
    «ENDIF»

    «IF generateVisitorSupport»
        «VisitorSupportTmpl.acceptMethod(it)»
    «ENDIF»

    «validateMethods(it)»

    «IF isConfigured»
        /**
         * @restrainedmodifiable
         */
        @Override
        public String toString(){
            // begin-user-code
            «IF hasSupertype && supertype.isConfigured»
              return super.toString();
            «ELSE»
              return getProductComponent() == null ? getClass().getSimpleName() : getClass().getSimpleName() + '[' + getProductComponent().toString() + ']';
            «ENDIF»
            // end-user-code
        }
    «ENDIF»
'''

def private static validateMethods(XPolicyCmptClass it) '''

    «IF type.generateValidatorClass && !hasSupertype()»
        /**
         *«localizedJDoc("GET_VALIDATOR", name, validatorClassName)»
         *
         * @generated
         */
        protected «validatorClassName» «getValidator()» {
            «validatorClassName» result = validator;
            if (result == null) {
              validator = result = createValidator();
            }
            return result;
        }
    «ENDIF»

    «IF type.generateValidatorClass»
        /**
         *«localizedJDoc("CREATE_VALIDATOR", name, validatorClassName)»
         *
         * @restrainedmodifiable
         */
        «overrideAnnotationIf(hasSupertype())»
        protected «validatorClassName» «createValidator()» {
            // begin-user-code
            return new «validatorClassName»(this);
            // end-user-code
        }
    «ENDIF»

    /**
     *«localizedJDoc("VALIDATE_SELF", name)»
     *
     * @generated
     */
    @Override
    public boolean «validateSelf(MessageList()+" ml", IValidationContext()+" context")» {
        if (!super.«validateSelf("ml", "context")») {
            return «STOP_VALIDATION»;
        }
        «IF type.generateValidatorClass»  
            «IF hasSupertype»
                return «CONTINUE_VALIDATION»;
            «ELSE»
                return getValidator().validate(ml, context);
            «ENDIF»
        «ELSE»
            «FOR it : attributesForGenericValidation»«PolicyCmptAttributeTmpl.genericValidation(it)»«ENDFOR»
            «FOR it : validationRules»«ValidationRuleTmpl.validate(it)»«ENDFOR»
            return «CONTINUE_VALIDATION»;
        «ENDIF»
    }

    /**
     *«localizedJDoc("VALIDATE_DEPENDANTS", name)»
     *
     * @generated
     */
    @Override
    public void «validateDependants(MessageList()+" ml", IValidationContext()+" context")» {
        super.«validateDependants("ml", "context")»;
        «FOR it : associations» «PolicyCmptAssociationTmpl.validateDependents(it)» «ENDFOR»
    }
'''

}
