/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Provides default implementations for the methods described in {@link IPartListener2}.
 * <p>
 * Subclasses must override only those methods they are interested in.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IPartListener2
 */
public class PartAdapter implements IPartListener2 {

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

}
