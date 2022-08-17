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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IType;

/**
 * This adapter factory could handle {@link IIpsSrcFileWrapper} and returns the wrapped
 * {@link IIpsElement} as an adapter.
 * <p>
 * To make an object an adaptable {@link IIpsSrcFileWrapper} you only have to implement the
 * interface {@link IIpsSrcFileWrapper} and extend from {@link PlatformObject}. Alternatively you
 * could implement the {@link IAdaptable#getAdapter(Class)} method for your own and calling the
 * workbench adapter manager @see{@link PlatformObject#getAdapter(Class)}
 * 
 * @author dirmeier
 */
public class IpsSrcFileWrapperAdapterFactory extends AbstractIpsAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    // Can suppress warning as eclipse IAdapterFactory is not generic.
    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (!(adaptableObject instanceof IIpsSrcFileWrapper) || (adapterType == null)) {
            return null;
        }

        IIpsSrcFile adaptedIpsSrcFile = adaptToIpsSrcFile(adaptableObject);
        if (adaptedIpsSrcFile == null) {
            return null;
        }
        if (adapterType.isAssignableFrom(IIpsSrcFile.class)) {
            return adaptedIpsSrcFile;
        }

        if (adapterType.isAssignableFrom(IIpsObject.class)) {
            return adaptToIpsObject(adaptedIpsSrcFile);
        }

        if (adapterType.isAssignableFrom(IProductCmpt.class)) {
            return adaptToProductCmpt(adaptedIpsSrcFile);
        }

        if (adapterType.isAssignableFrom(IType.class)) {
            return adaptToType(adaptedIpsSrcFile);
        }

        if (!IpsPlugin.getDefault().getIpsPreferences().isSimpleContextMenuEnabled()
                && adapterType.isAssignableFrom(IFile.class)) {
            return adaptedIpsSrcFile.getCorrespondingFile().unwrap();
        }

        return null;
    }

    private IIpsSrcFile adaptToIpsSrcFile(Object adaptableObject) {
        IIpsSrcFileWrapper wrapper = (IIpsSrcFileWrapper)adaptableObject;
        return wrapper.getWrappedIpsSrcFile();
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class<?>[] { IIpsObject.class, IIpsElement.class, IIpsSrcFile.class, IProductCmpt.class,
                IType.class, IResource.class };
    }

}
