/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * 
 * @author Jan Ortmann
 */
public class LinkWithoutAssociationEntry implements IDeltaEntry {

    private final IProductCmptLink link;

    public LinkWithoutAssociationEntry(IProductCmptLink link) {
        this.link = link;
    }

    /**
     * Returns the link that is missing the association.
     */
    public IProductCmptLink getLink() {
        return link;
    }

    @Override
    public void fix() {
        link.delete();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_WITHOUT_ASSOCIATION;
    }

    @Override
    public String getDescription() {
        return getDeltaType() + ": " + IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(link); //$NON-NLS-1$
    }

}
