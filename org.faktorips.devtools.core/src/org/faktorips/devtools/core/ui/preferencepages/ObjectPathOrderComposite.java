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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.ui.UIToolkit;


/// WORK IN PROGRESS! needs change of model -> sorting/moving of IPS object path entries

/**
 * Composite for modifying the sorting/order of IPS object path entries
 * @author Roman Grutza
 */
public class ObjectPathOrderComposite extends Composite {

    private IIpsObjectPath ipsObjectPath;
    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private Button addSrcFolderButton;
    private Button removeSrcFolderButton;

    /**
     * 
     */
    ObjectPathOrderComposite(Composite parent) {
        super(parent, SWT.NONE);
        this.toolkit = new UIToolkit(null);
        
        this.setLayout(new GridLayout(1, true));
        
        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label tableViewerLabel = new Label(tableWithButtons, SWT.NONE);
        tableViewerLabel.setText("Ips build path order:");
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        tableViewerLabel.setLayoutData(gd);
        
        tableViewer = createViewer(tableWithButtons);
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Composite buttons = toolkit.createComposite(tableWithButtons);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons);
    }

    private void createButtons(Composite buttons) {
        addSrcFolderButton = toolkit.createButton(buttons, "Move Up");
        addSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        
        removeSrcFolderButton = toolkit.createButton(buttons, "Move Down");
        removeSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
    }
    
    /**
     * @param parent
     * @return
     */
    private TableViewer createViewer(Composite parent) {
        TableViewer viewer = new TableViewer(parent);
        viewer.setLabelProvider(new IpsObjectPathLabelProvider());

        return viewer;
    }

    /**
     * @param ipsObjectPath
     */
    public void init(IIpsObjectPath ipsObjectPath) {
        
        this.ipsObjectPath = ipsObjectPath;
        tableViewer.setContentProvider(new IpsObjectPathContentProvider()); 
        
        tableViewer.setInput(this.ipsObjectPath);
    }

}
