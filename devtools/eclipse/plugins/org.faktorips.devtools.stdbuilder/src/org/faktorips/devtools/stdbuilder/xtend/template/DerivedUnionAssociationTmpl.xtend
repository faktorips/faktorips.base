package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.stdbuilder.xmodel.XDerivedUnionAssociation
import org.faktorips.devtools.stdbuilder.xmodel.XType
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XDetailToMasterDerivedUnionAssociation
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class DerivedUnionAssociationTmpl {

    def static methodsForDerivedUnion(XType currentContextType, XDerivedUnionAssociation it) '''
        «getter(currentContextType, it)»
        «getterNumOf(currentContextType, it)»
        «getterNumOfInternal(currentContextType, it)»
    '''

    def static getter(XType currentContextType, XDerivedUnionAssociation it) '''
        /**
         *«inheritDocOrJavaDocIf(it, !needOverride(currentContextType) && genInterface(), "METHOD_GET_MANY", getName(true), descriptionForJDoc)»
         *
         * @generated
         */
        «IF isDefinedIn(currentContextType) »
            «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface())»
        «ENDIF»
        «overrideAnnotationForPublishedMethodOrIf(it, needOverride(currentContextType))»
        public «List_(targetInterfaceName)» «methodNameGetter»() {
            «List_(targetInterfaceName)» result = new «ArrayList»(«methodNameGetNumOfInternal»());
            «IF isImplementedInSuperclass(currentContextType)»
                result.addAll(super.«methodNameGetter»());
            «ENDIF»
            «FOR subsetAssociation : getSubsetAssociations(currentContextType)»
                «IF subsetAssociation.oneToMany»
                    result.addAll(«subsetAssociation.methodNameGetter»());
                «ELSE»
                    «IF productCmptTypeAssociation»
                        if («subsetAssociation.fieldName» != null) {
                             result.add(«subsetAssociation.methodNameGetter»());
                        }
                    «ELSE»
                        if («subsetAssociation.methodNameGetter»() != null) {
                             result.add(«subsetAssociation.methodNameGetter»());
                        }
                    «ENDIF»
                «ENDIF»
            «ENDFOR»
            return result;
        }
    '''

    def static getterForDetailToMaster(XPolicyCmptClass currentContextType,
        XDetailToMasterDerivedUnionAssociation it) '''
        /**
         *«inheritDocOrJavaDocIf(it, !needOverride(currentContextType) && genInterface(), "METHOD_GET_ONE", getName(false), descriptionForJDoc)»
         *
         * @generated
         */
         «IF isDefinedIn(currentContextType) »
             «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface())»
         «ENDIF»
         «overrideAnnotationForPublishedMethodOrIf(it, needOverride(currentContextType))»
        public «targetInterfaceName» «methodNameGetter»() {
             «FOR it : getDetailToMasterSubsetAssociations(currentContextType)»
                 if («fieldName» != null) {
                     return «fieldName»;
                 }
             «ENDFOR»
            «IF isImplementedInSuperclass(currentContextType)»
                return super.«methodNameGetter»();
            «ELSE»
                return null;
            «ENDIF»
        }
    '''

    def static getterNumOf( XType currentContextType, XDerivedUnionAssociation it) '''
        /**
         *«inheritDocOrJavaDocIf(it ,!needOverride(currentContextType) && genInterface(), "METHOD_GET_NUM_OF", getName(true))»
         *
         * @generated
         */
         «overrideAnnotationForPublishedMethodOrIf(it, needOverride(currentContextType))»
         public int «methodNameGetNumOf»() {
             return «methodNameGetNumOfInternal»();
         }
    '''

    def static getterNumOfInternal( XType currentContextType, XDerivedUnionAssociation it) '''
        /**
         * @generated
         */
         private int «methodNameGetNumOfInternal»() {
            int num = 0;
            «IF generateGetNumOfInternalSuperCall(currentContextType)»
                num += super.«methodNameGetNumOf»();
            «ENDIF»
            «FOR subsetAssociation : getSubsetAssociations(currentContextType)»
                «IF subsetAssociation.oneToMany»
                    num += «subsetAssociation.methodNameGetNumOf»();
                «ELSE»
                    num += «subsetAssociation.fieldName» == null ? 0 : 1;
                «ENDIF»
            «ENDFOR»
            return num;
         }
    '''

}
