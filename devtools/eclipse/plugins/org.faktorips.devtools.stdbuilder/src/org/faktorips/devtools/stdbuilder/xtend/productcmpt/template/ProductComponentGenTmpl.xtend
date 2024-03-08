package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptGenerationClass

import static org.faktorips.devtools.model.builder.naming.BuilderAspect.*
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.MethodsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.TableUsagesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.DerivedUnionAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static org.faktorips.devtools.stdbuilder.xtend.template.CommonDefinitions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType

class ProductComponentGenTmpl {

    def static  String body (XProductCmptGenerationClass it) '''

            /**
            «IF generatePublishedInterfaces»
             *«localizedJDoc("CLASS", interfaceName)»
            «ELSE»
             *«localizedJDocOrDescription("CLASS_NO_INTERFACE", generationConceptNameSingular, productCmptClassNode.name, description)»
            «ENDIF»
            «IF !productCmptType.changingOverTime»
             *
             *«localizedJDoc("DEPRECATED_CLASS", generationConceptNamePlural)»
            «ENDIF»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «IF !productCmptType.changingOverTime»
             @Deprecated
            «ELSE»
              «getAnnotations(DEPRECATION)»
            «ENDIF»
            public «isAbstract(it)» class «implClassName» extends «superclassName» «implementedInterfaces(it)»
             {

                «FOR it : associations» «privateConstants» «ENDFOR»

                 «FOR it : overwritingAttributes»
                     «IF !generatePublishedInterfaces || !published »
                         «constantForValueSet»
                         «constantForDefaultValue»
                     «ENDIF»
                 «ENDFOR»
                 «FOR it : attributes»
                     «IF !generatePublishedInterfaces || !published »
                         «constantForPropertyName»
                         «constantForValueSet»
                         «constantForDefaultValue»
                     «ENDIF»
                 «ENDFOR»

                «FOR it : attributes» «memberField» «ENDFOR»
                «FOR it : configuredAttributes» «defaultAndAllowedValuesFields» «ENDFOR»

                «FOR it : associations» «field» «ENDFOR»

                «FOR it : tables» «propertyField» «ENDFOR»

                «FOR it : tables» «memberField» «ENDFOR»


                /**
                 *«localizedJDoc("CONSTRUCTOR", getSimpleName(IMPLEMENTATION))»
                «getAnnotations(ELEMENT_JAVA_DOC)»
                 *
                 * @generated
                 */
                «getAnnotations(DEPRECATION)»
                public «method(implClassName, productCmptClassNode.implClassName, "productCmpt")» {
                    super(productCmpt);
                    «FOR it : attributesIncludingNoContentGeneration» «setDefaultValue» «ENDFOR»
                }

                «FOR it : attributesIncludingNoContentGeneration»
                    «IF generateAbstractMethods»
                        «abstractGetter»
                    «ENDIF»
                «ENDFOR»


                «FOR it : attributes» «getterSetter» «ENDFOR»

                «FOR it : configuredAttributes» «getterAndSetter» «ENDFOR»

                «FOR superAttribute : attributesFromSupertypeWhenDifferentUnifyValueSetSettingsFor(GenerateValueSetType.GENERATE_BY_TYPE)»
                    «IF superAttribute.published»
                    «allowedValuesMethodForNotOverriddenAttributesButDifferentUnifyValueSetSettings(it, superAttribute, GenerateValueSetType.GENERATE_BY_TYPE)»
                    «ENDIF»
                «ENDFOR»

                «FOR superAttribute : attributesFromSupertypeWhenDifferentUnifyValueSetSettingsFor(GenerateValueSetType.GENERATE_UNIFIED)»
                    «IF superAttribute.published»
                    «allowedValuesMethodForNotOverriddenAttributesButDifferentUnifyValueSetSettings(it, superAttribute, GenerateValueSetType.GENERATE_UNIFIED)»
                    «ENDIF»
                «ENDFOR»

                «FOR it : associations» «getterSetterAdderRemover» «ENDFOR»

                «FOR derivedUnions : subsettedDerivedUnions» «methodsForDerivedUnion(derivedUnions)» «ENDFOR»

                «FOR it : methods»
                    «IF changingOverTime»
                        «IF !formulaSignature»
                            «MethodsTmpl.method(it)»
                        «ELSE»
                            «formulaMethod»
                        «ENDIF»
                    «ENDIF»
                «ENDFOR»

                «getProductCmpt(productCmptClassNode)»

                /**
                 *«inheritDoc»
                 *
                 * @generated
                 */
                @Override
                protected void «doInitPropertiesFromXml(Map("String", Element) + " configMap")» {
                    super.«doInitPropertiesFromXml("configMap")»;
                    «FOR it : attributes» «initFromXmlMethodCall» «ENDFOR»
                «FOR it : configuredAttributes» «initFromXmlMethodCall» «ENDFOR»
                }

                «FOR it : attributes» «initFromXmlMethod» «ENDFOR»
              «FOR it : configuredAttributes» «initFromXmlMethod» «ENDFOR»

                «IF containsNotDerivedOrConstrainingAssociations»
                    /**
                     * @generated
                     */
                    @Override
                    protected void «doInitReferencesFromXml(Map("String", List_(Element)) + " elementsMap")» {
                        super.«doInitReferencesFromXml("elementsMap")»;
                        «FOR it : associations» «doInitFromXmlMethodCall» «ENDFOR»
                    }
                «ENDIF»

                «FOR it : associations» «doInitFromXmlMethod» «ENDFOR»

                «IF containsTables»
                    /**
                     * @generated
                     */
                    @Override
                    protected void «doInitTableUsagesFromXml(Map("String", Element) + " tableUsageMap")» {
                        super.«doInitTableUsagesFromXml("tableUsageMap")»;
                        «FOR it : tables» «doInitFromXmlMethodCall» «ENDFOR»
                    }
                «ENDIF»

                «FOR it : tables» «doInitFromXmlMethod» «ENDFOR»

                «writeToXmlMethods»

                «FOR policy : policyTypeClassHierarchy» «createPolicyCmpt(productCmptClassNode, policy)» «ENDFOR»

                «IF generateMethodGenericCreatePolicyComponent»
                    /**
                     *«inheritDoc»
                     *
                     * @generated
                     */
                    @Override
                    public «policyInterfaceName» «createPolicyComponent()» {
                        «IF !configurationForPolicyCmptType»
                            return null;
                        «ELSE»
                            return «policyCmptClass.methodNameCreatePolicyCmpt»();
                        «ENDIF»
                    }
                «ENDIF»

                «getLinkMethods»

                «FOR it : tables» «getterAndSetter» «ENDFOR»

    «««            «IF generateProductBuilder && !abstract»
    «««                «ProductCmptGenCreateBuilder.builder(productGenBuilderModelNode)»
    «««            «ENDIF»
        }
    '''

