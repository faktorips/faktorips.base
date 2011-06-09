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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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

        PullUpUserInputPage() {
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

            destinationTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                    if (selection.getFirstElement() != null) {
                        userInputChanged();
                    }
                }
            });
        }

        @Override
        protected void setPromptMessage() {
            setMessage(NLS.bind(Messages.PullUpUserInputPage_message, getIpsElementName(getIpsElement()),
                    getIpsElement().getName()));
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {
            IStructuredSelection selection = (IStructuredSelection)destinationTreeViewer.getSelection();
            Object selectedElement = selection.getFirstElement();
            if (selectedElement == null) {
                throw new RuntimeException("No selection available."); //$NON-NLS-1$
            }

            if (!(selectedElement instanceof IType)) {
                throw new RuntimeException("Only types are valid selections."); //$NON-NLS-1$
            }

            getIpsPullUpProcessor().setTarget((IIpsObjectPartContainer)selectedElement);
        }

        private IIpsElement getIpsElement() {
            return getIpsRefactoringProcessor().getIpsElement();
        }

        private IpsPullUpProcessor getIpsPullUpProcessor() {
            return (IpsPullUpProcessor)getIpsRefactoringProcessor();
        }

    }

    private static class DestinationTreeContentProvider implements ITreeContentProvider {

        private Map<IType, IType> parentToChildInHierarchy = new HashMap<IType, IType>();

        private Map<IType, IType> childToParentInHierarchy = new HashMap<IType, IType>();

        private IType leafType;

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IAttribute) {
                IAttribute attribute = (IAttribute)inputElement;
                leafType = attribute.getType();
                RootOfHierarchyVisitor rootOfHierarchyVisitor = new RootOfHierarchyVisitor(attribute.getIpsProject());
                try {
                    rootOfHierarchyVisitor.start(leafType);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
                List<IType> visitedTypes = rootOfHierarchyVisitor.getVisited();
                IType rootType = visitedTypes.get(visitedTypes.size() - 1);
                return new Object[] { rootType };
            }
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IType) {
                IType child = parentToChildInHierarchy.get(parentElement);
                if (child != null) {
                    return new Object[] { child };
                }
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IType) {
                return childToParentInHierarchy.get(element);
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof IType) {
                IType child = parentToChildInHierarchy.get(element);
                return child != null && !child.equals(leafType);
            }
            return false;
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

        private class RootOfHierarchyVisitor extends TypeHierarchyVisitor<IType> {

            private IType previousType;

            public RootOfHierarchyVisitor(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IType currentType) throws CoreException {
                if (previousType != null) {
                    parentToChildInHierarchy.put(currentType, previousType);
                    childToParentInHierarchy.put(previousType, currentType);
                }
                previousType = currentType;
                return true;
            }

        }

    }

}
