package org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template

import org.faktorips.devtools.model.builder.xmodel.productcmptbuilder.XProductBuilder
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template.ProductBuilderNamesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.builder.template.CommonBuilderNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class ProductCmptCreateBuilderTmpl {

    //Methods to initialize a builder.
    //If no published interfaces are generated, with methods are static methods in the product classes. Else, they are in the factory class
    def static with(boolean isStatic, XProductBuilder it) '''
        /**
        *«localizedJDoc("METHOD_WITH_IDS_DEFAULT",productName)»
        «IF changingOverTime»*«localizedJDoc("METHOD_WITH_IDS_DEFAULT_GEN")»«ENDIF»
        *
        * @generated
        */
        public «IF isStatic»static«ENDIF» «implClassName» «builder(IModifiableRuntimeRepository + " runtimeRepository", "String id", "String kindId", "String versionId")» {
            «typeImplClassName» product = new «typeImplClassName»(runtimeRepository, id, kindId, versionId);
            product.«setValidFrom("new " + DateTime + "(1900,1,1)")»;
            runtimeRepository.«putProductComponent("product")»;

            «IF changingOverTime»
«««Create productGen and sets the date
                «prodGenImplClassName» generation = new «prodGenImplClassName»(product);
                generation.«setValidFrom("new " + DateTime + "(1900,1,1)")»;
                runtimeRepository.«putProductCmptGeneration("generation")»;

                return new «implClassName»(product, runtimeRepository, generation);
            «ELSE»
                return new «implClassName»(product, runtimeRepository);
            «ENDIF»
        }

        /**
        *«localizedJDoc("METHOD_WITH_IDS",productName)»
        «IF changingOverTime»*«localizedJDoc("METHOD_WITH_IDS_DEFAULT_GEN")»«ENDIF»
        *
        * @generated
        */
        public «IF isStatic»static«ENDIF» «implClassName» «builder(IModifiableRuntimeRepository + " runtimeRepository", "String id", "String kindId", "String versionId", DateTime + " validFrom")» {
            «typeImplClassName» product = new «typeImplClassName»(runtimeRepository,id,kindId,versionId);
            product.«setValidFrom("validFrom")»;
            runtimeRepository.«putProductComponent("product")»;

            «IF changingOverTime»
                «prodGenImplClassName» generation = new «prodGenImplClassName»(product);
                generation.«setValidFrom("validFrom")»;
                runtimeRepository.«putProductCmptGeneration("generation")»;

                return new «implClassName»(product, runtimeRepository, generation);
            «ELSE»
                return new «implClassName»(product, runtimeRepository);
            «ENDIF»
        }

        /**
        *«localizedJDoc("METHOD_WITH_PRODID", typeImplClassName)»
        *
        * @generated
        */
        public «IF isStatic»static«ENDIF» «implClassName» «builder(IModifiableRuntimeRepository + " runtimeRepository", "String prodCmptId")» {
            «typeImplClassName» product =  («typeImplClassName») runtimeRepository.getProductComponent(prodCmptId);

            if(product == null){
                throw new «RuntimeException»("«localizedText("EXCEPTION_NO_PRODCMPT_FOUND")»");
            }else{
                return «implClassName».«from("product", "runtimeRepository")»;
            }
        }
    '''

    // Method to get a builder from a product
    def static builder(XProductBuilder it) '''
        /**
        *«localizedJDoc("METHOD_BUILDER", name)»
        *
        * @generated
        */
        «overrideAnnotationForPublishedMethodImplementationOr(hasNonAbstractSupertype)»
        public «implClassName» «modify()» «IF genInterface»;
        «ELSE»
            {
                return «implClassName».«from("this", "(" + IModifiableRuntimeRepository + ") this.getRepository()")»;
            }
        «ENDIF»
    '''
}
