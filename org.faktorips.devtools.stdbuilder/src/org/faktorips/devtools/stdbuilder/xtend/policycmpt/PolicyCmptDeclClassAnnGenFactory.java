/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;

public class PolicyCmptDeclClassAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        if (type == AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS) {
            return new PolicyCmptDeclClassAnnGen();
        } else {
            return null;
        }
    }

}
