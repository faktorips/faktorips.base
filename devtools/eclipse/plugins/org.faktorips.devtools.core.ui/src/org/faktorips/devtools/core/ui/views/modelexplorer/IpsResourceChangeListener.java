/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.ArgumentCheck;

public class IpsResourceChangeListener implements IResourceChangeListener {

    /**
     * The viewer to update.
     */
    private StructuredViewer viewer;

    /**
     * The viewer's tree content provider
     */
    private ModelContentProvider contentProvider;

    /**
     * Creates a new ResourceChangeListener which will update the given StructuredViewer if a
     * resource change event occurs.
     * 
     * @param viewer The viewer to update.
     * 
     * @throws NullPointerException if viewer is <code>null</code>.
     * @throws ClassCastException if viewer has no model content provider
     */
    public IpsResourceChangeListener(StructuredViewer viewer) {
        ArgumentCheck.notNull(viewer);
        this.viewer = viewer;
        contentProvider = (ModelContentProvider)viewer.getContentProvider();
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        Control ctrl = viewer.getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
            ctrl.getDisplay().asyncExec(() -> {
                IResourceDelta delta = event.getDelta();
                try {
                    IpsViewRefreshVisitor visitor = new IpsViewRefreshVisitor(contentProvider);
                    delta.accept(visitor, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
                    for (Object element1 : visitor.getElementsToRefresh()) {
                        viewer.refresh(element1);
                    }
                    for (Object element2 : visitor.getElementsToUpdate()) {
                        viewer.update(element2, null);
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            });
        }
    }

}
