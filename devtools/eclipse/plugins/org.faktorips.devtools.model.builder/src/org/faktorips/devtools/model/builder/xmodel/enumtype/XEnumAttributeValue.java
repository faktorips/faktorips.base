/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.enumtype;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.runtime.internal.PropertiesReadingInternationalString;

public class XEnumAttributeValue extends AbstractGeneratorModelNode {

    public XEnumAttributeValue(IEnumAttributeValue enumAttributeValue, GeneratorModelContext context,
            ModelService modelService) {
        super(enumAttributeValue, context, modelService);
    }

    private IEnumAttributeValue getEnumAttributeValue() {
        return (IEnumAttributeValue)getIpsObjectPartContainer();
    }

    public XEnumAttribute getEnumAttribute() {
        return getModelNode(getEnumAttributeValue().findEnumAttribute(getIpsProject()), XEnumAttribute.class);
    }

    public String getEnumAttributeName() {
        return getEnumAttribute().getName();
    }

    private XEnumValue getEnumValue() {
        return getModelNode(getEnumAttributeValue().getEnumValue(), XEnumValue.class);
    }

    public String getMemberVariableValue() {
        if (getEnumAttribute().isMultilingual()) {
            String identifierAttributeValue = getEnumValue().getIdentifierAttributeValue().getStringValue();
            String key = getEnumAttributeName() + "_" + identifierAttributeValue;
            return "new " + addImport(PropertiesReadingInternationalString.class) + "(\"" + key + "\", "
                    + getEnumAttribute().getEnumType().getVarNameMessageHelper() + ")";
        } else {
            JavaCodeFragment newInstanceCode = getDatatypeHelper()
                    .newInstance(getEnumAttributeValue().getStringValue());
            addImport(newInstanceCode.getImportDeclaration());
            return newInstanceCode.getSourcecode();
        }
    }

    public DatatypeHelper getDatatypeHelper() {
        return getDatatypeHelper(getEnumAttribute().getDatatypeIgnoreEnumContents());
    }

    private String getStringValue() {
        return getEnumAttributeValue().getStringValue();
    }
}
