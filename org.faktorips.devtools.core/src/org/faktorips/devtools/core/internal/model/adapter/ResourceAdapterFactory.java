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

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IType;

/**
 * Adapts {@link IResource}s to all classes listed in {@link #getAdapterList()}.
 * 
 * @author Thorsten Günther
 */
public class ResourceAdapterFactory extends AbstractIpsAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!(adaptableObject instanceof IResource)) {
            return null;
        }

        if (IIpsSrcFile.class.equals(adapterType)) {
            return (T)adaptToIpsSrcFile(adaptableObject);
        }

        if (IProductCmpt.class.equals(adapterType)) {
            return (T)adaptToProductCmpt(adaptToIpsSrcFile(adaptableObject));
        }

        if (IType.class.equals(adapterType)) {
            return (T)adaptToType(adaptToIpsSrcFile(adaptableObject));
        }

        if (IIpsObject.class.equals(adapterType)) {
            return (T)adaptToIpsObject(adaptToIpsSrcFile(adaptableObject));
        }

        if (IIpsElement.class.equals(adapterType)) {
            return (T)adaptToIpsElement(adaptableObject);
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
        IIpsElement ipsElement = IIpsModel.get()
                .getIpsElement(wrap(adaptableObject).as(AResource.class));
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
