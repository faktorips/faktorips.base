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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
    private ITreeContentProvider contentProvider;

    /**
     * Creates a new ResourceChangeListener which will update the given StructuredViewer if a
     * resource change event occurs.
     * 
     * @param viewer The viewer to update.
     * 
     * @throws NullPointerException if viewer is <code>null</code>.
     * @throws ClassCastException if viewer has no tree content provider
     */
    public IpsResourceChangeListener(StructuredViewer viewer) {
        ArgumentCheck.notNull(viewer);
        this.viewer = viewer;
        contentProvider = (ITreeContentProvider)viewer.getContentProvider();
    }

    /**
     * {@inheritDoc}
     */
    public void resourceChanged(final IResourceChangeEvent event) {
        Control ctrl = viewer.getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
            ctrl.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    IResourceDelta delta = event.getDelta();
                    try {
                        IpsViewRefreshVisitor visitor = new IpsViewRefreshVisitor(contentProvider);
                        delta.accept(visitor);
                        for (Object element : visitor.getElementsToRefresh()) {
                            viewer.refresh(element);
                        }
                        for (Object element : visitor.getElementsToUpdate()) {
                            viewer.update(element, null);
                        }
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }

            });
        }
    }

}
