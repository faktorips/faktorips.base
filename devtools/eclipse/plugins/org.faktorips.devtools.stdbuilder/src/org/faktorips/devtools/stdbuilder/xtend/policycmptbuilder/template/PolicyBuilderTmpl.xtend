package org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xmodel.policycmptbuilder.XPolicyBuilder

import static extension org.faktorips.devtools.stdbuilder.xtend.builder.template.CommonBuilderNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType

class PolicyBuilderTmpl {

    def static String body(XPolicyBuilder it) '''
        
        /**
        *«localizedJDoc("CLASS", policyName)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public «isAbstract(it)» class «implClassName» «extendSuperclass» {
            «variableDeclaration»
        
            «constructors»
        
            «repositorySetter»
        
            «FOR attribute : attributesIncludingAbstract» «attributeSetter(it, attribute)» «ENDFOR»
            «IF hasSupertype»
                «FOR attribute: withThisGeneratorConfig(superAttributes)» «superAttributeSetter(it, attribute)» «ENDFOR»
            «ENDIF»
        
            «getPolicy»
            «IF !hasSupertype»
                «getRuntimeRepository»
            «ENDIF»
        
            «getPolicyClass»
        
            «IF !abstract»
                «from(it)»
            «ENDIF»
        
            «associationClass»
        
            «factoryClass»
        }
    '''

    def private static isAbstract(XPolicyBuilder it) '''
        «IF abstract» abstract «ENDIF»
    '''

    def private static extendSuperclass(XPolicyBuilder it) '''
        «IF hasSupertype»
            extends «supertype.implClassName»
        «ENDIF»
    '''

    // The method generates the two variables that are needed in this class. They are:
    // 1. The policy that is built by this class
    // 2. The RuntimeRepository in which existing product components are living
    def private static variableDeclaration(XPolicyBuilder it) '''
        «IF !hasSupertype»
            /**
            * @generated
            */
            private final «typeImplClassName» «field(variableName)»;
            
                /**
                *@generated
                */
                private «IRuntimeRepository» runtimeRepository;
        «ENDIF»
    '''

    // Internal constructors
    def private static constructors(XPolicyBuilder it) '''
        /**
        *«localizedJDoc("CONSTRUCTOR_WITH_REPO", implClassName)»
        *«localizedJDoc("CONSTRUCTOR_INTERNAL")»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        protected «method(implClassName, typeImplClassName, "policy", IRuntimeRepository, "runtimeRepository")»{
            «IF hasSupertype»
                super(policy, runtimeRepository);
            «ELSE»
                this.«variableName» = policy;
                this.runtimeRepository = runtimeRepository;
            «ENDIF»
        }
    '''

    def private static repositorySetter(XPolicyBuilder it) '''
        /**
        * @generated
        */
        «IF hasSupertype»@Override«ENDIF»
        public «implClassName» setRepository(«IRuntimeRepository» runtimeRepository) {
            «IF hasSupertype»
                super.setRepository(runtimeRepository);
            «ELSE»
                this.runtimeRepository = runtimeRepository;
            «ENDIF»
            return this;
        }
    '''

    // This method generates the setter method for an attribute. The setter method of the policy is then called for the attribute.
    // Returns the builder.
    def private static attributeSetter(XPolicyBuilder builder, XPolicyAttribute it) '''
        «IF !derived && !constant»
            /**
            *«localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
            *
            * @generated
            */
            «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
            «IF overwrite && !overwrittenAttribute.derived && !overwrittenAttribute.constant»@Override«ENDIF»
            «val parameterName = "new" + fieldName.toFirstUpper»
            public «builder.implClassName» «IF overwrite && overwrittenAttribute.abstract»«method(fieldName, overwrittenAttribute.javaClassName, parameterName)»«ELSE»«method(fieldName, javaClassName, parameterName)»«ENDIF»{
                «safeGetResult(builder)».«methodNameSetter»(«parameterName»);
                return this;
            }
        «ENDIF»
    '''

    // Generates setter for attributes of the supertypes in order to overwrite the return type
    def private static superAttributeSetter(XPolicyBuilder builder, XPolicyAttribute it) '''
        «IF !derived && !constant»
            /**
            *«localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
            *
            * @generated
            */
            «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
            @Override
            «val parameterName = "new" + fieldName.toFirstUpper»
            public «builder.implClassName» «method(fieldName, javaClassName, parameterName)»{
                «safeGetResult(builder)».«methodNameSetter»(«parameterName»);
                return this;
            }
        «ENDIF»
    '''

    def private static getPolicy(XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_GET_VALUE", policyName)»
        *
        * @generated
        */
        «IF hasSupertype»@Override«ENDIF»
        public «policyPublishedInterfaceName» «getResult» {
            «IF hasSupertype»
                return («policyPublishedInterfaceName») super.«getResult»;
            «ELSE»
                return «safeGetResult»;
            «ENDIF»
        }
    '''

    def private static getRuntimeRepository(XPolicyBuilder it) '''
        /**
        * @generated
        */
        public «IRuntimeRepository» «getRepository» {
            return runtimeRepository;
        }
    '''

    def private static getPolicyClass(XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_GET_CLASS")»
        *
        * @generated
        */
        public static Class<?> getPolicyClass() {
            return «typeImplClassName».class;
        }
    '''

    // A static method to create builder from a policy instance
    def static from(XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_FROM", name, policyName)»
        *
        * @generated
        */
        public static «implClassName» «from»(«policyPublishedInterfaceName» policy) {
            return new «implClassName»(«castToImplementation(typeImplClassName)»policy, null);
        }
        
        /**
        *«localizedJDoc("METHOD_FROM_REPO", name, policyName)»
        *
        * @generated
        */
        public static «implClassName» «from»(«policyPublishedInterfaceName» policy, «IRuntimeRepository» runtimeRepository) {
            return new «implClassName»(«castToImplementation(typeImplClassName)»policy, runtimeRepository);
        }
    '''

    def private static associationClass(XPolicyBuilder it) '''
        «IF builderAssociations.size > 0»
            /**
            *«localizedJDoc("METHOD_ASSOCIATION")»
            *
            * @generated
            */
            «overrideAnnotationIf(hasSuperAssociationBuilder)»
            public «associationBuilder» associate(){
                return new «associationBuilder()»(this);
            }
        «ENDIF»
        «IF builderAssociations.size > 0 || superBuilderAssociations.size > 0 »
            /**
            *«localizedJDoc("METHOD_ADD_ASSOCIATION")»
            *
            * @generated
            */
            «overrideAnnotationIf(hasSuperAssociationBuilder)»
            public «addAssociationBuilder» «add»{
                return new «addAssociationBuilder»(this);
            }
        «ENDIF»
        «PolicyAssociationBuilderTmpl::body(it)»
    '''

    def private static factoryClass(XPolicyBuilder it) '''
        «IF !abstract && generatePublishedInterfaces»
            /**
            *«localizedJDoc("CLASS_FACTORY", name)»
            *
            * @generated
            */
            public static class Factory {
                «PolicyCmptCreateBuilderTmpl::with(false, it)»
            }
        «ENDIF»
    '''

}