    def package static getProductCmpt (XProductCmptClass it) '''
            /**
             *«inheritDocOrJavaDocIf(genInterface, "METHOD_GET_PRODUCTCMPT_IN_GEN", name, generationConceptNameSingular)»
             *
             * @generated
             */
            «overrideAnnotationForPublishedMethodImplementation»
            public «interfaceName» «method(methodNameGetProductCmpt)»
            «IF genInterface»;«ELSE»
            {
                return («interfaceName»)«getProductComponent»;
            }
            «ENDIF»
    '''

    //The content of the method is always the same (use currentType). The methodName is derived from different types
    //TODO may use covariant return type
    def package static createPolicyCmpt(XProductCmptClass currentType, XPolicyCmptClass it) '''
        «IF currentType.isGenerateMethodCreatePolicyCmpt(it)»
            /**
             *«inheritDocOrJavaDocIf(genInterface, "METHOD_CREATE_POLICY_CMPT_IN_GEN")»
             *
             * @generated
             */
             «overrideAnnotationForPublishedMethodOrIf(!genInterface, productCmptNode != currentType)»
            public «publishedInterfaceName» «method(methodNameCreatePolicyCmpt)»
            «IF genInterface»;«ELSE»
            {
                «currentType.policyImplClassName» policy = new «currentType.policyImplClassName»(«currentType.methodNameGetProductCmpt»());
              «IF currentType.changingOverTime»
                policy.«setProductCmptGeneration("this")»;
              «ENDIF»
                policy.«initialize»;
                return policy;
            }
            «ENDIF»
        «ENDIF»
    '''
}