package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.runtime.IProductComponent

class MethodNames {

    // ---------------------------------
    // Methods in ListUtil
    // ---------------------------------
    
    def static convert(String list, String newType) {     "convert("+list+", "+newType+")"    }

    // ---------------------------------
    // Methods in IModelObject or AbstractModelObject
    // ---------------------------------

    def static createUnresolvedReference(String arguments) {     "createUnresolvedReference("+arguments+")"    }

    // ---------------------------------
    // Methods in IConfigurableModelObject or AbstractConfigurableModelObject
    // ---------------------------------

    def static getEffectiveFromAsCalendar()  {     "getEffectiveFromAsCalendar()"    }

    def static effectiveFromHasChanged()  {     "effectiveFromHasChanged()"    }

    // ---------------------------------
    // Methods in IDependantObject
    // ---------------------------------

    def static getParentModelObject()  {     "getParentModelObject()"    }

    // ---------------------------------
    // Methods in IProductComponentLink
    // ---------------------------------

    def static getTarget()  {     "getTarget()"    }

    def static getTargetId()  {     "getTargetId()"    }

    def static getCardinality()  {     "getCardinality()"    }

    // ---------------------------------
    // Methods in IProductComponent
    // ---------------------------------

    def static getId()  {     "getId()"    }

    def static getRepository()  {     "getRepository()"    }

    def static createPolicyComponent()  {     "createPolicyComponent()"    }

    def static isChangingOverTime()  {     "isChangingOverTime()"    }

    // ---------------------------------
    // Methods in ProductComponentGeneration
    // ---------------------------------

    def static isFormulaAvailable(String paramName)  {     "isFormulaAvailable(" + paramName + ")"    }

    def static doInitPropertiesFromXml(String paramMap)  {     "doInitPropertiesFromXml(" + paramMap + ")"    }

    def static doInitReferencesFromXml(String paramMap)  {     "doInitReferencesFromXml(" + paramMap + ")"    }

    def static doInitTableUsagesFromXml(String paramMap)  {     "doInitTableUsagesFromXml(" + paramMap + ")"    }

    def static writePropertiesToXml(String paramElement)  {     "writePropertiesToXml(" + paramElement + ")"    }

    def static writeReferencesToXml(String paramElement)  {     "writeReferencesToXml(" + paramElement + ")"    }

    def static writeTableUsagesToXml(String paramElement)  {     "writeTableUsagesToXml(" + paramElement + ")"    }
    
    // ---------------------------------
    // Methods in IProductComponentGeneration
    // ---------------------------------

    def static isValidationRuleActivated(String paramConstantName)  {     "isValidationRuleActivated(" + paramConstantName +")"    }

    def static getLink(String paramLinkName, String paramTarget)  {     "getLink(" + paramLinkName + ", " + paramTarget +")"    }

    def static getLinks()  {     "getLinks()"    }

    // ---------------------------------
    // Methods in IConfigurableModelObject
    // ---------------------------------

    def static getProductComponent()  {     "getProductComponent()"    }

    def static getProductCmptGeneration()  {     "getProductCmptGeneration()"    }

    def static initialize() {     "initialize()"    }

    def static copyProductCmptAndGenerationInternal(String argument) {     "copyProductCmptAndGenerationInternal("+argument+")"    }

    // ---------------------------------
    // Methods in AbstractConfigurableModelObject
    // ---------------------------------

    def static setProductCmptGeneration(String argument)  {     "setProductCmptGeneration(" + argument + ")"    }

    def static setProductComponent(String argument)  {     "setProductComponent("+argument+")"    }

    def static initPropertiesFromXml(String arg1, String arg2) {     "initPropertiesFromXml("+arg1+","+arg2+")"    }

    // ---------------------------------
    // Methods in AbstractModelObject
    // ---------------------------------

    def static createChildFromXml(String argument) {     "createChildFromXml("+argument+")"    }

    def static validateSelf(String argument1, String argument2) {     "validateSelf("+argument1+", "+argument2+")"    }

    def static validateDependants(String argument1, String argument2) {     "validateDependants("+argument1+", "+argument2+")"    }
    
    def static validate(String argument1, String argument2){
        "validate("+argument1+", "+argument2+")"
    }
    
    def static getValidator(){
        "getValidator()"
    }
    
     def static createValidator(){
        "createValidator()"
    }
    
    // ---------------------------------
    // Methods in IRuntimeRepository
    // ---------------------------------

    def static putProductComponent(String param) {     "putProductComponent(" + param + ")"    }


    def static isModifiable()  {     "isModifiable()"    }

    def static getProductComponentGeneration(String idParam, String effectiveFromParam) {     "getProductComponentGeneration(" + idParam + ", " + effectiveFromParam + ")"    }

    def static getTable(String name)  {     "getTable(" + name + ")"    }

    // ---------------------------------
    // Methods in ValueToXmlHelper
    // ---------------------------------

    def static getValueFromElement(String paramConfigElement, String paramValue)  {     "getValueFromElement(" + paramConfigElement + ", " + paramValue + ")"    }

