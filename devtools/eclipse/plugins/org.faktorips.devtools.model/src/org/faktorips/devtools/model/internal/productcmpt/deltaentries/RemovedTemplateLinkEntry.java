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
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.util.StringUtil;

/**
 * Delta entry for a link that is present in the current product component and marked as
 * {@link TemplateValueStatus#UNDEFINED} but was removed in the corresponding template.
 * 
 * To fix this situation we simply delete the useless link.
 * 
 */
public class RemovedTemplateLinkEntry extends AbstractDeltaEntryForLinks {

    public RemovedTemplateLinkEntry(IProductCmptLink deletedLink) {
        super(deletedLink);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.REMOVED_TEMPLATE_LINK;
    }

    @Override
    public String getDescription() {
        String linkCaption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getLink());
        String linkTarget = StringUtil.unqualifiedName(getLink().getTarget());
        if (getLink().getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return MessageFormat.format(Messages.DeletedTemplateLinkEntry_removeUndefinedTemplateLink, linkCaption,
                    linkTarget);
        } else {
            return MessageFormat.format(Messages.DeletedTemplateLinkEntry_removeInheritedTemplateLink, linkCaption,
                    linkTarget);
        }
    }

    /**
     * Remove the useless link.
     */
    @Override
    public void fix() {
        getLink().delete();
    }

}
