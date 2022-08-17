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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.core.ui.editors.type.AssociationsLabelProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

/**
 * The first page of the ConstrainableAssociationWizard. This Page makes it possible to select an
 * Association.
 * 
 * @since 3.11
 */
public class ConstrainableAssociationSelectionPage extends WizardPage {
    private Composite composite;
    private TreeViewer viewer;
    private BindingContext bindingContext;
    private ConstrainableAssociationPmo pmo;
    private CandidatesContentProvider contentProvider;

    protected ConstrainableAssociationSelectionPage(ConstrainableAssociationPmo pmo) {
        super(StringUtils.EMPTY);
        setTitle(Messages.ConstrainableAssociationWizard_labelSelectAssociation);
        bindingContext = new BindingContext();
        this.pmo = pmo;
    }

    @Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);

        createTreeViewer();
        bindContent();

        setControl(composite);
        setPageComplete(false);
    }

    private void createTreeViewer() {
        viewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getTree().setLayoutData(listLayoutData);

        contentProvider = new CandidatesContentProvider(pmo.getType());
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new AssociationsLabelProvider());
        viewer.setInput(pmo.getType());
        viewer.expandAll();
        handleNoConstrainableAssociationsAvailable();
    }

    private void handleNoConstrainableAssociationsAvailable() {
        if (isNoConstrainedAssociationAvailable()) {
            setMessage(Messages.ConstrainableAssociationSelectionPage_Message_NoOverridableAssociationsAvailable,
                    INFORMATION);
        }
    }

    private boolean isNoConstrainedAssociationAvailable() {
        return contentProvider.getElements(pmo.getType()).length == 0;
    }

    private void bindContent() {
        bindingContext.bindContent(viewer, IAssociation.class, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_ASSOCIATION);
        bindingContext.add(new PropertyChangeBinding<>(composite, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_ASSOCIATION, IAssociation.class) {

            @Override
            protected void propertyChanged(IAssociation oldValue, IAssociation newValue) {
                setPageComplete(pmo.getSelectedAssociation() != null);
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
    }

    protected static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        public CandidatesContentProvider(IType cmptType) {
            super(cmptType);
        }

        @Override
        public List<? extends IAssociation> getAvailableParts(IIpsObject ipsObject) {
            IType cmptType = (IType)ipsObject;
            return cmptType.findConstrainableAssociationCandidates(cmptType.getIpsProject());
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) {
            IType cmptType = (IType)ipsObject;
            return cmptType.getSupertypeHierarchy().getAllSupertypes(cmptType);
        }
    }

}
