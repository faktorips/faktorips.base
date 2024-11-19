/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.enums;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;

public class EnumAttributeAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        return createIpsEnumAttributeAnnotation((XEnumAttribute)modelNode);
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XEnumAttribute;
    }

    private JavaCodeFragment createIpsEnumAttributeAnnotation(XEnumAttribute attribute) {
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();
        StringBuilder attributes = new StringBuilder();
        attributes.append("name = \"");
        attributes.append(attribute.getName());
        attributes.append("\"");
        if (attribute.isIdentifier()) {
            attributes.append(", identifier = true");
        }
        if (attribute.isUnique()) {
            attributes.append(", unique = true");
        }
        if (attribute.isDisplayName()) {
            attributes.append(", displayName = true");
        }
        if (attribute.isMandatory()) {
            attributes.append(", mandatory = true");
        }
        codeFragmentBuilder.annotationLn(IpsEnumAttribute.class, attributes.toString());
        return codeFragmentBuilder.getFragment();
    }

}
