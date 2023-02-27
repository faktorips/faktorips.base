package org.faktorips.devtools.stdbuilder.xtend.template

class MethodNames {

    // ---------------------------------
    // Methods in ListUtil
    // ---------------------------------
    
    def static convert(String list, String newType) {

    // ---------------------------------
    // Methods in IModelObject or AbstractModelObject
    // ---------------------------------

    def static createUnresolvedReference(String arguments) {

    // ---------------------------------
    // Methods in IConfigurableModelObject or AbstractConfigurableModelObject
    // ---------------------------------

    def static getEffectiveFromAsCalendar()  {

    def static effectiveFromHasChanged()  {

    // ---------------------------------
    // Methods in IDependantObject
    // ---------------------------------

    def static getParentModelObject()  {

    // ---------------------------------
    // Methods in IProductComponentLink
    // ---------------------------------

    def static getTarget()  {

    def static getTargetId()  {

    def static getCardinality()  {

    // ---------------------------------
    // Methods in IProductComponent
    // ---------------------------------

    def static getId()  {

    def static getRepository()  {

    def static createPolicyComponent()  {

    def static isChangingOverTime()  {

    // ---------------------------------
    // Methods in ProductComponentGeneration
    // ---------------------------------

    def static isFormulaAvailable(String paramName)  {

    def static doInitPropertiesFromXml(String paramMap)  {

    def static doInitReferencesFromXml(String paramMap)  {

    def static doInitTableUsagesFromXml(String paramMap)  {

    def static writePropertiesToXml(String paramElement)  {

    def static writeReferencesToXml(String paramElement)  {

    def static writeTableUsagesToXml(String paramElement)  {
    
    // ---------------------------------
    // Methods in IProductComponentGeneration
    // ---------------------------------

    def static isValidationRuleActivated(String paramConstantName)  {

    def static getLink(String paramLinkName, String paramTarget)  {

    def static getLinks()  {

    // ---------------------------------
    // Methods in IConfigurableModelObject
    // ---------------------------------

    def static getProductComponent()  {

    def static getProductCmptGeneration()  {

    def static initialize() {

    def static copyProductCmptAndGenerationInternal(String argument) {

    // ---------------------------------
    // Methods in AbstractConfigurableModelObject
    // ---------------------------------

    def static setProductCmptGeneration(String argument)  {

    def static setProductComponent(String argument)  {

    def static initPropertiesFromXml(String arg1, String arg2) {

    // ---------------------------------
    // Methods in AbstractModelObject
    // ---------------------------------

    def static createChildFromXml(String argument) {

    def static validateSelf(String argument1, String argument2) {

    def static validateDependants(String argument1, String argument2) {
    
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

    def static putProductComponent(String param) {


    def static isModifiable()  {

    def static getProductComponentGeneration(String idParam, String effectiveFromParam) {

    def static getTable(String name)  {

    // ---------------------------------
    // Methods in ValueToXmlHelper
    // ---------------------------------

    def static getValueFromElement(String paramConfigElement, String paramValue)  {

    def static getValueFromElement(String paramConfigElement)  {

    def static getInternationalStringFromElement(String paramConfigElement, String paramValue)  {

    def static getEnumValueSetFromElement(String paramConfigElement, String paramValueSet)  {

    def static getRangeFromElement(String paramConfigElement, String paramValueSet)  {
    
    def static getStringLengthValueSetFromElement(String paramConfigElement, String paramValueSet)  {
     "getStringLengthValueSetFromElement(" + paramConfigElement + ", " + paramValueSet + ")"
    }

    def static addValueAndReturnElement(String valueParam, String elementParam, String tagNameParam)  {

    def static addValueToElement(String valueParam, String elementParam, String tagNameParam)  {

    def static addInternationalStringToElement(String valueParam, String elementParam, String tagNameParam)  {

    def static getUnrestrictedValueSet(String paramConfigElement, String paramValueSet)  {

    // ---------------------------------
    // Methods in MultiValueXmlHelper
    // ---------------------------------

    def static getValuesFromXML(String paramConfigElement)  {

    def static getInternationalStringsFromXML(String paramConfigElement)  {

    def static addValuesToElement(String attributeElementParam, String nameParam)  {

    def static addInternationalStringsToElement(String attributeElementParam, String nameParam)  {

    // ------------------------------------------
    // Methods in InternationalStringXmlHandler
    // ------------------------------------------

    def static fromXml(String paramConfigElement, String paramValue)  {

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

    def static getNumberOfValues()  {

    // ---------------------------------
    // Methods in IXmlPersistenceSupport
    // ---------------------------------

    def static toXml(String paramDocument)  {

    // ---------------------------------
    // Methods in IDeltaSupport
    // ---------------------------------

    def static computeDelta(String arg1, String arg2) {

    // ---------------------------------
    // Methods in ICopySupport
    // ---------------------------------

    def static newCopy() {

    def static newCopyInternal(String argument) {

    def static copyProperties(String arg1, String arg2) {

    def static copyAssociationsInternal(String arg1, String arg2) {

    // ---------------------------------
    // Methods in IVisitorSupport
    // ---------------------------------

    def static accept(String argument) {

    // ---------------------------------
    // Methods in ObjectUtil
    // ---------------------------------

    def static checkInstanceOf(String objectArg, String expectedClassName) {

    //----------------------------------
    // Method in ITable
    //----------------------------------

    def static init() {
    
}