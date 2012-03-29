/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.PolicyCmptTypeAttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ValidationRuleTablePageElement;

/**
 * A page representing an {@link IPolicyCmptType}
 * 
 * @author dicker
 * 
 */
public class PolicyCmptTypeContentPageElement extends AbstractTypeContentPageElement<IPolicyCmptType> {

    /**
     * creates a page for the given object according to the given context
     * 
     */
    PolicyCmptTypeContentPageElement(IPolicyCmptType object, DocumentationContext context) {
        super(object, context);
    }

    @Override
    public void build() {
        super.build();

        addValidationRuleTable();
    }

    private void addValidationRuleTable() {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.PolicyCmptTypeContentPageElement_rules), TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new ValidationRuleTablePageElement(getDocumentedIpsObject(),
                getContext()),
                getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_noValidationrules)));

        addPageElements(wrapper);
    }

    @Override
    AttributesTablePageElement getAttributesTablePageElement() {
        if (getContext().showInheritedObjectPartsInTable()) {
            try {
                List<IAttribute> attributes = getDocumentedIpsObject().findAllAttributes(getContext().getIpsProject());
                return new PolicyCmptTypeAttributesTablePageElement(getDocumentedIpsObject(), attributes, getContext());
            } catch (CoreException e) {
                getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error getting attributes of " //$NON-NLS-1$
                        + getDocumentedIpsObject().getQualifiedName(), e));
            }
        }

        return new PolicyCmptTypeAttributesTablePageElement(getDocumentedIpsObject(), getContext());
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        addPageElements(TextPageElement
                .createParagraph("Abstract Type" + ": " + (getDocumentedIpsObject().isAbstract() ? "X" : "-"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        IProductCmptType to;
        try {
            to = getDocumentedIpsObject().getIpsProject().findProductCmptType(
                    getDocumentedIpsObject().getProductCmptType());
        } catch (CoreException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error getting  " + getDocumentedIpsObject().getProductCmptType(), e)); //$NON-NLS-1$
            return;
        }
        if (to == null) {
            addPageElements(TextPageElement.createParagraph(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName()
                    + ": " + getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_none))); //$NON-NLS-1$ 
            return;
        }
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new IPageElement[] {
                new TextPageElement(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": "), //$NON-NLS-1$
                new PageElementUtils().createLinkPageElement(getContext(), to, TargetType.CONTENT, getContext()
                        .getLabel(to), true) }));

    }
}
