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

import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;

public class ProductCmptTypeAttributesTablePageElement extends AttributesTablePageElement {

    public ProductCmptTypeAttributesTablePageElement(IType type, DocumentationContext context) {
        super(type, context);
    }

    @Override
    protected List<String> getAttributeData(IAttribute attribute) {
        List<String> attributeData = super.getAttributeData(attribute);

        IProductCmptTypeAttribute polAttribute = (IProductCmptTypeAttribute)attribute;

        attributeData.add(polAttribute.isChangingOverTime() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

        return attributeData;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = super.getHeadlineWithIpsObjectPart();

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_changeableInAdjustment),
                Style.CENTER);

        return headline;
    }
}
