/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
