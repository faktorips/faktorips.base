package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass

import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*

class PolicyValidatorTmpl {

    def static String body(XPolicyCmptClass it) '''
		
		/**
		* «localizedJDoc("VALIDATOR_CLASS", implClassName)»
		*
		* @generated
		*/
		public class «implClassName»Validator«IF hasSupertype» extends «superclassName»Validator«ENDIF»
		{
		 		    
		    «IF !hasSupertype»private final «implClassName» «nameForVariable»;«ENDIF»
		    
		    public «implClassName»Validator(«implClassName» «nameForVariable») { 
          «IF hasSupertype»
            super(«nameForVariable»);
          «ELSE»
            this.«nameForVariable» = «nameForVariable»;
          «ENDIF»
		    }
		    
		    «getterForPolicyCmpt»
		    
		    «validationMethod»
		}
	'''  
    
  
    def private static getterForPolicyCmpt(XPolicyCmptClass it) '''
        /**
        * @generated
        */
        public «implClassName» get«implClassName»(){
            «IF hasSupertype»
            return («implClassName»)super.get«supertype.name»();
            «ELSE»
            return «nameForVariable»;
            «ENDIF»
        }
    '''

    def private static validationMethod(XPolicyCmptClass it) '''
        /**
        * «localizedJDoc("VALIDATOR_METHOD_VALIDATE", implClassName)»
        * @generated
        */        
        «overrideAnnotationIfHasSuperclass»
        public boolean «validate(MessageList() + " ml", IValidationContext() + " context")»{  
          «IF hasSupertype»
            if (!super.«validate("ml", "context")») {
              return «STOP_VALIDATION»;
            }
          «ENDIF»
««« TODO FIPS-7041           «FOR it : validationRules» «ValidationRuleTmpl.validate(it)» «ENDFOR»
            return «CONTINUE_VALIDATION»;
        }
    '''
    }
