/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;

/**
 * This class generates JPA annotations for attribute getter methods on policy component types.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER
 */
public class PolicyCmptImplClassAttributeGetterJpaAnnGen extends AbstractJpaAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        return newJavaCodeFragment();
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        return false;
    }
}
