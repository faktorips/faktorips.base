package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode
import org.faktorips.devtools.stdbuilder.xmodel.XAssociation
import org.faktorips.devtools.stdbuilder.xmodel.XAttribute
import org.faktorips.devtools.stdbuilder.xmodel.XClass
import org.faktorips.devtools.stdbuilder.xmodel.XType
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass
import org.faktorips.util.StringUtil

class CommonGeneratorExtensions {

    var static ThreadLocal<Boolean> generateInterface = new ThreadLocal


    def static setGenInterface(boolean genInterface) {
        generateInterface.set(genInterface)
    }

    def static boolean genInterface() {
        generateInterface.get
    }

    def static String camelCaseToUnderscore(String s) {

        StringUtil.camelCaseToUnderscore(s)
    }

    // Use it function for methods that overrides another method (no interface method) on a given condition.
    // Returns the override annotation if the given condition is true.
    def static overrideAnnotationIf(AbstractGeneratorModelNode it, boolean condition) {
        if(condition) "@Override"
    }

    // Use it function for published methods. i.e. methods that are defined in a published interface.
    // Returns the @Override annotation if published interfaces are being generated
    def static overrideAnnotationForPublishedMethod(AbstractGeneratorModelNode it) {
        if(generatePublishedInterfaces) "@Override"
    }
    
    def static generateUnifiedMethodToGetAllowedValues(AbstractGeneratorModelNode it){
        generatorConfig.valueSetMethods.unified
    }
    
    def static generateByTypeMethodsToGetAllowedValues(AbstractGeneratorModelNode it){
        generatorConfig.valueSetMethods.byValueSetType
    }

    def static generateBothMethodsToGetAllowedValues(AbstractGeneratorModelNode it){
        generatorConfig.valueSetMethods.both
    }

    def static generatePublishedInterfaces(AbstractGeneratorModelNode it){
        generatorConfig.isGeneratePublishedInterfaces(ipsProject)
    }
    
    def static formulaCompiling(AbstractGeneratorModelNode it){
        generatorConfig.formulaCompiling
    }
    
    def static generateToXmlSupport(AbstractGeneratorModelNode it){
        generatorConfig.generateToXmlSupport
    }
    
    def static generateProductBuilder(AbstractGeneratorModelNode it){
        generatorConfig.generateProductBuilder
    }
    
    def static generateChangeSupport(AbstractGeneratorModelNode it){
        generatorConfig.generateChangeSupport
    }
    
    def static generatePolicyBuilder(AbstractGeneratorModelNode it){
        generatorConfig.generatePolicyBuilder
    }
    
    def static generateCopySupport(AbstractGeneratorModelNode it){
        generatorConfig.generateCopySupport
    }
    
    def static generateSerializablePolicyCmptsSupport(AbstractGeneratorModelNode it){
        generatorConfig.generateSerializablePolicyCmptSupport
    }
    
    def static generateConvenienceGetters(AbstractGeneratorModelNode it){
        generatorConfig.generateConvenienceGetters
    }
    
    def static generateDeltaSupport(AbstractGeneratorModelNode it){
        generatorConfig.generateDeltaSupport
    }
    
    def static generateVisitorSupport(AbstractGeneratorModelNode it){
        generatorConfig.generateVisitorSupport
    }
    
    def static isGenerateMinimalJavadoc(AbstractGeneratorModelNode it){
        generatorConfig.isGenerateMinimalJavadoc
    }
    
    // Use it function for published methods. i.e. methods that are defined in a published interface.
    // Returns the @Override annotation if the condition is true and at the same time published interfaces are being generated
    def static overrideAnnotationForPublishedMethodImplementation(AbstractGeneratorModelNode it) {
        if (!genInterface() && generatePublishedInterfaces)
            "@Override"
    }

    // Use it function for published methods that require an override in a special condition. i.e. methods
    // that are defined in a published interface (if generated) but may also be overridden in subclasses. Returns the
    // @Override annotation if published interfaces are being generated or if the condition is true .
    def static overrideAnnotationForPublishedMethodOrIf(AbstractGeneratorModelNode it, boolean condition) {
        if (generatePublishedInterfaces || condition)
            "@Override"
    }

