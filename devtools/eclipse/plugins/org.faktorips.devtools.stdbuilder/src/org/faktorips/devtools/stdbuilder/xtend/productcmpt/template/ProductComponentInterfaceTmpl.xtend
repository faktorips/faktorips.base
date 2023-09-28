package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template.ProductCmptCreateBuilderTmpl.*

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.MethodsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductComponentTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType

class ProductComponentInterfaceTmpl{

    def static String body (XProductCmptClass it) '''
        /**
         *«localizedJDoc("INTERFACE", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
         «getAnnotations(PUBLISHED_INTERFACE_CLASS)»
         «getAnnotationsForPublishedInterface(PRODUCT_CMPT_DECL_CLASS, genInterface)»
        public interface «interfaceName»
        «IF extendsInterface»
            extends «FOR extendedInterface : extendedInterfaces SEPARATOR  ","» «extendedInterface» «ENDFOR»
        «ENDIF»
         {
             «IF generateProductBuilder && !abstract»
                /**
                * @generated
                */
                public static final «productBuilderModelNode.factoryImplClassName» NEW = new «productBuilderModelNode.factoryImplClassName»();
            «ENDIF»

             «FOR it : attributes»
                 «IF published »
                     «constantForPropertyName»
                     «IF !abstract»
                       «constantForValueSet»
                       «constantForDefaultValue»
                     «ENDIF»
                 «ENDIF»
             «ENDFOR»

             «FOR it : attributesIncludingNoContentGeneration»
«««                  TODO the old code generator generated the getter always to the published interface
«««                  !!! If you fix it you need to generate abstract getter for public-abstract attributes in ProductComponent
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
                    «IF !changingOverTime»
                        «IF !formulaSignature»
                            «MethodsTmpl.method(it)»
                        «ELSE»
                            «formulaMethod»
                        «ENDIF»
                    «ENDIF»
                «ENDIF»
            «ENDFOR»

            «IF generateGenerationAccessMethods»
                  «getProductComponentGeneration(productCmptGenerationNode)»
             «ENDIF»
            «IF configurationForPolicyCmptType»
                «createPolicyCmpt(policyCmptClass)»
            «ENDIF»

            «IF generateProductBuilder && !abstract»
                «builder(productBuilderModelNode)»
            «ENDIF»

         }
'''
}
