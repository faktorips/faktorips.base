package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template.ProductCmptCreateBuilderTmpl.*

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
import org.faktorips.devtools.stdbuilder.xtend.template.MethodNames
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class ProductComponentTmpl {

    def static String body(XProductCmptClass it) '''


        /**
            «IF generatePublishedInterfaces»
                * «localizedJDoc("CLASS", interfaceName)»
            «ELSE»
                * «localizedJDocOrDescription("CLASS_NO_INTERFACE", name, description)»
            «ENDIF»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        «getAnnotations(PRODUCT_CMPT_IMPL_CLASS)»
        «getAnnotationsForPublishedInterface(PRODUCT_CMPT_DECL_CLASS, genInterface)»
            public «isAbstract(it)» class «implClassName» extends «superclassName» «implementedInterfaces(it)»
            {

             «FOR it : associations» «privateConstants»«ENDFOR»

             «FOR it : attributes»
                 «IF !generatePublishedInterfaces || !published »
                     «constantForPropertyName»
                 «ENDIF»
             «ENDFOR»

             «FOR it : attributes» «memberField» «ENDFOR»

             «FOR it : configuredAttributes» «defaultAndAllowedValuesFields» «ENDFOR»

             «FOR it : associations» «field» «ENDFOR»

             «FOR it : tables» «propertyField» «ENDFOR»

             «FOR it : tables» «memberField» «ENDFOR»

            /**
            * «localizedJDoc("CONSTRUCTOR", implClassName)»
            * «getAnnotations(ELEMENT_JAVA_DOC)»
            * @generated
            */
                public «method(implClassName, IRuntimeRepository, "repository", "String", "id", "String", "kindId", "String", "versionId")» {
                    super(repository, id, kindId, versionId);
                    «FOR it : attributesInclOverwritten» «setDefaultValue» «ENDFOR»
                }

                «IF generateGenerationAccessMethods»
                    «getProductComponentGeneration(productCmptGenerationNode)»
                «ENDIF»

                «IF generateIsChangingOverTimeAccessMethod»
                    /**
                      * «inheritDoc»
                      *
                      * @generated
                      */
                    @Override
                    public boolean «MethodNames.isChangingOverTime» {
                        return «changingOverTime»;
                    }
                «ENDIF»

                «FOR it : attributesInclOverwritten»
                    «IF generateAbstractGetter»
                        «abstractGetter»
                    «ENDIF»
                «ENDFOR»

                «FOR it : attributes» «getterSetter» «ENDFOR»
                «FOR it : configuredAttributes» «getter» «ENDFOR»

                «FOR it : associations» «getterSetterAdder» «ENDFOR»
                «FOR it : tables» «getterAndSetter» «ENDFOR»

                «FOR union : subsettedDerivedUnions» «methodsForDerivedUnion(union)» «ENDFOR»

                «FOR it : methods»
                    «IF !changingOverTime»
                        «IF !formulaSignature»
                            «MethodsTmpl.method(it)»
                        «ELSE»
                            «formulaMethod»
                        «ENDIF»
                    «ENDIF»
                «ENDFOR»

                /**
                * «inheritDoc»
                *
                * @generated
                */
                @Override
                protected void «doInitPropertiesFromXml(Map("String", Element) + " configMap")» {
                    super.«doInitPropertiesFromXml("configMap")»;
                    «FOR it : attributes» «initFromXmlMethodCall»«ENDFOR»
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

                «FOR policy : policyTypeClassHierarchy» «createPolicyCmpt(policy)» «ENDFOR»

                «IF generateMethodGenericCreatePolicyComponent»
                    /**
                     * «inheritDoc»
                     *
                     * @generated
                     */
                    @Override
                    public «policyInterfaceName» «createPolicyComponent» {
                        «IF !configurationForPolicyCmptType»
                            return null;
                        «ELSE»
                            return «policyCmptClass.methodNameCreatePolicyCmpt»();
                        «ENDIF»
                    }
                «ENDIF»

                «IF associations.size > 0»
                    «getLinkMethods»
                «ENDIF»
                                «IF generateProductBuilder && !abstract»
                    «builder(productBuilderModelNode)»
                    «IF !generatePublishedInterfaces»
                        «with(true, productBuilderModelNode)»
                    «ENDIF»
                «ENDIF»
            }
    '''

    def package static getProductComponentGeneration(XProductCmptGenerationClass it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, "METHOD_GET_GENERATION", generationConceptNameSingular)»
         *
         * @generated
         */
        «overrideAnnotationForPublishedMethodImplementation»
        public «interfaceName» «method(methodNameGetProductComponentGeneration, Calendar, varNameEffectiveDate)»
        «IF genInterface»;«ELSE»
                {
                    return («interfaceName»)«getRepository».«getProductComponentGeneration("getId()", varNameEffectiveDate)»;
                }
        «ENDIF»
    '''

// The content of the method is always the same (use currentType). The methodName is derived from different types
    def package static createPolicyCmpt(XProductCmptClass currentType, XPolicyCmptClass it) '''
        «IF currentType.isGenerateMethodCreatePolicyCmpt(it)»
            /**
             * «inheritDocOrJavaDocIf(genInterface, "METHOD_CREATE_POLICY_CMPT", name)»
             *
             * @generated
             */
             «overrideAnnotationForPublishedMethodOrIf(!genInterface, productCmptNode != currentType)»
            public «publishedInterfaceName» «method(methodNameCreatePolicyCmpt)»
            «IF genInterface»;
            «ELSE»
                {
                 «currentType.policyImplClassName» policy = new «currentType.policyImplClassName»(this);
                    policy.«initialize»;
                    return policy;
                }
            «ENDIF»
        «ENDIF»
    '''

}