    // Use it function for potentially published methods that require an override in a special condition. i.e. methods
    // that are defined in a published interface (if generated) but may also be overridden in subclasses. Returns the
    // @Override annotation if the first parameter (isPublished) is true and at the same time published interfaces are being generated
    // or if the condition is true.
    def static overrideAnnotationForPublishedMethodOrIf(AbstractGeneratorModelNode it, boolean isPublished,
        boolean condition) {
        if (generatePublishedInterfaces && isPublished || condition)
            "@Override"
    }

    def static overrideAnnotationForPublishedMethodImplementationOr(AbstractGeneratorModelNode it, boolean condition) {
        overrideAnnotationIf((generatePublishedInterfaces && !genInterface()) || condition)
    }

    def static overrideAnnotationForConstainedAssociation(XAssociation it) {
        overrideAnnotationForPublishedMethodImplementationOr(it, constrain)
    }

    // Use it function for attribute methods (e.g. getter, setter).
    // Will return the @Override annotation if the attribute overwrites an attribute in a supertype.
    // If the attribute does not override another attribute, the boolean parameter specifies whether the attribute
    // is published
    def static overrideAnnotationForAttribute(XAttribute it) {
        overrideAnnotationForPublishedMethodOrIf(published, overwrite)
    }

    // Use it function if the method in question is generated for all generated classes and subclasses always
    // override their super class' implementation. The method in question should not be an interface method.
    // Returns the @Override annotation if the given XPolicyCmptClass has a superclass.
    // Returns nothing in all other cases.
    def static overrideAnnotationIfHasSuperclass(XType it) {
        if(hasSupertype()) "@Override"
    }

    def static inheritDoc(AbstractGeneratorModelNode it) {
        generateMinimalJavadoc ? "" : "{@inheritDoc}"
    }

    def static inheritDocOrText(AbstractGeneratorModelNode it, String text) {
        inheritDocOrTextIf(false, text)
    }

    def static inheritDocOrTextIf(AbstractGeneratorModelNode it, boolean generateInterface, String text) {
        if (generatePublishedInterfaces && !generateInterface)
            inheritDoc
        else
            text
    }

    def static inheritDocOrJavaDoc(AbstractGeneratorModelNode it, String key) {
        inheritDocOrJavaDocIf(false, key)
    }

    def static inheritDocOrJavaDoc(AbstractGeneratorModelNode it, String key, String... params) {
        inheritDocOrJavaDocIf(false, key, params)
    }

    def static inheritDocOrJavaDocIf(AbstractGeneratorModelNode it, boolean generatesInterface, String key) {
        if(generatePublishedInterfaces && !generatesInterface) inheritDoc else localizedJDoc(key)
    }

    def static inheritDocOrJavaDocIf(AbstractGeneratorModelNode it, boolean generatesInterface, String key,
        String... params) {
        if(generatePublishedInterfaces && !generatesInterface) inheritDoc else localizedJDoc(key, params)
    }

    def static localizedJDocOrDescription(AbstractGeneratorModelNode it, String key, String param, String description) {
        if(description.length > 0) description else localizedJDoc(key, param)
    }

    def static localizedJDocOrDescription(AbstractGeneratorModelNode it, String key, String param1, String param2,
        String description) {
        if(description.length > 0) description else localizedJDoc(key, param1, param2)
    }

    def static isAbstract(XType it) {
        if(abstract && !genInterface()) "abstract"
    }

    def static isPolicyCmptClass(XClass xClass) {
        if(XPolicyCmptClass.isInstance(xClass)) true else false
    }

    def static castToImplementation(AbstractGeneratorModelNode it, String className) {
        if(generatePublishedInterfaces) "(" + className + ")" else ""
    }

    def static castToImplementation(AbstractGeneratorModelNode it, String className, String varName) {
        if(generatePublishedInterfaces) "((" + className + ")" + varName + ")" else varName
    }

    def static castFromTo(AbstractGeneratorModelNode it, String currentClassName, String castClassName) {
        if(currentClassName != castClassName) "(" + castClassName + ")"
    }

    def static castFromTo(AbstractGeneratorModelNode it, String currentClassName, String castClassName,
        String varName) {
        if(currentClassName != castClassName) "((" + castClassName + ")" + varName + ")" else varName
    }
}
