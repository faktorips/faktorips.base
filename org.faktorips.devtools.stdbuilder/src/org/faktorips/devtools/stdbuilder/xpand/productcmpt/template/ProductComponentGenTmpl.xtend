package org.faktorips.devtools.stdbuilder.xpand.productcmpt.template

import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass

import static org.faktorips.devtools.core.builder.naming.BuilderAspect.*
import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.MethodsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.TableUsagesTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.CommonDefinitionsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.template.ClassNamesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.template.CommonGeneratorExtensionsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.template.DerivedUnionAssociationTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.MethodNamesTmpl.*

class ProductComponentGenTmpl {

    def static  String body (XProductCmptGenerationClass it) '''

            /**
            «IF generatePublishedInterfaces»
             * «localizedJDoc("CLASS", interfaceName)»
            «ELSE»
             * «localizedJDocOrDescription("CLASS_NO_INTERFACE", generationConceptNameSingular, productCmptClassNode.name, description)»
            «ENDIF»
            «IF !productCmptType.changingOverTime»
             * «localizedJDoc("DEPRECATED_CLASS", generationConceptNamePlural)»
            «ENDIF»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
            «IF !productCmptType.changingOverTime»
             @Deprecated
            «ENDIF»
            public «isAbstract(it)» class «implClassName» extends «superclassName» «implementedInterfaces(it)»
             {

                «FOR it : associations» «privateConstants» «ENDFOR»


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
                 * «localizedJDoc("CONSTRUCTOR", getSimpleName(IMPLEMENTATION))»
                 * «getAnnotations(ELEMENT_JAVA_DOC)»
                 * @generated
                 */
                public «method(implClassName, productCmptClassNode.implClassName, "productCmpt")» {
                    super(productCmpt);
                    «FOR it : attributesInclOverwritten» «setDefaultValue» «ENDFOR»

                }

                «FOR it : attributesInclOverwritten»
                    «IF generateAbstractGetter»
                        «abstractGetter»
                    «ENDIF»
                «ENDFOR»


                «FOR it : attributes» «getterSetter» «ENDFOR»

           «FOR it : configuredAttributes» «getter» «ENDFOR»

                «FOR it : associations» «getterSetterAdder» «ENDFOR»

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
                 * «inheritDoc»
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
                     * «inheritDoc»
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
             * «inheritDocOrJavaDocIf(genInterface, "METHOD_GET_PRODUCTCMPT_IN_GEN", name, generationConceptNameSingular)»
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
             * «inheritDocOrJavaDocIf(genInterface, "METHOD_CREATE_POLICY_CMPT_IN_GEN")»
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