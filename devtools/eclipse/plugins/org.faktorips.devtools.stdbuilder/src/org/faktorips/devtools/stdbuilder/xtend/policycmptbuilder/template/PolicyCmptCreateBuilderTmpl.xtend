package org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmptbuilder.XPolicyBuilder


import static extension org.faktorips.devtools.stdbuilder.xtend.builder.template.CommonBuilderNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class PolicyCmptCreateBuilderTmpl {

    //Methods to initialize a builder.
    //If a published interface is generated, this method is generated in the builder factory class. If not, this method should be in the policy class and static.
    def static with(boolean isStatic, XPolicyBuilder it) '''

    «««    new policy, no repository
        /**
        *«localizedJDoc("METHOD_WITH_NO_ARG", implClassName)»
        *
        * @generated
        */
        public «IF isStatic»static«ENDIF» «implClassName» «builder» {
            return «implClassName».«from("new " + typeImplClassName + "()", "null")»;
        }

    «««    new policy, with repository
        /**
        *«localizedJDoc("METHOD_WITH_NO_ARG", implClassName)»
        *«localizedJDoc("METHOD_WITH_REPO")»
        *
        * @generated
        */
        public «IF isStatic»static«ENDIF» «implClassName» «builder(IRuntimeRepository + " runtimeRepository")» {
            return «implClassName».«from("new " + typeImplClassName + "()", "runtimeRepository")»;
        }

        «IF configured»
            /**
            *«localizedJDoc("METHOD_WITH_PROD", implClassName)»
            *
            * @generated
            */
            public «IF isStatic»static«ENDIF» «implClassName» «builder(productCmptClassName + " productCmpt")» {
                return «implClassName».«from("new " + typeImplClassName + "(productCmpt)", "productCmpt." + getRepository)»;
            }

    «««        policy from repository with id
            /**
            *«localizedJDoc("METHOD_WITH_PROD_ID", implClassName)»
            «IF productCmptNode.changingOverTime»*«localizedJDoc("METHOD_WITH_PROD_ID_GEN")»«ENDIF»
            *
            * @generated
            */
            public «IF isStatic»static«ENDIF» «implClassName» «builder(IRuntimeRepository + " runtimeRepository", "String productCmptId")» {
                «productCmptClassName» product = («productCmptClassName») runtimeRepository.«getProductComponent("productCmptId")»;
                if(product == null){
                    throw new «RuntimeException»("«localizedText("EXCEPTION_NO_PRODCMPT_FOUND")»");
                } else{
                    «IF productCmptNode.changingOverTime»
                        «productCmptGenerationNode.implClassName» generation = («productCmptGenerationNode.implClassName») product.«getLatestProductComponentGeneration»;
                        if(generation == null) {
                                throw new «RuntimeException»("«localizedText("EXCEPTION_NO_GENERATION_FOUND")»");
                        }
                        «typeImplClassName» policy = «castToImplementation(typeImplClassName)» generation.create«policyName»();
                    «ELSE»
                        «typeImplClassName» policy = «castToImplementation(typeImplClassName)» product.create«policyName»();
                    «ENDIF»

                    policy.«initialize»;
                    return «implClassName».«from("policy", "runtimeRepository")»;
                }
            }

            «IF productCmptNode.changingOverTime»
    «««        policy from repository with id and validity date
            /**
            *«localizedJDoc("METHOD_WITH_PROD_ID_DATE", implClassName)»
            *
            * @generated
            */
            public «IF isStatic»static«ENDIF» «implClassName» «builder(IRuntimeRepository + " runtimeRepository", "String productCmptId", Calendar + " validityDate")» {
                «productCmptClassName» product = («productCmptClassName») runtimeRepository.«getProductComponent("productCmptId")»;
                if(product == null) {
                    throw new «RuntimeException»("«localizedText("EXCEPTION_NO_PRODCMPT_FOUND")»");
                }
                «productCmptGenerationNode.implClassName» generation = («productCmptGenerationNode.implClassName») product.«getGenerationBase("validityDate")»;
                if(generation == null) {
                    throw new «RuntimeException»("«localizedText("EXCEPTION_NO_GENERATION_FOUND")»");
                }
                «typeImplClassName» policy = «castToImplementation(typeImplClassName)» generation.create«policyName»();
                policy.«initialize»;
                return «implClassName».«from("policy", "runtimeRepository")»;
            }
            «ENDIF»
        «ENDIF»
    '''

    //Methods to get a builder from a policy
    def static builder (XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_BUILDER", name)»
        *
        * @generated
        */
        «overrideAnnotationForPublishedMethodImplementationOr(hasNonAbstractSupertype)»
        public «implClassName» «modify» «IF genInterface»;
        «ELSE»
            {
                «IF configured»
                    return «implClassName».«from("this", getProductComponent + "." + getRepository)»;
                «ELSE»
                    return «implClassName».«from("this", "null")»;
                «ENDIF»
            }
        «ENDIF»

        /**
        *«localizedJDoc("METHOD_BUILDER_REPO",name)»
        *
        * @generated
        */
        «overrideAnnotationForPublishedMethodImplementationOr(hasNonAbstractSupertype)»
        public «implClassName» «modify(IRuntimeRepository + " runtimeRepository")» «IF genInterface»;
        «ELSE»
            {
                return «implClassName».«from("this", "runtimeRepository")»;
            }
        «ENDIF»
    '''
}