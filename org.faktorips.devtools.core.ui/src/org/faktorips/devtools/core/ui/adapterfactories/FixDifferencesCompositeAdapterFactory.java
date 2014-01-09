/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
