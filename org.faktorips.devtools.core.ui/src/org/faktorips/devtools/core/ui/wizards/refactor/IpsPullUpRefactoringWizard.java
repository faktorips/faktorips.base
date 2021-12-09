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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.HierarchyVisitor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;

/**
 * A wizard to guide the user trough a Faktor-IPS "Pull Up" refactoring.
 * 
 * @author Alexander Weickmann
 */
public class IpsPullUpRefactoringWizard extends IpsRefactoringWizard {

    public IpsPullUpRefactoringWizard(IIpsRefactoring refactoring) {
        super(refactoring, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/PullUpWizard.png")); //$NON-NLS-1$
        setDefaultPageTitle(Messages.IpsPullUpRefactoringWizard_title);
    }

    @Override
    protected void addUserInputPages() {
        addPage(new PullUpUserInputPage());
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    /**
     * Provides a tree viewer that enables the user to choose a target destination for the pull up
     * refactoring.
     */
    private class PullUpUserInputPage extends IpsRefactoringUserInputPage {

        private TreeViewer destinationTreeViewer;

        private PullUpUserInputPage() {
            super("PullUpUserInputPage"); //$NON-NLS-1$
        }

        @Override
        public void createControlThis(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);

            getUiToolkit().createLabel(controlComposite, Messages.PullUpUserInputPage_labelChooseDestination);
            createTreeViewer(controlComposite);

            setPageComplete(false);
        }

        private void createTreeViewer(Composite parent) {
            destinationTreeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.widthHint = convertWidthInCharsToPixels(40);
            gridData.heightHint = convertHeightInCharsToPixels(15);
            destinationTreeViewer.getTree().setLayoutData(gridData);

            destinationTreeViewer.setLabelProvider(new DefaultLabelProvider());
            destinationTreeViewer.setContentProvider(new DestinationTreeContentProvider());
            destinationTreeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
            destinationTreeViewer.setInput(getIpsElement());

            destinationTreeViewer.addSelectionChangedListener(event -> {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                if (selection.getFirstElement() != null) {
                    userInputChanged();
                }
            });
        }

        @Override
        protected void setPromptMessage() {
            setMessage(NLS.bind(Messages.PullUpUserInputPage_message, getIpsElementName(getIpsElement()),
                    getIpsElement().getName()));
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreRuntimeException {
            IStructuredSelection selection = (IStructuredSelection)destinationTreeViewer.getSelection();
            Object selectedElement = selection.getFirstElement();
            if (selectedElement == null) {
                throw new RuntimeException("No selection available."); //$NON-NLS-1$
            }

            getIpsPullUpProcessor().setTarget((IIpsObjectPartContainer)selectedElement);

            status.merge(getIpsRefactoring().validateUserInput(new NullProgressMonitor()));
        }

        private IIpsElement getIpsElement() {
            return getIpsRefactoringProcessor().getIpsElement();
        }

        private IpsPullUpProcessor getIpsPullUpProcessor() {
            return (IpsPullUpProcessor)getIpsRefactoringProcessor();
        }

    }

    private static class DestinationTreeContentProvider implements ITreeContentProvider {

        private Map<IIpsObject, IIpsObject> parentToChildInHierarchy = new HashMap<>();

        private Map<IIpsObject, IIpsObject> childToParentInHierarchy = new HashMap<>();

        private IIpsObject leafObject;

        @Override
        public Object[] getElements(Object inputElement) {
            if (!(inputElement instanceof IIpsObjectPart)) {
                return new Object[0];
            }
            // The leaf of the tree is the container object of the input object part
            leafObject = ((IIpsObjectPart)inputElement).getIpsObject();
            RootHierarchyVisitor rootOfHierarchyVisitor = new RootHierarchyVisitor(leafObject.getIpsProject());
            // Beginning from the leaf we want to find the root object of the hierarchy
            rootOfHierarchyVisitor.start(leafObject);
            List<IIpsObject> visitedIpsObjects = rootOfHierarchyVisitor.getVisited();
            // The root object of the hierarchy is the last visited object
            IIpsObject rootIpsObject = visitedIpsObjects.get(visitedIpsObjects.size() - 1);
            return visitedIpsObjects.isEmpty() ? new Object[0] : new Object[] { rootIpsObject };
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            IIpsObject child = parentToChildInHierarchy.get(parentElement);
            return child != null ? new Object[] { child } : new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return childToParentInHierarchy.get(element);
        }

        @Override
        public boolean hasChildren(Object element) {
            IIpsObject child = parentToChildInHierarchy.get(element);
            return child != null && !child.equals(leafObject);
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

        private class RootHierarchyVisitor extends HierarchyVisitor<IIpsObject> {

            private IIpsObject previousObject;

            private RootHierarchyVisitor(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IIpsObject currentObject) {
                if (previousObject != null) {
                    parentToChildInHierarchy.put(currentObject, previousObject);
                    childToParentInHierarchy.put(previousObject, currentObject);
                }
                previousObject = currentObject;
                return true;
            }

            @Override
            protected IIpsObject findSupertype(IIpsObject currentObject, IIpsProject ipsProject) {
                if (currentObject instanceof IType) {
                    return ((IType)currentObject).findSupertype(ipsProject);
                }
                if (currentObject instanceof IEnumType) {
                    return ((IEnumType)currentObject).findSuperEnumType(ipsProject);
                }
                throw new RuntimeException("Illegal type, " + currentObject + " must be of type IType or IEnumType"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

    }

}
