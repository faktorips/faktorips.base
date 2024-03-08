package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class VisitorSupportTmpl {

def package static acceptMethod (XPolicyCmptClass it) '''
    /**
     *«inheritDoc(it)»
     *
     * @generated
     */
    @Override
    public boolean «accept(IModelObjectVisitor() +" visitor")» {
        «IF hasSupertype()»
            if (!super.accept(visitor)) {
                return false;
            }
        «ELSE»
            if (!visitor.visit(this)) {
                return false;
            }
        «ENDIF»
        «FOR association : associations» «acceptVisitor(association)» «ENDFOR»
        return true;
    }
'''

def private static acceptVisitor (XPolicyAssociation it) '''
    «IF considerInVisitorSupport»
        «IF oneToMany»
            for («targetInterfaceName» «visitorSupportLoopVarName» : «fieldName») {
                «visitorSupportLoopVarName».accept(visitor);
            }
        «ELSE»
            if («fieldName» != null) {
                «fieldName».accept(visitor);
            }
        «ENDIF»
    «ENDIF»
'''
}