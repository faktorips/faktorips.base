/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xtend.association.AbstractAssociationAnnGen;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;

public class PolicyCmptAssociationGetterAnnGen extends AbstractAssociationAnnGen {

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode node) {
        return node instanceof XPolicyAssociation || node instanceof XDerivedUnionAssociation;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragment superAnnotations = super.createAnnotation(modelNode);

        XPolicyAssociation xPolicyAssociation = getXPolicyAssociation(modelNode);
        if (xPolicyAssociation.hasInverseAssociation()) {
            return new JavaCodeFragmentBuilder().append(superAnnotations)
                    .append(createAnnInverseAssociation(xPolicyAssociation)).getFragment();
        } else {
            return superAnnotations;
        }
    }

    protected XPolicyAssociation getXPolicyAssociation(AbstractGeneratorModelNode modelNode) {
        if (modelNode instanceof XPolicyAssociation) {
            return (XPolicyAssociation)modelNode;
        } else if (modelNode instanceof XDerivedUnionAssociation derivedUnionAssociation) {
            return modelNode.getModelNode(derivedUnionAssociation.getAssociation(), XPolicyAssociation.class);
        } else {
            throw new IllegalArgumentException("Unsupported model node " + modelNode);
        }
    }

    protected JavaCodeFragment createAnnInverseAssociation(XPolicyAssociation association) {
        XPolicyAssociation inverseAssociation = association.getInverseAssociation();
        return new JavaCodeFragmentBuilder().annotationLn(IpsInverseAssociation.class,
                "\"" + inverseAssociation.getName(false) + "\"").getFragment();

    }
}
