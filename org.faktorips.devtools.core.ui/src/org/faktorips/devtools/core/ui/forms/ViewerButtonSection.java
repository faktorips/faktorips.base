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

package org.faktorips.devtools.core.ui.forms;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A section that displays a model object's data in a viewer on the left and has one or more buttons
 * on the right, e.g. to add, edit, or delete objects that are displayed in viewer.
 */
public abstract class ViewerButtonSection extends IpsSection {

    private Viewer viewer;

    public ViewerButtonSection(Composite parent, int style, UIToolkit toolkit) {
        super(parent, style, GridData.FILL_BOTH, toolkit);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout clientLayout = new GridLayout(2, false);
        clientLayout.marginHeight = 2;
        clientLayout.marginWidth = 1;
        client.setLayout(clientLayout);
        viewer = createViewer(client, toolkit);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonEnabledStates();
            }
        });
        Composite buttons = toolkit.getFormToolkit().createComposite(client);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);

        createButtons(buttons, toolkit);
    }

    /**
     * Creates and returns the (table or tree) Viewer.
     */
    protected abstract Viewer createViewer(Composite parent, UIToolkit toolkit);

    /**
     * Creates the buttons.
     */
    protected abstract void createButtons(Composite buttonComposite, UIToolkit toolkit);

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

    @Override
    protected void performRefresh() {
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
