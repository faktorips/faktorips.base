package org.faktorips.devtools.stdbuilder.xpand.policycmpt.template

import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation

class PolicyCmptAssociationExtensionTmpl {

    def package static parentVar(XPolicyAssociation it) {
        if(name.toLowerCase() == "parent") "existingParent" else "parent"
    }

}
