package org.faktorips.devtools.stdbuilder.xpand.policycmpt.template

import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xpand.template.ClassNamesTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.CommonGeneratorExtensionsTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.MethodNamesTmpl.*

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