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

package org.faktorips.devtools.core.ui.wizards.testcasecopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.StyledCellMessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseContentProvider;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseLabelProvider;
import org.faktorips.devtools.core.ui.editors.testcase.TreeViewerExpandStateStorage;
import org.faktorips.util.message.MessageList;

/**
 * Page to show the test case structure and replace product cmpt or deselect test objects.
 * 
 * TODO Joerg if changing selection in tree from need product to doesn't need product then right
 * side will not be cleared!
 * 
 * @author Joerg Ortmann
 */
public class TestCaseStructurePage extends WizardPage {
    private UIToolkit toolkit;

    // controls
    private ContainerCheckedTreeViewer treeViewer;
    private TableViewer tableViewer;
    private TableColumn[] columns = new TableColumn[2];
    private IIpsProject ipsProject;
    private IIpsSrcFile checkedProductCmpt = null;
    private TestCaseContentProvider testCaseContentProvider;

    public TestCaseStructurePage(UIToolkit toolkit, IIpsProject ipsProject) {
        super("TestCaseStructurePage"); //$NON-NLS-1$
        super.setTitle(Messages.TestCaseStructurePage_Title);

        this.toolkit = toolkit;
        this.ipsProject = ipsProject;
        setPageComplete(false);
    }

    private TestCaseCopyWizard getTestCaseCopyWizard() {
        return (TestCaseCopyWizard)super.getWizard();
    }

    public void createControl(Composite parent) {
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(2, true));

        createTree(root);
        createTable(root);

