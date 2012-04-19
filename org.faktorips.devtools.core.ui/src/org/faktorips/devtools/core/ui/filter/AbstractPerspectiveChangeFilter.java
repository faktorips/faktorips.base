/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.filter;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.IManagedForm;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;

/**
 * Base implementation of {@link IProductCmptPropertyFilter} that simplifies the implementation of
 * filters. Furthermore, this class is responsible to make sure to call {@link #notifyController()}
 * as soon as the filtering conditions change.
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
public abstract class AbstractPerspectiveChangeFilter extends AbstractProductCmptPropertyFilter {

    private final LocalWindowListener localWindowListener;

    public AbstractPerspectiveChangeFilter() {
        localWindowListener = addWindowListener();
    }

    /**
     * Adds a listener to the workbench that manages the {@link LocalWindowListener}
     */
    private LocalWindowListener addWindowListener() {
        LocalWindowListener listener = new LocalWindowListener(this);
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

        public LocalWindowListener(AbstractPerspectiveChangeFilter abstractPropertyFilter) {
            perspectiveActivatedOrDeactivatedListener = new PerspectiveActivatedOrDeactivatedListener(
                    abstractPropertyFilter);
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
        private final AbstractPerspectiveChangeFilter filter;
        private String perspectiveId;

        public PerspectiveActivatedOrDeactivatedListener(AbstractPerspectiveChangeFilter abstractPropertyFilter) {
            this.filter = abstractPropertyFilter;
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
                filter.notifyController();

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
