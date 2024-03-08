package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation

class PolicyCmptAssociationExtensionTmpl {

    def package static parentVar(XPolicyAssociation it) {
        if(name.toLowerCase() == "parent") "existingParent" else "parent"
    }

}
