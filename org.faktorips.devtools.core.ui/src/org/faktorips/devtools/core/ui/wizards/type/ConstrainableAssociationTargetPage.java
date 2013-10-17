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

package org.faktorips.devtools.core.ui.wizards.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.editors.type.AssociationsLabelProvider;

public class ConstrainableAssociationTargetPage extends WizardPage {

    private ConstrainableAssociationPmo constrainableAssociationPmo;
    private TreeViewer viewer;

    public ConstrainableAssociationTargetPage(ConstrainableAssociationPmo pmo, IType cmptType) {
        super(StringUtils.EMPTY);
        setTitle(cmptType.getName());
        constrainableAssociationPmo = pmo;
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = createComposite(parent);

        setLabel(composite);

        createTreeViewer(composite);

        setControl(composite);
        setPageComplete(true);
    }

    private Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        return composite;
    }

    private void setLabel(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        String text = NLS.bind(Messages.ConstrainableAssociationWizard_labelSelectionTarget,
                constrainableAssociationPmo.getSelectedAssociation());
        label.setText(text);
    }

    private void createTreeViewer(Composite composite) {
        viewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setLabelProvider(new AssociationsLabelProvider());

        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getControl().setLayoutData(listLayoutData);
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    public ISelection getTreeViewerSelection() {
        return viewer.getSelection();
    }
}
