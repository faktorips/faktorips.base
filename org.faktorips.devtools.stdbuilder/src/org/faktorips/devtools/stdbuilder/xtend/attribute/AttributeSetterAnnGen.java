/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.attribute;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;

/**
 * Generates the {@link IpsAttributeSetter} annotation on attribute setter methods.
 */
public class AttributeSetterAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        return new JavaCodeFragmentBuilder().annotationLn(IpsAttributeSetter.class, "\"" + modelNode.getName() + "\"")
                .getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XAttribute;
    }
}
