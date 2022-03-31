/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.util.StringUtil;

/**
 * Delta entry for links that are present in a product template but are missing in the current
 * product component.
 * 
 * The link was created in the template and it needs to be added in the product component.
 * 
 * A matching link is created and marked as {@link TemplateValueStatus#INHERITED}
 * 
 */
public class MissingTemplateLinkEntry extends AbstractDeltaEntryForLinks {

    private final IProductCmptLinkContainer linkContainer;

    public MissingTemplateLinkEntry(IProductCmptLink missingLink, IProductCmptLinkContainer linkContainer) {
        super(missingLink);
        this.linkContainer = linkContainer;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_TEMPLATE_LINK;
    }

    @Override
    public String getDescription() {
        String linkCaption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getLink());
        String linkTarget = StringUtil.unqualifiedName(getLink().getTarget());
        String templateName = getLink().getProductCmpt().getName();
        return MessageFormat.format(Messages.MissingTemplateLinkEntry_missingTemplateLink, linkCaption, linkTarget,
                templateName);
    }

    /**
     * Creates a new link that matches the template link and marks the new link as
     * {@link TemplateValueStatus#INHERITED}
     */
    @Override
    public void fix() {
        IProductCmptLink newLink = linkContainer.newLink(getLink().getAssociation());
        newLink.setTarget(getLink().getTarget());
        newLink.setTemplateValueStatus(TemplateValueStatus.INHERITED);
    }

}
