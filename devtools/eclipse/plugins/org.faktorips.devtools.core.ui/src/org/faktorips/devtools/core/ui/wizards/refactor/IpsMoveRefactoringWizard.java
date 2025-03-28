/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A wizard to guide the user trough a Faktor-IPS "Move" refactoring.
 *
 * @author Alexander Weickmann
 */
public final class IpsMoveRefactoringWizard extends IpsRefactoringWizard {

    public IpsMoveRefactoringWizard(IIpsCompositeMoveRefactoring refactoring) {
        super(refactoring, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/MoveWizard.png")); //$NON-NLS-1$
        setDefaultPageTitle(Messages.IpsMoveRefactoringWizard_title);
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
    private static final class MoveUserInputPage extends IpsRefactoringUserInputPage {

        /**
         * The {@link TreeViewer} that allows the user to select the target
         * {@link IIpsPackageFragment}.
         */
        private TreeViewer treeViewer;

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
            setReferencingAndReferencedProjectsAsInput();
            treeViewer.addSelectionChangedListener(event -> {
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
            });
        }

        private void setReferencingAndReferencedProjectsAsInput() {
            IIpsProject ipsProject = getIpsRefactoring().getIpsProject();
            IIpsProject[] allProjects = ipsProject.getIpsModel().getIpsProjects();
            List<IIpsProject> refProjectsList = new ArrayList<>();
            refProjectsList.add(0, ipsProject);
            for (IIpsProject project : allProjects) {
                if (ipsProject.isReferencing(project) || ipsProject.isReferencedBy(project, true)) {
                    refProjectsList.add(project);
                }
            }
            IIpsProject[] refProjects = refProjectsList.toArray(new IIpsProject[refProjectsList.size()]);
            treeViewer.setInput(refProjects);
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
        protected void validateUserInputThis(RefactoringStatus status) {
            IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
            Object selectedElement = selection.getFirstElement();
            if (selectedElement == null) {
                throw new RuntimeException("No selection available."); //$NON-NLS-1$
            }

            if (selectedElement instanceof IIpsPackageFragment targetFragment) {
                getIpsCompositeMoveRefactoring().setTargetIpsPackageFragment(targetFragment);
            } else {
                throw new RuntimeException("Only package fragments are valid selections."); //$NON-NLS-1$
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
                if (!(element instanceof IIpsPackageFragment)
                        || ((IIpsPackageFragment)element).isDefaultPackage()) {
                    text = super.getText(element);
                } else {
                    text = ((IIpsPackageFragment)element).getName();
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
                    if (parentElement instanceof IIpsProject ipsProject) {
                        return ipsProject.getSourceIpsPackageFragmentRoots();
                    } else if (parentElement instanceof IIpsPackageFragmentRoot root) {
                        return root.getIpsPackageFragments();
                    }
                } catch (IpsException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                return new Object[0];
            }

            @Override
            public Object getParent(Object element) {
                return switch (element) {
                    case IIpsPackageFragment packageFragment -> packageFragment.getParent();
                    case IIpsPackageFragmentRoot root -> root.getIpsProject();
                    default -> null;
                };
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
