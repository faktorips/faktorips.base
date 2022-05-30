package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductComponentGenTmpl.*

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.MethodsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAttributeTmpl.*
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType

class ProductComponentGenInterfaceTmpl {

    def static String body (XProductCmptGenerationClass it) '''


            /**
             *«localizedJDoc("INTERFACE", generationConceptNameSingular, productCmptClassNode.name)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
            «IF !productCmptType.changingOverTime»
             *
             *«localizedJDoc("DEPRECATED_INTERFACE", generationConceptNamePlural)»
            «ENDIF»
             *
             * @generated
             */
             «IF !productCmptType.changingOverTime»
             @Deprecated
            «ENDIF»
            «getAnnotations(PUBLISHED_INTERFACE_CLASS)»
            public interface «interfaceName»
            «IF extendsInterface»
                extends «FOR extendedInterface : extendedInterfaces SEPARATOR  ","» «extendedInterface» «ENDFOR»
            «ENDIF»
             {

                 «FOR it : attributes»
                     «IF published »
                         «constantForPropertyName»
                     «ENDIF»
                 «ENDFOR»

                 «FOR it : attributesIncludingNoContentGeneration»

    «««                  TODO the old code generator generated the getter always to the published interface
    «««                  !!! If you fix it you need to generate abstract getter for public-abstract attributes in ProductComponentGen
    «««                 «IF published »
    «««                 «ENDIF»

                     «IF generateInterfaceGetter»
                        «getter»
                    «ENDIF»
                 «ENDFOR»

                «FOR it : configuredAttributes»
                    «IF published»
                        «getterAndSetter»
                    «ENDIF»
                «ENDFOR»

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

                «FOR it : methods»
                    «IF published»
                        «IF changingOverTime»
                            «IF !formulaSignature»
                                «MethodsTmpl.method(it)»
                            «ELSE»
                                «formulaMethod»
                            «ENDIF»
                        «ENDIF»
                    «ENDIF»
                «ENDFOR»

                «getProductCmpt(productCmptClassNode)»

                «IF configurationForPolicyCmptType»
                    «createPolicyCmpt(productCmptClassNode, policyCmptClass)»
                «ENDIF»

    «««            «IF generateProductBuilder && !abstract»
    «««                «ProductCmptGenCreateBuilder.builder(productGenBuilderModelNode)»
    «««            «ENDIF»

        }
    '''
}