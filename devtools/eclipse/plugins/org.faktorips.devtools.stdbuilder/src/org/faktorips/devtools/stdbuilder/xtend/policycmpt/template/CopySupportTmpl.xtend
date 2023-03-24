package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass

import static org.faktorips.devtools.model.builder.naming.BuilderAspect.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class CopySupportTmpl {

    def package static copyMethods(XPolicyCmptClass it) '''

        «IF genInterface»
            «copyMethodsDecl»
        «ELSE»
            «copyMethodsImpl»
        «ENDIF»

    '''

    def private static copyMethodsDecl(XPolicyCmptClass it) '''

            /**
             *«inheritDoc»
             *
             * @generated
             */
            @Override
            public «CommonGeneratorExtensions::isAbstract(it)» «getClassName(genInterface())» «newCopy()»;

    '''

    def private static copyMethodsImpl(XPolicyCmptClass it) '''

           «IF !abstract»
               /**
                *«inheritDoc»
                *
                * @generated
                */
               @Override
               public «implClassName» «newCopy» {
                   «Map(IModelObject, IModelObject)» copyMap = new «HashMap»();
                   «implClassName» newCopy = «newCopyInternal("copyMap")»;
                   «copyAssociationsInternal("newCopy", "copyMap")»;
                   return newCopy;
               }
        «ELSE»
            «copyMethodsDecl»
        «ENDIF»

        /**
         *«localizedJDoc("METHOD_NEW_COPY_INTERNAL")»
         *
         * @generated
         */
        «overrideAnnotationIfHasSuperclass()»
        public «implClassName» «newCopyInternal(Map(IModelObject, IModelObject)+" copyMap")» {
            «IF !abstract»
                «implClassName» newCopy = («implClassName»)copyMap.get(this);
                if (newCopy == null) {
                    newCopy = new «implClassName»();
                    copyMap.put(this, newCopy);
                    «IF configured»
                        newCopy.copyProductCmptAndGenerationInternal(this);
                    «ENDIF»
                    «copyProperties("newCopy", "copyMap")»;
                }
                return newCopy;
            «ELSE»
                throw new RuntimeException(
                        "This method has to be abstract. It needs to have an empty body because of a bug in JMerge.");
            «ENDIF»
        }

        «IF firstConfigurableInHierarchy»
            /**
             *«localizedJDoc("METHOD_COPY_PRODUCT_AND_GENERATION")»
             *
             * @generated
             */
            protected void copyProductCmptAndGenerationInternal(«getSimpleName(IMPLEMENTATION)» otherObject) {
                productConfiguration.copy(otherObject.productConfiguration);
            }
        «ENDIF»

        /**
         *«localizedJDoc("METHOD_COPY_PROPERTIES")»
         *
         * @generated
         */
        «overrideAnnotationIfHasSuperclass»
        protected void «copyProperties(IModelObject+" copy", Map(IModelObject, IModelObject)+" copyMap")» {
            «IF hasSupertype()»
                super.copyProperties(copy, copyMap);
            «ENDIF»
            «IF attributesToCopy.size > 0 || associationsToCopy.size > 0»
                «implClassName» concreteCopy = («implClassName»)copy;
                «copyAttributes»
                «copyAssociations»
            «ENDIF»
            «IF attributesToCopy.size == 0 && associationsToCopy.size == 0 && !hasSupertype()»
                «localizedComment("NOTHING_TO_DO")»
            «ENDIF»
        }

        /**
         *«localizedJDoc("METHOD_COPY_ASSOCIATIONS_INTERNAL")»
         *
         * @generated
         */
        «overrideAnnotationIfHasSuperclass»
        public void «copyAssociationsInternal(IModelObject+" abstractCopy", Map(IModelObject, IModelObject)+" copyMap")» {
            «IF hasSupertype()»
                super.«copyAssociationsInternal("abstractCopy", "copyMap")»;
            «ENDIF»
            «IF associationsToCopy.size > 0»
                «IF requiresLocalVariableInCopyAssocsInternal»
                    «implClassName» newCopy = («implClassName»)abstractCopy;
                «ENDIF»
                «FOR it : associations» «copyAssociationInternal» «ENDFOR»
            «ENDIF»
            «IF associationsToCopy.size == 0 && !hasSupertype()»
                «localizedComment("NOTHING_TO_DO")»
            «ENDIF»
        }
    '''

    def private static copyAttributes(XPolicyCmptClass it) '''
        «FOR it : attributesToCopy»
            «IF considerInCopySupport»
                concreteCopy.«fieldName» = «getReferenceOrSafeCopyIfNecessary(fieldName)»;
            «ENDIF»
        «ENDFOR»
    '''

    def private static copyAssociations(XPolicyCmptClass it) '''
        «FOR it : associations»
            «IF considerInCopySupport»
                «IF typeAssociation»
                    «IF oneToMany»
                        concreteCopy.«fieldName».addAll(«fieldName»);
                    «ELSE»
                        concreteCopy.«fieldName» = «fieldName»;
                    «ENDIF»
                «ELSE»
                    «IF oneToMany»
                        for («Iterator(targetInterfaceName)» it = «fieldName».iterator(); it.hasNext();) { ««« TODO cast ohne published interfaces nicht noetig
                        «targetClassName» «copySupportLoopVarName» = «castToImplementation(targetModelNode, targetClassName)» it.next();
                            «targetClassName» «copySupportCopyVarName» = «copySupportLoopVarName».newCopyInternal(copyMap);
                            «IF setInverseAssociationInCopySupport»
                                «copySupportCopyVarName».«inverseAssociation.methodNameSetOrAddInternal»(concreteCopy);
                            «ENDIF»
                            concreteCopy.«fieldName».add(«copySupportCopyVarName»);
                        }
                    «ELSE»
                        if («fieldName» != null) {
                        concreteCopy.«fieldName» = «fieldName».newCopyInternal(copyMap);
                            «IF setInverseAssociationInCopySupport»
                                concreteCopy.«fieldName».«inverseAssociation.methodNameSetOrAddInternal»(concreteCopy);
                            «ENDIF»
                        }
                    «ENDIF»
                «ENDIF»
            «ENDIF»
        «ENDFOR»
    '''

    def private static copyAssociationInternal(XPolicyAssociation it) '''
        «IF considerInCopySupport»
            «IF typeAssociation»
                «IF oneToMany»
                    for («targetInterfaceName» «copySupportLoopVarNameInternal» : «fieldName») {
                        if (copyMap.containsKey(«copySupportLoopVarNameInternal»)) {
                            newCopy.«fieldName».remove(«copySupportLoopVarNameInternal»);
                            newCopy.«fieldName».add((«targetInterfaceName»)copyMap.get(«copySupportLoopVarNameInternal»));
                        }
                    }
                «ELSE»
                    if (copyMap.containsKey(«fieldName»)) {
                        newCopy.«fieldName» = («targetClassName»)copyMap.get(«fieldName»);
                    }
                «ENDIF»
            «ELSE»
                «IF oneToMany»
                    for («targetInterfaceName» «copySupportLoopVarNameInternal» : «fieldName») {
                        «targetClassName» «copySupportCopyVarName» = («targetClassName»)copyMap.get(«copySupportLoopVarNameInternal»);
                        «castToImplementation(targetModelNode, targetClassName, copySupportLoopVarNameInternal)».copyAssociationsInternal(«copySupportCopyVarName», copyMap);
                    }
                «ELSE»
                    if («fieldName» != null) {
                        «targetClassName» «copySupportCopyVarName» = («targetClassName»)copyMap.get(«fieldName»);
                        «fieldName».copyAssociationsInternal(«copySupportCopyVarName», copyMap);
                    }
                «ENDIF»
            «ENDIF»
        «ENDIF»
    '''

}
