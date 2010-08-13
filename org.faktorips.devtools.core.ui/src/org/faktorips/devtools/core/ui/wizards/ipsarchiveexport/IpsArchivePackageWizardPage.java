/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.ipsarchiveexport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider;
import org.faktorips.util.ArgumentCheck;

/**
 * Wizard page to select projects and package fragment root element which will be exported as ips
 * archive.
 * 
 * @author Joerg Ortmann
 */
public class IpsArchivePackageWizardPage extends WizardDataTransferPage implements ValueChangeListener, ModifyListener,
        ICheckStateListener {

    private static final String PAGE_NAME = "IpsArchivePackageWizardPage"; //$NON-NLS-1$

    // Stored widget contents
    private static final String SELECTED_TREE_ELEMENTS = PAGE_NAME + ".SELECTED_TREE_ELEMENTS"; //$NON-NLS-1$
    private static final String STORE_DESTINATION_NAMES = PAGE_NAME + ".DESTINATION_NAMES_ID"; //$NON-NLS-1$
    private static final String OPTION_INCLUDE_JAVA_SOURCES = PAGE_NAME + ".OPTION_INCLUDE_JAVA_SOURCES"; //$NON-NLS-1$
    private static final String OPTION_INCLUDE_JAVA_BINARIES = PAGE_NAME + ".OPTION_INCLUDE_JAVA_BINARIES"; //$NON-NLS-1$

    private IStructuredSelection selection;

    private CheckboxTreeViewer treeViewer;

    private Combo destinationNamesCombo;

    private Checkbox includeJavaBinaries;

    private Checkbox includeJavaSources;

    private UIToolkit toolkit = new UIToolkit(null);

    private ILabelProvider labelProvider;

    private Map<Object, Object> elementsInTree = new HashMap<Object, Object>();

    private class IpsPackageFragmentRootTreeViewer extends ContainerCheckedTreeViewer {
        public IpsPackageFragmentRootTreeViewer(Composite parent) {
            super(parent);
        }
    }

    public IpsArchivePackageWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME);
        this.selection = selection;
        setTitle(Messages.IpsArchivePackageWizardPage_Title);
        setDescription(Messages.IpsArchivePackageWizardPage_Description_EnterDestination);
        setPageComplete(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        ITreeContentProvider treeContentProvider = new StandardJavaElementContentProvider() {
            private Object[] EMPTY_ARRAY = new Object[0];

            @Override
            public boolean hasChildren(Object element) {
                // prevent the + from being shown in front of packages
                return !(element instanceof IPackageFragmentRoot) && super.hasChildren(element);
            }

            @Override
            public Object[] getChildren(Object element) {
                // show only ips projects and ips package fragment roots
                if (element instanceof IJavaModel) {
                    Object[] children = super.getChildren(element);
                    List<Object> result = new ArrayList<Object>(children.length);
                    for (Object element2 : children) {
                        if (element2 instanceof IJavaProject) {
                            IProject project = ((IJavaProject)element2).getProject();
                            try {
                                if (project.hasNature(IIpsProject.NATURE_ID)) {
                                    IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(
                                            project.getName());
                                    elementsInTree.put(project, ipsProject);
                                    result.add(ipsProject);
                                }
                            } catch (CoreException e) {
                                IpsPlugin.logAndShowErrorDialog(e);
                            }
                        }
                    }
                    return result.toArray();
                } else if (element instanceof IIpsProject) {
                    // store elements for product definition view
                    elementsInTree.put(element, element);
                    // store to be mapped objects
                    try {
                        IIpsPackageFragmentRoot[] roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
                        List<Object> rootResult = new ArrayList<Object>(roots.length);
                        for (IIpsPackageFragmentRoot root : roots) {
                            if (root.getIpsArchive() != null) {
                                continue;
                            }
                            rootResult.add(root);
                            // store elements for product definition view
                            elementsInTree.put(root, root);
                            // store to be mapped objects
                            elementsInTree.put(root.getEnclosingResource(), root);
                        }
                        return rootResult.toArray();
                    } catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                }
                return EMPTY_ARRAY;
            }
        };

        treeViewer = new IpsPackageFragmentRootTreeViewer(composite);
        treeViewer.setContentProvider(treeContentProvider);
        labelProvider = new ModelLabelProvider();
        treeViewer.setLabelProvider(labelProvider);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()));
        treeViewer.expandAll();
        treeViewer.addCheckStateListener(this);
        List<Object> selectedObjects = new ArrayList<Object>();
        for (Iterator<Object> iter = selection.iterator(); iter.hasNext();) {
            Object objectInTree = findCorrespondingObjectInTree(iter.next());
            if (objectInTree != null) {
                selectedObjects.add(objectInTree);
            }
        }
        if (selectedObjects.size() == 0) {
            setMessage(Messages.IpsArchivePackageWizardPage_WarningNoIpsProjectSelected, IMessageProvider.WARNING);
        } else {
            treeViewer.setCheckedElements(selectedObjects.toArray());
        }
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setMessage(null);
            }
        });

        includeJavaSources = toolkit.createCheckbox(composite,
                Messages.IpsArchivePackageWizardPage_Label_IncludeJavaSources);
        includeJavaBinaries = toolkit.createCheckbox(composite,
                Messages.IpsArchivePackageWizardPage_Label_IncludeJavaBinaries);

        toolkit.createLabel(composite, ""); //$NON-NLS-1$

        createDestinationGroup(composite);

        restoreWidgetValues();

        setControl(composite);
    }

    private Object findCorrespondingObjectInTree(Object selectedObject) {
        Object objectInTree = elementsInTree.get(selectedObject);
        if (objectInTree != null) {
            return objectInTree;
        }
        if (selectedObject instanceof IIpsElement) {
            return ((IIpsElement)selectedObject).getIpsProject();
        }
        if (selectedObject instanceof IProject) {
            return null;
        }
        if (selectedObject instanceof IResource) {
            return findCorrespondingObjectInTree(((IResource)selectedObject).getProject());
        }
        return null;
    }

    private void createDestinationGroup(Composite parent) {
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));

        new Label(destinationSelectionGroup, SWT.NONE).setText(Messages.IpsArchivePackageWizardPage_Label_Target);

        // destination name entry field
        destinationNamesCombo = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        ComboField destinationNameComboField = new ComboField(destinationNamesCombo);
        destinationNameComboField.addChangeListener(this);
        destinationNamesCombo.addModifyListener(this);
        destinationNamesCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // destination browse button
        Button destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        destinationBrowseButton.setText(Messages.IpsArchivePackageWizardPage_Label_Browse);
        destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDestinationBrowseButtonPressed();
            }
        });
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from
     */
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*." + IIpsArchiveEntry.FILE_EXTENSION, "*.zip", "*.jar" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
            dialog.setFileName(currentSourceString.substring(lastSeparatorIndex + 1, currentSourceString.length()));
        } else {
            dialog.setFileName(currentSourceString);
        }
        String selectedFileName = dialog.open();
        if (selectedFileName != null) {
            destinationNamesCombo.setText(selectedFileName);
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        canFinish();
    }

    @Override
    public void modifyText(ModifyEvent e) {
        canFinish();
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        canFinish();
    }

    public Object[] getCheckedElements() {
        return treeViewer.getCheckedElements();
    }

    public File getDestinationFile() {
        return new Path(getDestinationValue()).toFile();
    }

    public boolean isInclJavaBinaries() {
        return includeJavaBinaries.isChecked();
    }

    public boolean isInclJavaSources() {
        return includeJavaSources.isChecked();
    }

    /**
     * Answer the contents of the destination specification widget. If this value does not have the
     * required suffix then add it first.
     */
    private String getDestinationValue() {
        String destinationText = destinationNamesCombo.getText().trim();
        if (destinationText.indexOf('.') < 0) {
            destinationText += "." + IIpsArchiveEntry.FILE_EXTENSION; //$NON-NLS-1$
        }
        return destinationText;
    }

    /**
     * Sets if the page could be finished or not. The page could be finished if an archive name is
     * given and a selection is set.
     */
    private void canFinish() {
        boolean canFinish = true;
        String target = destinationNamesCombo.getText().trim();
        Path destPath = new Path(target);

        if (getCheckedElements().length == 0) {
            canFinish = false;
            setDescription(Messages.IpsArchivePackageWizardPage_Description_DefineWhichResource);
        }

        if (canFinish && (StringUtils.isEmpty(target) || !destPath.isValidPath(target))) {
            canFinish = false;
            setDescription(Messages.IpsArchivePackageWizardPage_Description_EnterValidDestination);
        }

        setPageComplete(canFinish);
        if (canFinish) {
            setDescription(Messages.IpsArchivePackageWizardPage_Description);
        }
    }

    @Override
    public void setDescription(String description) {
        // to avoid flickering set description only if the description changed
        ArgumentCheck.notNull(description);
        if (!description.equals(getDescription())) {
            super.setDescription(description);
        }
    }

    @Override
    public void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }

        // restore previous selected elements only if the selection which was given when creating
        // the
        // wizard is empty
        if (selection == null || selection.isEmpty()) {
            String[] selectedElements = settings.getArray(SELECTED_TREE_ELEMENTS);
            List<Object> prevSelectedObject = new ArrayList<Object>(selectedElements.length);
            for (String selectedElement : selectedElements) {
                for (Object objectInTree : elementsInTree.values()) {
                    if (labelProvider.getText(objectInTree).equals(selectedElement)) {
                        prevSelectedObject.add(objectInTree);
                        continue;
                    }

                }
            }
            treeViewer.setCheckedElements(prevSelectedObject.toArray());
        }

        // restore previous entered destination
        destinationNamesCombo.setText(""); //$NON-NLS-1$
        String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES);
        if (directoryNames == null) {
            return; // ie.- no settings stored
        }
        if (!destinationNamesCombo.getText().equals(directoryNames[0])) {
            destinationNamesCombo.add(destinationNamesCombo.getText());
        }
        for (String directoryName : directoryNames) {
            destinationNamesCombo.add(directoryName);
        }

        // restore options
        includeJavaSources.setChecked(settings.getBoolean(OPTION_INCLUDE_JAVA_SOURCES));
        includeJavaBinaries.setChecked(settings.getBoolean(OPTION_INCLUDE_JAVA_BINARIES));
    }

    @Override
    public void saveWidgetValues() {
        // store selected elements
        Object[] checkedElements = treeViewer.getCheckedElements();
        String[] selectedElements = new String[checkedElements.length];
        for (int i = 0; i < checkedElements.length; i++) {
            selectedElements[i] = labelProvider.getText(checkedElements[i]);
        }
        getDialogSettings().put(SELECTED_TREE_ELEMENTS, selectedElements);

        // store destination history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }
            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES, directoryNames);
        }
        // options
        settings.put(OPTION_INCLUDE_JAVA_SOURCES, isInclJavaSources());
        settings.put(OPTION_INCLUDE_JAVA_BINARIES, isInclJavaBinaries());
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // Nothing to do
    }

}
