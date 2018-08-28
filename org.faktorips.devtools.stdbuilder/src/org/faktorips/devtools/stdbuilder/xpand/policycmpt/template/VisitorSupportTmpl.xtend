package org.faktorips.devtools.stdbuilder.xpand.policycmpt.template

import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xpand.template.ClassNamesTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.CommonGeneratorExtensionsTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.MethodNamesTmpl.*

class VisitorSupportTmpl {

def package static acceptMethod (XPolicyCmptClass it) '''
    /**
     * «inheritDoc»
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