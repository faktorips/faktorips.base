/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsPackageSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.ListElementMover;
import org.faktorips.runtime.test.AbstractIpsTestRunner;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestSelectionComposite extends Composite {
    private UIToolkit toolkit;

    private List<ITestConfigurationChangeListener> listeners = new ArrayList<>(1);

    // buttons
    private Button newButton;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    private Table table;

    private TableViewer viewer;

    private List<Object> content = new ArrayList<>();

    private IIpsProject project;

    private Button newSuiteButton;

    public TestSelectionComposite(Composite parent) {
        super(parent, SWT.NONE);
        toolkit = new UIToolkit(null);

        setLayout(new GridLayout(1, true));

        Composite tableWithBtns = toolkit.createGridComposite(this, 2, false, true);

        tableWithBtns.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = createViewer(tableWithBtns);
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.addSelectionChangedListener($ -> updateButtonEnabledStates());

        Composite buttons = toolkit.createComposite(tableWithBtns);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons, toolkit);
    }

    private boolean createButtons(Composite buttons, UIToolkit toolkit) {
        createNewButton(buttons, toolkit);
        createNewSuiteButton(buttons, toolkit);
        createDeleteButton(buttons, toolkit);
        createButtonSpace(buttons, toolkit);
        createMoveButtons(buttons, toolkit);
        return true;
    }

    private TableViewer createViewer(Composite parent) {
        table = new Table(parent, SWT.BORDER | SWT.MULTI);
        viewer = new TableViewer(table);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TestSuiteLabelProvider());

        viewer.setInput(content);

        return viewer;
    }

    private final void createButtonSpace(Composite buttons, UIToolkit toolkit) {
        Label spacer = toolkit.createLabel(buttons, null);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        data.heightHint = 5;
        spacer.setLayoutData(data);
    }

    private final void createNewButton(final Composite buttons, UIToolkit toolkit) {
        newButton = toolkit.createButton(buttons, Messages.TestSelectionComposite_labelButtonAddTestCase);
        newButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        newButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    newTest(buttons);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private final void createNewSuiteButton(final Composite buttons, UIToolkit toolkit) {
        newSuiteButton = toolkit.createButton(buttons, Messages.TestSelectionComposite_labelButtonAddTestSuite);
        newSuiteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        newSuiteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    newTestSuite();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private final void createDeleteButton(Composite buttons, UIToolkit toolkit) {
        deleteButton = toolkit.createButton(buttons, Messages.TestSelectionComposite_labelButtonDelete);
        deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        deleteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    deleteElement();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private IStructuredSelection getSelectedObjects() {
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection)selection;
        }
        return StructuredSelection.EMPTY;
    }

    private void deleteElement() {
        for (Iterator<?> iter = getSelectedObjects().iterator(); iter.hasNext();) {
            Object selectedObj = iter.next();
            viewer.remove(selectedObj);
            content.remove(selectedObj);
        }
        notifyListener();
    }

    private final void createMoveButtons(Composite buttons, UIToolkit toolkit) {
        upButton = toolkit.createButton(buttons, Messages.TestSelectionComposite_labelButtonUp);
        upButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        upButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveParts(true);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
        downButton = toolkit.createButton(buttons, Messages.TestSelectionComposite_labelButtonDown);
        downButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        downButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveParts(false);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

    }

    private void moveParts(boolean up) {
        ListElementMover<Object> mover = new ListElementMover<>(content);
        mover.move(viewer.getTable().getSelectionIndices(), up);
        viewer.refresh();
        notifyListener();
    }

    private void newTest(Composite buttonComposite) {
        List<IIpsSrcFile> allIpsSrcFiles = project.findAllIpsSrcFiles(IpsObjectType.TEST_CASE);
        StaticContentSelectIpsObjectContext context = new StaticContentSelectIpsObjectContext();
        context.setElements(allIpsSrcFiles.toArray(new IIpsSrcFile[allIpsSrcFiles.size()]));
        OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(buttonComposite.getShell(),
                Messages.TestSelectionComposite_dialogTitleSelectTestCase, context, false);
        dialog.setFilter(""); //$NON-NLS-1$
        if (dialog.open() == Window.OK) {
            Object[] result = dialog.getResult();
            Object lastAdded = null;
            for (Object element : result) {
                if (!content.contains(element)) {
                    content.add(element);
                }
                lastAdded = element;
            }
            viewer.refresh();
            if (lastAdded != null) {
                viewer.setSelection(new StructuredSelection(lastAdded));
            }
            notifyListener();
        }
    }

    protected void newTestSuite() {
        try {
            IpsPackageSelectionDialog dialog = new IpsPackageSelectionDialog(getShell());
            dialog.setMultipleSelection(true);
            dialog.setElements(getPackageFragments());
            if (dialog.open() == Window.OK) {
                Object[] result = dialog.getResult();
                for (Object element : result) {
                    content.add(element);
                }
                viewer.refresh();
                notifyListener();
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns all ips package fragments of all projects in the current workspace.
     */
    private IIpsPackageFragment[] getPackageFragments() {
        ArrayList<IIpsPackageFragment> packageFragmentList = new ArrayList<>();
        try {
            IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot root : roots) {
                if (!root.isBasedOnSourceFolder()) {
                    continue;
                }
                IIpsPackageFragment[] childs = root.getIpsPackageFragments();
                for (IIpsPackageFragment child : childs) {
                    packageFragmentList.add(child);
                }
            }
            return packageFragmentList.toArray(new IIpsPackageFragment[0]);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return new IIpsPackageFragment[0];
    }

    /**
     * Calls <code>IIpsProject#findAllIpsObjects()</code> on all projects in the workspace and
     * returns the collective list of <code>IIpsObject</code>s.
     * 
     * @throws IpsException if getting objects from a <code>IIpsProject</code> fails.
     */
    public IIpsObject[] getAllIpsTestObjects() {
        List<IIpsSrcFile> allIpsSrcFiles = project.findAllIpsSrcFiles(IpsObjectType.TEST_CASE,
                IpsObjectType.PRODUCT_CMPT);
        List<IIpsObject> list = new ArrayList<>();
        for (IIpsSrcFile ipsSrcFile : allIpsSrcFiles) {
            list.add(ipsSrcFile.getIpsObject());
        }
        return list.toArray(new IIpsObject[list.size()]);
    }

    public void initContent(IIpsProject project, String testSuites) {
        ArgumentCheck.notNull(project);

        this.project = project;
        content.clear();

        List<String> testSuiteList = AbstractIpsTestRunner.extractListFromString(testSuites);
        if (project == null) {
            throw new IpsException(new IpsStatus(Messages.TestSelectionComposite_errorProjectNotDetermined));
        }

        for (String qualifiedName : testSuiteList) {
            IIpsObject testCase = project.findIpsObject(IpsObjectType.TEST_CASE, qualifiedName);
            if (testCase != null) {
                content.add(testCase);
                continue;
            }
            IProductCmpt productCmpt = project.findProductCmpt(qualifiedName);
            if (productCmpt != null) {
                content.add(productCmpt);
                continue;
            }
            boolean found = false;
            IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot root : roots) {
                IIpsPackageFragment[] frgmts = root.getIpsPackageFragments();
                for (IIpsPackageFragment frgmt : frgmts) {
                    if (frgmt.getName().equals(qualifiedName)) {
                        content.add(frgmt);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        updateButtonEnabledStates();
        viewer.refresh();
    }

    public String getPackageFragmentRootText() {
        List<String> roots = new ArrayList<>();
        for (Object object : content) {
            IIpsElement element = (IIpsElement)object;
            if (element instanceof IIpsPackageFragment) {
                roots.add(IpsTestRunner.getRepPckNameFromPckFrgmtRoot(((IIpsPackageFragment)element).getRoot()));
            } else if (element instanceof IIpsObject) {
                roots.add(IpsTestRunner.getRepPckNameFromPckFrgmtRoot(((IIpsObject)element).getIpsPackageFragment()
                        .getRoot()));
            }
        }
        if (roots.size() == 0) {
            IIpsPackageFragmentRoot[] rootsFromProject = project.getIpsPackageFragmentRoots();
            if (rootsFromProject.length > 0) {
                roots.add(IpsTestRunner.getRepPckNameFromPckFrgmtRoot(rootsFromProject[0]));
            }
        }
        return AbstractIpsTestRunner.toStringFromList(roots);
    }

    public String getTestCasesText() {
        List<String> testSuites = new ArrayList<>();
        for (Object object : content) {
            IIpsElement element = (IIpsElement)object;
            if (element instanceof IIpsPackageFragment) {
                testSuites.add(((IIpsPackageFragment)element).getName());
            } else if (element instanceof IIpsObject) {
                testSuites.add(((IIpsObject)element).getQualifiedName());
            }
        }
        return AbstractIpsTestRunner.toStringFromList(testSuites);
    }

    public void addChangeListener(ITestConfigurationChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListener() {
        for (ITestConfigurationChangeListener l : listeners) {
            l.testConfigurationHasChanged();
        }
    }

    private void updateButtonEnabledStates() {
        setButtonStateSelBtns(getSelectedObjects().getFirstElement() != null);
    }

    private void setButtonStateSelBtns(boolean enabled) {
        deleteButton.setEnabled(enabled);
        upButton.setEnabled(enabled);
        downButton.setEnabled(enabled);
    }

    private class TestSuiteLabelProvider extends DefaultLabelProvider {
        @Override
        public String getText(Object element) {
            String text = ""; //$NON-NLS-1$
            String pckFrgmtRootName = ""; //$NON-NLS-1$
            if (element instanceof IIpsObject) {
                text = ((IIpsObject)element).getQualifiedName();
                pckFrgmtRootName = ((IIpsObject)element).getIpsPackageFragment().getRoot().getName();
            } else {
                text = super.getText(element);
                if (element instanceof IIpsPackageFragment) {
                    pckFrgmtRootName = ((IIpsPackageFragment)element).getRoot().getName();
                }
            }
            return text + (pckFrgmtRootName.length() > 0 ? " (" + pckFrgmtRootName + ")" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
}
