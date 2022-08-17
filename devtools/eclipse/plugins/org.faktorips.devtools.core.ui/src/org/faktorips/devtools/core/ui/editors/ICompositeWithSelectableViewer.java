/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.Viewer;

/**
 * This interface should be implemented by any composite holding a viewer that would provide its
 * selection to the editors selection provider. The {@link IpsObjectEditorPage} would search for
 * composites implementing this interface and register the necessary listeners to the given viewer
 * and its control.
 * 
 * @author dirmeier
 */
public interface ICompositeWithSelectableViewer {

    /**
     * Returns the viewer that would provide its selection to the editors selection provider.
     * 
     */
    Viewer getViewer();

}
