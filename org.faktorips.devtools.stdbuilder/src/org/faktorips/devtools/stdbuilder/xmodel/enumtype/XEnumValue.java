/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;

public class XEnumValue extends AbstractGeneratorModelNode {

    public XEnumValue(IEnumValue enumValue, GeneratorModelContext context, ModelService modelService) {
        super(enumValue, context, modelService);
    }

    private IEnumValue getEnumValue() {
        return (IEnumValue)getIpsObjectPartContainer();
    }

    public XEnumAttributeValue getLiteralNameAttributeValue() {
        return getAttributeValue(getEnumType().getEnumLiteralNameAttribute());
    }

    public String getMemberVarNameLiteralNameAttribute() {
        return getEnumValue().getEnumLiteralNameAttributeValue().getValue()
                .getLocalizedContent(getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns all attribute values for attributes (other than the literal name attribute) that have
     * a corresponding field.
     */
    public List<XEnumAttributeValue> getEnumAttributeValuesWithField() {
        ArrayList<XEnumAttributeValue> attributeValues = new ArrayList<>();
        for (IEnumAttributeValue attributeValue : getEnumValue().getEnumAttributeValues()) {
            if (!attributeValue.isEnumLiteralNameAttributeValue()) {
                XEnumAttributeValue modelNode = getModelNode(attributeValue, XEnumAttributeValue.class);
                if (modelNode.getEnumAttribute().isGenerateField()) {
                    attributeValues.add(modelNode);
                }
            }
        }
        return attributeValues;
    }

    protected XEnumAttributeValue getIdentifierAttributeValue() {
        return getAttributeValue(getEnumType().getIdentifierAttribute());
    }

    private XEnumAttributeValue getAttributeValue(XEnumAttribute xEnumAttribute) {
        return getModelNode(getEnumValue().getEnumAttributeValue(xEnumAttribute.getEnumAttribute()),
                XEnumAttributeValue.class);
    }

    public XEnumType getEnumType() {
        return getModelNode(getEnumValue().getEnumValueContainer(), XEnumType.class);
    }

    public int getIndex() {
        return getEnumType().getEnumValues().indexOf(this);
    }
}
