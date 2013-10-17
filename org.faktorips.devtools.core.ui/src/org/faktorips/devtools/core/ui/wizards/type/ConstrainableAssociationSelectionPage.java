/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;

public class ConstrainableAssociationSelectionPage extends WizardPage {
    private Composite container;
    private Label label;
    private TreeViewer viewer;

    protected ConstrainableAssociationSelectionPage(String pageName) {
        super(pageName);
        setTitle("First Page");
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        label = new Label(container, SWT.NONE);

        label.setText(Messages.ConstrainableAssociationWizard_labelSelectAssociation);

        viewer = new TreeViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        // viewer.setContentProvider(new ArrayContentProvider());
        // viewer.setLabelProvider(new ProductStructureLabelProvider());

        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getControl().setLayoutData(listLayoutData);
        StructuredViewerField<IIpsObject> listViewerField = new StructuredViewerField<IIpsObject>(viewer,
                IIpsObject.class);

        // Required to avoid an error in the system
        setControl(container);
        setPageComplete(false);

    }

    @Override
    public boolean canFlipToNextPage() {
        return true;
    }

}
