/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.adapterfactories;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.core.ui.workbenchadapters.FixDifferencesCompositeWorkbenchAdapter;

public class FixDifferencesCompositeAdapterFactory implements IAdapterFactory {

    @Override
    // eclipse adapters are not type safe
    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (adaptableObject instanceof IFixDifferencesComposite) {
            if (IWorkbenchAdapter.class.isAssignableFrom(adapterType)) {
                return new FixDifferencesCompositeWorkbenchAdapter();
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // eclipse adapters are not type safe
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }

}
