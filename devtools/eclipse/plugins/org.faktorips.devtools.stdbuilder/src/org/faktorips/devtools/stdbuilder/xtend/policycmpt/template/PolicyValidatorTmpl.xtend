package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass

import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.ValidationRuleTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*
import org.faktorips.devtools.model.builder.naming.BuilderAspect

class PolicyValidatorTmpl {

  def static String body(XPolicyCmptClass it) '''
    «importConstants»
    /**
    *«localizedJDoc("VALIDATOR_CLASS", implClassName)»
    «IF !type.generateValidatorClass»
    * <p>
    *«localizedJDoc("DEPRECATED_VALIDATOR_CLASS")»
    «ENDIF»
    *
    * @generated
    */
    «getAnnotations(POLICY_CMPT_SEPARATE_VALIDATOR_CLASS)»
    public class «implClassName»Validator«IF hasSupertype» extends «superclassValidatorName»«ENDIF» {
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
      
      «FOR v : validationRules»«validationRuleMethods(v, it)»«ENDFOR»
    }
  '''
  
  def private static importConstants(XPolicyCmptClass it) {
    var qualifiedClassName = getQualifiedName(BuilderAspect.IMPLEMENTATION)
    for (validationRule : validationRules){
      addStaticImport(qualifiedClassName, validationRule.constantNameRuleName)
      addStaticImport(qualifiedClassName, validationRule.constantNameMessageCode)
    }
  }
  
  def private static String getSuperclassValidatorName(XPolicyCmptClass it) {
    var superQualifiedName = getSupertype().getQualifiedName(BuilderAspect.IMPLEMENTATION) + "Validator"
    return addImport(superQualifiedName)
  }


  def private static getterForPolicyCmpt(XPolicyCmptClass it) '''
    /**
    * @generated
    */
    «IF !type.generateValidatorClass»
    @Deprecated
    «ENDIF»
    public «implClassName» get«implClassName»() {
        «IF hasSupertype»
          return («implClassName»)super.get«supertype.name»();
        «ELSE»
          return «nameForVariable»;
        «ENDIF»
    }
  '''

  def private static validationMethod(XPolicyCmptClass it) '''
    /**
    *«localizedJDoc("VALIDATOR_METHOD_VALIDATE", implClassName)»
    *
    * @generated
    */        
    «overrideAnnotationIfHasSuperclass»
    public boolean «validate(MessageList() + " ml", IValidationContext() + " context")»{  
      «IF hasSupertype»
        if (!super.«validate("ml", "context")») {
          return «STOP_VALIDATION»;
        }
      «ENDIF»
      «FOR it : attributesForGenericValidation»«PolicyCmptAttributeTmpl.genericValidation(it)»«ENDFOR»
      «FOR it : validationRules»«ValidationRuleTmpl.validate(it)»«ENDFOR»
      return «CONTINUE_VALIDATION»;
    }
  '''

}