    def static getValueFromElement(String paramConfigElement)  {     "getValueFromElement(" + paramConfigElement+ ")"    }

    def static getInternationalStringFromElement(String paramConfigElement, String paramValue, String productComponent, String propertyName)  {     "getInternationalStringFromElement(" + paramConfigElement + ", " + paramValue + ", " + productComponent + ", " + propertyName + ")"    }

    def static getEnumValueSetFromElement(String paramConfigElement, String paramValueSet)  {     "getEnumValueSetFromElement(" + paramConfigElement + ", " + paramValueSet + ")"    }

    def static getRangeFromElement(String paramConfigElement, String paramValueSet)  {     "getRangeFromElement(" + paramConfigElement + ", " + paramValueSet + ")"    }
    
    def static getStringLengthValueSetFromElement(String paramConfigElement, String paramValueSet)  {
     "getStringLengthValueSetFromElement(" + paramConfigElement + ", " + paramValueSet + ")"
    }

    def static addValueAndReturnElement(String valueParam, String elementParam, String tagNameParam)  {     "addValueAndReturnElement(" + valueParam + ", " +  elementParam + ", " + tagNameParam + ")"    }

    def static addValueToElement(String valueParam, String elementParam, String tagNameParam)  {     "addValueToElement(" + valueParam + ", " +  elementParam + ", " + tagNameParam + ")"    }
    
    def static setValue(String valueParam, String elementParam)  {
     "setValue(" + valueParam + ", " +  elementParam + ")"
    }

    def static addInternationalStringToElement(String valueParam, String elementParam, String tagNameParam)  {     "addInternationalStringToElement(" + valueParam + ", " +  elementParam + ", " + tagNameParam + ")"    }

    def static getUnrestrictedValueSet(String paramConfigElement, String paramValueSet)  {     "getUnrestrictedValueSet("+ paramConfigElement + ", " + paramValueSet + ")"    }
    
    
    def static deleteExistingElementAndCreateNewElement(String elementParam, String tagNameParam, String attributeNameParam) {
      "deleteExistingElementAndCreateNewElement(" + elementParam + ", " + tagNameParam + ", " + attributeNameParam + ")"
    }
    

    // ---------------------------------
    // Methods in MultiValueXmlHelper
    // ---------------------------------

    def static getValuesFromXML(String paramConfigElement)  {     "getValuesFromXML(" + paramConfigElement + ")"    }

    def static getInternationalStringsFromXML(String paramConfigElement, String productComponent, String propertyName)  {     "getInternationalStringsFromXML(" + paramConfigElement + ", " + productComponent + ", " + propertyName + ")"    }

    def static addValuesToElement(String attributeElementParam, String nameParam)  {     "addValuesToElement(" + attributeElementParam + ", " + nameParam + ")"    }

    def static addInternationalStringsToElement(String attributeElementParam, String nameParam)  {     "addInternationalStringsToElement(" + attributeElementParam + ", " + nameParam + ")"    }

    // ------------------------------------------
    // Methods in InternationalStringXmlHandler
    // ------------------------------------------

    def static fromXml(String paramConfigElement, String paramValue)  {     "fromXml(" + paramConfigElement + ", " + paramValue + ")"    }

    // ---------------------------------
    // Methods in ValueSet
    // ---------------------------------

    def static containsNull()  {
     "containsNull()"
    }

    def static isEmpty()  {
     "isEmpty()"
    }

    // ---------------------------------
    // Methods in EnumValueSet
    // ---------------------------------

    def static getNumberOfValues()  {     "getNumberOfValues()"    }

    // ---------------------------------
    // Methods in IXmlPersistenceSupport
    // ---------------------------------

    def static toXml(String paramDocument)  {     "toXml(" + paramDocument + ")"    }

    // ---------------------------------
    // Methods in IDeltaSupport
    // ---------------------------------

    def static computeDelta(String arg1, String arg2) {     "computeDelta("+arg1+","+arg2+")"    }

    // ---------------------------------
    // Methods in ICopySupport
    // ---------------------------------

    def static newCopy() {     "newCopy()"    }

    def static newCopyInternal(String argument) {     "newCopyInternal("+argument+")"    }

    def static copyProperties(String arg1, String arg2) {     "copyProperties("+arg1+","+arg2+")"    }

    def static copyAssociationsInternal(String arg1, String arg2) {     "copyAssociationsInternal("+arg1+","+arg2+")"    }

    // ---------------------------------
    // Methods in IVisitorSupport
    // ---------------------------------

    def static accept(String argument) {     "accept("+argument+")"    }

    // ---------------------------------
    // Methods in ObjectUtil
    // ---------------------------------

    def static checkInstanceOf(String objectArg, String expectedClassName) {     "checkInstanceOf(" + objectArg + ", " + expectedClassName + ".class)"    }

    //----------------------------------
    // Method in ITable
    //----------------------------------

    def static init() {     "init()"    }
    
}
