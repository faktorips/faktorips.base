package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class PolicyCmptAttributeExtensionTmpl {

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc) {

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc, Object param) {

def package static inheritDocOrJavaDoc(XPolicyAttribute it, String jDoc, Object param, Object desc) {

def package static getPropertyValueContainer(XPolicyAttribute it, Boolean publishedInterface)  {
 else if (!generatePublishedInterfaces || publishedInterface) methodNameGetProductCmpt + "()"
 else "((" + policyCmptNode.productCmptNode.implClassName + ")" + methodNameGetProductCmpt + "())"

def private static getProductCmptGeneration(XPolicyAttribute it, Boolean publishedInterface)  {
 else "((" + policyCmptNode.productCmptGenerationNode.implClassName + ")" + methodNameGetProductCmptGeneration + "())"

}