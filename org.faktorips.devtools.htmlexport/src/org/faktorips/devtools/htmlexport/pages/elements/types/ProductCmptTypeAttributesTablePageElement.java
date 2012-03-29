/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

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
                getContext()
                        .getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_changeableInAdjustment),
                Style.CENTER);

        return headline;
    }
}