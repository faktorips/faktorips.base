package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAssociation
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductClass

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class PolicyLinkCardinalityTmpl {

    def package static cardinalityField(XProductAssociation it) '''
        /**
         * @generated
         */
        private «IntegerRange» «field(fieldNameCardinality)» = «IntegerRange».valueOf(0, Integer.MAX_VALUE);
    '''

    def package static cardinalityField(XPolicyAssociation it) '''
        /**
         * @generated
         */
        private «IntegerRange» «field(fieldNameCardinality)» = «IntegerRange».valueOf(0, Integer.MAX_VALUE);
    '''

    def package static cardinalityGetter(XProductAssociation it) '''
        /**
         * @generated
         */
        «overrideAnnotationForPublishedMethodImplementation»
        public «IntegerRange» «method(methodNameGetCardinalityFor)»
        «IF genInterface»;«ELSE»
        {
            return «fieldNameCardinality»;
        }
        «ENDIF»
    '''

    def package static cardinalityGetter(XPolicyAssociation it) '''
        /**
         * @generated
         */
        «overrideAnnotationForPublishedMethodImplementation»
        public «IntegerRange» «method(methodNameGetCardinalityFor)»
        «IF genInterface»;«ELSE»
        {
            return «fieldNameCardinality»;
        }
        «ENDIF»
    '''

    def package static cardinalitySetter(XProductAssociation it) '''
        /**
         * @generated
         */
        public void «method(methodNameSetCardinalityFor, IntegerRange, "newValue")» {
            «checkRepositoryModifyable»
            this.«fieldNameCardinality» = newValue;
        }
    '''

    def package static cardinalitySetter(XPolicyAssociation it) '''
        /**
         * @generated
         */
        public void «method(methodNameSetCardinalityFor, IntegerRange, "newValue")» {
            «checkRepositoryModifyable»
            this.«fieldNameCardinality» = newValue;
        }
    '''

    def package static cardinalityGetterSetter(XProductAssociation it) '''
        «cardinalityGetter»
        «IF !genInterface»
            «cardinalitySetter»
        «ENDIF»
    '''

    def package static cardinalityGetterSetter(XPolicyAssociation it) '''
        «cardinalityGetter»
        «IF !genInterface»
            «cardinalitySetter»
        «ENDIF»
    '''

    def package static doInitCardinalitiesFromXml(XProductClass it) '''
        «IF !cardinalityConfigurableAssociations.empty || !pureCardinalityConfigurablePolicyAssociations.empty»
            /**
             * @generated
             */
            @Override
            protected void «doInitPolicyLinkCardinalitiesFromXml(Map("String", Element) + " cardinalityElements")» {
                super.«doInitPolicyLinkCardinalitiesFromXml("cardinalityElements")»;
                «FOR it : cardinalityConfigurableAssociations» «doInitCardinalityFromXmlMethodCall» «ENDFOR»
                «FOR it : pureCardinalityConfigurablePolicyAssociations» «doInitCardinalityFromXmlMethodCall» «ENDFOR»
            }

            «FOR it : cardinalityConfigurableAssociations» «doInitCardinalityFromXmlMethod» «ENDFOR»
            «FOR it : pureCardinalityConfigurablePolicyAssociations» «doInitCardinalityFromXmlMethod» «ENDFOR»
        «ENDIF»
    '''

    def package static doInitCardinalityFromXmlMethodCall(XProductAssociation it) '''
        «methodNameDoInitCardinalityFromXml»(cardinalityElements);
    '''

    def package static doInitCardinalityFromXmlMethodCall(XPolicyAssociation it) '''
        «methodNameDoInitCardinalityFromXml»(cardinalityElements);
    '''

    def package static doInitCardinalityFromXmlMethod(XProductAssociation it) '''
        /**
         * @generated
         */
        private void «method(methodNameDoInitCardinalityFromXml, Map("String", Element), "cardinalityElements")» {
            «Element» element = cardinalityElements.get("«nameOfMatchingAssociation»");
            if (element != null) {
                this.«fieldNameCardinality» = parseCardinalityRange(element);
            }
        }
    '''

    def package static doInitCardinalityFromXmlMethod(XPolicyAssociation it) '''
        /**
         * @generated
         */
        private void «method(methodNameDoInitCardinalityFromXml, Map("String", Element), "cardinalityElements")» {
            «Element» element = cardinalityElements.get("«getName(false)»");
            if (element != null) {
                this.«fieldNameCardinality» = parseCardinalityRange(element);
            }
        }
    '''

    def package static writeCardinalitiesToXml(XProductClass it) '''
        «FOR it : cardinalityConfigurableAssociations» «writeCardinalityToXmlMethod» «ENDFOR»
        «FOR it : pureCardinalityConfigurablePolicyAssociations» «writeCardinalityToXmlMethod» «ENDFOR»
    '''

    def package static writeCardinalityToXmlMethodCall(XProductAssociation it) '''
        «methodNameWriteCardinalityToXml»(element);
    '''

    def package static writeCardinalityToXmlMethodCall(XPolicyAssociation it) '''
        «methodNameWriteCardinalityToXml»(element);
    '''

    def package static writeCardinalityToXmlMethod(XProductAssociation it) '''
        /**
         * @generated
         */
        private void «method(methodNameWriteCardinalityToXml, Element, "element")» {
            «Element» cardinalityElement = element.getOwnerDocument().createElement("PolicyLinkCardinality");
            cardinalityElement.setAttribute("association", "«nameOfMatchingAssociation»");
            cardinalityElement.setAttribute("minCardinality",
                    Integer.toString(«fieldNameCardinality».getLowerBound()));
            String max = «fieldNameCardinality».getUpperBound() == Integer.MAX_VALUE
                    ? "*"
                    : Integer.toString(«fieldNameCardinality».getUpperBound());
            cardinalityElement.setAttribute("maxCardinality", max);
            element.appendChild(cardinalityElement);
        }
    '''

    def package static writeCardinalityToXmlMethod(XPolicyAssociation it) '''
        /**
         * @generated
         */
        private void «method(methodNameWriteCardinalityToXml, Element, "element")» {
            «Element» cardinalityElement = element.getOwnerDocument().createElement("PolicyLinkCardinality");
            cardinalityElement.setAttribute("association", "«getName(false)»");
            cardinalityElement.setAttribute("minCardinality",
                    Integer.toString(«fieldNameCardinality».getLowerBound()));
            String max = «fieldNameCardinality».getUpperBound() == Integer.MAX_VALUE
                    ? "*"
                    : Integer.toString(«fieldNameCardinality».getUpperBound());
            cardinalityElement.setAttribute("maxCardinality", max);
            element.appendChild(cardinalityElement);
        }
    '''

}
