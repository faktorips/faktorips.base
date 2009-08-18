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

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class CreateMissingEnumContentsWizard extends Wizard {

    private SelectEnumContentsPage selectEnumContentsPage;

    private WizardDialog wizardDialog;

    public CreateMissingEnumContentsWizard() {
        setWindowTitle(Messages.CreateMissingEnumContentsWizard_title);
        setNeedsProgressMonitor(true);
    }

    public void open(Shell parentShell) {
        wizardDialog = new WizardDialog(parentShell, this);
        wizardDialog.open();
    }

    @Override
    public void addPages() {
        selectEnumContentsPage = new SelectEnumContentsPage();
        addPage(selectEnumContentsPage);
    }

    @Override
    public boolean performFinish() {
        try {
            wizardDialog.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.CreateMissingEnumContentsWizard_labelOperation, 9999999);
                    for (int i = 0; i < 9999999; i++) {
                        monitor.worked(1);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private class SelectEnumContentsPage extends WizardPage {

        private ContainerCheckedTreeViewer viewer;

        private ITreeContentProvider contentProvider;

        protected SelectEnumContentsPage() {
            super(Messages.SelectEnumContentsPage_title);
            setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(
                    "wizards/CreateMissingEnumContentsWizard.png"));
            setMessage(Messages.SelectEnumContentsPage_prompt);
            setTitle(Messages.SelectEnumContentsPage_title);
        }

        public void createControl(Composite parent) {
            Composite control = new Composite(parent, SWT.NONE);
            control.setLayout(new GridLayout(1, true));
            control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            viewer = new ContainerCheckedTreeViewer(control, SWT.FILL | SWT.BORDER);
            viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            contentProvider = new EnumContentsContentProvider();
            viewer.setContentProvider(contentProvider);
            viewer.setLabelProvider(new EnumContentsLabelProvider());
            viewer.setInput("");
            viewer.expandAll();
            viewer.addCheckStateListener(new ICheckStateListener() {
                public void checkStateChanged(CheckStateChangedEvent event) {
                    setPageComplete(viewer.getCheckedElements().length > 0);
                }
            });

            Composite selectionComposite = new Composite(control, SWT.NONE);
            selectionComposite.setLayout(new FillLayout());
            Button selectAllButton = new Button(selectionComposite, SWT.NONE);
            selectAllButton.setText(Messages.SelectEnumContentsPage_buttonSelectAll);
            selectAllButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    setAllChecked(true);
                }
            });
            Button deselectAllButton = new Button(selectionComposite, SWT.NONE);
            deselectAllButton.setText(Messages.SelectEnumContentsPage_buttonDeselectAll);
            deselectAllButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    setAllChecked(false);
                }
            });

            setAllChecked(true);
            setPageComplete(viewer.getCheckedElements().length > 0);
            control.pack();
            setControl(control);
        }

        private void setAllChecked(boolean checked) {
            Object[] checkedElements = checked ? contentProvider.getElements(new Object()) : new Object[0];
            viewer.setCheckedElements(checkedElements);
        }
    }

    private class EnumContentsLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element instanceof String) {
                return IpsUIPlugin.getDefault().getImage("EnumContent.gif");
            }
            return IpsUIPlugin.getDefault().getImage("IpsProject.gif");
        }

    }

    private class EnumContentsContentProvider implements ITreeContentProvider {

        public Object[] getElements(Object inputElement) {
            List<Object> objects = new ArrayList<Object>();
            try {
                objects.addAll(Arrays.asList(IpsPlugin.getDefault().getIpsModel().getIpsProjects()));
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return objects.toArray();
        }

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        public Object[] getChildren(Object parentElement) {
            List<Object> children = new LinkedList<Object>();
            try {
                if (parentElement instanceof IIpsProject) {
                    IIpsProject ipsProject = (IIpsProject)parentElement;
                    List<IEnumType> enumTypesThisProject = ipsProject.findEnumTypes(false, true);
                    for (IEnumType currentEnumType : enumTypesThisProject) {
                        String enumContentName = currentEnumType.getEnumContentName();
                        if (currentEnumType.isContainingValues() || enumContentName.length() == 0) {
                            continue;
                        }
                        IEnumContent enumContent = ipsProject.findEnumContent(currentEnumType);
                        if (enumContent == null) {
                            children.add(enumContentName + " (" + currentEnumType.getQualifiedName() + ')');
                        }
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return children.toArray();
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            try {
                if (element instanceof IIpsProject) {
                    IIpsProject ipsProject = (IIpsProject)element;
                    List<IEnumType> enumTypesThisProject = ipsProject.findEnumTypes(false, true);
                    for (IEnumType currentEnumType : enumTypesThisProject) {
                        String enumContentName = currentEnumType.getEnumContentName();
                        if (currentEnumType.isContainingValues() || enumContentName.length() == 0) {
                            continue;
                        }
                        if (ipsProject.findEnumContent(currentEnumType) == null) {
                            return true;
                        }
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return false;
        }

    }

}
