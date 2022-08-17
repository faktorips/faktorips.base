package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass

import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class DeltaSupportTmpl {

def package static computeDeltaMethod (XPolicyCmptClass it) '''
    /**
     *«inheritDoc»
     *
     * @generated
     */
    @Override
    public «IModelObjectDelta()» «computeDelta(IModelObject() +" otherObject", IDeltaComputationOptions()+" options")» {
        «IF hasSupertype()»
            «ModelObjectDelta()» delta = («ModelObjectDelta()») super.computeDelta(otherObject, options);
        «ELSE»
            «ModelObjectDelta()» delta = «ModelObjectDelta()».newDelta(this, otherObject, options);
        «ENDIF»
        if (!«implClassName».class.isAssignableFrom(otherObject.getClass())) {
            return delta;
        }
        «IF !attributesForDeltaComputation.isEmpty || !associationsForDeltaComputation.isEmpty»
            «implClassName» «localVarNameDeltaSupportOtherObject» = («implClassName»)otherObject;
            «FOR attribute : attributesForDeltaComputation» «deltaCheckForAttribute(it, localVarNameDeltaSupportOtherObject, attribute)» «ENDFOR»
            «FOR association : associationsForDeltaComputation.filter[isMasterToDetail]» «deltaCheckForRelatedClasses(it, association)» «ENDFOR»
            «IF !associationsForDeltaComputation.filter[isAssociation].isEmpty»
                if (!options.ignoreAssociations()) {
                    «FOR association : associationsForDeltaComputation.filter[isAssociation]» «deltaCheckForRelatedClasses(it, association)» «ENDFOR»
                }
            «ENDIF»
        «ENDIF»
        return delta;
    }
'''

def private static deltaCheckForAttribute(XPolicyCmptClass policyClass, String localVarName, XPolicyAttribute it) '''
    «IF considerInDeltaComputation»
        delta.checkPropertyChange(«policyClass.publishedInterfaceName».«constantNamePropertyName», «fieldName», «localVarName».«fieldName», options);
    «ENDIF»
'''

def private static deltaCheckForRelatedClasses(XPolicyCmptClass policyClass ,XPolicyAssociation it) '''
    «IF considerInDeltaComputation»
        ModelObjectDelta.create«IF isAssociation»Associated«ENDIF»ChildDeltas(delta, «fieldName», other«policyClass.implClassName».«fieldName», «constantNamePropertyName», options);
    «ENDIF»
'''

}