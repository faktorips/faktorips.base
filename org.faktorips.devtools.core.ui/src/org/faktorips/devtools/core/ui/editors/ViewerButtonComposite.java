/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class ViewerButtonComposite extends Composite {

    private Viewer viewer;

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
    protected abstract Viewer createViewer(Composite parent, UIToolkit toolkit);

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

    public void refresh() {
        if (viewer.getInput() == null) {
            viewer.setInput(this); // if viewer's input is null, it's content provider is not asked
            // for the contents!
        }
        viewer.refresh();
        updateButtonEnabledStates();
    }

    protected Viewer getViewer() {
        return viewer;
    }

}
