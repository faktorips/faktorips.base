package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
class PolicyValidatorTmpl {

    def static String body(XPolicyCmptClass it) '''
		
		/**
		* «localizedJDoc("VALIDATOR_CLASS", implClassName)»
		«IF !type.generateValidatorClass»
		* <p>
		* «localizedJDoc("DEPRECATED_VALIDATOR_CLASS")»
		«ENDIF»
		*
		* @generated
		*/
		«IF !type.generateValidatorClass»
		@Deprecated
		«ENDIF»
		public class «implClassName»Validator
		{
		 		    
		    private final «implClassName» «nameForVariable»;
		    
		    public «implClassName»Validator(«implClassName» «nameForVariable»){
		        this.«nameForVariable» = «nameForVariable»;
		    }
		    
		    «validationMethod»
		}
	'''

    def private static validationMethod(XPolicyCmptClass it) '''
        /**
        * «localizedJDoc("VALIDATOR_METHOD_VALIDATE", implClassName)»
        * @generated
        */
        public boolean «validate(MessageList() + " ml", IValidationContext() + " context")»{
««« TODO FIPS-7041           «FOR it : validationRules» «ValidationRuleTmpl.validate(it)» «ENDFOR»
            return «CONTINUE_VALIDATION»;
        }
    '''
    }
