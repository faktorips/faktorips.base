package org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template

import org.faktorips.devtools.model.builder.xmodel.productcmptbuilder.XProductBuilder
import org.faktorips.devtools.model.builder.xmodel.productcmptbuilder.XProductBuilderAssociation

import static org.faktorips.devtools.stdbuilder.xtend.builder.template.CommonBuilderNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class ProductAssociationBuilderTmpl {

    def package static body (XProductBuilder it) '''
        «IF builderAssociations.size > 0»
            /**
            *«localizedJDoc("CLASS_BUILDER")»
            *
            * @generated
            */
            public static class AssociationBuilder «extendSuperclassAssociation» {

                «productBuilderField»

                «constructorWithProductBuilder(false, it)»

                «FOR association : builderAssociations» «associationEvaluation(it, association)» «ENDFOR»

                «get»

                «getCurrentGeneration»

                «getRepository(it)»
            }
        «ENDIF»

        «IF builderAssociations.size > 0 || superBuilderAssociations.size > 0 »
            /**
            *«localizedJDoc("CLASS_BUILDER_MULTI", implClassName)»
            *
            * @generated
            */
            public static class «addAssociationBuilder()» «extendSuperclassAssociations»{

                «productBuilderField»

                «constructorWithProductBuilder(true, it)»

                «FOR association : builderAssociations» «associationsEvaluation(it, false, association)» «ENDFOR»
                «FOR association : superBuilderAssociations» «associationsEvaluation(it, true, association)» «ENDFOR»

                /**
                * @generated
                */
                «overrideAnnotationIf(hasSuperAssociationBuilder)»
                protected «implClassName» done() {
                    «IF hasSuperAssociationBuilder»
                        return («implClassName») super.done();
                    «ELSE»
                        return productBuilder;
                    «ENDIF»
                }

                «get»

                «getCurrentGeneration»

                «getRepository(it)»
            }
        «ENDIF»
    '''

    //Adds the extension of the super class of the policy
    def private static extendSuperclassAssociation (XProductBuilder it) '''
        «IF hasSuperAssociationBuilder»
            extends «superBuilderForAssociationBuilder.implClassName».AssociationBuilder
        «ENDIF»
    '''

    // Adds the extension of the super class of the policy
    def private static extendSuperclassAssociations (XProductBuilder it) '''
        «IF hasSuperAssociationBuilder»
            extends «supertype.implClassName».«addAssociationBuilder»
        «ENDIF»
    '''

    // An association can have up to 4 association setters.
    def private static associationEvaluation(XProductBuilder builder, XProductBuilderAssociation it) '''
        ««« for generic subtype
        «associationSetterWithType(builder, it, false, false, targetBuilderClass, false)»
        «IF matchingAssociation!==null»
            «associationSetterWithType(builder, it, false, false, targetBuilderClass, true)»
        «ENDIF»
        «IF !targetBuilderClass.abstract»
            «associationSetter(builder, it, false, false, targetBuilderClass, false)»
            «IF matchingAssociation!==null»
                «associationSetter(builder, it, false, false, targetBuilderClass, true)»
            «ENDIF»
        «ENDIF»
    '''

    def private static associationsEvaluation(XProductBuilder builder, boolean isSuper,
        XProductBuilderAssociation it) '''
        
        «standardAssociationSetter(builder, it, isSuper, targetBuilderClass, false)»
        «IF matchingAssociation!==null»
             «standardAssociationSetter(builder, it, isSuper, targetBuilderClass, true)»
        «ENDIF»
        
        «associationSetterWithType(builder, it, true, isSuper, targetBuilderClass, false)»
        «IF matchingAssociation!==null»
            «associationSetterWithType(builder, it, true, isSuper, targetBuilderClass, true)»
        «ENDIF»
        «IF !targetBuilderClass.abstract»
            «associationSetter(builder, it, true, isSuper, targetBuilderClass, false)»
            «IF matchingAssociation!==null»
                «associationSetter(builder, it, true, isSuper, targetBuilderClass, true)»
            «ENDIF»
        «ENDIF»
    '''

    def private static standardAssociationSetter(XProductBuilder it, XProductBuilderAssociation association,
        boolean isSuper, XProductBuilder targetBuilder, boolean withCardinality) '''
        /**
        *«localizedJDoc("METHOD_ASS_STD", targetBuilder.productName, association.name)»
        *
        * @generated
        */
        «IF isSuper || (association.needOverrideForConstrainNewChildMethod && association.getAssociation.target==association.getAssociation.findSuperAssociationWithSameName(association.ipsProject).target)» @Override «ENDIF»
        public «implClassName» «IF withCardinality»«association.method(association.methodName, targetBuilder.productPublishedInterfaceName, "targetProduct", CardinalityRange, "cardinality" )»«ELSE»«association.method(association.methodName, targetBuilder.productPublishedInterfaceName, "targetProduct")»«ENDIF»{
            «IF isSuper»
                super.«association.methodName»(targetProduct«IF withCardinality», cardinality«ENDIF»);
            «ELSE»
                «getProdOrGen(it, association.changingOverTime, targetBuilder)».«association.methodNameSetOrAdd»(targetProduct«IF withCardinality», cardinality«ENDIF»);
            «ENDIF»
            return done();
        }
    '''

    def private static associationSetterWithType(XProductBuilder it, XProductBuilderAssociation association,
        boolean multi, boolean isSuper, XProductBuilder targetBuilder, boolean withCardinality) '''
        /**
        *«localizedJDoc("METHOD_ASS_TYPE_PROD", targetBuilder.productName, association.name)»
        *
        * @generated
        */
        «IF isSuper || (association.needOverrideForConstrainNewChildMethod && association.getAssociation.target==association.getAssociation.findSuperAssociationWithSameName(association.ipsProject).target)»@Override«ENDIF»
        public «returnType(it, multi, true, targetBuilder)» «association.methodName»(«targetBuilderType(multi, targetBuilder)» targetBuilder«IF withCardinality», «CardinalityRange» cardinality«ENDIF») {
            «IF isSuper»
                super.«association.methodName»(targetBuilder«IF withCardinality», cardinality«ENDIF»);
            «ELSE»
                «getProdOrGen(it, association.changingOverTime, targetBuilder)».«association.methodNameSetOrAdd»(targetBuilder.«getResult()»«IF withCardinality», cardinality«ENDIF»);
            «ENDIF»
            return «returnValue(multi, targetBuilder)»;
        }
    '''

    def private static getProdOrGen(XProductBuilder it, boolean changingOverTime, XProductBuilder targetBuilder) '''
        «IF changingOverTime»
            «castToImplementation(prodGenImplClassName, "getCurrentGeneration()")»
        «ELSE»
            «castToImplementation(typeImplClassName, getResult)»
        «ENDIF»
    '''

    def private static associationSetter(XProductBuilder it, XProductBuilderAssociation association, boolean multi,
        boolean isSuper, XProductBuilder targetBuilder, boolean withCardinality) '''
        /**
        *«localizedJDoc("METHOD_ASS_PROD", targetBuilder.productName, association.name)»
        *
        * @generated
        */
        «IF isSuper || (association.needOverrideForConstrainNewChildMethod)» @Override «ENDIF»
        public «returnType(it, multi, false, targetBuilder)» «IF withCardinality»«association.method(association.methodName, "String", "productCmptId", CardinalityRange, "cardinality")»«ELSE»«association.method(association.methodName, "String", "productCmptId")»«ENDIF» {
            «IF isSuper»
                super.«association.methodName»(productCmptId«IF withCardinality», cardinality«ENDIF»);
                return done();
            «ELSE»
                «IF targetBuilder.generatePublishedInterfaces»
                    «targetBuilder.implClassName» targetBuilder = new «targetBuilder.factoryImplClassName»().«builder(getRepository, "productCmptId")»;
                «ELSE»
                    «targetBuilder.implClassName» targetBuilder = «targetBuilder.typeImplClassName».«builder(getRepository, "productCmptId")»;
                «ENDIF»
                «getProdOrGen(it, association.changingOverTime, it)».«association.methodNameSetOrAdd»(targetBuilder.«getResult()»«IF withCardinality», cardinality«ENDIF»);
                return «returnValue(multi, targetBuilder)»;
            «ENDIF»
        }
        
        /**
        *«localizedJDoc("METHOD_ASS_IDS", targetBuilder.productName, association.name)»
        *
        * @generated
        */
        «IF isSuper || (association.needOverrideForConstrainNewChildMethod)» @Override «ENDIF»
        public «returnType(it, multi, false, targetBuilder)» «IF withCardinality»«association.method(association.methodName,"String","id","String","kindId","String","versionId", CardinalityRange, "cardinality")»«ELSE»«association.method(association.methodName,"String","id","String","kindId","String","versionId")»«ENDIF»{
            «IF isSuper»
                super.«association.methodName»(id, kindId, versionId«IF withCardinality», cardinality«ENDIF»);
                return done();
            «ELSE»
                «IF targetBuilder.generatePublishedInterfaces»
                    «targetBuilder.implClassName» targetBuilder = new «targetBuilder.factoryImplClassName»().«builder(getRepository(), "id", "kindId", "versionId")»;
                «ELSE»
                    «targetBuilder.implClassName» targetBuilder = «targetBuilder.typeImplClassName».«builder(getRepository(), "id", "kindId", "versionId")»;
                «ENDIF»
                «getProdOrGen(it, association.changingOverTime, targetBuilder)».«association.methodNameSetOrAdd»(targetBuilder.«getResult()»«IF withCardinality», cardinality«ENDIF»);
                return «returnValue(multi, targetBuilder)»;
            «ENDIF»
        }
    '''

    def private static returnType(XProductBuilder it, boolean multi, boolean gen, XProductBuilder targetBuilder) '''
        «IF multi» «implClassName»
        «ELSEIF gen»<T extends «targetBuilder.implClassName»> T
        «ELSE»«targetBuilder.implClassName»
        «ENDIF»
    '''

    def package static targetBuilderClass(boolean multi, XProductBuilder it) '''
        «IF multi»Class<? extends «implClassName»>
        «ELSE»Class<T>
        «ENDIF»
    '''

    def private static targetBuilderType(boolean multi, XProductBuilder it) '''
        «IF multi»«implClassName»
        «ELSE»T
        «ENDIF»
    '''

    def private static returnValue(boolean multi, XProductBuilder it) '''
        «IF multi» done()
        «ELSE»    targetBuilder
        «ENDIF»
    '''

    def private static productBuilderField (XProductBuilder it) '''
        «IF !hasSuperAssociationBuilder»
            /**
            * @generated
            */
            private «implClassName» productBuilder;
        «ENDIF»
    '''

    def private static constructorWithProductBuilder(boolean multi, XProductBuilder it) '''
        /**
        * @generated
        */
        protected «IF multi»«addAssociationBuilder» «ELSE»«associationBuilder» «ENDIF»(«implClassName» productBuilder){
            «IF hasSuperAssociationBuilder»
                super(productBuilder);
            «ELSE»
                this.productBuilder = productBuilder;
            «ENDIF»
        }
    '''

    def private static get (XProductBuilder it) '''
        /**
        * @generated
        */
        «IF hasSuperAssociationBuilder» @Override «ENDIF»
        protected «productPublishedInterfaceName» «getResult» {
            «IF hasSuperAssociationBuilder»
                return («productPublishedInterfaceName») super.«getResult»;
            «ELSE»
                return productBuilder.«getResult»;
            «ENDIF»
        }
    '''

    def private static getCurrentGeneration (XProductBuilder it) '''
        «IF changingOverTime»
            /**
            * @generated
            */
            «IF hasSuperAssociationBuilder» @Override «ENDIF»
            protected «prodGenPublishedInterfaceName» getCurrentGeneration() {
                «IF hasSuperAssociationBuilder»
                    return («prodGenPublishedInterfaceName») super.getCurrentGeneration();
                «ELSE»
                    return productBuilder.getCurrentGeneration();
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static getRepository (XProductBuilder it) '''
        «IF !hasSuperAssociationBuilder»
        /**
        * @generated
        */
        protected «IModifiableRuntimeRepository» «getRepository» {
            return productBuilder.«getRepository»;
        }
        «ENDIF»
    '''

}