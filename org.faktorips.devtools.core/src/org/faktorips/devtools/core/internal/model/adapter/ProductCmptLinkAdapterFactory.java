/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * This is a adapter factory to adapt from {@link IProductCmptLink} to {@link IResource} or
 * {@link IFile}. This is necessary because in case of {@link IProductCmptLink} we do not want to
 * use the enclosing resource like in nearly every other {@link IIpsElement} (@see
 * {@link IpsElementAdapterFactory}) but we want to get the resource of the target.
 * <p>
 * Note: You cannot adapt to {@link IResource} directly because it is prevented in
 * {@link IpsObjectPart#getAdapter(Class)}
 * 
 * @author dirmeier
 */
public class ProductCmptLinkAdapterFactory extends AbstractIpsAdapterFactory {

    @Override
    // eclipse adapters are not type safe
    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (!(adaptableObject instanceof IProductCmptLink)) {
            return null;
        }

        IProductCmptLink link = (IProductCmptLink)adaptableObject;

        try {
            IProductCmpt target = link.findTarget(link.getIpsProject());
            if (target == null) {
                return null;
            }
            if (IIpsObject.class.isAssignableFrom(adapterType)) {
                return target;
            }
            if (IIpsSrcFile.class.isAssignableFrom(adapterType)) {
                return target.getIpsSrcFile();
            }
            IResource enclosingResource = target.getEnclosingResource();
            if (adapterType.isInstance(enclosingResource)) {
                return enclosingResource;
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // eclipse adapters are not type safe
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IFile.class, IIpsSrcFile.class, IIpsObject.class };
    }

}
