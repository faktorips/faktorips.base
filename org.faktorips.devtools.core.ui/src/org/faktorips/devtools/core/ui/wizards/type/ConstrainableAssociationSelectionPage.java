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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.core.ui.editors.type.AssociationsLabelProvider;

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
        bindContext();

        setControl(composite);
        setPageComplete(false);
    }

    private void createTreeViewer() {
        viewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getTree().setLayoutData(listLayoutData);

        viewer.setContentProvider(new CandidatesContentProvider(pmo.getType()));
        viewer.setLabelProvider(new AssociationsLabelProvider());
        viewer.setInput(pmo.getType());
        viewer.expandAll();
    }

    private void bindContext() {
        bindingContext.bindContent(viewer, IAssociation.class, pmo,
                ConstrainableAssociationPmo.PROPERTY_SELECTED_ASSOCIATION);
        bindingContext.add(new PropertyChangeBinding<IAssociation>(composite, pmo,
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
            try {
                return cmptType.findConstrainableAssociationCandidates(cmptType.getIpsProject());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) throws CoreException {
            IType cmptType = (IType)ipsObject;
            return cmptType.getSupertypeHierarchy().getAllSupertypes(cmptType);
        }
    }

}
