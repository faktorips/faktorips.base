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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

public class ConstrainableAssociationTargetPage extends WizardPage {

    private ConstrainableAssociationWizard wizard;
    private UIToolkit toolkit;
    private BindingContext bindingContext;
    private ConstrainableAssociationPmo constrainableAssociationPmo;
    private TreeViewer viewer;
    // private CandidatesContentProvider contentProvider;
    private IType cmptType;

    public ConstrainableAssociationTargetPage(ConstrainableAssociationWizard wizard, UIToolkit toolkit,
            BindingContext bindingContext, ConstrainableAssociationPmo constrainableAssociationPmo, IType cmptType) {
        super("");
        setDescription("");
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        this.constrainableAssociationPmo = constrainableAssociationPmo;
        this.cmptType = cmptType;
        // contentProvider = new CandidatesContentProvider(cmptType, this);
        setPageComplete(false);

    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        GridData gd = null;
        composite.setLayout(layout);

        Label messageLabel = createMessageArea(composite);
        if (messageLabel != null) {
            gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            gd.horizontalSpan = 2;
            messageLabel.setLayoutData(gd);
        }

        /*
         * viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
         * viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
         * viewer.setContentProvider(contentProvider); // viewer.setLabelProvider(new
         * AssociationsLabelProvider());
         * 
         * GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
         * listLayoutData.heightHint = 200; listLayoutData.widthHint = 300;
         * viewer.getControl().setLayoutData(listLayoutData); StructuredViewerField<IIpsObject>
         * listViewerField = new StructuredViewerField<IIpsObject>(viewer, IIpsObject.class);
         * 
         * viewer.setInput(contentProvider);
         */
        setControl(composite);
    }

    private Label createMessageArea(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        if (getSelectionText() != null) {
            label.setText(getSelectionText());
        }
        label.setFont(composite.getFont());
        return label;
    }

    private String getSelectionText() {
        return constrainableAssociationPmo.getSelectedAssociationText();
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    public ISelection getTreeViewerSelection() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Provides the <tt>IAttribute</tt>s available for selection. / protected static class
     * CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {
     * ConstrainableAssociationTargetPage targetPage;
     * 
     * /**
     * 
     * @param cmptType The <tt>cmptType</tt> the <tt>IAttribute</tt>s available for selection belong
     *            to.
     * @param constrainableAssociationTargetPage /
     * 
     *            public CandidatesContentProvider(IType cmptType,
     *            ConstrainableAssociationTargetPage targetPage) { super(cmptType); this.targetPage
     *            = targetPage; }
     * @Override public List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
     *           IType cmptType = (IType)ipsObject; try {
     * 
     *           return targetPage.cmptType; } catch (CoreException e) { throw new
     *           RuntimeException(e); } }
     * @Override protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) throws
     *           CoreException { IType cmptType = (IType)ipsObject; return
     *           cmptType.getSupertypeHierarchy().getAllSupertypes(cmptType); } }
     */

}
