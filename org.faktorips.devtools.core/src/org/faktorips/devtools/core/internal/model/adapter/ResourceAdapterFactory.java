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

import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IType;

/**
 * Adapts {@link IResource}s to all classes listed in {@link #getAdapterList()}.
 * 
 * @author Thorsten Günther
 */
public class ResourceAdapterFactory extends AbstractIpsAdapterFactory {

    @Override
    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof IResource)) {
            return null;
        }

        if (IIpsSrcFile.class.equals(adapterType)) {
            return adaptToIpsSrcFile(adaptableObject);
        }

        if (IProductCmpt.class.equals(adapterType)) {
            return adaptToProductCmpt(adaptToIpsSrcFile(adaptableObject));
        }

        if (IType.class.equals(adapterType)) {
            return adaptToType(adaptToIpsSrcFile(adaptableObject));
        }

        if (IIpsObject.class.equals(adapterType)) {
            return adaptToIpsObject(adaptToIpsSrcFile(adaptableObject));
        }

        if (IIpsElement.class.equals(adapterType)) {
            return adaptToIpsElement(adaptableObject);
        }

        return null;
    }

    private IIpsSrcFile adaptToIpsSrcFile(Object adaptableObject) {
        IIpsElement element = adaptToIpsElement(adaptableObject);
        if (element != null) {
            Object file = element.getAdapter(IIpsSrcFile.class);
            if (file == null) {
                return null;
            }
            return (IIpsSrcFile)file;
        } else {
            return null;
        }
    }

    private IIpsElement adaptToIpsElement(Object adaptableObject) {
        IIpsElement ipsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement((IResource)adaptableObject);
        if (ipsElement == null || !ipsElement.exists()) {
            return null;
        }
        return ipsElement;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IIpsSrcFile.class, IProductCmpt.class, IType.class, IIpsObject.class, IIpsElement.class };
    }

}
