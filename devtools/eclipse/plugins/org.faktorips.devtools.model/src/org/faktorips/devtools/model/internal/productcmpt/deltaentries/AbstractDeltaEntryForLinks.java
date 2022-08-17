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

import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;

public abstract class AbstractDeltaEntryForLinks implements IDeltaEntry {

    private final IProductCmptLink link;

    public AbstractDeltaEntryForLinks(IProductCmptLink link) {
        this.link = link;
    }

    /**
     * Returns the respective link.
     */
    public IProductCmptLink getLink() {
        return link;
    }

    @Override
    public Class<ProductCmptLink> getPartType() {
        return ProductCmptLink.class;
    }

}
