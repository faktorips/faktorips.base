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
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;

/**
 * This is a adapter factory to adapt from {@link IProductCmptLink} to {@link IResource} or
 * {@link IFile}. This is necessary because in case of {@link IProductCmptLink} we do not want to
 * use the enclosing resource like in nearly every other {@link IIpsElement} (@see
 * {@link IpsElementAdapterFactory}) but we want to get the resource of the target.
 * <p>
 * Note: You cannot adapt to {@link IResource} directly because it is prevented in
 * {@link IIpsObjectPart#getAdapter(Class)}
 * 
 * @author dirmeier
 */
public class ProductCmptLinkAdapterFactory extends AbstractIpsAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
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
                return (T)target;
            }
            if (IIpsSrcFile.class.isAssignableFrom(adapterType)) {
                return (T)target.getIpsSrcFile();
            }
            AResource enclosingResource = target.getEnclosingResource();
            if (adapterType.isInstance(enclosingResource)) {
                return (T)enclosingResource;
            }
            IResource eclipseResource = enclosingResource.unwrap();
            if (adapterType.isInstance(eclipseResource)) {
                return (T)eclipseResource;
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IFile.class, IIpsSrcFile.class, IIpsObject.class };
    }

}
