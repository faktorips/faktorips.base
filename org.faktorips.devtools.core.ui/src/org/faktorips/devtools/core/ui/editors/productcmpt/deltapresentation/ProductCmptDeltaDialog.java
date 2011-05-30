/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.core.ui.editors.deltapresentation.AbstractDeltaDialog;
import org.faktorips.devtools.core.ui.util.TypedSelection;

/**
 * Dialog to display differences between a product component and its type.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptDeltaDialog extends AbstractDeltaDialog {

    private IFixDifferencesComposite deltaComposite;
    private TreeViewer productCmptGenTree;
    private TreeViewer tree;

    /**
     * Create a new dialog, showing all the differences given. The first delta found in the given
     * deltaComposite has to be for the first generation and so on.
     * 
     * @param deltaComposite All deltaComposite for the generations.
     * @param parentShell The SWT parent-shell
     */
    public ProductCmptDeltaDialog(IFixDifferencesComposite deltaComposite, Shell parentShell) {

        super(parentShell);
        super.setShellStyle(getShellStyle() | SWT.RESIZE);
        this.deltaComposite = deltaComposite;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        String genTextPlural = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNamePlural();
        String genTextSingular = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
        Composite root = (Composite)super.createDialogArea(parent);

        // layouting

        // create composite with margins
        Composite listParent = toolkit.createGridComposite(root, 3, false, false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 10 + 30 + 50 + 10;
        listParent.setLayoutData(gridData);

        // line 1 (label for list)
        toolkit.createVerticalSpacer(listParent, 1);
        String text = NLS.bind(Messages.ProductCmptDeltaDialog_labelSelectGeneration, genTextPlural);
        Label label = toolkit.createLabel(listParent, text, false);
        ((GridData)label.getLayoutData()).minimumHeight = 10;
        toolkit.createVerticalSpacer(listParent, 1);

        // line 2 (list)
        toolkit.createVerticalSpacer(listParent, 1);
        productCmptGenTree = new TreeViewer(listParent, SWT.SINGLE | SWT.BORDER);
        toolkit.createVerticalSpacer(listParent, 1);
        productCmptGenTree.setLabelProvider(new WorkbenchLabelProvider());
        productCmptGenTree.setContentProvider(new WorkbenchContentProvider() {
            @Override
            public Object[] getElements(Object element) {
                return new Object[] { deltaComposite };
            }
        });

        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 70;
        productCmptGenTree.getTree().setLayoutData(gridData);

        // line 3 (label for tree)
        toolkit.createVerticalSpacer(listParent, 1);
        text = NLS.bind(Messages.ProductCmptDeltaDialog_labelSelectedDifferences, genTextSingular);
        label = toolkit.createLabel(listParent, text, false);
        ((GridData)label.getLayoutData()).minimumHeight = 10;
        toolkit.createVerticalSpacer(listParent, 1);

        // line 4 (tree)
        toolkit.createVerticalSpacer(listParent, 1);
        tree = new TreeViewer(listParent);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 100;
        tree.getTree().setLayoutData(gridData);
        toolkit.createVerticalSpacer(listParent, 1);

        // adding data and behaviour
        getShell().setText(Messages.ProductCmptDeltaDialog_title);

        if (isProductEditableInAllGenerations()) {
            setMessage(Messages.ProductCmptDeltaDialog_message, IMessageProvider.INFORMATION);
        } else {
            // set warning to inform that recent generation could not be edit,
            // but with this wizard recent generation could be changed
            setMessage(
                    Messages.ProductCmptDeltaDialog_message
                            + "\n" //$NON-NLS-1$
                            + NLS.bind(Messages.ProductCmptDeltaDialog_messageWarningRecentGenerationCouldBeChanged,
                                    genTextPlural), IMessageProvider.WARNING);
        }

        tree.setContentProvider(new DeltaContentProvider());
        tree.setLabelProvider(new DeltaLabelProvider());

        productCmptGenTree.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateDeltaView();
            }
        });

        // initialize view
        // need to set any element different form the deltaComposite itself because the content
        // provider does return the deltaComposite as root element. The root element could not be
        // the same as the input element @see IStructuredContentProvider#getElemement(Object)
        productCmptGenTree.setInput(deltaComposite.getCorrespondingIpsElement());
        productCmptGenTree.expandAll();
        productCmptGenTree.setSelection(new StructuredSelection(deltaComposite));
        updateDeltaView();

        return root;
    }

    /**
     * Returns <code>true</code> if the user can edit recent generations, if recent generations
     * couldn't be edit return <code>false</code>.
     */
    private boolean isProductEditableInAllGenerations() {
        return IpsPlugin.getDefault().getIpsPreferences().canEditRecentGeneration();
    }

    private void updateDeltaView() {
        TypedSelection<IFixDifferencesComposite> selection = new TypedSelection<IFixDifferencesComposite>(
                IFixDifferencesComposite.class, productCmptGenTree.getSelection());
        updateDeltaView(selection.getFirstElement());
    }

    public IFixDifferencesComposite getDeltaComposite() {
        return deltaComposite;
    }

    @Override
    protected TreeViewer getTreeViewer() {
        return tree;
    }
}
