/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IType;

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
public class IpsSrcFileWrapperAdapterFactory extends AbstractAdapterFactory {

    @SuppressWarnings("unchecked")
    // can suppress warning as eclipse IAdapterFactory is not generic
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof IIpsSrcFileWrapper)) {
            return null;
        }

        if (adapterType == null) {
            return null;
        }

        if (adapterType.isAssignableFrom(IIpsSrcFile.class)) {
            return adaptToIpsSrcFile(adaptableObject);
        }

        if (adapterType.isAssignableFrom(IProductCmpt.class)) {
            return adaptToProductCmpt(adaptToIpsSrcFile(adaptableObject));
        }

        if (adapterType.isAssignableFrom(IType.class)) {
            return adaptToType(adaptToIpsSrcFile(adaptableObject));
        }

        return null;
    }

    private IIpsSrcFile adaptToIpsSrcFile(Object adaptableObject) {
        IIpsSrcFileWrapper wrapper = (IIpsSrcFileWrapper)adaptableObject;
        return wrapper.getWrappedIpsSrcFile();
    }

    public Class<?>[] getAdapterList() {
        return new Class<?>[] { IIpsElement.class, IIpsSrcFile.class, IProductCmpt.class, IType.class };
    }

}
