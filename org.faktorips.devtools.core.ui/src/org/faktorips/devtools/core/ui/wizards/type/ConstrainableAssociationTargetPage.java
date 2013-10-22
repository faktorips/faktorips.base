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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.views.ipshierarchy.HierarchyContentProvider;

public class ConstrainableAssociationTargetPage extends WizardPage {

    private ConstrainableAssociationPmo pmo;
    private TreeViewer viewer;
    private BindingContext bindingContext;
    private Composite composite;
    private Label label;

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

        setLabel();

        setControl(composite);
        setPageComplete(false);
    }

    private void createComposite(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
    }

    public void bindContext() {
        bindingContext.bindContent(viewer, IType.class, pmo, ConstrainableAssociationPmo.PROPERTY_SELECTED_TARGET);
        bindingContext.add(new PropertyChangeBinding<IType>(composite, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_TARGET, IType.class) {

            @Override
            protected void propertyChanged(IType oldValue, IType newValue) {
                setPageComplete(pmo.getSelectedTarget() != null);
            }
        });
        bindingContext.add(new PropertyChangeBinding<IAssociation>(composite, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_ASSOCIATION, IAssociation.class) {

            @Override
            protected void propertyChanged(IAssociation oldValue, IAssociation newValue) {
                if (pmo.getSelectedAssociation() != null) {
                    try {
                        IType targetType = pmo.getSelectedAssociation().findTarget(pmo.getType().getIpsProject());
                        ITypeHierarchy subtypeHierarchy = targetType.getSubtypeHierarchy();
                        viewer.setInput(subtypeHierarchy);
                        setLabel();
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
        });
    }

    private void createTreeViewer(Composite composite) {
        viewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getTree().setLayoutData(listLayoutData);

        initContentLabelProvider();
        viewer.expandAll();
    }

    public void initContentLabelProvider() {
        viewer.setContentProvider(new HierarchyContentProvider());
        viewer.setLabelProvider(new DefaultLabelProvider());
    }

    public ISelection getTreeViewerSelection() {
        return viewer.getSelection();
    }

    public void setLabel() {
        label = new Label(composite, SWT.NONE);
        String labelText = label.getText();
        if (pmo.getSelectedAssociation() != null) {
            labelText = NLS.bind(Messages.ConstrainableAssociationWizard_labelSelectionTarget, pmo
                    .getSelectedAssociation().getName());
        }
        setTitle(labelText);
    }
}
