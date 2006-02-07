package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;

public class IpsResourceChangeListener implements IResourceChangeListener {

    /**
     * The viewer to update.
     */
    private StructuredViewer viewer;
    
    /**
     * Creates a new ResourceChangeListener which will update the given StructuredViewer 
     * if an event occurs.
     * 
     * @param viewer
     */
    public IpsResourceChangeListener(StructuredViewer viewer) {
        this.viewer = viewer;
    }
    
    /**
     * Overridden.
     */
    public void resourceChanged(IResourceChangeEvent event) {
        if (viewer != null) {
            Control ctrl = viewer.getControl();
            if (ctrl != null && !ctrl.isDisposed()) {
                ctrl.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        if (!viewer.getControl().isDisposed()) {
                            viewer.refresh();
                        }
                    }
                });
            }
        }
    }
}
