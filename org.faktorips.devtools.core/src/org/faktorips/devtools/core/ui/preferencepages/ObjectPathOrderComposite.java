/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.util.ArrayElementMover;


/**
 * Composite for modifying the sorting/order of IPS object path entries
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


    ObjectPathOrderComposite(Composite parent) {
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
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING );
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
     * @param ipsObjectPath IPS object path used to initialize this composite, must not be null
     * @throws CoreException 
     */    
    public void init(IIpsObjectPath ipsObjectPath) {
        
        this.ipsObjectPath = ipsObjectPath;
        tableViewer.setContentProvider(new IpsObjectPathContentProvider()); 
        
        tableViewer.setInput(this.ipsObjectPath);
    }
    
    
    private TableViewer createViewer(Composite parent, IpsPathOrderAdapter projectAdapter) {
        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI);
        viewer.setLabelProvider(new IpsObjectPathLabelProvider());
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
        moveBottomButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveBottomButton.addSelectionListener(projectAdapter);
        
        setButtonEnabledStates(false);
    }

    private void setButtonEnabledStates(boolean enabled) {
        moveBottomButton.setEnabled(enabled);
        moveDownButton.setEnabled(enabled);
        moveTopButton.setEnabled(enabled);
        moveUpButton.setEnabled(enabled);
    }

    // enable all buttons, then selectively disable buttons not applicable for the current selection
    private void setButtonEnabledStates(int index) {        
        setButtonEnabledStates(true);
        if (index == 0) {
            moveUpButton.setEnabled(false);
            moveTopButton.setEnabled(false);
        }
        if (index == tableViewer.getTable().getItemCount() - 1) {
            moveDownButton.setEnabled(false);
            moveBottomButton.setEnabled(false);                    
        }
    }
    
    
    // widget action handling
    private class IpsPathOrderAdapter implements ISelectionChangedListener, SelectionListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                setButtonEnabledStates(false);
            }
            else {
                setButtonEnabledStates(tableViewer.getTable().getSelectionIndex());
            }
        }

        public void widgetSelected(SelectionEvent e) {
            
            int selectionIndex = tableViewer.getTable().getSelectionIndex();
            if (selectionIndex < 0) {
                return;                 // nothing selected in tableViewer
            }
            
            int newIndex = -1;
            if (e.getSource() == moveUpButton) {
                moveEntries(true);
            }
            else if (e.getSource() == moveDownButton) {
                moveEntries(false);
            }
            else if (e.getSource() == moveTopButton) {
                moveEntriesTopBottom(true);
            }
            else if (e.getSource() == moveBottomButton) {
                moveEntriesTopBottom(false);
            }
            
            setButtonEnabledStates(newIndex);
            tableViewer.refresh(false);
        }

        public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */ }
    }


    /**
     * @param up, if true selected elements will be moved up by one if possible. Otherwise the elements will be moved down by one.
     */
    public void moveEntries(boolean up) {
        Table table = tableViewer.getTable();
        IIpsObjectPathEntry[] entries = ipsObjectPath.getEntries();
        if (entries.length == 0) {
            return;
        }
        
        ArrayElementMover mover = new ArrayElementMover(entries);
        
        int[] newSelection;
        if (up) {
            newSelection = mover.moveUp(table.getSelectionIndices());
        } else {
            newSelection = mover.moveDown(table.getSelectionIndices());
        }
        ipsObjectPath.setEntries(entries);
        tableViewer.refresh(false);
        table.setSelection(newSelection);
    }


    /**
     * @param top, if true selected elements will be moved to top, preserving the order of selected items. Otherwise the elements will
     * be moved to the bottom. 
     */
    private void moveEntriesTopBottom(boolean top) {
        int[] currentSelection = tableViewer.getTable().getSelectionIndices();
        java.util.Arrays.sort(currentSelection);
        if (currentSelection.length == 0)
            return;
        
        if (top) {
            for (int i = 0; i < currentSelection[currentSelection.length - 1]; i++) {
                moveEntries(true);
            }
        } else {
            for (int i = 0; i < tableViewer.getTable().getItemCount() - currentSelection.length; i++) {
                moveEntries(false);
            }
        }
    }
    
}
