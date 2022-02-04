/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.PolicyCmptTypeAttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ValidationRuleTablePageElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;

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
    protected void buildInternal() {
        super.buildInternal();

        addValidationRuleTable();
    }

    private void addValidationRuleTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(
                new TextPageElement(getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_rules),
                        TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(
                new ValidationRuleTablePageElement(getDocumentedIpsObject(), getContext()),
                getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_noValidationrules)));

        addPageElements(wrapper);
    }

    @Override
    AttributesTablePageElement getAttributesTablePageElement() {
        if (getContext().showInheritedObjectPartsInTable()) {
            try {
                List<IAttribute> attributes = getDocumentedIpsObject().findAllAttributes(getContext().getIpsProject());
                return new PolicyCmptTypeAttributesTablePageElement(getDocumentedIpsObject(), attributes, getContext());
            } catch (CoreRuntimeException e) {
                getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error getting attributes of " //$NON-NLS-1$
                        + getDocumentedIpsObject().getQualifiedName(), e));
            }
        }

        return new PolicyCmptTypeAttributesTablePageElement(getDocumentedIpsObject(), getContext());
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        addPageElements(TextPageElement.createParagraph(
                "Abstract Type" + ": " + (getDocumentedIpsObject().isAbstract() ? "X" : "-"), getContext())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        IProductCmptType to;
        to = getDocumentedIpsObject().getIpsProject()
                .findProductCmptType(getDocumentedIpsObject().getProductCmptType());
        if (to == null) {
            addPageElements(
                    TextPageElement
                            .createParagraph(
                                    IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": " //$NON-NLS-1$
                                            + getContext().getMessage(
                                                    HtmlExportMessages.PolicyCmptTypeContentPageElement_none),
                                    getContext()));
            return;
        }
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(),
                new TextPageElement(IpsObjectType.PRODUCT_CMPT_TYPE.getDisplayName() + ": ", getContext()), //$NON-NLS-1$
                new PageElementUtils(getContext()).createLinkPageElement(getContext(), to, TargetType.CONTENT,
                        getContext().getLabel(to), true)));

    }
}
