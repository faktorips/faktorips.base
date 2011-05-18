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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * A wizard to guide the user trough a Faktor-IPS move refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class IpsMoveRefactoringWizard extends IpsRefactoringWizard {

    /**
     * @param refactoring The {@link IIpsCompositeMoveRefactoring} used by the wizard
     * 
     * @throws NullPointerException If any parameter is null
     */
    public IpsMoveRefactoringWizard(IIpsCompositeMoveRefactoring refactoring) {
        super(refactoring, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/MoveWizard.png")); //$NON-NLS-1$
        setDefaultPageTitle(Messages.MoveRefactoringWizard_title);
    }

    @Override
    protected void addUserInputPages() {
        addPage(new MoveUserInputPage());
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    /**
     * Provides a tree viewer that enables the user to choose a target destination for the
     * {@link IIpsElement} to move.
     */
    private final static class MoveUserInputPage extends IpsRefactoringUserInputPage {

        /**
         * The {@link TreeViewer} that allows the user to select the target
         * {@link IIpsPackageFragment}.
         */
        private TreeViewer treeViewer;

        /**
         * Check box that enables the user to decide whether the runtime ID of an
         * {@link IProductCmpt} should be adapted.
         */
        private Checkbox adaptRuntimeIdField;

        private boolean initialSelectionOccurred;

        MoveUserInputPage() {
            super("MoveUserInputPage"); //$NON-NLS-1$
        }

        @Override
        protected void setPromptMessage() {
            setMessage(NLS.bind(Messages.MoveUserInputPage_message, getIpsCompositeMoveRefactoring()
                    .getNumberOfRefactorings()));
        }

        @Override
        public void createControlThis(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);
            setPageComplete(false);

            getUiToolkit().createLabel(controlComposite, Messages.MoveUserInputPage_labelChooseDestination);
            createTreeViewer(controlComposite);
            if (getIpsCompositeMoveRefactoring().getTargetIpsPackageFragment() != null) {
                setInitialTreeViewerSelection(getIpsCompositeMoveRefactoring().getTargetIpsPackageFragment());
            }

            if (getIpsCompositeMoveRefactoring().isAdaptRuntimeIdRelevant()) {
                Composite fieldsComposite = getUiToolkit().createLabelEditColumnComposite(controlComposite);
                getUiToolkit().createLabel(fieldsComposite, ""); //$NON-NLS-1$
                adaptRuntimeIdField = getUiToolkit().createCheckbox(fieldsComposite,
                        Messages.IpsRenameAndMoveUserInputPage_labelRefactorRuntimeId);
                adaptRuntimeIdField.getButton().addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        userInputChanged();
                    }
                    
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        // Nothing to do
                    }
                });
                if (getIpsCompositeMoveRefactoring().isAdaptRuntimeId()) {
                    adaptRuntimeIdField.setChecked(true);
                    userInputChanged();
                }
            }

            setInitialFocus();
        }

        /**
         * Creates the {@link TreeViewer} to select the target {@link IIpsPackageFragment} from.
         */
        private void createTreeViewer(Composite parent) {
            treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.widthHint = convertWidthInCharsToPixels(40);
            gridData.heightHint = convertHeightInCharsToPixels(15);
            treeViewer.getTree().setLayoutData(gridData);

            treeViewer.setLabelProvider(new MoveLabelProvider());
            treeViewer.setContentProvider(new MoveContentProvider());
            treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
            treeViewer.setInput(new IIpsProject[] { getIpsRefactoring().getIpsProject() });

            treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                    if (selection.getFirstElement() != null) {
                        // Only package fragments are valid selections
                        if (selection.getFirstElement() instanceof IIpsPackageFragment) {
                            userInputChanged();
                        } else {
                            if (initialSelectionOccurred) {
                                setErrorMessage(Messages.MoveUserInputPage_msgSelectOnlyPackages);
                                setPageComplete(false);
                            }
                        }
                    }
                    initialSelectionOccurred = true;
                }
            });
        }

        private void setInitialTreeViewerSelection(IIpsPackageFragment ipsPackageFragment) {
            treeViewer.setSelection(new StructuredSelection(ipsPackageFragment));
            treeViewer.refresh();
        }

        /**
         * This operation is responsible for setting the initial focus.
         */
        private void setInitialFocus() {
            treeViewer.getControl().setFocus();
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {
            IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
            Object selectedElement = selection.getFirstElement();
            if (selectedElement == null) {
                throw new RuntimeException("No selection available."); //$NON-NLS-1$
            }

            if (selectedElement instanceof IIpsPackageFragment) {
                IIpsPackageFragment targetFragment = (IIpsPackageFragment)selectedElement;
                getIpsCompositeMoveRefactoring().setTargetIpsPackageFragment(targetFragment);
            } else {
                throw new RuntimeException("Only package fragments are valid selections."); //$NON-NLS-1$
            }

            if (adaptRuntimeIdField != null) {
                getIpsCompositeMoveRefactoring().setAdaptRuntimeId(adaptRuntimeIdField.isChecked());
            }

            status.merge(getIpsRefactoring().validateUserInput(new NullProgressMonitor()));
        }

        private IIpsCompositeMoveRefactoring getIpsCompositeMoveRefactoring() {
            return (IIpsCompositeMoveRefactoring)getIpsRefactoring();
        }

        /**
         * Label provider for the selection tree used by this page.
         */
        private class MoveLabelProvider extends DefaultLabelProvider {

            @Override
            public String getText(Object element) {
                String text = ""; //$NON-NLS-1$
                if (element instanceof IIpsPackageFragment) {
                    if (((IIpsPackageFragment)element).isDefaultPackage()) {
                        text = super.getText(element);
                    } else {
                        text = ((IIpsPackageFragment)element).getName();
                    }
                } else {
                    text = super.getText(element);
                }
                return text;
            }
        }

        /**
         * Content provider for the selection tree used by this page.
         */
        private static class MoveContentProvider implements ITreeContentProvider {

            @Override
            public Object[] getChildren(Object parentElement) {
                try {
                    if (parentElement instanceof IIpsProject) {
                        return ((IIpsProject)parentElement).getSourceIpsPackageFragmentRoots();
                    } else if (parentElement instanceof IIpsPackageFragmentRoot) {
                        IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot)parentElement;
                        return root.getIpsPackageFragments();
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                return new Object[0];
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof IIpsPackageFragment) {
                    return ((IIpsPackageFragment)element).getParent();
                } else if (element instanceof IIpsPackageFragmentRoot) {
                    return ((IIpsPackageFragmentRoot)element).getIpsProject();
                }
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                return getChildren(element).length > 0;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof IIpsProject[]) {
                    return (IIpsProject[])inputElement;
                }
                return new Object[0];
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        }

    }

}
