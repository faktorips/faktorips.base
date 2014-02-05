/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * AdapterFactory to adapt a {@link LinkViewItem LinkViewItem} to an {@link IProductCmptLink}.
 * 
 * @author widmaier
 */
public class LinkViewItemAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("rawtypes")
    // IAdaptable forces raw type upon implementing classes
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof LinkViewItem)) {
            return null;
        }
        IProductCmptLink link = ((LinkViewItem)adaptableObject).getLink();
        if (IIpsObjectPart.class.equals(adapterType) || IProductCmptLink.class.equals(adapterType)) {
            return link;
        }
        if (IIpsObject.class.equals(adapterType)) {
            try {
                return link.findTarget(link.getIpsProject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // IAdaptable forces raw type upon implementing classes
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IIpsObjectPart.class, IProductCmptLink.class, IIpsObject.class };
    }

}
