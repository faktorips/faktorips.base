package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class PolicyCmptAttributeExtensionTmpl {

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc) { if (overwrite || generatePublishedInterfaces) "{@inheritDoc}" else localizedJDoc(jDoc)}

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc, Object param) { if (overwrite || generatePublishedInterfaces) "{@inheritDoc}" else localizedJDoc(jDoc, param)}

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc, Object param, Object desc) { if (overwrite || generatePublishedInterfaces) "{@inheritDoc}" else localizedJDoc(jDoc, param, desc)}

def package static getPropertyValueContainer(XPolicyAttribute it, Boolean publishedInterface)  { if (changingOverTime) getProductCmptGeneration(it, publishedInterface)
 else if (!generatePublishedInterfaces || publishedInterface) methodNameGetProductCmpt + "()"
 else "((" + policyCmptNode.productCmptNode.implClassName + ")" + methodNameGetProductCmpt + "())"}

def private static getProductCmptGeneration(XPolicyAttribute it, Boolean publishedInterface)  { if (!generatePublishedInterfaces || publishedInterface)  methodNameGetProductCmptGeneration + "()"
 else "((" + policyCmptNode.productCmptGenerationNode.implClassName + ")" + methodNameGetProductCmptGeneration + "())"}

}