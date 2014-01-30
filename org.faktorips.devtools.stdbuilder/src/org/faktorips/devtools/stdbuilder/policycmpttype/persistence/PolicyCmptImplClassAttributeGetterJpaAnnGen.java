/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;

/**
 * This class generates JPA annotations for attribute getter methods on policy component types.
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassAttributeGetterJpaAnnGen extends AbstractJpaAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        return newJavaCodeFragment();
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER;
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        return false;
    }
}
