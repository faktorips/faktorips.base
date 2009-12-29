/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A wizard to guide the user trough a Faktor-IPS move refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRefactoringWizard extends IpsRefactoringWizard {

    /**
     * Creates a <tt>MoveRefactoringWizard</tt>.
     * 
     * @param refactoring The refactoring used by the wizard.
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public MoveRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement) {
        super(refactoring, ipsElement, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/MoveAndRenameWizard.png"));
        setDefaultPageTitle(NLS.bind(Messages.MoveRefactoringWizard_title, getIpsElementName()));
    }

    @Override
    protected void addUserInputPages() {
        addPage(new MovePage(getIpsElement()));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    /**
     * The <tt>MovePage</tt> provides a tree viewer that enables the user to choose a target
     * destination for the <tt>IIpsElement</tt> to move.
     */
    private final static class MovePage extends IpsRefactoringUserInputPage {

        /**
         * The <tt>TreeViewer</tt> that allows the user to select the target
         * <tt>IIpsPackageFragment</tt>.
         */
        private TreeViewer treeViewer;

        /**
         * Creates the <tt>MovePage</tt>.
         * 
         * @param ipsElement The <tt>IIpsElement</tt> to be moved.
         */
        MovePage(IIpsElement ipsElement) {
            super(ipsElement, "MovePage");
        }

        @Override
        protected void setPromptMessage() {
            setMessage(NLS.bind(Messages.MovePage_message, getIpsElementName(), getIpsElement().getName()));
        }

        public void createControl(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);

            getUiToolkit().createLabel(controlComposite,
                    NLS.bind(Messages.MovePage_labelChooseDestination, getIpsElement().getName()));
            createTreeViewer(controlComposite);

            setFocus();
            setPageComplete(false);
        }

        /** Creates the <tt>TreeViewer</tt> to select the target <tt>IIpsPackageFragment</tt> from. */
        private void createTreeViewer(Composite parent) {
            treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.widthHint = convertWidthInCharsToPixels(40);
            gridData.heightHint = convertHeightInCharsToPixels(15);
            treeViewer.getTree().setLayoutData(gridData);

            treeViewer.setLabelProvider(new MoveLabelProvider());
            treeViewer.setContentProvider(new MoveContentProvider());
            treeViewer.setInput(new IIpsProject[] { getIpsElement().getIpsProject() });

            setInitialTreeViewerSelection();

            treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                    if (selection.getFirstElement() != null) {
                        // Only package fragments are valid selections.
                        if (selection.getFirstElement() instanceof IIpsPackageFragment) {
                            userInputChanged();
                        } else {
                            setErrorMessage(Messages.MovePage_msgSelectOnlyPackages);
                            setPageComplete(false);
                        }
                    }
                }
            });
        }

        /**
         * Sets the initial selection of the selection tree to the package that contains the
         * <tt>IIpsElement</tt> to move.
         */
        private void setInitialTreeViewerSelection() {
            treeViewer.setSelection(new StructuredSelection(getIpsMoveProcessor().getOriginalIpsPackageFragment()));
            treeViewer.refresh();
        }

        /** This operation is responsible for setting the initial focus. */
        private void setFocus() {
            treeViewer.getControl().setFocus();
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {
            IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
            Object selectedElement = selection.getFirstElement();
            if (selectedElement == null) {
                throw new RuntimeException("No selection available.");
            }

            IIpsPackageFragment targetFragment;
            if (selectedElement instanceof IIpsPackageFragment) {
                targetFragment = (IIpsPackageFragment)selectedElement;
            } else {
                throw new RuntimeException("Only package fragments are valid selections.");
            }

            getIpsMoveProcessor().setTargetIpsPackageFragment(targetFragment);
            status.merge(getIpsMoveProcessor().validateUserInput(new NullProgressMonitor()));
        }

        /** Returns the <tt>IIpsMoveProcessor</tt> this refactoring is working with. */
        private IIpsMoveProcessor getIpsMoveProcessor() {
            return (IIpsMoveProcessor)((ProcessorBasedRefactoring)getRefactoring()).getProcessor();
        }

        /** Label provider for the selection tree used by this page. */
        private class MoveLabelProvider extends DefaultLabelProvider {

            @Override
            public String getText(Object element) {
                String text = ""; //$NON-NLS-1$
                if (element instanceof IIpsPackageFragment) {
                    if (((IIpsPackageFragment)element).isDefaultPackage()) {
                        text = "(default package)";
                    } else {
                        text = ((IIpsPackageFragment)element).getName();
                    }
                } else {
                    text = super.getText(element);
                }
                return text;
            }
        }

        /** Content provider for the selection tree used by this page. */
        private static class MoveContentProvider implements ITreeContentProvider {

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

            public Object getParent(Object element) {
                if (element instanceof IIpsPackageFragment) {
                    return ((IIpsPackageFragment)element).getParent();
                } else if (element instanceof IIpsPackageFragmentRoot) {
                    return ((IIpsPackageFragmentRoot)element).getIpsProject();
                }
                return null;
            }

            public boolean hasChildren(Object element) {
                return getChildren(element).length > 0;
            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof IIpsProject[]) {
                    return (IIpsProject[])inputElement;
                }
                return new Object[0];
            }

            public void dispose() {
                // Nothing to do.
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do.
            }

        }

    }

}
