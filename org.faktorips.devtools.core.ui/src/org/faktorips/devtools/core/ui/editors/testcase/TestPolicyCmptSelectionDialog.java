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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Tree selection dialog to select a test policy component that is defined in the test case.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptSelectionDialog extends SelectionStatusDialog {
    private UIToolkit toolkit;

    private TreeViewer treeViewer;

    private ITestCase testCase;

    private String filteredPolicyCmptType;

    private int fWidth = 60;
    private int fHeight = 18;

    private boolean isEmpty;

    private TestCaseContentProvider contentProvider;
    private TestCaseLabelProvider labelProvider;
    private ViewerFilter filter;

    private Label messageLabel;

    private IIpsProject ipsProject;

    public TestPolicyCmptSelectionDialog(Shell parentShell, UIToolkit toolkit, ITestCase testCase, int contentType,
            String policyCmptType) {
        super(parentShell);

        this.toolkit = toolkit;
        this.testCase = testCase;
        ipsProject = testCase.getIpsProject();
        filteredPolicyCmptType = policyCmptType;

        contentProvider = new TestCaseContentProvider(contentType, testCase);
        labelProvider = new TestCaseLabelProvider(ipsProject);
        filter = new TestPolicyCmptFilter();

        setTitle(Messages.TestPolicyCmptSelectionDialog_Title);

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int open() {
        isEmpty = evaluateIfTreeEmpty(testCase);
        super.open();
        return getReturnCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(Composite parent) {
        Control ctrl = super.createContents(parent);
        if (isEmpty) {
            getOkButton().setEnabled(false);
        }
        return ctrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);

        messageLabel = createMessageArea(composite);

        Composite treeComposite = toolkit.createLabelEditColumnComposite(composite);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        layout.verticalSpacing = 1;
        layout.horizontalSpacing = 1;
        treeComposite.setLayout(layout);
        treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        TreeViewer treeViewer = createTreeViewer(treeComposite);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(fWidth);
        data.heightHint = convertHeightInCharsToPixels(fHeight);
        Tree treeWidget = treeViewer.getTree();
        treeWidget.setLayoutData(data);
        treeWidget.setFont(parent.getFont());

        treeWidget.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDefaultSelected();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleWidgetSelected();
            }
        });

        if (isEmpty) {
            treeComposite.setEnabled(false);
            messageLabel.setText(NLS.bind(Messages.TestPolicyCmptSelectionDialog_Error_NoTestPolicyCmptFound,
                    filteredPolicyCmptType));
        } else {
            messageLabel.setText(NLS.bind(Messages.TestPolicyCmptSelectionDialog_Description, filteredPolicyCmptType));
        }

        return composite;
    }

    /**
     * Handles default selection (double click). By default, the OK button is pressed.
     */
    protected void handleDefaultSelected() {
        if (validateCurrentSelection()) {
            buttonPressed(IDialogConstants.OK_ID);
        }
    }

    private boolean validateCurrentSelection() {
        return getOkButton().isEnabled();
    }

    private void handleWidgetSelected() {
        IStructuredSelection newSelection = (IStructuredSelection)treeViewer.getSelection();

        if (newSelection.getFirstElement() instanceof ITestPolicyCmpt) {
            try {
                ITestPolicyCmptTypeParameter param = ((ITestPolicyCmpt)newSelection.getFirstElement())
                        .findTestPolicyCmptTypeParameter(ipsProject);
                if (param.getPolicyCmptType().equals(filteredPolicyCmptType)) {
                    messageLabel.setText(""); //$NON-NLS-1$
                    getOkButton().setEnabled(true);
                    return;
                }
            } catch (CoreException e) {
            }
        }
        messageLabel.setText(NLS.bind(Messages.TestPolicyCmptSelectionDialog_Error_WrongType, filteredPolicyCmptType));
        messageLabel.pack();
        getOkButton().setEnabled(false);
    }

    /**
     * Creates the tree viewer.
     */
    protected TreeViewer createTreeViewer(Composite parent) {
        Tree tree = toolkit.getFormToolkit().createTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer = new TreeViewer(tree);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(labelProvider);

        if (filter != null) {
            treeViewer.addFilter(filter);
        }

        treeViewer.setInput(testCase);
        treeViewer.expandAll();

        return treeViewer;
    }

    /**
     * Returns <code>true</code> if the tree is empty, otherwise <code>false</code>.
     */
    private boolean evaluateIfTreeEmpty(Object input) {
        Object[] elements = contentProvider.getElements(input);
        if (elements.length == 0) {
            return true;
        }
        if (filter != null) {
            elements = filter.filter(treeViewer, input, elements);
        }
        for (Object element : elements) {
            Object[] childs = filter.filter(treeViewer, input, contentProvider.getChildren(element));
            if (childs.length != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void computeResult() {
        ArrayList<Object> result = new ArrayList<Object>(1);
        if (treeViewer.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
            result.add(selection.getFirstElement());
        }
        setResult(result);
    }

    /**
     * Inner class of filter implementation.
     */
    private class TestPolicyCmptFilter extends ViewerFilter {
        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
            int size = elements.length;
            ArrayList<Object> out = new ArrayList<Object>(size);
            for (int i = 0; i < size; ++i) {
                Object element = elements[i];
                if (select(viewer, parent, element)) {
                    out.add(element);
                }
            }
            return out.toArray();
        }

        /**
         * The filter is always active.
         * 
         * {@inheritDoc}
         */
        @Override
        public boolean isFilterProperty(Object element, String property) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            try {
                if (element instanceof ITestPolicyCmpt) {
                    return isFilterChildOf((ITestPolicyCmpt)element, filteredPolicyCmptType);
                } else if (element instanceof TestCaseTypeAssociation) {
                    TestCaseTypeAssociation dummyAssociation = (TestCaseTypeAssociation)element;
                    ITestPolicyCmpt testPolicyCmpt = dummyAssociation.getParentTestPolicyCmpt();
                    if (testPolicyCmpt == null) {
                        return true;
                    }
                    ITestPolicyCmptLink childs[] = testPolicyCmpt.getTestPolicyCmptLinks();
                    boolean found = false;
                    for (ITestPolicyCmptLink elem : childs) {
                        String linkName = ""; //$NON-NLS-1$
                        if (elem.findTarget() != null) {
                            linkName = elem.findTarget().getTestPolicyCmptTypeParameter();
                            if (linkName.equals(dummyAssociation.getName())) {
                                found = isFilterChildOfLink(elem, filteredPolicyCmptType);
                                if (found) {
                                    return found;
                                }
                            }
                        }
                    }
                }
            } catch (CoreException e) {
                // ignore exception and don't display the element
            }
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the to be filtered object is a child of the given test policy
     * component. If there is no such child object return <code>false</code>.
     * 
     * @throws CoreException if an error occurs
     */
    private boolean isFilterChildOf(ITestPolicyCmpt testPolicyCmpt, String filter) throws CoreException {
        boolean found = false;
        ITestPolicyCmptLink[] realtions = testPolicyCmpt.getTestPolicyCmptLinks();
        for (ITestPolicyCmptLink link : realtions) {
            found = isFilterChildOfLink(link, filter);
            if (found) {
                // exit, at least one link contains the filtered element
                break;
            }
        }
        ITestPolicyCmptTypeParameter param = null;
        try {
            param = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
            if (param.getPolicyCmptType().equals(filteredPolicyCmptType)) {
                found = true;
            }
        } catch (CoreException e) {
            // ignored exception and don't display the element
        }

        return found;
    }

    /**
     * Returns <code>true</code> if the to be filtered object is a child of the given link. If there
     * is no such child object return <code>false</code>.
     * 
     * @throws CoreException if an error occurs
     */
    private boolean isFilterChildOfLink(ITestPolicyCmptLink link, String filter) throws CoreException {
        boolean found = false;
        ITestPolicyCmpt testPolicyCmpt = link.findTarget();
        if (!link.isAccoziation() && testPolicyCmpt != null) {
            found = isFilterChildOf(testPolicyCmpt, filter);
        }
        return found;
    }
}
