package org.faktorips.devtools.stdbuilder.xpand.productcmpt.template

import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductComponentGenTmpl.*

import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.MethodsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductAttributeTmpl.*

class ProductComponentGenInterfaceTmpl {

    def static String body (XProductCmptGenerationClass it) '''


            /**
             * «localizedJDoc("INTERFACE", generationConceptNameSingular, productCmptClassNode.name)»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
            «IF !productCmptType.changingOverTime»
             * «localizedJDoc("DEPRECATED_INTERFACE", generationConceptNamePlural)»
            «ENDIF»
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

                 «FOR it : attributesInclOverwritten»

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
                        «getter»
                    «ENDIF»
                «ENDFOR»

                «FOR it : associations» «getterSetterAdder» «ENDFOR»



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