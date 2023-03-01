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
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;

/**
 * Generates the {@link IpsAttribute} annotation on attribute getter methods.
 */
public class AttributeGetterAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XAttribute xAttribute = (XAttribute)modelNode;
        IAttribute attribute = xAttribute.getAttribute();

        JavaCodeFragmentBuilder annotationCode = new JavaCodeFragmentBuilder();

        JavaCodeFragmentBuilder attributeAnnArg = new JavaCodeFragmentBuilder();
        attributeAnnArg.append("name = \"");
        attributeAnnArg.append(attribute.getName());
        attributeAnnArg.append("\", kind = ");
        attributeAnnArg.appendClassName(AttributeKind.class);
        attributeAnnArg.append(".");
        attributeAnnArg.append(getAttributeKind(attribute).name());
        attributeAnnArg.append(", valueSetKind = ");
        attributeAnnArg.appendClassName(ValueSetKind.class);
        attributeAnnArg.append(".");
        attributeAnnArg.append(getValueSetKind(attribute).name());

        annotationCode.annotationLn(IpsAttribute.class, attributeAnnArg.getFragment());

        if (xAttribute instanceof XPolicyAttribute && ((XPolicyAttribute)xAttribute).isProductRelevant()) {
            JavaCodeFragmentBuilder configuredAttributeAnnArg = new JavaCodeFragmentBuilder();
            configuredAttributeAnnArg.append("changingOverTime = ");
            configuredAttributeAnnArg.append(Boolean.toString(xAttribute.isChangingOverTime()));

            annotationCode.annotationLn(IpsConfiguredAttribute.class, configuredAttributeAnnArg.getFragment());
        }

        return annotationCode.getFragment();
    }

    private AttributeKind getAttributeKind(IAttribute attribute) {
        if (attribute instanceof IPolicyCmptTypeAttribute) {
            return AttributeKind.forName(((IPolicyCmptTypeAttribute)attribute).getAttributeType().getId());
        } else {
            return AttributeKind.CONSTANT;
        }
    }

    private ValueSetKind getValueSetKind(IAttribute attribute) {
        return switch (attribute.getValueSet().getValueSetType()) {
            case ENUM -> ValueSetKind.Enum;
            case RANGE -> ValueSetKind.Range;
            default -> ValueSetKind.AllValues;
        };
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XAttribute;
    }
}
