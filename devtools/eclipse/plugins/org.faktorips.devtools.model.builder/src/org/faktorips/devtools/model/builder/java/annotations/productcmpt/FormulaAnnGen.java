/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.productcmpt;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.XMethod;
import org.faktorips.runtime.model.annotation.IpsFormula;

/**
 * Generates the {@link IpsFormula} annotation on formula methods
 */
public class FormulaAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name = \"").append(((XMethod)modelNode).getFormularName()).append("\"");

        boolean isRequired = !((XMethod)modelNode).isFormulaOptional();
        if (isRequired) {
            stringBuilder.append(", required = ").append(true);
        }

        builder.annotationLn(IpsFormula.class, stringBuilder.toString());
        return builder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XMethod;
    }
}
