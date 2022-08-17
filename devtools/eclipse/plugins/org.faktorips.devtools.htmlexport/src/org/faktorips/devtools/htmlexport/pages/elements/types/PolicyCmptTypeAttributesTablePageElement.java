/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;

public class PolicyCmptTypeAttributesTablePageElement extends AttributesTablePageElement {

    public PolicyCmptTypeAttributesTablePageElement(IPolicyCmptType type, DocumentationContext context) {
        this(type, type.getAttributes(), context);
    }

    public PolicyCmptTypeAttributesTablePageElement(IPolicyCmptType type, List<IAttribute> attributes,
            DocumentationContext context) {
        super(type, attributes, context);
    }

    @Override
    protected List<String> getAttributeData(IAttribute attribute) {
        List<String> attributeData = super.getAttributeData(attribute);

        IPolicyCmptTypeAttribute polAttribute = (IPolicyCmptTypeAttribute)attribute;

        attributeData.add(polAttribute.isProductRelevant() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        attributeData.add(polAttribute.getAttributeType().getName());
        attributeData.add(polAttribute.isOverwrite() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

        return attributeData;
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IAttribute attribute) {
        List<IPageElement> rowWithIpsObjectPart = super.createRowWithIpsObjectPart(attribute);

        if (!getContext().showInheritedObjectPartsInTable()) {
            return rowWithIpsObjectPart;
        }

        List<IPageElement> rows = new ArrayList<>(rowWithIpsObjectPart);
        rows.add(createPageElementForDefiningSuperType(attribute));
        return rows;
    }

    private IPageElement createPageElementForDefiningSuperType(IAttribute attribute) {
        IPageElement pageElement;
        IPolicyCmptTypeAttribute polAttribute = (IPolicyCmptTypeAttribute)attribute;
        IPolicyCmptType attributeDefiningPolicyCmptType = polAttribute.getPolicyCmptType();

        if (attributeDefiningPolicyCmptType.equals(getType())) {
            pageElement = new TextPageElement("-", getContext()); //$NON-NLS-1$
        } else {
            pageElement = new PageElementUtils(getContext()).createLinkPageElement(getContext(),
                    attributeDefiningPolicyCmptType, TargetType.CONTENT, attributeDefiningPolicyCmptType.getName(),
                    true);
        }
        return pageElement;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = super.getHeadlineWithIpsObjectPart();

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_productRelevant),
                Style.CENTER);

        headline.add(getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_attributeType));

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_overwrite), Style.CENTER);

        if (getContext().showInheritedObjectPartsInTable()) {
            headline.add(getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_inheritedFrom));
        }

        return headline;
    }
}
