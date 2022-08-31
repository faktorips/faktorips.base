/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.ui.wizards;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;

public class HtmlExportWizardPage extends WizardDataTransferPage implements ValueChangeListener, ModifyListener,
        ICheckStateListener {

    private static final String PAGE_NAME = "IpsProjectHtmlExportWizardPage"; //$NON-NLS-1$

    private static final String STORE_DESTINATION_NAMES = PAGE_NAME + ".DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private IStructuredSelection selection;

    private UIToolkit toolkit = new UIToolkit(null);
    private Combo destinationNamesCombo;
    private Combo supportedLanguageCombo;
    private Combo ipsProjectsCombo;

    private Checkbox showValidationErrorsCheckBox;
    private Checkbox showObjectPartsInTableCheckBox;

    private CheckboxTreeViewer objectTypesTreeViewer;

    private IIpsProject[] ipsProjects;

    private StringValueComboField destinationNameComboField;

    protected HtmlExportWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME);
        this.selection = selection;
        setPageComplete(false);

        initIpsProjects();
    }

    private void initIpsProjects() {
        ipsProjects = IIpsModel.get().getIpsProjects();
    }

    private void updateSelectedProject() {
        IIpsProject project = getSelectedIpsProject();

        if (project == null) {
            setTitle(Messages.HtmlExportWizardPage_htmlExport);
            setMessage(Messages.HtmlExportWizardPage_noProjectSelected, IMessageProvider.WARNING);
            return;
        }

        setMessage(null);

        setTitle(Messages.HtmlExportWizardPage_projectName + project.getName());
        setDescription(Messages.HtmlExportWizardPage_description);

        updateSupportedLanguageGroup();

        destinationNamesCombo.setFocus();
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // nothing to do
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));

        Composite grid = new Composite(composite, SWT.NONE);
        grid.setLayout(new GridLayout(2, false));
        grid.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        createIpsProjectsCombo(grid);

        createDestinationGroup(grid);

        createSupportedLanguageGroup(grid);

        createIpsObjectTypesGroup(composite);

        showValidationErrorsCheckBox = toolkit.createCheckbox(composite,
                Messages.HtmlExportWizardPage_showValidationErrors);

        showObjectPartsInTableCheckBox = toolkit.createCheckbox(composite,
                Messages.HtmlExportWizardPage_inheritedObjectPartsWithinTable);

        restoreWidgetValues();

        setControl(composite);

        updateSelectedProject();

    }

    private void createIpsObjectTypesGroup(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.HtmlExportWizardPage_objectTypes);

        objectTypesTreeViewer = new ContainerCheckedTreeViewer(parent);
        objectTypesTreeViewer.setUseHashlookup(false);
        objectTypesTreeViewer.setContentProvider(new IpsObjectTreeContentProvider());
        objectTypesTreeViewer.setLabelProvider(new IpsObjectLabelProvider());
        objectTypesTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        objectTypesTreeViewer.setInput(IpsObjectTypeTreeViewBaseNodes.ROOT);
        objectTypesTreeViewer.setCheckedElements(IIpsModel.get().getIpsObjectTypes());
        // objectTypesTreeViewer.expandAll();

    }

    private void createIpsProjectsCombo(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.HtmlExportWizardPage_project);

        ipsProjectsCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        ipsProjectsCombo.addModifyListener(this);
        ipsProjectsCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ipsProjectsCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleIpsProjectSelectionChanged();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        for (IIpsProject ipsProject : ipsProjects) {
            ipsProjectsCombo.add(ipsProject.getName());
            if (ipsProject.equals(getIpsProject(selection))) {
                ipsProjectsCombo.select(ipsProjectsCombo.getItemCount() - 1);
            }
        }
    }

    protected void handleIpsProjectSelectionChanged() {
        updateSelectedProject();
    }

    private void createSupportedLanguageGroup(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.HtmlExportWizardPage_supportedLanguage);

        supportedLanguageCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        supportedLanguageCombo.addModifyListener(this);
        supportedLanguageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        updateSupportedLanguageGroup();
    }

    private void updateSupportedLanguageGroup() {
        IIpsProject ipsProject = getSelectedIpsProject();
        if (ipsProject == null) {
            return;
        }
        supportedLanguageCombo.removeAll();
        for (ISupportedLanguage iSupportedLanguage : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
            supportedLanguageCombo.add(iSupportedLanguage.getLanguageName());
            if (iSupportedLanguage.isDefaultLanguage()) {
                supportedLanguageCombo.select(supportedLanguageCombo.getItemCount() - 1);
            }
        }
    }

    private void createDestinationGroup(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.HtmlExportWizardPage_destination);

        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));

        // destination name entry field
        destinationNamesCombo = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        destinationNameComboField = new StringValueComboField(destinationNamesCombo);
        destinationNameComboField.addChangeListener(this);
        destinationNameComboField.setText(getDefaultDestinationDirectory());

        destinationNamesCombo.addModifyListener(this);
        destinationNamesCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // destination browse button
        Button destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        destinationBrowseButton.setText(Messages.HtmlExportWizardPage_browse);
        destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDestinationBrowseButtonPressed();
            }
        });
    }

    /**
     * Open an appropriate destination browser
     */
    protected void handleDestinationBrowseButtonPressed() {
        DirectoryDialog directoryDialog = new DirectoryDialog(getContainer().getShell());
        directoryDialog.setText(Messages.HtmlExportWizardPage_directoryDialogText);
        directoryDialog.setFilterPath(getSelectedIpsProject().getProject().getLocation() + File.separator + "html"); //$NON-NLS-1$

        String selectedDirectoryName = directoryDialog.open();
        if (selectedDirectoryName != null) {
            destinationNamesCombo.setText(selectedDirectoryName);
        }
    }

    protected IIpsProject getSelectedIpsProject() {
        if (ipsProjectsCombo.getSelectionIndex() == -1) {
            return null;
        }
        return ipsProjects[ipsProjectsCombo.getSelectionIndex()];
    }

    private IProject getProject(IStructuredSelection strSelection) {
        Object selected = strSelection.getFirstElement();
        if (selected instanceof IResource) {
            return ((IResource)selected).getProject();
        }
        if (selected instanceof IIpsElement) {
            return ((IIpsElement)selected).getIpsProject().getProject().unwrap();
        }
        if (selected instanceof IJavaElement) {
            return ((IJavaElement)selected).getJavaProject().getProject();
        }
        return null;
    }

    public IIpsProject getIpsProject(IStructuredSelection selection) {
        IProject project = getProject(selection);
        if (project == null) {
            return null;
        }
        IIpsModel ipsModel = IIpsModel.get();
        return ipsModel.getIpsProject(wrap(project.getProject()).as(AProject.class));
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

    public String getDestinationDirectory() {
        return destinationNamesCombo.getText().trim();
    }

    public IpsObjectType[] getSelectedIpsObjectTypes() {
        List<IpsObjectType> checkedElements = new ArrayList<>();
        for (Object checkedObject : objectTypesTreeViewer.getCheckedElements()) {
            if (checkedObject instanceof IpsObjectType) {
                checkedElements.add((IpsObjectType)checkedObject);
            }
        }
        return checkedElements.toArray(new IpsObjectType[checkedElements.size()]);
    }

    public boolean getShowValidationErrors() {
        return showValidationErrorsCheckBox.isChecked();
    }

    public boolean getShowObjectPartsInTableCheckBox() {
        return showObjectPartsInTableCheckBox.isChecked();
    }

    public Locale getSupportedLanguage() {
        if (supportedLanguageCombo == null) {
            return null;
        }
        for (ISupportedLanguage iSupportedLanguage : getSelectedIpsProject().getReadOnlyProperties()
                .getSupportedLanguages()) {
            if (supportedLanguageCombo.getText().equals(iSupportedLanguage.getLanguageName())) {
                return iSupportedLanguage.getLocale();
            }
        }
        return null;
    }

    private String getDefaultDestinationDirectory() {
        IIpsProject firstElement = getSelectedIpsProject();
        if (firstElement == null) {
            return ""; //$NON-NLS-1$
        }
        return firstElement.getProject().getLocation().toString() + File.separator + "html"; //$NON-NLS-1$
    }

    public boolean isIncludingReferencedProjects() {
        return true;
    }

    private void canFinish() {
        if ((getSelectedIpsProject() == null) || (getSupportedLanguage() == null)) {
            setPageComplete(false);
            return;
        }

        if (StringUtils.isNotBlank(getDestinationDirectory())) {
            setPageComplete(true);
            return;
        }
        setPageComplete(false);
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }

        // restore previous entered destination
        destinationNamesCombo.setText(""); //$NON-NLS-1$
        String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES);
        if (directoryNames == null) {
            // ie.- no settings stored
            return;
        }
        if (!destinationNamesCombo.getText().equals(directoryNames[0])) {
            destinationNamesCombo.add(destinationNamesCombo.getText());
        }
        for (String directoryName : directoryNames) {
            destinationNamesCombo.add(directoryName);
        }

    }

    @Override
    protected void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }

        // store destination history
        String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES);
        if (directoryNames == null) {
            directoryNames = new String[0];
        }
        directoryNames = addToHistory(directoryNames, getDestinationDirectory());
        settings.put(STORE_DESTINATION_NAMES, directoryNames);
    }

    private static final class IpsObjectLabelProvider implements ILabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
            // nothing to do
        }

        @Override
        public void dispose() {
            // nothing to dispose
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // nothing to do
        }

        @Override
        public Image getImage(Object element) {
            if (!(element instanceof IpsObjectType)) {
                return null;
            }
            IpsObjectType ipsObjectType = (IpsObjectType)element;
            return IpsUIPlugin.getImageHandling().getImage(IIpsDecorators.getDefaultImageDescriptor(ipsObjectType));
        }

        @Override
        public String getText(Object element) {
            if (element == IpsObjectTypeTreeViewBaseNodes.POLICY) {
                return Messages.HtmlExportWizardPage_policy;
            }
            if (element == IpsObjectTypeTreeViewBaseNodes.PRODUCT) {
                return Messages.HtmlExportWizardPage_product;
            }
            if (element instanceof IpsObjectType) {
                IpsObjectType ipsObjectType = (IpsObjectType)element;
                return ipsObjectType.getDisplayNamePlural();
            }
            return null;
        }

    }

    private static final class IpsObjectTreeContentProvider implements ITreeContentProvider {

        private static final Object[] EMPTY_ARRAY = {};

        private static final IpsObjectTypeTreeViewBaseNodes[] OBJECT_TYPES_ARRAY = {
                IpsObjectTypeTreeViewBaseNodes.POLICY, IpsObjectTypeTreeViewBaseNodes.PRODUCT };

        private IpsObjectType[] ipsObjectTypesPolicy;
        private IpsObjectType[] ipsObjectTypesProduct;

        public IpsObjectTreeContentProvider() {
            createIpsObjectTypesArrays();
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement == IpsObjectTypeTreeViewBaseNodes.ROOT) {
                return OBJECT_TYPES_ARRAY;
            }
            if (parentElement == IpsObjectTypeTreeViewBaseNodes.POLICY) {
                return ipsObjectTypesPolicy;
            }
            if (parentElement == IpsObjectTypeTreeViewBaseNodes.PRODUCT) {
                return ipsObjectTypesProduct;
            }

            return EMPTY_ARRAY;
        }

        private void createIpsObjectTypesArrays() {
            IpsObjectType[] ipsObjectTypes = IIpsModel.get().getIpsObjectTypes();
            List<IpsObjectType> ipsObjectTypesPolicyList = new ArrayList<>();
            List<IpsObjectType> ipsObjectTypesProductList = new ArrayList<>();

            for (IpsObjectType ipsObjectType : ipsObjectTypes) {
                if (ipsObjectType.isProductDefinitionType()) {
                    ipsObjectTypesProductList.add(ipsObjectType);
                } else {
                    ipsObjectTypesPolicyList.add(ipsObjectType);
                }
            }
            ipsObjectTypesPolicy = ipsObjectTypesPolicyList.toArray(new IpsObjectType[ipsObjectTypesPolicyList
                    .size()]);
            ipsObjectTypesProduct = ipsObjectTypesProductList.toArray(new IpsObjectType[ipsObjectTypesProductList
                    .size()]);
        }

        @Override
        public Object getParent(Object element) {
            if (element == IpsObjectTypeTreeViewBaseNodes.ROOT) {
                return null;
            }
            if (element instanceof IpsObjectTypeTreeViewBaseNodes) {
                return IpsObjectTypeTreeViewBaseNodes.ROOT;
            }
            if (!(element instanceof IpsObjectType)) {
                return null;
            }
            IpsObjectType type = (IpsObjectType)element;
            return type.isProductDefinitionType() ? IpsObjectTypeTreeViewBaseNodes.PRODUCT
                    : IpsObjectTypeTreeViewBaseNodes.POLICY;
        }

        @Override
        public boolean hasChildren(Object element) {
            return element instanceof IpsObjectTypeTreeViewBaseNodes;
        }
    }

    private enum IpsObjectTypeTreeViewBaseNodes {
        ROOT,
        POLICY,
        PRODUCT
    }

}
