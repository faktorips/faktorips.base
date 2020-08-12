package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.ValidationRuleTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
class PolicyValidatorTmpl {

    def static String body(XPolicyCmptClass it) '''
		
		/**
		* «localizedJDoc("VALIDATOR_CLASS", implClassName)»
		*
		* @generated
		*/
		public class «implClassName»Validator
		{
		 
		    private static final boolean STOP_VALIDATION = false;

		    private static final boolean CONTINUE_VALIDATION = true;
		    
            «FOR it : validationRules» «constants» «ENDFOR»
		    
		    private «implClassName» policyCmpt;
		    
		    public «implClassName»Validator(«implClassName» policyCmpt){
		        this.policyCmpt = policyCmpt;
		    }
		    
		    «validationMethod»
		    
		    «FOR it : validationRules» «validationRuleMethods("policyCmpt.")» «ENDFOR»
		}
	'''

    def private static validationMethod(XPolicyCmptClass it) '''
        /**
        * «localizedJDoc("VALIDATOR_METHOD_VALIDATE_POLICY", implClassName)»
        * @generated
        */
        public boolean «validatePolicy(MessageList() + " ml", IValidationContext() + " context")»{
            «FOR it : validationRules» «ValidationRuleTmpl.validate(it)» «ENDFOR»
            return CONTINUE_VALIDATION;
        }
    '''
    }
