/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.views.ipshierarchy.HierarchyContentProvider;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;

/**
 * Second page of <code>ConstrainableAssociationWizard</code>.
 * <p>
 * This page shows the target (and all its subclasses) of the previous chosen
 * constrainable-association from the first page <code>ConstrainableAssociationSelectionPage</code>.
 * </p>
 * 
 * @author hbaagil
 */

public class ConstrainableAssociationTargetPage extends WizardPage {

    private ConstrainableAssociationPmo pmo;
    private TreeViewer viewer;
    private BindingContext bindingContext;
    private Composite composite;
    private Label label;

    /**
     * Creates a new page with the given PMO <code>ConstrainableAssociationPmo</code>.
     * 
     * @param pmo The presentation model object.
     */
    public ConstrainableAssociationTargetPage(ConstrainableAssociationPmo pmo) {
        super(StringUtils.EMPTY);
        bindingContext = new BindingContext();
        this.pmo = pmo;
    }

    @Override
    public void createControl(Composite parent) {
        createComposite(parent);

        createTreeViewer(composite);
        bindContext();

        setControl(composite);
        setPageComplete(false);
    }

    private void createComposite(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
    }

    private void createTreeViewer(Composite composite) {
        viewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getTree().setLayoutData(listLayoutData);

        initContentLabelProvider();
    }

    private void initContentLabelProvider() {
        viewer.setContentProvider(new HierarchyContentProvider());
        viewer.setLabelProvider(new DefaultLabelProvider());
    }

    private void bindContext() {
        bindingContext.bindContent(viewer, IType.class, pmo, ConstrainableAssociationPmo.PROPERTY_SELECTED_TARGET);
        bindingContext.add(new PropertyChangeBinding<>(composite, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_TARGET, IType.class) {

            @Override
            protected void propertyChanged(IType oldValue, IType newValue) {
                setPageComplete(pmo.getSelectedTarget() != null);
            }
        });
        bindingContext.add(new PropertyChangeBinding<>(composite, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_ASSOCIATION, IAssociation.class) {

            @Override
            protected void propertyChanged(IAssociation oldValue, IAssociation newValue) {
                if (pmo.getSelectedAssociation() != null) {
                    IType targetType = pmo.getSelectedAssociation().findTarget(pmo.getType().getIpsProject());
                    ITypeHierarchy subtypeHierarchy = targetType.getSubtypeHierarchy();
                    viewer.setInput(subtypeHierarchy);
                    viewer.expandAll();
                    setLabel();
                }
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
    }

    private void setLabel() {
        label = new Label(composite, SWT.NONE);
        String labelText = label.getText();
        if (pmo.getSelectedAssociation() != null) {
            labelText = NLS.bind(Messages.ConstrainableAssociationWizard_labelSelectionTarget, pmo
                    .getSelectedAssociation().getName());
        }
        setTitle(labelText);
    }
}
