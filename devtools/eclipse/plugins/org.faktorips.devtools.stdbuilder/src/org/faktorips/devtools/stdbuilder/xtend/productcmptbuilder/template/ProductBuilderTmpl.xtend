package org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAttribute
import org.faktorips.devtools.stdbuilder.xmodel.productcmptbuilder.XProductBuilder

import static org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template.ProductBuilderNamesTmpl.*
import static org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template.ProductCmptCreateBuilderTmpl.*

import static extension org.faktorips.devtools.stdbuilder.xtend.builder.template.CommonBuilderNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType

class ProductBuilderTmpl{

    def static String body (XProductBuilder it) '''
        /**
        *«localizedJDoc("CLASS",implClassName,productName)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
        *
        * @generated
        */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public «isAbstract(it)» class «implClassName» «extendSuperclass»{
            «variableDeclaration»

            «constructors»
            «FOR attribute : attributes»«attributeSetter(attribute,false)»«ENDFOR»

            «IF hasSupertype»
                ««« Generates setter for attributes of the supertypes that are not overwritten in order to override the return type
                «FOR attribute : superAttributes»«attributeSetter(attribute,true)»«ENDFOR»
            «ENDIF»
            
            «IF configurationForPolicyCmptType»
                «FOR configuredAttribute : configuredAttributes»
                    «defaultSetter(configuredAttribute,false)»
                    «allowedValuesSetter(configuredAttribute,false)»
                «ENDFOR»
            
                «IF hasSupertype»
                    «FOR configuredAttribute : configuredSuperAttributes»
                      «defaultSetter(configuredAttribute,true)»
                      «allowedValuesSetter(configuredAttribute,true)»
                    «ENDFOR»
                «ENDIF»
            «ENDIF»

            «IF changingOverTime && !isAbstract»
                «anpSetter»
            «ENDIF»

            «getBuilderValue»

            «getProductClass»

            «IF !isAbstract»
                «from(it)»
            «ENDIF»

            «associationClass»

            «factoryClass»
        }
'''

def private static  isAbstract (XProductBuilder it) '''
    «IF isAbstract» abstract    «ENDIF»
'''

def private static extendSuperclass (XProductBuilder it) '''
    «IF hasSupertype»
        extends «supertype.implClassName»
    «ENDIF»
'''


//    The method generates variables that are needed in this class. They are:
//    1. The IModifiableRuntimeRepository, which is needed to add changes to the product class at runtime.
//    2. The product that is actually built by the class.
//    In case the product is changing over time, a field is also generated to store a generation to edit.
//
//    Note that if a product has a generation, the supertype also has generation.

def private static variableDeclaration (XProductBuilder it) '''
    «IF !hasSupertype»
        /**
        * @generated
        */
        private final «IModifiableRuntimeRepository» runtimeRepository;

        /**
        * @generated
        */
        private final «typeImplClassName» «field(variableName)»;

        «IF changingOverTime»
            /**
            * @generated
            */
            private «prodGenImplClassName» «field(prodGenFieldName)»;

            /**
            * @generated
            */
            protected void «methodNameSetGeneration»(«prodGenImplClassName» currentGeneration){
                this.«prodGenFieldName» = currentGeneration;
            }
        «ENDIF»
    «ENDIF»
'''

//Internal constructors
// The constructor needs a product and InMemeryRuntimeReopsitory, both should not be null.

def private static constructors (XProductBuilder it) '''
    /**
    *«localizedJDoc("CONSTRUCTOR_INTERNAL", implClassName)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
    *
    * @generated
    */
    «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
    «IF changingOverTime»
        protected «method(implClassName, typeImplClassName, "product", IModifiableRuntimeRepository, "runtimeRepository", prodGenImplClassName, "currentGeneration")»{
    «ELSE»
        protected «method(implClassName, typeImplClassName, "product", IModifiableRuntimeRepository, "runtimeRepository")»{
    «ENDIF»
        «IF hasSupertype()»««« supertype has to be changing too
            super(product, runtimeRepository «IF changingOverTime», currentGeneration«ENDIF»);
        «ELSE»
            if(product == null ||  runtimeRepository == null){
                throw new «RuntimeException»("«localizedText("EXCEPTION_CONSTR_NULL")»");
            }else{
                runtimeRepository.«getExistingProductComponent("product.getId()")»;

                this.runtimeRepository = runtimeRepository;
                this.«variableName» = product;
                «IF changingOverTime» this.«prodGenFieldName» = currentGeneration;«ENDIF»
            }
        «ENDIF»
        }
'''

def private static attributeSetter(XProductBuilder builder, XProductAttribute it, boolean overrideSuper) '''
    /**
    *«localizedJDoc("METHOD_SETVALUE", name, descriptionForJDoc)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
    *
    * @generated
    */
    «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
    «IF overrideSuper || (overwrite && !overwrittenAttribute.isAbstract)» @Override «ENDIF»
    public «builder.implClassName» «method(fieldName,javaClassName,fieldName)»{
        «IF changingOverTime»
            «builder.prodGenFieldName».«methodNameSetter»(«fieldName»);
        «ELSE»
            «safeGetResult(builder)».«methodNameSetter»(«fieldName»);
        «ENDIF»
        return this;
    }
'''

def private static defaultSetter(XProductBuilder builder, XPolicyAttribute it, boolean overrideSuper) '''
    /**
    *«localizedJDoc("METHOD_SET_DEFAULTVALUE", name, descriptionForJDoc)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
    *
    * @generated
    */
    «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
    «IF overrideSuper || (overwrite && !overwrittenAttribute.abstract 
        && overwrittenAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue && overwrittenAttribute.productRelevantInHierarchy 
        && builder.hasSupertype && builder.supertype.configurationForPolicyCmptType)» @Override «ENDIF»
    public «builder.implClassName» «method(fieldName+"Default",javaClassName,fieldName)»{
        «IF changingOverTime»
            «builder.prodGenFieldName».«methodNameSetDefaultValue»(«fieldName»);
        «ELSE»
            «safeGetResult(builder)».«methodNameSetDefaultValue»(«fieldName»);
        «ENDIF»
        return this;
    }
'''

def private static allowedValuesSetter(XProductBuilder builder, XPolicyAttribute it, boolean overrideSuper) '''
    /**
    *«localizedJDoc("METHOD_SET_VALUESET", name, descriptionForJDoc)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
    *
    * @generated
    */
    «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
    «IF overrideSuper || (overwrite && !overwrittenAttribute.abstract 
        && overwrittenAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue && overwrittenAttribute.productRelevantInHierarchy 
        && builder.hasSupertype && builder.supertype.configurationForPolicyCmptType)» @Override «ENDIF»
    public «builder.implClassName» «method(fieldName+"AllowedValues",ValueSet(javaClassUsedForValueSet),fieldName)»{
        «IF changingOverTime»
            «builder.prodGenFieldName».«methodNameSetAllowedValuesFor»(«fieldName»);
        «ELSE»
            «safeGetResult(builder)».«methodNameSetAllowedValuesFor»(«fieldName»);
        «ENDIF»
        return this;
    }
'''



//        creates/gets generation builder
//    - look for the valid generation to given date
//      * found and validFrom same as given date -> replace if not the same is already stored
//      * else -> create a new one with the given validFrom date

def private static anpSetter (XProductBuilder it) '''
    /**
    *«localizedJDoc("METHOD_ANP_SETTER", productName)»
    *
    * @generated
    */
    «IF needOverrideForProductGenSetter»@Override«ENDIF»
    public «implClassName» «methodNameGeneration»(int year, int month, int day){
        «DateTime» genDate= new «DateTime»(year, month, day);
        «prodGenImplClassName» generation = («prodGenImplClassName») «getRepository»
                .«getProductCmptGeneration(safeGetResult + ".getId()", "genDate.toGregorianCalendar(" + TimeZone + ".getDefault())")»;

        if (generation == null || !genDate.equals(generation.getValidFrom())) {
            generation = new «prodGenImplClassName»(«safeGetResult»);
            generation.«setValidFrom("new " + DateTime +"(year, month, day)")»;
            «IF hasSupertype»
                super.«getRepository».«putProductCmptGeneration("generation")»;
            «ELSE»
                runtimeRepository.«putProductCmptGeneration("generation")»;
            «ENDIF»
        }
        «methodNameSetGeneration»(generation);
        return this;
    }

    /**
    *«localizedJDoc("METHOD_LATEST_GEN")»
    *
    * @generated
    */
    «IF needOverrideForProductGenSetter»@Override«ENDIF»
    public «implClassName» «methodNameSetLatestGeneration»() {
        «methodNameSetGeneration»((«prodGenImplClassName») «getRepository»
            .«getLatestProductComponentGeneration(safeGetResult)»);

        «««Nullpointer exception earlier in getLaterstProductComponentGeneration, if no such generation exists....
        return this;
    }
'''

def private static getBuilderValue (XProductBuilder it) '''
    «IF !hasSupertype»
        /**
        *«localizedJDoc("METHOD_GET_RUNTIMEREPOSITORY")»
        *
        * @generated
        */
        public «IModifiableRuntimeRepository» «getRepository» {
            return this.runtimeRepository;
        }
    «ENDIF»

    /**
    *«localizedJDoc("METHOD_GET_VALUE", productName)»
    *
    * @generated
    */
    «IF hasSupertype»@Override«ENDIF»
    public «productPublishedInterfaceName» «getResult» {
        «IF hasSupertype»
            return («productPublishedInterfaceName») super.«getResult»;
        «ELSE»
            return «safeGetResult»;
        «ENDIF»
    }

    «IF changingOverTime»
        /**
        *«localizedJDoc("METHOD_GET_GENERATION", prodGenImplClassName)»
        *
        * @generated
        */
        «IF hasSupertype»@Override«ENDIF»
        public «prodGenPublishedInterfaceName» getCurrentGeneration() {
            «IF hasSupertype»
                return («prodGenPublishedInterfaceName») super.getCurrentGeneration();
            «ELSE»
                return «prodGenFieldName»;
            «ENDIF»
        }

        /**
        *«localizedJDoc("METHOD_GET_LATEST_GEN", prodGenImplClassName)»
        *
        * @generated
        */
        «IF hasSupertype»@Override«ENDIF»
        public «prodGenPublishedInterfaceName» «methodNameGetLatestGeneration»() {
            «IF hasSupertype»
                return («prodGenPublishedInterfaceName») super.«methodNameGetLatestGeneration»();
            «ELSE»
                return («prodGenPublishedInterfaceName») runtimeRepository.«getLatestProductComponentGeneration(safeGetResult)»;
            «ENDIF»
        }
    «ENDIF»
'''

//internal method used for association setter with generic type  if (no interfaces are generated. BUILD has)  to be called from the product class
def private static getProductClass (XProductBuilder it) '''
    /**
    *«localizedJDoc("METHOD_GET_CLASS")»
    *
    * @generated
    */
    public static Class<?> getProductClass() {
        return «typeImplClassName».class;
    }
'''

//A static method to create builder from a product instance.
def private static from (XProductBuilder it) '''
    «IF changingOverTime»
        /**
        *«localizedJDoc("METHOD_FROM_REPO_CHANGING", name, productName)»
        *
        * @generated
        */
        public static «implClassName» «from»(«productPublishedInterfaceName» product, «IModifiableRuntimeRepository» runtimeRepository, «prodGenImplClassName» currentGeneration) {
            return new «implClassName»(«castToImplementation(typeImplClassName)» product, runtimeRepository, currentGeneration);
        }
    «ENDIF»

    /**
    *«localizedJDoc("METHOD_FROM_REPO", name, productName)»
    «IF changingOverTime» *«localizedJDoc("METHOD_FROM_REPO_CHANGING_LATEST")»«ENDIF»
    *
    * @generated
    */
    public static «implClassName» «from»(«productPublishedInterfaceName» product, «IModifiableRuntimeRepository» runtimeRepository) {
        «IF changingOverTime»
            return new «implClassName»(«castToImplementation(typeImplClassName)»product, runtimeRepository, («prodGenImplClassName») runtimeRepository.«getLatestProductComponentGeneration("product")»);
        «ELSE»
            return new «implClassName»(«castToImplementation(typeImplClassName)»product, runtimeRepository);
        «ENDIF»
    }
'''

def private static associationClass (XProductBuilder it) '''
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
        public «addAssociationBuilder» «add» {
            return new «addAssociationBuilder»(this);
        }
    «ENDIF»
    «ProductAssociationBuilderTmpl.body(it)»
'''

def private static factoryClass (XProductBuilder it) '''
    «IF !isAbstract && generatePublishedInterfaces»
        /**
        *«localizedJDoc("CLASS_FACTORY", name)»
        *
        * @generated
        */
        public static class Factory {
            «with(false, it)»
        }
    «ENDIF»
'''

}
