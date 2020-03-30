package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductClass

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.AssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class ProductAssociationTmpl {

    def package static privateConstants(XProductAssociation it) '''
        «IF !derivedUnion && !constrain»
            /** «localizedJDoc("CONSTANT_XML_TAG_ASSOCIATION", fieldName)»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
            private static final String «field(constantNameXmlTag)» = "«name»";
        «ENDIF»
    '''

    def package static field(XProductAssociation it) '''
        «IF !derivedUnion && !constrain»
            /**
             * «localizedJDoc("FIELD_ASSOCIATION", getName(oneToMany).toFirstUpper())»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
            «IF oneToMany»
                private «Map("String", IProductComponentLink(targetInterfaceName))» «field(fieldName)»  = new «LinkedHashMap("String", IProductComponentLink(targetInterfaceName))»(0);
            «ELSE»
                private «IProductComponentLink(targetInterfaceName)» «field(fieldName)» = null;
            «ENDIF»
        «ENDIF»
    '''

    def package static getterSetterAdderRemover(XProductAssociation it) '''
        «IF !derivedUnion»
            «getterProductCmpt»
            «IF generateGenerationAccessMethods»
                «getterProductCmptGen»
            «ENDIF»
            «getterProductCmptPerIndex»
            «IF !genInterface»
                «setterMethodForToOne»
                «setterMethodForToOneWithCardinality»
                «addMethod»
                «addMethodWithCardinality»
                «removeMethod»
                «removeAllMethod»
            «ENDIF»
            «IF !constrain»
                «getterLinksFor»
                «getterSingleLinkFor»
                «getCardinalityFor»
            «ENDIF»
            «getNumOf»
        «ENDIF»
        «abstractMethods»
    '''

    def private static getterProductCmpt(XProductAssociation it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface(), getJavadocKey("METHOD_GET"), getName(oneToMany), descriptionForJDoc)»
         * «getAnnotations(ELEMENT_JAVA_DOC)»
         * @generated
         */
         «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface)»
         «overrideAnnotationForConstainedAssociation»
           «IF oneToMany»
               public «List_("? extends " +targetInterfaceName)» «method(methodNameGetter)»
               «IF genInterface»;«ELSE»
                           {
                               «IF constrain»
                                   return «ListUtil».«convert("super."+methodNameGetter+"()", targetInterfaceName+".class")»;
                               «ELSE»
                                   «List_(targetInterfaceName)» result = new «ArrayList(targetInterfaceName)»(«fieldName».size());
                                   for («IProductComponentLink(targetInterfaceName)» «getterLoopVarName» : «fieldName».values()) {
                                       result.add(«getterLoopVarName».«getTarget»);
                                   }
                                   return result;
                           «ENDIF»
                           }
               «ENDIF»
        «ELSE»
            public «targetInterfaceName» «method(methodNameGetter)»
            «IF genInterface»;«ELSE»
                        {
                            «IF constrain»
                                return «castFromTo(superAssociationWithSameName.targetInterfaceName, targetInterfaceName)»super.«methodNameGetter»();
                            «ELSE»
                                return «fieldName» != null ? «fieldName».«getTarget» : null;
                            «ENDIF»
                        }
            «ENDIF»
        «ENDIF»
    '''

    def private static getterProductCmptGen(XProductAssociation it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, getJavadocKey("METHOD_GET_CMPT_GEN"), getName(oneToMany), descriptionForJDoc)»
         * «getAnnotations(ELEMENT_JAVA_DOC)»
         * @generated
         */
         «overrideAnnotationForConstainedAssociation»
           «IF oneToMany»
               public «List_("? extends " +targetClassGenerationName)» «method(methodNameGetter, Calendar, "effectiveDate")»
               «IF genInterface»;«ELSE»
                           {
                               «IF constrain»
                                   return «ListUtil».«convert("super."+methodNameGetter+"(effectiveDate)", targetClassGenerationName + ".class")»;
                               «ELSE»
                                   «List_("? extends " +targetInterfaceName)» targets = «methodNameGetter»();
                                   «List_(targetClassGenerationName)» result = new «ArrayList(targetClassGenerationName)»();
                                   for («targetInterfaceName» target : targets) {
                                       «targetClassGenerationName» gen = target.«methodNameGetTargetGeneration»(effectiveDate);
                                       if (gen != null) {
                                           result.add(gen);
                                       }
                                   }
                                   return result;
                               «ENDIF»
                           }
               «ENDIF»
        «ELSE»
            public «targetClassGenerationName» «method(methodNameGetter, Calendar, "effectiveDate")»
               «IF genInterface»;«ELSE»
                           {
                            «IF constrain»
                                return «castFromTo(superAssociationWithSameName.targetClassGenerationName, targetClassGenerationName)»super.«methodNameGetter»(effectiveDate);
                            «ELSE»
                                return «fieldName» != null ? «fieldName».«getTarget».«methodNameGetTargetGeneration»(effectiveDate) : null;
                            «ENDIF»
                   }
            «ENDIF»
        «ENDIF»
    '''

    def private static getterProductCmptPerIndex(XProductAssociation it) '''
        «IF oneToMany»
            /**
             * «inheritDocOrJavaDocIf(genInterface, "METHOD_GET_CMPT_AT_INDEX", name, descriptionForJDoc)»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationForConstainedAssociation»
            public «targetInterfaceName» «method(methodNameGetSingle, "int", "index")»
            «IF genInterface»;«ELSE»
                        {
                            «IF constrain»
                                return «castFromTo(superAssociationWithSameName.targetInterfaceName, targetInterfaceName)»super.«methodNameGetSingle»(index);
                            «ELSE»
                               return «ProductComponentLinks».getTarget(index, «fieldName»);
                            «ENDIF»
                        }
            «ENDIF»
        «ENDIF»
    '''

    def private static getterLinksFor(XProductAssociation it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, getJavadocKey("METHOD_GET_CMPT_LINK"), getName(oneToMany), descriptionForJDoc)»
         * «getAnnotations(ELEMENT_JAVA_DOC)»
         * @generated
         */
        «getAnnotationsForPublishedInterface(PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_LINKS, genInterface())»
        «overrideAnnotationForPublishedMethodImplementation»
        «IF oneToMany»
            public «Collection_(IProductComponentLink(targetInterfaceName))» «method(methodNameGetLinksFor)»
            «IF genInterface»;«ELSE»
                        {
                            return «Collections».unmodifiableCollection(«fieldName».values());
                        }
            «ENDIF»
        «ELSE»
            public «IProductComponentLink(targetInterfaceName)» «method(methodNameGetLinksFor)»
            «IF genInterface»;«ELSE»
                        {
                            return «fieldName»;
                        }
            «ENDIF»
        «ENDIF»
    '''

    def private static getterSingleLinkFor(XProductAssociation it) '''
        /**
         * «inheritDocOrJavaDocIf(genInterface, "METHOD_GET_CMPT_LINK_AT_INDEX", name, descriptionForJDoc)»
         * «getAnnotations(ELEMENT_JAVA_DOC)»
         * @generated
         */
         «overrideAnnotationForPublishedMethodImplementation»
        public «IProductComponentLink(targetInterfaceName)» «method(methodNameGetLinkFor, targetInterfaceName, "productComponent")»
        «IF genInterface»;«ELSE»
                    {
                        «IF oneToMany»
                            return «fieldName».get(productComponent.getId());
                        «ELSE»
                            return «fieldName» != null && «fieldName».«getTargetId».equals(productComponent.getId()) ? «fieldName» : null;
                        «ENDIF»
                    }
        «ENDIF»
    '''

    def private static addMethod(XProductAssociation it) '''
        «IF oneToMany»
            /**
             * «localizedJDoc("METHOD_ADD_CMPT")»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(methodNameSetOrAdd, targetInterfaceNameBase, "target")» {
                «IF constrain»
                    «ObjectUtil».«checkInstanceOf("target", targetInterfaceName)»;
                    super.«methodNameSetOrAdd»(target);
                «ELSE»
                    «checkRepositoryModifyable»
                    this.«fieldName».put(target.getId(), new «ProductComponentLink(targetInterfaceName)»(this, target, "«it.name»"));
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static addMethodWithCardinality(XProductAssociation it) '''
        «IF oneToMany && matchingAssociation!==null»
            /**
             * «localizedJDoc("METHOD_ADD_CMPT_WITH_CARDINALITY")»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(methodNameSetOrAdd, targetInterfaceNameBase, "target", CardinalityRange, "cardinality")» {
                «IF constrain»
                    «ObjectUtil».«checkInstanceOf("target", targetInterfaceName)»;
                    super.«methodNameSetOrAdd»(target, cardinality);
                «ELSE»
                    «checkRepositoryModifyable»
                    this.«fieldName».put(target.getId(), new «ProductComponentLink(targetInterfaceName)»(this, target, cardinality, "«it.name»"));
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static removeMethod(XProductAssociation it) '''
        «IF oneToMany»
            /**
             * «localizedJDoc("METHOD_REMOVE_CMPT")»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(getMethodNameRemove(false), targetInterfaceNameBase, "target")» {
                «IF constrain»
                    «ObjectUtil».«checkInstanceOf("target", targetInterfaceName)»;
                    super.«getMethodNameRemove(false)»(target);
                «ELSE»
                    «checkRepositoryModifyable»
                    this.«fieldName».remove(target.getId());
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static removeAllMethod(XProductAssociation it) '''
        «IF oneToMany»
            /**
             * «localizedJDoc("METHOD_REMOVE_ALL_CMPT", getName(true))»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(getMethodNameRemove(true))» {
                «IF constrain»
                    super.«getMethodNameRemove(true)»();
                «ELSE»
                    «checkRepositoryModifyable»
                    this.«fieldName».clear();
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static setterMethodForToOne(XProductAssociation it) '''
        «IF !oneToMany»
            /**
             * «localizedJDoc("METHOD_SET_CMPT", name)»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(methodNameSetOrAdd, targetInterfaceNameBase, "target")» {
                «IF constrain»
                    «ObjectUtil()».«checkInstanceOf("target", targetInterfaceName)»;
                    super.«methodNameSetOrAdd»(target);
                «ELSE»
                    «checkRepositoryModifyable»
                    «fieldName» = (target == null ? null : new «ProductComponentLink(targetInterfaceName)»(this, target, "«it.name»"));
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static setterMethodForToOneWithCardinality(XProductAssociation it) '''
        «IF !oneToMany && matchingAssociation!==null»
            /**
             * «localizedJDoc("METHOD_SET_CMPT_WITH_CARDINALITY", name)»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationIf(constrain)»
            public void «method(methodNameSetOrAdd, targetInterfaceNameBase, "target", CardinalityRange, "cardinality")» {
                «IF constrain»
                    «ObjectUtil()».«checkInstanceOf("target", targetInterfaceName)»;
                    super.«methodNameSetOrAdd»(target, cardinality);
                «ELSE»
                    «checkRepositoryModifyable»
                    «fieldName» = (target == null ? null : new «ProductComponentLink(targetInterfaceName)»(this, target, cardinality, "«it.name»"));
                «ENDIF»
            }
        «ENDIF»
    '''

    def private static getCardinalityFor(XProductAssociation it) '''
        «IF hasMatchingAssociation»
            /**
             * «inheritDocOrJavaDocIf(genInterface, "METHOD_GET_CARDINALITY_FOR", nameOfMatchingAssociation)»
             * «getAnnotations(ELEMENT_JAVA_DOC)»
             * @generated
             */
             «overrideAnnotationForPublishedMethodImplementation»
            public «CardinalityRange» «method(methodNameGetCardinalityFor, targetInterfaceName, "productCmpt")»
            «IF genInterface»;«ELSE»
                        {
                            if (productCmpt != null) {
                            «IF oneToMany»
                                return «fieldName».containsKey(productCmpt.«getId») ? «fieldName».get(productCmpt.«getId»)
                                        .«getCardinality» : null;
                            «ELSE»
                                return «fieldName» != null && «fieldName».«getTargetId».equals(productCmpt.«getId») ? «fieldName»
                                        .«getCardinality» : null;
                            «ENDIF»
                            }
                            return null;
                        }
            «ENDIF»
        «ENDIF»
    '''

    def package static getLinkMethods(XProductClass it) '''
        «getLinkMethod»
        «getLinksMethod»
    '''

    def private static getLinkMethod(XProductClass it) '''
        /**
         * «inheritDoc»
         *
         * @generated
         */
        @Override
        public «IProductComponentLink("? extends " + IProductComponent)» «getLink("String linkName", IProductComponent + " target")» {
            «FOR it : associations»
                «IF !derivedUnion && !constrain»
                    if ("«name»".equals(linkName)) {
                        return «methodNameGetLinkFor»((«targetInterfaceName»)target);
                    }
                «ENDIF»
            «ENDFOR»
            «IF hasSupertype »
                return super.«getLink("linkName", "target")»;
            «ELSE»
                return null;
            «ENDIF»
        }
    '''

    def private static getLinksMethod(XProductClass it) '''
        /**
         * «inheritDoc»
         *
         * @generated
         */
        @Override
        public «List_(IProductComponentLink("? extends " + IProductComponent))» «getLinks()» {
            «List_(IProductComponentLink("? extends " + IProductComponent))» list =
            «IF hasSupertype»
                super.«getLinks»;
            «ELSE»
                new «ArrayList(IProductComponentLink("? extends " + IProductComponent))»();
            «ENDIF»
            «FOR it : associations»
                «IF !derivedUnion && !constrain»
                    «IF oneToMany»
                        list.addAll(«methodNameGetLinksFor»());
                    «ELSE»
                        if («methodNameGetLinksFor»() != null) {
                            list.add(«methodNameGetLinksFor»());
                        }
                    «ENDIF»
                «ENDIF»
            «ENDFOR»
            return list;
        }
    '''

    def package static doInitFromXmlMethodCall(XProductAssociation it) '''
        «IF !derivedUnion && !constrain»
            «methodNameDoInitFromXml»(elementsMap);
        «ENDIF»
    '''

    def package static doInitFromXmlMethod(XProductAssociation it) '''
        «IF !derivedUnion && !constrain»
            /**
              * @generated
            */
            private void «method(methodNameDoInitFromXml, Map("String", List_(Element)), "elementsMap")» {
                «List_(Element)» associationElements = elementsMap.get(«constantNameXmlTag»);
                if (associationElements != null) {
                «IF oneToMany»
                    this.«fieldName» = new «LinkedHashMap("String", IProductComponentLink(targetInterfaceName))»(associationElements.size());
                    for (Element element : associationElements) {
                        «IProductComponentLink(targetInterfaceName)» link = new «ProductComponentLink(targetInterfaceName)»(this);
                        link.initFromXml(element);
                        this.«fieldName».put(link.getTargetId(), link);
                    }
                «ELSE»
                    Element element = associationElements.get(0);
                    «fieldName» = new «ProductComponentLink(targetInterfaceName)»(this);
                    «fieldName».initFromXml(element);
                «ENDIF»
                }
            }
        «ENDIF»
    '''

    def package static writeReferencesToXmlMethodCall(XProductAssociation it) '''
        «IF !derivedUnion && !constrain»
            «methodNameWriteToXml»(element);
        «ENDIF»
    '''

    def package static writeReferencesToXmlMethod(XProductAssociation it) '''
        «IF !derivedUnion && !constrain»
            /**
             * @generated
             */
            private void «method(methodNameWriteToXml, Element, " element")» {
                «IF oneToMany»
                    for («IProductComponentLink(targetInterfaceName)» link : «fieldName».values()) {
                        element.appendChild(((«IXmlPersistenceSupport»)link).«toXml("element.getOwnerDocument()")»);
                    }
                «ELSE»
                    if («fieldName» != null) {
                        element.appendChild(((«IXmlPersistenceSupport»)«fieldName»).«toXml("element.getOwnerDocument()")»);
                    }
                «ENDIF»
            }
        «ENDIF»
    '''
}
