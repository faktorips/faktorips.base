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

import java.util.List;
import java.util.stream.Collectors;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XTableUsage;
import org.faktorips.runtime.model.annotation.IpsTableUsage;

/**
 * Generates the {@link IpsTableUsage} annotation on getter methods
 */
public class TableUsageAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name = \"").append(modelNode.getName()).append("\"");

        XTableUsage tableUsage = (XTableUsage)modelNode;
        List<String> tableClassNames = tableUsage.getAllTableClassNames();

        boolean isRequired = tableUsage.getTableStructureUsage().isMandatoryTableContent();
        stringBuilder.append(", required = ").append(isRequired);

        if (!tableClassNames.isEmpty()) {
            String joinedTableClasses = tableClassNames.stream()
                    .map(className -> className + ".class")
                    .collect(Collectors.joining(", "));

            stringBuilder.append(", tableClasses = ");
            if (tableClassNames.size() > 1) {
                stringBuilder.append("{").append(joinedTableClasses).append("}");
            } else {
                stringBuilder.append(joinedTableClasses);
            }
        }

        builder.annotationLn(IpsTableUsage.class, stringBuilder.toString());
        return builder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XTableUsage;
    }
}
