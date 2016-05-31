/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.policycmpt;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.xpand.AbstractAssociationAnnGen;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;

public class PolicyCmptAssociationAnnGen extends AbstractAssociationAnnGen {

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement) {
        return ipsElement instanceof XPolicyAssociation;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragment annotation = super.createAnnotation(modelNode);

        return new JavaCodeFragmentBuilder().append(annotation)
                .append(createAnnInverseAssociation((XPolicyAssociation)modelNode)).getFragment();
    }

    protected JavaCodeFragment createAnnInverseAssociation(XAssociation association) {
        XPolicyAssociation policyAssociation = (XPolicyAssociation)association;

        try {
            XPolicyAssociation inverseAssociation = policyAssociation.getInverseAssociation();
            return new JavaCodeFragmentBuilder().annotationLn(IpsInverseAssociation.class,
                    "\"" + inverseAssociation.getName(false) + "\"").getFragment();
        } catch (NullPointerException e) {
            return new JavaCodeFragment();
        }

    }
}
