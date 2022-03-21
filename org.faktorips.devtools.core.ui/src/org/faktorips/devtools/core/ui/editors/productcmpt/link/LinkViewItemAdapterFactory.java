/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;

/**
 * AdapterFactory to adapt a {@link LinkViewItem LinkViewItem} to an {@link IProductCmptLink}.
 * 
 * @author widmaier
 */
public class LinkViewItemAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!(adaptableObject instanceof LinkViewItem)) {
            return null;
        }
        IProductCmptLink link = ((LinkViewItem)adaptableObject).getLink();
        if (IIpsObjectPart.class.equals(adapterType) || IProductCmptLink.class.equals(adapterType)) {
            return (T)link;
        }
        if (IIpsObject.class.equals(adapterType)) {
            return (T)link.findTarget(link.getIpsProject());
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IIpsObjectPart.class, IProductCmptLink.class, IIpsObject.class };
    }

}
