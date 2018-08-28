package org.faktorips.devtools.stdbuilder.xpand.policycmpt.template

import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute

class PolicyCmptAttributeExtensionTmpl {


def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc) { if (overwrite || generatePublishedInterfaces) "{@inheritDoc}" else localizedJDoc(jDoc)}

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc, Object param) { if (overwrite || generatePublishedInterfaces) "{@inheritDoc}" else localizedJDoc(jDoc, param)}

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc, Object param, Object desc) { if (overwrite || generatePublishedInterfaces) "{@inheritDoc}" else localizedJDoc(jDoc, param, desc)}


def package static getPropertyValueContainer(XPolicyAttribute it, Boolean publishedInterface)  { if (changingOverTime) getProductCmptGeneration(it, publishedInterface)
    else methodNameGetProductCmpt + "()"}

def private static getProductCmptGeneration(XPolicyAttribute it, Boolean publishedInterface)  { if (!generatePublishedInterfaces || publishedInterface)  methodNameGetProductCmptGeneration + "()"
    else "((" + policyCmptNode.productCmptGenerationNode.implClassName + ")" + methodNameGetProductCmptGeneration + "())"}

}