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
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A link should inherit its configuration from a template but is undefined in the template.
 *
 * @since 24.1.2
 */
public class InheritedUndefinedLinkTemplateMismatchEntry extends AbstractDeltaEntryForLinks {

    private IProductCmptTypeAssociation association;
    private IProductCmptLink link;

    public InheritedUndefinedLinkTemplateMismatchEntry(IProductCmptTypeAssociation association, IProductCmptLink link) {
        super(link);
        this.association = association;
        this.link = link;
    }

    @Override
    public void fix() {
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.INHERITED_UNDEFINED_TEMPLATE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(Messages.InheritedUndefinedTemplateMismatchEntry_desc,
                IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association));
    }

}
