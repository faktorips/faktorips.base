package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.XAssociation
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.stdbuilder.xtend.template.AssociationTmpl


import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptAssociationExtensionTmpl.*
import static extension org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class PolicyCmptAssociationTmpl {

def package static constants (XPolicyAssociation it) '''
    «IF !compositionDetailToMaster && !derived && !constrain»
        /**
         *«localizedJDoc("FIELD_MAX_CARDINALITY", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public static final «IntegerRange()» «field(constantNameMaxCardinalityFor)» = «IntegerRange()».valueOf(«minCardinality», «maxCardinality»);
    «ENDIF»

    «IF !hasSuperAssociationWithSameName()»
        /**
         *«localizedJDoc("FIELD_ASSOCIATION_NAME", fieldName)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public static final String «field(constantNamePropertyName)» = "«fieldName»";
    «ENDIF»
'''

def package static field (XPolicyAssociation it) '''
    «IF generateField»
        «IF masterToDetail || typeAssociation»
            /**
             *«localizedJDoc("FIELD_ASSOCIATION", name)»
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotations(POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD)»
            «getAnnotations(POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD)»
            «IF oneToMany»
                private «List_(targetInterfaceName)» «field(fieldName)»  = new «ArrayList»();
            «ELSE»
                private «targetClassName» «field(fieldName)» = null;
            «ENDIF»
        «ELSEIF compositionDetailToMaster»
            /**
             *«localizedJDoc("FIELD_PARENT", targetClassName)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotations(POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD)»
            «getAnnotations(POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD)»
            private «targetClassName» «field(fieldName)»;
        «ENDIF»
    «ENDIF»
'''

def package static methods (XPolicyAssociation it) '''
    «AssociationTmpl.getNumOf(it)»
    «contains»
    «getters»
    «setterOrAdder»
    «newChildMethods»
    «remove»
    «AssociationTmpl.abstractMethods(it)»
'''

def private static contains (XAssociation it) '''
    «IF oneToMany && !constrain»
        /**
         *«inheritDocOrJavaDocIf(genInterface(), "METHOD_CONTAINS_OBJECT")»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
         «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
         «overrideAnnotationForPublishedMethodImplementation()»
        public boolean «method(methodNameContains, targetInterfaceName, "objectToTest")»
        «IF genInterface()»;«ELSE»
        {
            «IF derivedUnion»
                return «methodNameGetter»().contains(objectToTest);
            «ELSE»
                return «fieldName».contains(objectToTest);
            «ENDIF»
        }
        «ENDIF»
    «ENDIF»
'''

def private static getters (XPolicyAssociation it) '''
    «IF generateGetter»
        «IF oneToMany»
        /**
         *«inheritDocOrJavaDocIf(genInterface(), "METHOD_GET_MANY", getName(true), descriptionForJDoc)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface())»
        «overrideAnnotationForConstrainedAssociation()»
        public «List_("? extends " + targetInterfaceName)» «method(methodNameGetter)»
        «IF genInterface()»;«ELSE»
            {
            «IF constrain»
                return «ListUtil()».«convert("super." + methodNameGetter + "()", targetInterfaceName+".class")»;
            «ELSE»
                return «Collections()».unmodifiableList(«fieldName»);
            «ENDIF»
            }
        «ENDIF»
        /**
         *«inheritDocOrJavaDocIf(genInterface(), "METHOD_GET_REF_OBJECT_BY_INDEX", name, descriptionForJDoc)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        «overrideAnnotationForConstrainedAssociation()»
        public «targetInterfaceName» «method(methodNameGetSingle, "int", "index")»
         «IF genInterface()»;«ELSE»
             {
            «IF constrain»
                return («targetInterfaceName»)super.«methodNameGetSingle»(index);
            «ELSE»
                return «fieldName».get(index);
               «ENDIF»
               }
         «ENDIF»
        «ELSE»
            /**
             *«inheritDocOrJavaDocIf(genInterface(), "METHOD_GET_ONE", name, descriptionForJDoc)»
            «getAnnotations(ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface())»
            «overrideAnnotationForConstrainedAssociation()»
            public «targetInterfaceName» «method(methodNameGetter)»
            «IF genInterface()»;«ELSE»
                {
                «IF constrain»
                    return («targetInterfaceName»)super.«methodNameGetter»();
                «ELSE»
                    return «fieldName»;
                «ENDIF»
                }
            «ENDIF»
        «ENDIF»
    «ENDIF»
    «IF generateQualifiedGetter»
        /**
         *«inheritDocOrJavaDocIf(genInterface, "METHOD_GET_REF_OBJECT_BY_QUALIFIER", name, descriptionForJDoc)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        «overrideAnnotationForConstrainedAssociation()»
         «IF oneToManyIgnoringQualifier»
            public  «List_("? extends " +targetInterfaceName)» «method(methodNameGetSingle, targetProductCmptInterfaceNameBase, "qualifier")»
            «IF genInterface»;«ELSE»
                {
                «IF constrain»
                    «ObjectUtil».«checkInstanceOf("qualifier", targetProductCmptInterfaceName)»;
                    return «ListUtil».«convert("super." + methodNameGetSingle + "(qualifier)", targetInterfaceName+".class")»;
                «ELSE»
                    if (qualifier == null) {
                        return null;
                    }
                    «IF derived»
                        «List_("? extends " + targetInterfaceName)» elements = «methodNameGetter»();
                        «List_(targetInterfaceName)» result = new  «ArrayList»();
                        for («targetInterfaceName» element : elements) {
                            «val targetClass = targetPolicyCmptClass»
                            if (element.«targetClass.methodNameGetProductCmpt»().equals(qualifier)) {
                                result.add(element);
                            }
                        }
        «««FIPS-1142.remove this if-else
                    «ELSE»
                        «List_(targetInterfaceName)» result = new  «ArrayList»();
                        for («targetInterfaceName» «targetClassName.toFirstLower()» : «fieldName») {
                                «val targetClass = targetPolicyCmptClass»
                                if («targetClassName.toFirstLower()».«targetClass.methodNameGetProductCmpt»().equals(qualifier)) {
                                    result.add(«targetClassName.toFirstLower()»);
                                }
                            }
                    «ENDIF»
                    return result;
                «ENDIF»
                }
               «ENDIF»
        «ELSE»
            public  «targetInterfaceName» «method(methodNameGetSingle, targetProductCmptInterfaceNameBase, "qualifier")»
            «IF genInterface()»;«ELSE»
            {
                «IF constrain»
                    «ObjectUtil».«checkInstanceOf("qualifier", targetProductCmptInterfaceName)»;
                    return («targetInterfaceName»)super.«methodNameGetSingle»(qualifier);
                «ELSE»
                    if (qualifier == null) {
                        return null;
                    }
                    «IF derived»
                        «List_(targetInterfaceName)» elements = «methodNameGetter»();
                        for («targetInterfaceName» element : elements) {
                            «val targetClass = targetPolicyCmptClass»
                            if (element.«targetClass.methodNameGetProductCmpt»().equals(qualifier)) {
                                return element;
                            }
                        }
        «««FIPS-1142.remove this if-else
                    «ELSE»
                        for («targetInterfaceName» «targetClassName.toFirstLower» : «fieldName») {
                            «val targetClass = targetPolicyCmptClass»
                            if («targetClassName.toFirstLower».«targetClass.methodNameGetProductCmpt»().equals(qualifier)) {
                                return «targetClassName.toFirstLower»;
                            }
                        }
                    «ENDIF»
                    return null;
                «ENDIF»
            }
            «ENDIF»
        «ENDIF»
    «ENDIF»
'''

def private static setterOrAdder (XPolicyAssociation it) '''
    «IF generateSetter»
        «generateSetter(it)»
    «ELSEIF generateAddAndRemoveMethod»
        «generateAdder»
    «ENDIF»
'''

def private static generateSetter (XPolicyAssociation it) '''
    «IF compositionDetailToMaster»
        «IF !sharedAssociationImplementedInSuperclass && !genInterface()»
            /** 
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotationsForPublishedInterface(POLICY_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER, genInterface())»
            «overrideAnnotationIf(constrain)»
            public void «method(methodNameSetOrAddInternal,targetInterfaceNameBase, "newParent")» {
                «IF constrain»
                    «ObjectUtil».«checkInstanceOf("newParent", targetInterfaceName)»;
                    super.«methodNameSetOrAddInternal»(newParent);
                «ELSE»
                    if («methodNameGetter»() == newParent) {
                        return;
                    }
                    «IModelObject» «parentVar» = «getParentModelObject()»;
                    if (newParent != null && «parentVar» != null) {
                        throw new «IllegalStateException»(String.format(«localizedText("RUNTIME_EXCEPTION_SET_PARENT_OBJECT_INTERNAL", typeName, name)», toString(), newParent.toString(), «parentVar».toString()));
                    }
                    this.«fieldName» = «castToImplementation(targetClassName)» newParent;
                    «IF typeConfigurableByProductCmptType»
                        effectiveFromHasChanged();
                    «ENDIF»
                «ENDIF»
            }
        «ENDIF»
    «ELSE»
        «IF !constrain || !generateInternalSetterOrAdder»
            /**
             *«inheritDocOrJavaDocIf(genInterface(), "METHOD_SET_OBJECT", name)»
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotationsForPublishedInterface(POLICY_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER, genInterface())»
            «overrideAnnotationForConstrainedAssociation()»
            public void «method(methodNameSetOrAdd, targetInterfaceNameBase, "newObject")»
            «IF genInterface()»;«ELSE»
            {
                «IF generateChangeSupport»
                    «PropertyChangeSupportTmpl.storeOldValue(it)»
                    «methodNameSetOrAddInternal»(newObject);
                    «PropertyChangeSupportTmpl.notify(it)»
                «ELSE»
                    «setterMethodCode(methodNameSetOrAdd, it)»
                «ENDIF»
            }
            «ENDIF»
        «ENDIF»

        «IF generateInternalSetterOrAdder && !genInterface()»
            /**
             *«localizedJDoc("METHOD_SET_OBJECT_INTERNAL", name)»
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(methodNameSetOrAddInternal, targetInterfaceNameBase, "newObject")»{
                «setterMethodCode(methodNameSetOrAddInternal, it)»
            }
        «ENDIF»
    «ENDIF»
'''

def private static setterMethodCode(String methodName, XPolicyAssociation it) '''
    «IF constrain»
        «ObjectUtil».«checkInstanceOf("newObject", targetInterfaceName)»;
        super.«methodName»(newObject);
    «ELSEIF masterToDetail»
        «IF generateCodeToSynchronizeInverseCompositionForSet»
            if («fieldName» != null) {
                «synchronizeInverseCompositionIfNecessaryForSet(false, fieldName, "null", it)»
            }
            if (newObject != null) {
                «synchronizeInverseCompositionIfNecessaryForSet(true, "newObject", "this", it)»
            }
        «ENDIF»
        «fieldName» = «castToImplementation(targetClassName)» newObject;
        «synchronizeInverseAssociationIfNecessary(fieldName, it)»
    «ELSEIF typeAssociation»
        if (newObject == «fieldName») {
            return;
        }
        «IF hasInverseAssociation()»
            «targetInterfaceName» oldRefObject = «fieldName»;
            «fieldName» = null;
            «cleanupOldReference("oldRefObject", it)»
        «ENDIF»
        «fieldName» = «castToImplementation(targetClassName)» newObject;
        «synchronizeInverseAssociationIfNecessary(fieldName, it)»
    «ENDIF»
'''

def private static generateAdder (XPolicyAssociation it) '''
    «IF !constrain || !generateInternalSetterOrAdder»
        /**
         *«inheritDocOrJavaDocIf(genInterface(), "METHOD_ADD_OBJECT", name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotationsForPublishedInterface(POLICY_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER, genInterface())»
        «overrideAnnotationForConstrainedAssociation()»
        public void «method(methodNameSetOrAdd, targetInterfaceNameBase, "objectToAdd")»
        «IF genInterface()»;«ELSE»
        {
            «IF generateInternalSetterOrAdder»
                «methodNameSetOrAddInternal»(objectToAdd);
                «PropertyChangeSupportTmpl.notifyNewAssociation("objectToAdd", it)»
            «ELSE»
                «addMethodCode(methodNameSetOrAdd, it)»
            «ENDIF»
        }
        «ENDIF»
    «ENDIF»

    «IF generateInternalSetterOrAdder && !genInterface()»
        /**
         *«localizedJDoc("METHOD_ADD_OBJECT_INTERNAL", name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        «overrideAnnotationIf(constrain)»
        public void «method(methodNameSetOrAddInternal, targetInterfaceNameBase, "objectToAdd")»{
            «addMethodCode(methodNameSetOrAddInternal, it)»
        }
    «ENDIF»
'''

def private static addMethodCode(String methodName, XPolicyAssociation it) '''
    «IF constrain»
        «ObjectUtil».«checkInstanceOf("objectToAdd", targetInterfaceName)»;
        super.«methodName»(objectToAdd);
    «ELSE»
        if (objectToAdd == null) {
            throw new NullPointerException("Can't add null to association «name» of " + this);
        }
        if («fieldName».contains(objectToAdd)) {
            return;
        }
        «synchronizeInverseCompositionIfNecessaryForAdd(it)»
        «fieldName».add(objectToAdd);
        «synchronizeInverseAssociationIfNecessary("objectToAdd", it)»
    «ENDIF»
'''

def private static addOrSetNewInstance (XPolicyAssociation it) '''
    «IF oneToMany»
        «IF generateChangeSupport»
               «methodNameSetOrAddInternal»(«variableNameNewInstance»);
           «ELSE»
               «methodNameSetOrAdd»(«variableNameNewInstance»);
           «ENDIF»
    «ELSE»
        «IF generateChangeSupport || compositionDetailToMaster»
               «methodNameSetOrAddInternal»(«variableNameNewInstance»);
        «ELSE»
               «methodNameSetOrAdd»(«variableNameNewInstance»);
        «ENDIF»
    «ENDIF»
'''

def private static synchronizeInverseCompositionIfNecessaryForSet(boolean cast, String varName, String newRef, XPolicyAssociation it) '''
    «IF cast»«castToImplementation(targetClassName, varName)»«ELSE»«varName»«ENDIF».«inverseAssociation.methodNameSetOrAddInternal»(«newRef»);
'''

def private static cleanupOldReference(String varToCleanUp, XPolicyAssociation it) '''
    «IF !oneToMany»
        if(«varToCleanUp» != null){
            «cleanupOldReferenceInner(varToCleanUp, it)»
        }
    «ELSE»
        «cleanupOldReferenceInner(varToCleanUp, it)»
    «ENDIF»
'''

def private static cleanupOldReferenceInner(String varToCleanUp, XPolicyAssociation it) '''
    «IF inverseAssociation.oneToMany»
        «varToCleanUp».«inverseAssociation.methodNameRemove»(this);
    «ELSE»
        «varToCleanUp».«inverseAssociation.methodNameSetOrAdd»(null);
    «ENDIF»
'''

def private static newChildMethods (XPolicyAssociation it) '''
    «IF generateNewChildMethods»
        /**
         *«inheritDocOrJavaDocIf(genInterface(), "METHOD_NEW_CHILD", targetName, name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        «overrideAnnotationForPublishedMethodImplementationOr(needOverrideForConstrainNewChildMethod)»
        public «targetInterfaceName» «method(methodNameNew)»
        «IF genInterface()»;«ELSE»
        {
            «targetClassName» «variableNameNewInstance» = new «targetClassName»();
            «initializeChildInstanceAndReturn(it)»
        }
        «ENDIF»

        «IF generateNewChildWithArgumentsMethod»
            /**
             *«inheritDocOrJavaDocIf(genInterface(), "METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", targetName, getName(false), targetProductCmptVariableName)»
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
             *
             * @generated
             */
            «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
            «overrideAnnotationForPublishedMethodImplementationOr(needOverrideForConstrainNewChildMethod)»
            public «targetInterfaceName» «method(methodNameNew, targetProductCmptInterfaceNameBase, targetProductCmptVariableName)»
            «IF genInterface()»;«ELSE»
            {
                if («targetProductCmptVariableName» == null) {
                    return «methodNameNew»();
                }
                «targetInterfaceName» «variableNameNewInstance» = «castFromTo(targetProductCmptInterfaceNameBase, targetProductCmptInterfaceName, targetProductCmptVariableName)».«methodNameCreatePolicyCmptForTargetProductCmpt»();
                «initializeChildInstanceAndReturn(it)»
            }
            «ENDIF»
        «ENDIF»
    «ENDIF»
'''

def private static initializeChildInstanceAndReturn (XPolicyAssociation it) '''
    «addOrSetNewInstance(it)»
    «variableNameNewInstance».«initialize()»;
    «PropertyChangeSupportTmpl.notifyNewAssociation(variableNameNewInstance, it)»
    return «variableNameNewInstance»;
'''

def private static synchronizeInverseCompositionIfNecessaryForAdd (XPolicyAssociation it) '''
    «IF generateCodeToSynchronizeInverseCompositionForAdd»
        «castToImplementation(targetClassName, "objectToAdd")».«inverseAssociation.methodNameSetOrAddInternal»(this);
    «ENDIF»
'''

def private static synchronizeInverseAssociationIfNecessary(String objectToSynchronize, XPolicyAssociation it) '''
    «IF generateCodeToSynchronizeInverseAssociation»
        «IF inverseAssociation.oneToMany»
            if («varNameNullCheckIfNecessary(objectToSynchronize, it)» !«objectToSynchronize».«inverseAssociation.methodNameContains»(this)) {
                «objectToSynchronize».«inverseAssociation.methodNameSetOrAdd»(this);
            }
        «ELSE»
            if («varNameNullCheckIfNecessary(objectToSynchronize, it)» «objectToSynchronize».«inverseAssociation.methodNameGetter»() != this) {
                «IF false»

«««                    TODO unnötiger Cast und Fallunterscheidung!
«««                    «castIfNeccessary(objectToSynchronize)(this)»

                «ENDIF»
                «IF oneToMany»
                    «castToImplementation(targetClassName, objectToSynchronize)».«inverseAssociation.methodNameSetOrAdd»(this);
                «ELSE»
                    «objectToSynchronize».«inverseAssociation.methodNameSetOrAdd»(this);
                «ENDIF»
            }
        «ENDIF»
    «ENDIF»
'''

def private static varNameNullCheckIfNecessary(String varName, XPolicyAssociation it) '''
    «IF !oneToMany»
        «varName» !=null &&
    «ENDIF»
'''

def private static remove (XPolicyAssociation it) '''
    «IF generateAddAndRemoveMethod && !constrain»
        /**
         *«inheritDocOrJavaDocIf(genInterface(), "METHOD_REMOVE_OBJECT", name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotationsForPublishedInterface(POLICY_CMPT_DECL_CLASS_ASSOCIATION_REMOVER, genInterface())»
         «overrideAnnotationForPublishedMethodImplementation()»
        public void «method(methodNameRemove, targetInterfaceName, "objectToRemove")»
        «IF genInterface()»;«ELSE»
        {
            if (objectToRemove == null) {
                return;
            }
            «removeAndDetach(it)»
            «PropertyChangeSupportTmpl.notifyRemovedAssociation("objectToRemove", it)»
        }
        «ENDIF»
    «ENDIF»
'''

def private static removeAndDetach (XPolicyAssociation it) '''
    «IF generateCodeToSynchronizeInverseCompositionForRemove»
        if («fieldName».remove(objectToRemove)) {
            «detachRemovedObject(it)»
        }
    «ELSE»
        «fieldName».remove(objectToRemove);
    «ENDIF»
'''

//TODO FIPS-1141 (7): delete the «IF hasInverseAssociation()» clause when fixed
def private static detachRemovedObject (XPolicyAssociation it) '''
    «IF hasInverseAssociation()»
        «IF inverseAssociation.oneToMany»
                objectToRemove.«inverseAssociation.methodNameRemove»(this);
        «ELSE»
            «IF inverseAssociation.typeAssociation»
                if («castToImplementation(targetClassName, "objectToRemove")».«inverseAssociation.methodNameGetter»() == this) {
                    «castToImplementation(targetClassName, "objectToRemove")».«inverseAssociation.methodNameSetOrAdd»(null);
                }
            «ELSE»
                «castToImplementation(targetClassName, "objectToRemove")».«inverseAssociation.methodNameSetOrAddInternal»(null);
            «ENDIF»
        «ENDIF»
    «ENDIF»
'''

def package static delegateEffectiveFromHasChanged (XPolicyAssociation it) '''
    «IF considerInEffectiveFromHasChanged»
        «IF oneToMany»
            for («Iterator(targetInterfaceName)» it = «fieldName».iterator(); it.hasNext();) {
                «targetClassName» child = «castToImplementation(targetClassName)»it.next();
                child.effectiveFromHasChanged();
            }
        «ELSE»
            if («fieldName» != null) {
                «fieldName».effectiveFromHasChanged();
            }
        «ENDIF»
    «ENDIF»
'''

def package static validateDependents (XPolicyAssociation it) '''
    «IF considerInValidateDependents»
        «IF oneToMany»
            if («methodNameGetNumOf»() > 0) {
                for («targetInterfaceName» rel : «methodNameGetter»()) {
                    ml.add(rel.validate(context));
                }
            }
        «ELSE»
            if («fieldName» != null) {
                ml.add(«fieldName».validate(context));
            }
        «ENDIF»
    «ENDIF»
'''

def package static createTargetFromXmlMethodCall (XPolicyAssociation it) '''
    «IF considerInCreateChildFromXML»
        if ("«name»".equals(childEl.getNodeName())) {
            return «methodNameDoInitFromXml»(childEl);
        }
    «ENDIF»
'''

def package static createTargetFromXmlMethod (XPolicyAssociation it) '''
    «IF considerInCreateChildFromXML»
        /**
         * @generated
         */
        private «AbstractModelObject()» «method(methodNameDoInitFromXml, Element(), "childEl")»{
            String className = childEl.getAttribute("class");
            if (className.length() > 0) {
                try {
                    «targetClassName» «createChildFromXMLLocalVarName» = («targetClassName»)Class.forName(className).getConstructor().newInstance();
                        «methodNameSetOrAdd»(«createChildFromXMLLocalVarName»);
                    return «createChildFromXMLLocalVarName»;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            «IF abstractTarget»
                throw new RuntimeException(childEl.toString() + ": Attribute className is missing.");
            «ELSE»
                return «castToImplementation(AbstractModelObject())»«methodNameNew»();
            «ENDIF»
        }
    «ENDIF»
'''

def package static createUnresolvedReference (XPolicyAssociation it) '''
    «IF considerInCreateCreateUnresolvedReference»
        if ("«name»".equals(targetRole)) {
            return new «DefaultUnresolvedReference()»(this, objectId, "«methodNameSetOrAdd»",
                    «targetInterfaceName».class, targetId);
        }
    «ENDIF»
'''

}