package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.ValidationRuleTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.template.PolicyCmptCreateBuilderTmpl
import org.faktorips.devtools.model.builder.settings.ValueSetMethods

class PolicyCmptInterfaceTmpl {

    def static String body(XPolicyCmptClass it) '''

        /**
        * «localizedJDoc("INTERFACE", name)» «description»
       «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(AnnotatedJavaElementType.PUBLISHED_INTERFACE_CLASS)»
        «getAnnotationsForPublishedInterface(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS, genInterface())»
        public interface «interfaceName» «IF extendsInterface» extends «FOR extendedInterface : extendedInterfaces SEPARATOR  ","»«extendedInterface»«ENDFOR» «ENDIF»
            {
                «IF generatePolicyBuilder && !abstract»
                    /**
                    * @generated
                    */
                    public static final «policyBuilderModelNode.factoryImplClassName» NEW = new «policyBuilderModelNode.factoryImplClassName»();
                «ENDIF»

                «FOR it : associations» «constants» «ENDFOR»
                «FOR it : validationRules» «constants» «ENDFOR»

                «FOR it : attributesIncludingAbstract»
                    «IF published»
                        «constantForPropertyName»
                        «IF !abstract»
                          «constantForValueSet»
                        «ENDIF»
                    «ENDIF»
                «ENDFOR»

                «FOR it : attributes»
                    «IF published»
                        «PolicyCmptAttributeTmpl.constantField(it)»
                    «ENDIF»
                «ENDFOR»

                «FOR it : attributesIncludingAbstract»
                    «IF published»
                        «IF generateUnifiedMethodNameGetAllowedValues && notDuplicateMethodNameGetAllowedValues && notDuplicateMethodNameGetAllowedValuesWithOverride»
                            «allowedValuesMethod(ValueSetMethods.Unified)»
                        «ENDIF»
                        «IF generateDifferentMethodsByValueSetType»
                            «allowedValuesMethod(ValueSetMethods.ByValueSetType)»
                        «ENDIF»
                        «getter»
                        «setter»
                    «ENDIF»
                «ENDFOR»

                «FOR it : associations» «methods» «ENDFOR»

                «IF configured»
            «PolicyCmptTmpl.getAndSetProductComponent(productCmptNode)»
            «IF generateGenerationAccessMethods»
                «PolicyCmptTmpl.getAndSetProductComponentGeneration(productCmptGenerationNode)»
            «ENDIF»
                «ENDIF»

                «FOR method : methods» «MethodsTmpl.method(method)» «ENDFOR»

                «IF generatePolicyBuilder && !abstract»
            «PolicyCmptCreateBuilderTmpl.builder(policyBuilderModelNode)»
                «ENDIF»

                «IF generateCopySupport»
            «CopySupportTmpl.copyMethods(it)»
                 «ENDIF»

            }
    '''

}
