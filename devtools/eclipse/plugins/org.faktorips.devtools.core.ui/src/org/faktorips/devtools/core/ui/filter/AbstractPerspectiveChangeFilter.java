/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.filter;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.IManagedForm;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * Base implementation of {@link IProductCmptPropertyFilter} that simplifies the implementation of
 * filters. Automatically updates the visibility filters on the currently active editor when the
 * perspective changes.
 * <p>
 * <strong>Subclassing:</strong><br>
 * Subclasses must implement the {@link #isFiltered(IProductCmptProperty)} method to indicate
 * whether a given {@link IProductCmptProperty} is filtered at a given time.
 * 
 * @since 3.7
 * 
 * 
 * @see IProductCmptPropertyFilter
 */
public abstract class AbstractPerspectiveChangeFilter implements IProductCmptPropertyFilter {

    private final LocalWindowListener localWindowListener;

    public AbstractPerspectiveChangeFilter() {
        localWindowListener = addWindowListener();
    }

    /**
     * Adds a listener to the workbench that manages the {@link LocalWindowListener}
     */
    private LocalWindowListener addWindowListener() {
        LocalWindowListener listener = new LocalWindowListener();
        IpsUIPlugin.getDefault().getWorkbench().addWindowListener(listener);
        return listener;
    }

    public void setPerspectiveId(String perspectiveId) {
        localWindowListener.setPerspectiveId(perspectiveId);
    }

    /**
     * A listener that manages the {@link PerspectiveActivatedOrDeactivatedListener} for each
     * {@link IWorkbenchWindow} that is opened.
     */
    private static class LocalWindowListener implements IWindowListener {

        private final PerspectiveActivatedOrDeactivatedListener perspectiveActivatedOrDeactivatedListener;

        public LocalWindowListener() {
            perspectiveActivatedOrDeactivatedListener = new PerspectiveActivatedOrDeactivatedListener();
        }

        @Override
        public void windowOpened(IWorkbenchWindow window) {
            window.addPerspectiveListener(perspectiveActivatedOrDeactivatedListener);
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {
            window.removePerspectiveListener(perspectiveActivatedOrDeactivatedListener);
        }

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowActivated(IWorkbenchWindow window) {
            // Nothing to do
        }

        public void setPerspectiveId(String perspectiveId) {
            perspectiveActivatedOrDeactivatedListener.setPerspectiveId(perspectiveId);
        }

    }

    /**
     * A listener that notifies the {@link IPropertyVisibleController} on activation and
     * deactivation of the analysis perspective.
     */
    private static class PerspectiveActivatedOrDeactivatedListener implements IPerspectiveListener {
        /**
         * Flag to track whether the analysis perspective is currently active or not. This
         * information is required to be able to deduce whether the analysis perspective was the
         * last open perspective upon activating another perspective.
         */
        private Boolean perspectiveOpen;
        private String perspectiveId;

        public PerspectiveActivatedOrDeactivatedListener() {
        }

        @Override
        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            boolean targetPerspective = perspective.getId().equals(perspectiveId);
            boolean triggerNotify = perspectiveOpen == null || perspectiveOpen || targetPerspective;
            perspectiveOpen = targetPerspective;
            /*
             * The target perspective is identified by the set perspective id. A notify is triggered
             * if the target perspective is activated, OR any other perspective is opened and the
             * previous perspective was the target perspective.
             */
            if (triggerNotify) {
                /*
                 * If the product component editor is opened, it's layout must be refreshed to
                 * reflect the changed filtering status.
                 */
                IEditorPart editor = page.getActiveEditor();
                if (editor instanceof ProductCmptEditor) {
                    refreshProductCmptEditor((ProductCmptEditor)editor);
                }
            }
        }

        private void refreshProductCmptEditor(ProductCmptEditor pcEditor) {
            pcEditor.getVisibilityController().updateUI(true);
            IManagedForm form = pcEditor.getActiveIpsObjectEditorPage().getManagedForm();
            if (form != null) {
                form.reflow(true);
            }
        }

        @Override
        public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
            // Nothing to do

        }

        public void setPerspectiveId(String perspectiveId) {
            this.perspectiveId = perspectiveId;
        }

    }

}
