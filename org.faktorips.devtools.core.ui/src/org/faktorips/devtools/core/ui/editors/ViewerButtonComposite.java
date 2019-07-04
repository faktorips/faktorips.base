/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class ViewerButtonComposite extends Composite implements ICompositeWithSelectableViewer {

    private ContentViewer viewer;

    public ViewerButtonComposite(Composite parent) {
        super(parent, SWT.NONE);
    }

    /**
     * Creates the composite's controls. This method has to to be called explicitly by subclasses
     * <b>after</b> they have initialized any subclass specific instance variables. The
     * <code>ViewerButtonComposite</code> does not call this method in it's constructor, because in
     * subclasses in might be necessary to initialize instance variable first, but the call to the
     * super constructor has to be the first statement in the subclass constructor.
     */
    protected void initControls(UIToolkit toolkit) {
        setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout clientLayout = new GridLayout(2, false);
        clientLayout.marginHeight = 2;
        clientLayout.marginWidth = 1;
        setLayout(clientLayout);
        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().paintBordersFor(this);
        }
        viewer = createViewer(this, toolkit);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonEnabledStates();
            }
        });
        Composite buttons = toolkit.createComposite(this);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);

        boolean anyButtonCreated = createButtons(buttons, toolkit);
        if (anyButtonCreated) {
            return;
        }
        // no button created, so use the whole space for the viewer
        buttons.dispose();
        clientLayout = new GridLayout(1, false);
        clientLayout.marginHeight = 2;
        clientLayout.marginWidth = 1;
        setLayout(clientLayout);
    }

    /**
     * Creates and returns the (table or tree) Viewer.
     */
    protected abstract ContentViewer createViewer(Composite parent, UIToolkit toolkit);

    /**
     * Creates the buttons.
     * 
     * @return true if at least one button was created, otherwise false.
     */
    protected abstract boolean createButtons(Composite buttonComposite, UIToolkit toolkit);

    /**
     * Updates the button's enabled state depending on the object selected in the viewer.
     */
    protected abstract void updateButtonEnabledStates();

    /**
     * Returns the object that is selected in the viewer or <code>null</code> if no object is
     * selected.
     */
    public Object getSelectedObject() {
        IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
        if (selection.isEmpty()) {
            return null;
        }
        return selection.getFirstElement();
    }

    /**
     * Sets the selection of the viewer to the provided object.
     */
    public void setSelectedObject(Object object) {
        IStructuredSelection selection = new StructuredSelection(object);
        viewer.setSelection(selection, true);
    }

    /**
     * Returns whether the first element of this composite's viewer is selected.
     */
    protected final boolean isFirstElementSelected() {
        return isFirstOrLastElementSelected(true);
    }

    /**
     * Returns whether the last element of this composite's viewer is selected.
     */
    protected final boolean isLastElementSelected() {
        return isFirstOrLastElementSelected(false);
    }

    private boolean isFirstOrLastElementSelected(boolean firstElement) {
        IStructuredContentProvider contentProvider = (IStructuredContentProvider)getViewer().getContentProvider();
        Object[] elements = contentProvider.getElements(null);
        if (getSelectedObject() != null && elements.length > 0
                && elements[firstElement ? 0 : elements.length - 1] == getSelectedObject()) {
            return true;
        }
        return false;
    }

    public void refresh() {
        if (viewer.getInput() == null) {
            viewer.setInput(this);
            // if viewer's input is null, it's content provider is not asked
            // for the contents!
        }
        viewer.refresh();
        updateButtonEnabledStates();
        refreshThis();
    }

    /**
     * <strong>Subclassing:</strong><br>
     * This implementation does nothing. Subclasses may override this method in order to extend the
     * {@link #refresh()} method.
     */
    protected void refreshThis() {
        // Empty default implementation
    }

    @Override
    public ContentViewer getViewer() {
        return viewer;
    }

}
