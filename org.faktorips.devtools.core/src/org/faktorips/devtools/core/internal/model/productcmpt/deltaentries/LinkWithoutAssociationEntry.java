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

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * 
 * @author Jan Ortmann
 */
public class LinkWithoutAssociationEntry extends AbstractDeltaEntryForLinks {

    public LinkWithoutAssociationEntry(IProductCmptLink link) {
        super(link);
    }

    @Override
    public void fix() {
        getLink().delete();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_WITHOUT_ASSOCIATION;
    }

    @Override
    public String getDescription() {
        return getDeltaType().getDescription()
                + ": " + IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getLink()); //$NON-NLS-1$
    }

}
