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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.core.ui.editors.type.AssociationsLabelProvider;

public class ConstrainableAssociationSelectionPage extends WizardPage {
    private Composite composite;
    private Label label;
    private TreeViewer viewer;
    private final CandidatesContentProvider contentProvider;
    private IType cmptType;
    private BindingContext bindingContext;
    private StructuredViewerField<IIpsObject> listViewerField;

    protected ConstrainableAssociationSelectionPage(String pageName, IType cmptType) {
        super(pageName);
        setTitle("First Page");
        this.cmptType = cmptType;
        contentProvider = new CandidatesContentProvider(cmptType);
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.ConstrainableAssociationWizard_labelSelectAssociation);

        viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new AssociationsLabelProvider());

        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getControl().setLayoutData(listLayoutData);
        viewer.setInput(contentProvider);
        Object input = viewer.getInput();
        listViewerField = new StructuredViewerField<IIpsObject>(viewer, IIpsObject.class);

        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(false);

    }

    @Override
    public boolean canFlipToNextPage() {
        return true;
    }

    public ISelection getTreeViewerSelection() {
        return viewer.getSelection();
    }

    public void bindContext() {
        bindingContext.bindContent(listViewerField, new ConstrainableAssociationPmo(),
                ConstrainableAssociationPmo.PROPERTY_SELECTED_ASSOCIATION_TEXT);
        /*
         * ConstrainableAssociationPmo pmo.addPropertyChangeListener(new PropertyChangeListener() {
         * 
         * @Override public void propertyChange(PropertyChangeEvent evt) { if
         * (property.equals(evt.getPropertyName())) {
         * updateDescription((IIpsObject)evt.getNewValue()); }
         * 
         * } });
         */
    }

    /** Provides the <tt>IAttribute</tt>s available for selection. */
    protected static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {
        /**
         * @param cmptType The <tt>cmptType</tt> the <tt>IAttribute</tt>s available for selection
         *            belong to.
         */

        public CandidatesContentProvider(IType cmptType) {
            super(cmptType);
        }

        @Override
        public List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
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
