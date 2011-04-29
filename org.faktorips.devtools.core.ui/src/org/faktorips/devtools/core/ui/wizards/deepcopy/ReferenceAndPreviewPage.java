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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.AbstractInputFormat;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarFormat;
import org.faktorips.util.StringUtil;

/**
 * Page to preview the changes to the names of copied products and to switch between a copy or a
 * reference.
 * 
 */
public class ReferenceAndPreviewPage extends WizardPage {

    // The ID to identify this page.
    public static final String PAGE_ID = "deepCopyWizard.preview"; //$NON-NLS-1$

    // The viewer to display the products to copy
    private TreeViewer tree;

    // Label shows the current working date
    private Label workingDateLabel;

    private final int type;

    /**
     * @param type The type of the wizard displaying this page.
     * 
     * @return The title for this page - which depends on the given type.
     */
    private static String getTitle(int type) {
        return Messages.ReferenceAndPreviewPage_title;
    }

    /**
     * Create a new page to show the previously selected products with new names and allow the user
     * to choose between copy and reference, select the target package, search- and replace-pattern.
     * 
     * @param type The type used to create the <code>DeepCopyWizard</code>.
     * 
     * @throws IllegalArgumentException if the given type is neither
     *             DeepCopyWizard.TYPE_COPY_PRODUCT nor DeepCopyWizard.TYPE_NEW_VERSION.
     */
    protected ReferenceAndPreviewPage(int type) {
        super(PAGE_ID, getTitle(type), null);
        this.type = type;

        if (type != DeepCopyWizard.TYPE_COPY_PRODUCT && type != DeepCopyWizard.TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }

        setTitle(getTitle(type));

        //
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {

        if (getStructure() == null) {
            Label errormsg = new Label(parent, SWT.WRAP);
            GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
            errormsg.setLayoutData(layoutData);
            errormsg.setText(Messages.ReferenceAndPreviewPage_msgCircleDetected);
            setControl(errormsg);
            return;
        }

        UIToolkit toolkit = new UIToolkit(null);

        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        workingDateLabel = toolkit.createFormLabel(inputRoot, ""); //$NON-NLS-1$
        updateWorkingDateLabel();

        tree = new TreeViewer(root);
        tree.setUseHashlookup(true);
        tree.setLabelProvider(new DeepCopyLabelProvider(getWizard().getDeepCopyPreview()) {

            @Override
            public void update(ViewerCell cell) {
                cell.setText(getNewOrOldName(cell.getElement()));
                cell.setImage(getObjectImage(cell.getElement()));
                addStyledSuffix(cell);
            }

        });
        tree.setContentProvider(new ContentProvider(getPresentationModel().getTreeStatus()));
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void updateWorkingDateLabel() {
        GregorianCalendar newValidFrom = getPresentationModel().getNewValidFrom();
        AbstractInputFormat<GregorianCalendar> format = GregorianCalendarFormat.newInstance();
        workingDateLabel.setText(format.format(newValidFrom));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
                String productCmptTypeName = StringUtil.unqualifiedName(getStructure().getRoot().getProductCmpt()
                        .getProductCmptType());
                setDescription(NLS.bind(Messages.ReferenceAndPreviewPage_descritionPreviewNewCopy, productCmptTypeName));
            } else if (type == DeepCopyWizard.TYPE_NEW_VERSION) {
                String versionConceptNameSingular = IpsPlugin.getDefault().getIpsPreferences()
                        .getChangesOverTimeNamingConvention().getVersionConceptNameSingular();
                setDescription(NLS.bind(Messages.ReferenceAndPreviewPage_descritionPreviewNewGeneration,
                        versionConceptNameSingular));
            }

            try {
                getWizard().getContainer().run(false, false, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        updateWorkingDateLabel();

                        if (monitor == null) {
                            monitor = new NullProgressMonitor();
                        }
                        monitor.beginTask(Messages.ReferenceAndPreviewPage_msgValidateCopy, 6);

                        monitor.worked(1);
                        tree.setInput(getStructure());
                        monitor.worked(1);
                        tree.expandAll();
                        monitor.worked(1);
                        monitor.worked(1);
                        monitor.worked(1);
                        monitor.worked(1);
                    }

                });
            } catch (InvocationTargetException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            } catch (InterruptedException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /**
     * Does only show the nodes which where selected on the source page. As input, an array of all
     * the selected nodes of the source page is expected.
     * 
     */
    private class ContentProvider extends DeepCopyContentProvider {

        private final DeepCopyTreeStatus treeStatus;

        public ContentProvider(DeepCopyTreeStatus treeStatus) {
            super(true, true);
            this.treeStatus = treeStatus;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (treeStatus.isEnabled((IProductCmptStructureReference)parentElement)) {
                IProductCmptStructureReference[] children = (IProductCmptStructureReference[])super
                        .getChildren(parentElement);
                List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();
                for (IProductCmptStructureReference child : children) {
                    if (treeStatus.isEnabled(child)) {
                        result.add(child);
                    }
                }
                return result.toArray(new IProductCmptStructureReference[result.size()]);
            } else {
                return new Object[0];
            }
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProductCmptTreeStructure) {
                IProductCmptReference node = ((IProductCmptTreeStructure)inputElement).getRoot();
                if (treeStatus.isEnabled(node)) {
                    return new Object[] { node };
                }
            }
            return new Object[0];
        }

    }

    @Override
    public DeepCopyWizard getWizard() {
        return (DeepCopyWizard)super.getWizard();
    }

    private DeepCopyPresentationModel getPresentationModel() {
        if (getWizard() == null) {
            return null;
        }
        return getWizard().getPresentationModel();
    }

    private IProductCmptTreeStructure getStructure() {
        if (getPresentationModel() == null) {
            return null;
        }
        return getPresentationModel().getStructure();
    }

}
