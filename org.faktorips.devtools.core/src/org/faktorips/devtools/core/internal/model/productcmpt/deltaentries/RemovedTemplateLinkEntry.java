/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
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
        String linkCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getLink());
        String linkTarget = StringUtil.unqualifiedName(getLink().getTarget());
        if (getLink().getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return NLS.bind(Messages.DeletedTemplateLinkEntry_removeUndefinedTemplateLink, new String[] { linkCaption,
                    linkTarget });
        } else {
            return NLS.bind(Messages.DeletedTemplateLinkEntry_removeInheritedTemplateLink, new String[] { linkCaption,
                    linkTarget });
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
