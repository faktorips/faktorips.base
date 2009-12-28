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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * The <tt>MovePage</tt> provides a tree viewer that enables the user to choose a target destination
 * for the <tt>IIpsElement</tt> to move. An additional text field is provided so that it is possible
 * to rename the <tt>IIpsElement</tt> while moving it.
 * 
 * @author Alexander Weickmann
 */
public class MovePage extends IpsRenameMovePage {

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

    @Override
    protected LocationDescriptor getTargetLocationFromUserInput() {
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
        Object selectedElement = selection.getFirstElement();
        if (selectedElement == null) {
            throw new RuntimeException("No selection available.");
        }

        IIpsPackageFragment targetFragment;
        if (selectedElement instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot targetRoot = (IIpsPackageFragmentRoot)selectedElement;
            targetFragment = targetRoot.getDefaultIpsPackageFragment();
        } else if (selectedElement instanceof IIpsPackageFragment) {
            targetFragment = (IIpsPackageFragment)selectedElement;
        } else {
            throw new RuntimeException("Only package fragment roots and package fragments are valid selections.");
        }

        return new LocationDescriptor(targetFragment, getUserInputNewName());
    }

    @Override
    protected void createControlBefore(Composite pageControlComposite) {
        getUiToolkit().createLabel(pageControlComposite,
                NLS.bind(Messages.MovePage_labelChooseDestination, getIpsElement().getName()));
        createTreeViewer(pageControlComposite);
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
        try {
            IIpsProject[] modelProjects = IpsPlugin.getDefault().getIpsModel().getIpsModelProjects();
            treeViewer.setInput(modelProjects);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        setInitialTreeViewerSelection();

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                if (selection.getFirstElement() != null) {
                    // Only package fragments or package fragment roots are valid selections.
                    if (selection.getFirstElement() instanceof IIpsPackageFragment
                            || selection.getFirstElement() instanceof IIpsPackageFragmentRoot) {
                        userInputChanged();
                    } else {
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
        treeViewer.setSelection(new StructuredSelection(getOriginalLocation().getIpsPackageFragment()));
        treeViewer.refresh();
    }

    @Override
    protected void setFocus() {
        treeViewer.getControl().setFocus();
    }

    /** Label provider for the selection tree used by this page. */
    private class MoveLabelProvider extends DefaultLabelProvider {

        @Override
        public String getText(Object element) {
            String text = ""; //$NON-NLS-1$
            if (element instanceof IIpsPackageFragment) {
                if (((IIpsPackageFragment)element).isDefaultPackage()) {
                    text = super.getText(element);
                } else {
                    text = ((IIpsPackageFragment)element).getLastSegmentName();
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
                    List<IIpsPackageFragment> result = new ArrayList<IIpsPackageFragment>();
                    IIpsPackageFragment defaultFragment = ((IIpsPackageFragmentRoot)parentElement)
                            .getDefaultIpsPackageFragment();
                    result.add(defaultFragment);
                    result.addAll(Arrays.asList(defaultFragment.getChildIpsPackageFragments()));
                    return result.toArray();
                } else if (parentElement instanceof IIpsPackageFragment) {
                    if (((IIpsPackageFragment)parentElement).isDefaultPackage()) {
                        return new Object[0];
                    }
                    return ((IIpsPackageFragment)parentElement).getChildIpsPackageFragments();
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
