package org.faktorips.devtools.stdbuilder.xtend.productcmpt.templateimport org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAttributeimport static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*import static extension org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*class ProductAttributeExtensionTmpl {    def package static xmlValueType(        XProductAttribute it) {        if(multilingual) DefaultInternationalString else "String"    }    def package static getFromElement(        XProductAttribute it,        String paramConfigElement,        String paramValue) {        if(multilingual) getInternationalStringFromElement(            paramConfigElement,            paramValue) else getValueFromElement(            paramConfigElement,            paramValue)    }    def package static addToElement(        XProductAttribute it,        String valueParam,        String elementParam,        String tagNameParam) {        if(multilingual) addInternationalStringToElement(            valueParam,            elementParam,            tagNameParam) else addValueToElement(            valueParam,            elementParam,            tagNameParam)    }    def package static getMultiValueFromXML(        XProductAttribute it,        String paramConfigElement) {        if(multilingual) getInternationalStringsFromXML(            paramConfigElement) else getValuesFromXML(            paramConfigElement)    }    def package static addMultiValueToElement(        XProductAttribute it,        String paramConfigElement,        String nameParam) {        if(multilingual) addInternationalStringsToElement(            paramConfigElement,            nameParam) else addValuesToElement(            paramConfigElement,            nameParam)    }}