package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class DeltaSupportTmpl {

def package static computeDeltaMethod (XPolicyCmptClass it) '''
    /**
     * «inheritDoc()»
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
            «FOR association : associationsForDeltaComputation» «deltaCheckForRelatedClasses(it, association)» «ENDFOR»
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
        ModelObjectDelta.createChildDeltas(delta, «fieldName», other«policyClass.implClassName».«fieldName», "«fieldName»", options);
    «ENDIF»
'''

}