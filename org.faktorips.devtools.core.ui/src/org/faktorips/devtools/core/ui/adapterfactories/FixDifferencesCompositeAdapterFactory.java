/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.adapterfactories;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.ui.workbenchadapters.FixDifferencesCompositeWorkbenchAdapter;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;

public class FixDifferencesCompositeAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof IFixDifferencesComposite) {
            if (IWorkbenchAdapter.class.isAssignableFrom(adapterType)) {
                return (T)new FixDifferencesCompositeWorkbenchAdapter();
            }
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }

}
