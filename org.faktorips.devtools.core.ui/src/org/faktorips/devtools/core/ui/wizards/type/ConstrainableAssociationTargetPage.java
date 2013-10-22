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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.views.ipshierarchy.HierarchyContentProvider;

public class ConstrainableAssociationTargetPage extends WizardPage {

    private ConstrainableAssociationPmo pmo;
    private TreeViewer viewer;
    private BindingContext bindingContext;
    private Composite composite;
    private HierarchyContentProvider contentProvider;

    public ConstrainableAssociationTargetPage(ConstrainableAssociationPmo pmo) {
        super(StringUtils.EMPTY);
        contentProvider = new HierarchyContentProvider();
        bindingContext = new BindingContext();
        this.pmo = pmo;
    }

    @Override
    public void createControl(Composite parent) {
        composite = createComposite(parent);

        createTreeViewer(composite);
        bindContext();

        setControl(composite);
        setPageComplete(false);
    }

    private Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        return composite;
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
    }

    private void createTreeViewer(Composite composite) {
        viewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getTree().setLayoutData(listLayoutData);
        viewer.expandAll();
    }

    public void setLabel() {
        String text = NLS.bind(Messages.ConstrainableAssociationWizard_labelSelectionTarget, pmo
                .getSelectedAssociation().getName());
        setTitle(text);
    }

    public void initContentLabelProvider() {
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new DefaultLabelProvider());
        try {
            viewer.setInput(pmo.getSelectedAssociation().findTarget(pmo.getType().getIpsProject()));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
