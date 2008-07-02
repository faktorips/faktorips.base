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
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * Composite for modifying the sorting/order of IPS object path entries
 * @author Roman Grutza
 */
public class ObjectPathOrderComposite extends Composite {

    private IIpsObjectPath ipsObjectPath;
    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private Button moveEntryUpButton;
    private Button moveEntryDownButton;
    private Button moveEntryTopButton;
    private Button moveEntryBottomButton;


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
        // disallow multiple selection
        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE);
        viewer.setLabelProvider(new IpsObjectPathLabelProvider());
        viewer.addSelectionChangedListener(projectAdapter);
        
        return viewer;
    }

    private void createButtons(Composite buttons, IpsPathOrderAdapter projectAdapter) {
        moveEntryUpButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonUp_label);
        moveEntryUpButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveEntryUpButton.addSelectionListener(projectAdapter);
        
        moveEntryDownButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonDown_label);
        moveEntryDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveEntryDownButton.addSelectionListener(projectAdapter);
        
        moveEntryTopButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonTop_label);
        moveEntryTopButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveEntryTopButton.addSelectionListener(projectAdapter);
        
        moveEntryBottomButton = toolkit.createButton(buttons, Messages.ObjectPathOrderComposite_buttonBottom_label);
        moveEntryBottomButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveEntryBottomButton.addSelectionListener(projectAdapter);
        
        setButtonEnabledStates(false);
    }

    private void setButtonEnabledStates(boolean enabled) {
        moveEntryBottomButton.setEnabled(enabled);
        moveEntryDownButton.setEnabled(enabled);
        moveEntryTopButton.setEnabled(enabled);
        moveEntryUpButton.setEnabled(enabled);
    }

    // enable all buttons, then selectively disable buttons not applicable for the current selection
    private void setButtonEnabledStates(int index) {        
        setButtonEnabledStates(true);
        if (index == 0) {
            moveEntryUpButton.setEnabled(false);
            moveEntryTopButton.setEnabled(false);
        }
        if (index == tableViewer.getTable().getItemCount() - 1) {
            moveEntryDownButton.setEnabled(false);
            moveEntryBottomButton.setEnabled(false);                    
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
            if (e.getSource() == moveEntryUpButton) {
                newIndex = selectionIndex - 1;
                ipsObjectPath.moveEntry(selectionIndex, newIndex);
            }
            else if (e.getSource() == moveEntryDownButton) {
                newIndex = selectionIndex + 1;
                ipsObjectPath.moveEntry(selectionIndex, newIndex);
            }
            else if (e.getSource() == moveEntryTopButton) {
                newIndex = 0;
                ipsObjectPath.moveEntry(selectionIndex, newIndex);
            }
            else if (e.getSource() == moveEntryBottomButton) {
                newIndex = tableViewer.getTable().getItemCount() - 1;
                ipsObjectPath.moveEntry(selectionIndex, newIndex);
            }
            
            setButtonEnabledStates(newIndex);
            tableViewer.refresh(false);
        }

        public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */ }
    }

}
