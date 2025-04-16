package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XValidationRule
import org.faktorips.devtools.model.valueset.ValueSetType

import static org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*

class ValidationRuleTmpl {

def package static validate (XValidationRule it) '''
        if (!«methodNameExecRule»(ml, context)) {
            return «STOP_VALIDATION»;
        }
'''

def package static constants (XValidationRule it) '''
    «IF !overwrite || differentMessageCodeInOverridingRule»
        «IF differentMessageCodeInOverridingRule && !generatePublishedInterfaces(it)»@SuppressWarnings("hiding")«ENDIF»
        «constantMsgCode»
    «ENDIF»
    «IF !overwrite»
        «constantRuleName»
    «ENDIF»
'''
def package static validationRuleMethods (XValidationRule it, XPolicyCmptClass modelObject) '''
    «IF modelObject !== null»
      «execRuleMethod("get"+ modelObject.implClassName +"().")»
      «createMessageFor(modelObject.interfaceOrClassName + ".","get"+ modelObject.implClassName +"()")»
    «ELSE»
      «execRuleMethod("")»
      «createMessageFor("","this")»
    «ENDIF»
'''


def package static validationRuleMethods (XValidationRule it) '''«validationRuleMethods(it,null)»'''

def private static constantMsgCode (XValidationRule it) '''
    /**
     *«localizedJDoc("FIELD_MSG_CODE", name)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @generated
     */
    public static final String «field(constantNameMessageCode)» = "«messageCode»";
'''

def private static constantRuleName (XValidationRule it) '''
    «IF configured»
        /**
         *«localizedJDoc("FIELD_RULE_NAME", name)»
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        public static final String «field(constantNameRuleName)» = "«name»";
    «ENDIF»
'''

def private static execRuleMethod (XValidationRule it, String modelObject) '''
    /**
     «IF description.length > 0»
     * «description»
     * <p>
     «ENDIF»
     *
     *«localizedJDoc("EXEC_RULE", name)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @restrainedmodifiable
     */
    «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_VALIDATION_RULE)»
    «overrideAnnotationIf(it, overwrite)»
    protected boolean «method(methodNameExecRule, MessageList, "ml", IValidationContext, "context")» {
    	«IF overwrite»
    		// begin-user-code
    		super.«methodNameExecRule»(ml, context);
    		Message message = ml.getMessageByCode(«qualifierForConstantNameMessageCodeIfNecessary»«constantNameMessageCode»);
    		if (message != null) {
    			«methodNameCreateMessage»(context«getReplacementParametersForOverwrittingRule»«IF validatedAttrSpecifiedInSrc», new «ObjectProperty()»[0]«ENDIF»);
    		}
    		return «CONTINUE_VALIDATION»;
    		// end-user-code
    	«ENDIF»
    	«IF !overwrite»
	        «IF configured»
	        if («modelObject»«IF changingOverTime»«getProductCmptGeneration()»«ELSE»«getProductComponent()»«ENDIF».«isValidationRuleActivated(constantNameRuleName)») {
	        «ENDIF»
	        «IF !checkValueAgainstValueSetRule»
	             // begin-user-code
	            «localizedComment("EXEC_RULE_IMPLEMENT_TODO", name)»
	            if (true) {
	                ml.add(«methodNameCreateMessage»(context «FOR param : replacementParameters», null«ENDFOR»«IF validatedAttrSpecifiedInSrc», new «ObjectProperty()»[0]«ENDIF»));«IF needTodoCompleteCallCreateMsg»
	                «localizedComment("EXEC_RULE_COMPLETE_CALL_CREATE_MSG_TODO",name)»
	                «ENDIF»
	            }
	            return «CONTINUE_VALIDATION»;
	            // end-user-code
	        «ELSE»
	            «val attribute = checkedAttribute»
	            «val valueSetMethods = generatorConfig.valueSetMethods»
	            «val valueSetType = GenerateValueSetType.mapFromSettings(valueSetMethods, GenerateValueSetType.GENERATE_UNIFIED)»
	            «IF !attribute.datatype.primitive && (attribute.valueSetType == ValueSetType.RANGE || attribute.valueSetType == ValueSetType.ENUM)»
	                if («modelObject»«attribute.getMethodNameGetAllowedValuesFor(valueSetType)»(«attribute.allowedValuesMethodParameter(GenerateValueSetType.GENERATE_BY_TYPE,valueSetType)»).isEmpty() && «modelObject»«attribute.methodNameGetter»() == «attribute.datatypeHelper.nullExpression.sourcecode») {
	                    return «CONTINUE_VALIDATION»;
	                }
	            «ENDIF»
	            if (!«modelObject»«attribute.getMethodNameGetAllowedValuesFor(valueSetType)»(«attribute.allowedValuesMethodParameter(GenerateValueSetType.GENERATE_BY_TYPE,valueSetType)»).contains(«modelObject»«attribute.methodNameGetter»())) {
	                // begin-user-code
	                ml.add(«methodNameCreateMessage»(context «FOR param : replacementParameters», null«ENDFOR»));
	                // end-user-code
	            }
	              return «CONTINUE_VALIDATION»;
	        «ENDIF»
	        «IF configured»
	        }
	        return «CONTINUE_VALIDATION»;
	        «ENDIF»
        «ENDIF»
    }
'''

def private static createMessageFor (XValidationRule it, String modelObject, String that) '''
    /**
     *«localizedJDoc("CREATE_MESSAGE", name)»
    «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
     *
     * @generated
     */
    «overrideAnnotationIf(it, overwrite && !signatureChanged)»
    protected «Message()» «method(methodNameCreateMessage, createMessageParameters)» {
        «IF validateAttributes && !validatedAttrSpecifiedInSrc»
                «List_(ObjectProperty())» invalidObjectProperties = «Arrays()».asList(
                «FOR constant : validatedAttributeConstants SEPARATOR  ","»
                new «ObjectProperty()»(«that», «modelObject»«constant»)
                «ENDFOR»
                 );
        «ENDIF»
        «IF containsReplacementParameters»
            «MsgReplacementParameter()»[] replacementParameters = new «MsgReplacementParameter()»[] {
                «FOR param : replacementParameters SEPARATOR  ","»
                    new «MsgReplacementParameter()»("«param»", «param»)
                «ENDFOR»
                 };
        «ENDIF»
        «MessagesHelper()» messageHelper = new «MessagesHelper()»("«validateMessageBundleName»",
                getClass().getClassLoader(), «defaultLocale»);
        String msgText = messageHelper.getMessage("«validationMessageKey»", context.getLocale() «FOR param : replacementParameters», «param»«ENDFOR»);

           «Message()».Builder builder = new «Message()».Builder(msgText, «severityConstant»)
               .code(«qualifierForConstantNameMessageCodeIfNecessary»«constantNameMessageCode»)
          «IF validatedAttrSpecifiedInSrc»
               .invalidObjects(invalidObjectProperties)
             «ELSEIF validateAttributes»
               .invalidObjects(invalidObjectProperties)
          «ELSE»
            .invalidObject(new «ObjectProperty()»(«that»))
          «ENDIF»
             «IF containsReplacementParameters»
               .replacements(replacementParameters)
             «ENDIF»
             «IF containingMarkers»
               .markers(«FOR param : markers SEPARATOR  ","» «param»«ENDFOR»)
             «ENDIF»
               ;
        return builder.create();
    }
'''
  
def private static getInterfaceOrClassName(XPolicyCmptClass it)'''
    «IF genInterface»
        «interfaceName»
    «ELSE»
        «name»
    «ENDIF»
'''

}