        setControl(root);
    }

    private void createTree(Composite parent) {
        Composite treeComposite = toolkit.createComposite(parent);
        treeComposite.setLayout(new GridLayout(1, true));
        treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        toolkit.createLabel(treeComposite, Messages.TestCaseStructurePage_LabelTestCaseStructure);

        Tree tree = new Tree(treeComposite, SWT.CHECK | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        treeViewer = new ContainerCheckedTreeViewer(tree);
        treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        hookTreeListeners();
        TestCaseLabelProvider labelProvider = new TestCaseLabelProvider(ipsProject);
        treeViewer.setLabelProvider(new StyledCellMessageCueLabelProvider(labelProvider, ipsProject));
        treeViewer.setUseHashlookup(true);
        treeViewer.expandAll();

        treeViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                pageChanged();
                treeViewer.refresh();
            }
        });

        new TreeMessageHoverService(treeViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                MessageList result = new MessageList();
                if (element instanceof IIpsObjectPartContainer) {
                    IIpsObjectPartContainer part = (IIpsObjectPartContainer)element;
                    MessageList msgList = part.getIpsObject().validate(ipsProject);
                    collectMessages(result, msgList, part);
                }
                return result;
            }
        };
    }

    private void createTable(Composite parent) {
        Composite candidateListComposite = toolkit.createComposite(parent);
        candidateListComposite.setLayout(new GridLayout(1, true));
        candidateListComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        toolkit.createLabel(candidateListComposite, Messages.TestCaseStructurePage_LabelProductCmptCandidates);

        tableViewer = new TableViewer(candidateListComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
                | SWT.FULL_SELECTION);
        tableViewer.setUseHashlookup(true);
        tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewer.getTable().setLinesVisible(true);

        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(ColumnWeightData.MINIMUM_WIDTH));
        layout.addColumnData(new ColumnWeightData(ColumnWeightData.MINIMUM_WIDTH));
        tableViewer.getTable().setLayout(layout);

        columns[0] = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        columns[1] = new TableColumn(tableViewer.getTable(), SWT.LEFT);

        tableViewer.setColumnProperties(new String[] { "check", "productcmpt" }); //$NON-NLS-1$ //$NON-NLS-2$

        tableViewer.setCellEditors(new CellEditor[] { new CheckboxCellEditor(tableViewer.getTable()),
                new CheckboxCellEditor(tableViewer.getTable()) });

        tableViewer.setCellModifier(new ICellModifier() {
            public boolean canModify(Object element, String property) {
                return true;
            }

            public Object getValue(Object element, String property) {
                return Boolean.TRUE;
            }

            public void modify(Object element, String property, Object value) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)((TableItem)element).getData();
                checkedProductCmpt = ipsSrcFile;
                IProductCmpt newProductCmpt = null;
                IProductCmpt oldProductCmpt = null;
                try {
                    newProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                    oldProductCmpt = replaceSelectedProductCmpt(newProductCmpt);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                    return;
                }
                refreshTree(oldProductCmpt, newProductCmpt);
                tableViewer.refresh();
            }

            /*
             * Refreshs the tree. This method resets the target as new input, because a simple
             * refresh doesn't work correctly if we change product cmpts and the labels.
             */
            private void refreshTree(IProductCmpt oldProductCmpt, IProductCmpt newProductCmpt) {
                try {
                    treeViewer.getTree().setRedraw(false);
                    TreeViewerExpandStateStorage storage = new TreeViewerExpandStateStorage(treeViewer);
                    storage.storeExpandedStatus();
                    pageChanged();
                    treeViewer.refresh();
                    // TODO Joerg check state get lost? Workaround: check all again
                    treeViewer.setAllChecked(true);
                    storage.restoreExpandedStatus();
                } finally {
                    treeViewer.getTree().setRedraw(true);
                }
            }
        });
        final ILabelProvider defaultLabelProvider = DefaultLabelProvider.createWithIpsSourceFileMapping();

        tableViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof IIpsSrcFile[]) {
                    return (IIpsSrcFile[])inputElement;
                } else {
                    return new IIpsSrcFile[0];
                }
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });
        tableViewer.setLabelProvider(new ITableLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex) {
                if (columnIndex == 1) {
                    return defaultLabelProvider.getImage(element);
                } else {
                    return element.equals(checkedProductCmpt) ? IpsPlugin.getDefault().getImage("ArrowRight.gif") : null; //$NON-NLS-1$
                }
            }

            public String getColumnText(Object element, int columnIndex) {
                if (columnIndex == 1) {
                    return defaultLabelProvider.getText(element);
                } else {
                    return ""; //$NON-NLS-1$
                }
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }
        });
    }

    private void hookTreeListeners() {
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                try {
                    ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmptFromSelection(event.getSelection());
                    if (testPolicyCmpt == null) {
                        clearCandidatesInTable();
                        return;
                    }
                    ITestPolicyCmpt parentPolicyCmpt = testPolicyCmpt.getParentTestPolicyCmpt();
                    changeCandidatesInTable(testPolicyCmpt.findProductCmpt(testPolicyCmpt.getIpsProject()),
                            testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject), parentPolicyCmpt == null ? null
                                    : parentPolicyCmpt.findProductCmpt(testPolicyCmpt.getIpsProject()));
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
    }

    /**
     * Sets and display a given target test case.
     * 
     * @param targetTestCase
     */
    public void setTargetTestCase(ITestCase targetTestCase) {
        // Note: creating a new tree viewer is necessary because the checked elements are cached
        // inside the viewer and if the target test case has changed on the first page the old
        // child referenced are returned by using getCheckedElements later
        treeViewer = new ContainerCheckedTreeViewer(treeViewer.getTree());
        testCaseContentProvider = new TestCaseContentProvider(TestCaseContentProvider.COMBINED, getTestCaseCopyWizard()
                .getTargetTestCase());
        treeViewer.setContentProvider(testCaseContentProvider);
        treeViewer.setInput(targetTestCase);
        treeViewer.expandAll();
        treeViewer.setAllChecked(true);
        treeViewer.refresh();
        pageChanged();
    }

    /*
     * Changes the available product cmpt candidates.
     */
    private void changeCandidatesInTable(IProductCmpt productCmptSelected,
            ITestPolicyCmptTypeParameter parameter,
            IProductCmpt parentProductCmpt) throws CoreException {
        checkedProductCmpt = null;
        if (productCmptSelected == null) {
            return;
        }
        checkedProductCmpt = productCmptSelected.getIpsSrcFile();

        IIpsSrcFile[] allowedProductCmpt = parameter.getAllowedProductCmpt(testCaseContentProvider.getTestCase()
                .getIpsProject(), parentProductCmpt);
        List<IIpsSrcFile> list = new ArrayList<IIpsSrcFile>();
        List<IIpsSrcFile> allowedProductCmptList = Arrays.asList(allowedProductCmpt);
        list.addAll(allowedProductCmptList);
        // add current product cmpt if not in candidate list
        if (!allowedProductCmptList.contains(productCmptSelected.getIpsSrcFile())) {
            list.add(productCmptSelected.getIpsSrcFile());
        }
        refreshTable(list.toArray(new IIpsSrcFile[list.size()]));
    }

    /*
     * Clear all product cmpt candidates.
     */
    private void clearCandidatesInTable() {
        checkedProductCmpt = null;
        refreshTable(new IIpsSrcFile[0]);
    }

    private void refreshTable(IIpsSrcFile[] files) {
        tableViewer.setInput(files);
        columns[0].pack();
        columns[1].pack();
        tableViewer.refresh(true);
    }

    /*
     * Returns the selected test policy cmpt or null if no test policy cmpt is selected.
     */
    private ITestPolicyCmpt getTestPolicyCmptFromSelection(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object selected = ((IStructuredSelection)selection).getFirstElement();
            if (selected instanceof ITestPolicyCmpt) {
                return (ITestPolicyCmpt)selected;
            }
        }
        return null;
    }

    /*
     * Replace the product cmpt of the selected test policy cmpt object. Returns the product cmpt
     * which was replaced.
     */
    private IProductCmpt replaceSelectedProductCmpt(IProductCmpt cmpt) {
        ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmptFromSelection(treeViewer.getSelection());
        if (testPolicyCmpt == null) {
            return null;
        }
        IProductCmpt oldProductCmp;
        try {
            oldProductCmp = testPolicyCmpt.findProductCmpt(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return null;
        }
        testPolicyCmpt.setProductCmpt(cmpt.getQualifiedName());
        String testPolicyCmptName = testPolicyCmpt.getTestCase().generateUniqueNameForTestPolicyCmpt(testPolicyCmpt,
                cmpt.getName());
        testPolicyCmpt.setName(testPolicyCmptName);

        return oldProductCmp;
    }

    private void pageChanged() {
        boolean pageComplete = validatePage();
        setPageComplete(pageComplete);
        getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                getContainer().updateButtons();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canFlipToNextPage() {
        boolean newTestCaseCreated = false;
        // create test case if not exists
        if (testCaseContentProvider == null) {
            getTestCaseCopyWizard().createNewTargetTestCase();
            newTestCaseCreated = true;
        } else {
            // delete previous temporary test case if the target changed
            if (!testCaseContentProvider.getTestCase().getQualifiedName().equals(
                    getTestCaseCopyWizard().getTargetTestCaseQualifiedName())
                    || getTestCaseCopyWizard().isReplaceParameterChanged()) {
                getTestCaseCopyWizard().deleteTestCase(testCaseContentProvider.getTestCase());
                getTestCaseCopyWizard().createNewTargetTestCase();
                newTestCaseCreated = true;
            }
        }

        if (newTestCaseCreated) {
            setTargetTestCase(getTestCaseCopyWizard().getTargetTestCase());
        } else {
            validatePage();
        }

        return false;
    }

    // TODO Joerg additional feature: finish not allowed if tree contains validation errors?
    private boolean validatePage() {
        setMessage(null);
        setErrorMessage(null);

        if (treeViewer.getCheckedElements().length == 0) {
            setMessage(Messages.TestCaseStructurePage_ValidationErrorNoElementSelected, ERROR);
            return false;
        }
        return true;
    }

    /*
     * Returns all checked objects.
     */
    Object[] getCheckedObjects() {
        return treeViewer.getCheckedElements();
    }

    /*
     * Collect all messages for the given element and all its child's. If an element isn't checked
     * then this element will be ignored.
     */
    private void collectMessages(MessageList result, MessageList msgList, IIpsObjectPartContainer container)
            throws CoreException {
        boolean checked = treeViewer.getChecked(container);
        if (checked) {
            result.add(msgList.getMessagesFor(container));
        }

        IIpsElement[] childs = container.getChildren();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof IIpsObjectPartContainer) {
                collectMessages(result, msgList, (IIpsObjectPartContainer)childs[i]);
            }
        }
    }

    // TODO Joerg CheckMessageCueLabelProvider doesn't work!
    /*
     * Special implementation of MessageCueLabelProvider. If an element isn't checked then the
     * validation messages will be ignored.
     */
    private class CheckMessageCueLabelProvider extends StyledCellMessageCueLabelProvider {
        public CheckMessageCueLabelProvider(StyledCellLabelProvider baseProvider, IIpsProject ipsProject) {
            super(baseProvider, ipsProject);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MessageList getMessages(Object element) throws CoreException {
            MessageList result = new MessageList();
            if (element instanceof IIpsObjectPartContainer) {
                IIpsObjectPartContainer part = (IIpsObjectPartContainer)element;
                MessageList msgList = part.getIpsObject().validate(ipsProject);
                collectMessages(result, msgList, part);
            }
            return result;
        }
    }
}
