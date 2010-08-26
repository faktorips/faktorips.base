/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapter factory for adapting <code>IResource</code>s to <code>IWorbenchAdapter</code>s.
 * 
 * @author Thorsten Guenther
 */
class ResourceAdapterFactory implements IAdapterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object o, Class adapterType) {
        // can suppress the warning as the Eclipse interface defineds the method signature
        if (adapterType.isInstance(o)) {
            return o;
        } else if (adapterType == IWorkbenchAdapter.class) {
            return new IpsWorkbenchAdapter();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        // can suppress the warning as the Eclipse interface defineds the method signature
        return new Class[] { IWorkbenchAdapter.class };
    }
}
