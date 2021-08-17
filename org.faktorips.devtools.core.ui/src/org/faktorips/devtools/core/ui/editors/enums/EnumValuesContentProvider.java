/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.enums.IEnumValueContainer;

/** The content provider for the table viewer. */
public class EnumValuesContentProvider implements IStructuredContentProvider {

    private final IEnumValueContainer enumValueContainer;

    public EnumValuesContentProvider(IEnumValueContainer enumValueContainer) {
        this.enumValueContainer = enumValueContainer;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return enumValueContainer.getEnumValues().toArray();
    }

    @Override
    public void dispose() {
        // Nothing to dispose.
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do on input change event.
    }

}
