/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.Arrays;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Composite for modifying the sorting/order of IPS object path entries
 * 
 * @author Roman Grutza
 */
public class ObjectPathOrderComposite extends Composite {

    private IIpsObjectPath ipsObjectPath;
    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button moveTopButton;
    private Button moveBottomButton;
    private boolean dataChanged = false;

    public ObjectPathOrderComposite(Composite parent) {
        super(parent, SWT.NONE);
        this.toolkit = new UIToolkit(null);

        this.setLayout(new GridLayout(1, true));

        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IpsPathOrderAdapter pathOrderAdapter = new IpsPathOrderAdapter();

        Label tableViewerLabel = new Label(tableWithButtons, SWT.NONE);
        tableViewerLabel.setText(Messages.ObjectPathOrderComposite_tableViewer_label);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        tableViewerLabel.setLayoutData(gd);

        tableViewer = createViewer(tableWithButtons, pathOrderAdapter);
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = toolkit.createComposite(tableWithButtons);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttons.setLayoutData(gd);

        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons, pathOrderAdapter);
    }

    /**
     * Initializes the composite for an existing IPS Project
     * 
     * @param ipsObjectPath IPS object path used to initialize this composite, must not be null
     */
    public void init(IIpsObjectPath ipsObjectPath) {
        this.ipsObjectPath = ipsObjectPath;
        dataChanged = false;

        tableViewer.setContentProvider(new IpsObjectPathContentProvider());

        tableViewer.setInput(this.ipsObjectPath);
    }

    private TableViewer createViewer(Composite parent, IpsPathOrderAdapter projectAdapter) {
        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI);
        viewer.setLabelProvider(new DecoratingLabelProvider(new IpsObjectPathLabelProvider(), IpsPlugin.getDefault()
                .getWorkbench().getDecoratorManager().getLabelDecorator()));

        viewer.addSelectionChangedListener(projectAdapter);
        return viewer;
    }

    private void createButtons(Composite buttons, IpsPathOrderAdapter projectAdapter) {
        moveUpButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonUp_label);
        moveUpButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveUpButton.addSelectionListener(projectAdapter);

        moveDownButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonDown_label);
        moveDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveDownButton.addSelectionListener(projectAdapter);

        moveTopButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonTop_label);
        moveTopButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveTopButton.addSelectionListener(projectAdapter);

        moveBottomButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonBottom_label);
        moveBottomButton
                .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveBottomButton.addSelectionListener(projectAdapter);

        setButtonEnabledStates(false);
    }

    private void setButtonEnabledStates(boolean enabled) {
        moveBottomButton.setEnabled(enabled);
        moveDownButton.setEnabled(enabled);
        moveTopButton.setEnabled(enabled);
        moveUpButton.setEnabled(enabled);
    }

    /**
     * enable all buttons, then selectively disable buttons not applicable for the current selection
     */
    private void setButtonEnabledStates(int[] indices) {
        int numSelections = indices.length;
        setButtonEnabledStates(false);
        if (numSelections == 0) {
            return;
        }

        Arrays.sort(indices);

        // groesster selected index > numSelections
        if (indices[numSelections - 1] >= numSelections) {
            moveUpButton.setEnabled(true);
            moveTopButton.setEnabled(true);
        }
        if (indices[0] < tableViewer.getTable().getItemCount() - numSelections) {
            moveDownButton.setEnabled(true);
            moveBottomButton.setEnabled(true);
        }
    }

    // widget action handling
    private class IpsPathOrderAdapter implements ISelectionChangedListener, SelectionListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                setButtonEnabledStates(false);
            } else {
                setButtonEnabledStates(tableViewer.getTable().getSelectionIndices());
            }
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == moveUpButton) {
                moveSelectedEntries(true);
            } else if (e.getSource() == moveDownButton) {
                moveSelectedEntries(false);
            } else if (e.getSource() == moveTopButton) {
                moveEntriesTopBottom(true);
            } else if (e.getSource() == moveBottomButton) {
                moveEntriesTopBottom(false);
            }

            setButtonEnabledStates(tableViewer.getTable().getSelectionIndices());
            tableViewer.refresh(false);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing to do
        }
    }

    private void moveSelectedEntries(boolean up) {
        Table table = tableViewer.getTable();

        int[] selectionIndices = table.getSelectionIndices();
        int[] newSelection = ipsObjectPath.moveEntries(selectionIndices, up);

        tableViewer.refresh(false);
        table.setSelection(newSelection);

        dataChanged = true;
    }

    /**
     * @param top if true selected elements will be moved to top, preserving the order of selected
     *            items. Otherwise the elements will be moved to the bottom.
     */
    private void moveEntriesTopBottom(boolean top) {
        int[] currentSelection = tableViewer.getTable().getSelectionIndices();

        if (currentSelection.length == 0) {
            return;
        }

        if (top) {
            for (int i = 0; i < currentSelection[currentSelection.length - 1]; i++) {
                moveSelectedEntries(true);
            }
        } else {
            for (int i = 0; i < tableViewer.getTable().getItemCount() - currentSelection.length; i++) {
                moveSelectedEntries(false);
            }
        }
        dataChanged = true;
    }

    /**
     * The order of IPS object path entries has been modified
     * 
     * @return true if current project's order of object path entries has been modified, false
     *         otherwise
     */
    public boolean isDataChanged() {
        return dataChanged;
    }

    /**
     * Method to manually update the UI
     */
    public void doUpdateUI() {
        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

}
