/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productrelease;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.eclipse.productrelease.IReleaseAndDeploymentOperation;
import org.faktorips.devtools.model.eclipse.productrelease.ITargetSystem;
import org.faktorips.devtools.model.eclipse.productrelease.ObservableProgressMessages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IVersionFormat;

public class ProductReleaserBuilderWizardPage extends WizardPage {

    static final String SELECTED_PROJECT_SETTING = "selectedProject"; //$NON-NLS-1$
    static final String SELECTED_TARGET_SYSTEMS_SETTING = "selectedTargetSystems"; //$NON-NLS-1$

    private static final String EXTENSION_ATTRIBUTE_VERSION_MUST_CHANGE = "versionMustChange"; //$NON-NLS-1$

    private final ObservableProgressMessages observableProgressMessages;

    private IIpsProject ipsProject;
    private Label currentVersionLabel;
    private Text newVersionText;

    private boolean correctVersionFormat = false;
    private boolean correctNewVersion = false;
    private CheckboxTableViewer targetSystemViewer;
    private ProductReleaseProcessor productReleaseProcessor;
    private Label versionFormatLabel;
    private Group selectTargetSystemGroup;
    private boolean versionChangeRequired;

    protected ProductReleaserBuilderWizardPage(ObservableProgressMessages observableProgressMessages) {
        super(Messages.ReleaserBuilderWizardSelectionPage_title, Messages.ReleaserBuilderWizardSelectionPage_title,
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/DeploymentWizard.png")); //$NON-NLS-1$
        this.observableProgressMessages = observableProgressMessages;
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, true));
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        pageControl.setLayoutData(data);

        Group selectProjectGroup = toolkit.createGroup(pageControl,
                Messages.ReleaserBuilderWizardSelectionPage_group_project);
        Composite selectProjectControl = toolkit.createLabelEditColumnComposite(selectProjectGroup);

        toolkit.createLabel(selectProjectControl, Messages.ReleaserBuilderWizardSelectionPage_select_project);

        Combo projectSelectCombo = toolkit.createCombo(selectProjectControl);
        ComboViewer projectSelectComboViewer = new ComboViewer(projectSelectCombo);

        Group selectVersionGroup = toolkit.createGroup(pageControl,
                Messages.ReleaserBuilderWizardSelectionPage_group_version);
        Composite selectVersionControl = toolkit.createLabelEditColumnComposite(selectVersionGroup);

        toolkit.createLabel(selectVersionControl, Messages.ReleaserBuilderWizardSelectionPage_latest_version);
        currentVersionLabel = toolkit.createLabel(selectVersionControl, ""); //$NON-NLS-1$

        toolkit.createLabel(selectVersionControl, Messages.ReleaserBuilderWizardSelectionPage_new_version);
        newVersionText = new Text(selectVersionControl, SWT.SINGLE | SWT.BORDER);
        newVersionText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(selectVersionControl, ""); //$NON-NLS-1$
        versionFormatLabel = toolkit.createLabel(selectVersionControl, ""); //$NON-NLS-1$

        selectTargetSystemGroup = toolkit.createGroup(pageControl,
                Messages.ReleaserBuilderWizardSelectionPage_group_targetsystem);
        selectTargetSystemGroup.setVisible(false);
        Composite selectTargetSystemControl = toolkit.createLabelEditColumnComposite(selectTargetSystemGroup);
        // Label targetSystemLabel = toolkit.createLabel(selectTargetSystemControl, "Select:");
        // targetSystemLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        Table table = toolkit.createTable(selectTargetSystemControl, SWT.CHECK | SWT.BORDER);
        GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, true);
        layoutData.minimumHeight = 90;
        table.setLayoutData(layoutData);

        targetSystemViewer = new CheckboxTableViewer(table);
        targetSystemViewer.setContentProvider(new ArrayContentProvider());
        targetSystemViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ITargetSystem) {
                    ITargetSystem targetSystem = (ITargetSystem)element;
                    return targetSystem.getName();
                }
                return super.getText(element);
            }
        });

        targetSystemViewer.setInput(new String[] {});

        projectSelectComboViewer.setContentProvider(new ArrayContentProvider());
        projectSelectComboViewer.addSelectionChangedListener(event -> {
            TypedSelection<IIpsProject> typedSelection = new TypedSelection<>(IIpsProject.class,
                    event.getSelection());
            if (typedSelection.isValid()) {
                IIpsProject selectedProject = typedSelection.getFirstElement();
                updateIpsProject(selectedProject);
            } else {
                updateIpsProject(null);
            }
        });

        newVersionText.addModifyListener($ -> {
            if (ipsProject != null) {
                updateMessage();
            }
            updatePageComplete();
        });

        IIpsProject[] projects = {};
        try {
            projects = IIpsModel.get().getIpsProductDefinitionProjects();
            ArrayList<IIpsProject> sortedProjectList = new ArrayList<>(Arrays.asList(projects));
            Collections.sort(sortedProjectList, Comparator.comparing(p -> p.getName().toLowerCase()));
            projectSelectComboViewer.setInput(sortedProjectList);
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }

        if (ipsProject == null) {
            String lastProject = getDialogSettings().get(SELECTED_PROJECT_SETTING);
            for (IIpsProject aProject : projects) {
                if (aProject.getName().equals(lastProject)) {
                    ipsProject = aProject;
                }
            }
        }
        if (ipsProject == null && projects.length > 0) {
            ipsProject = projects[0];
        }
        if (ipsProject != null) {
            projectSelectComboViewer.setSelection(new StructuredSelection(ipsProject));
        }

        setControl(pageControl);
    }

    public void updateIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        String oldVersion = ""; //$NON-NLS-1$
        productReleaseProcessor = null;
        if (ipsProject != null) {
            oldVersion = ipsProject.getVersionProvider().getProjectVersion().asString();
            try {
                productReleaseProcessor = new ProductReleaseProcessor(ipsProject, observableProgressMessages);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        versionChangeRequired = isVersionChangeRequired();
        updateVersion(oldVersion, ipsProject);
        updateTargetSystem(ipsProject);
        updateMessage();
        updatePageComplete();
    }

    private void updateVersion(String oldVersion, IIpsProject ipsProject) {
        if (currentVersionLabel != null && !currentVersionLabel.isDisposed()) {
            String latestVersionText = currentVersionLabel.getText();
            if (newVersionText != null && !newVersionText.isDisposed()) {
                if (latestVersionText.equals(newVersionText.getText())) {
                    // only overwrite the text if the label has the default value
                    newVersionText.setText(oldVersion);
                    IVersionFormat versionFormat;
                    if (ipsProject != null) {
                        versionFormat = ipsProject.getVersionProvider();
                        if (versionFormat != null) {
                            versionFormatLabel.setText(versionFormat.getVersionFormat());
                        }
                    }
                }
            }
            currentVersionLabel.setText(oldVersion);
        }
    }

    private void updateTargetSystem(IIpsProject ipsProject) {
        if (targetSystemViewer != null && !targetSystemViewer.getTable().isDisposed()) {
            boolean targetSystemWasVisible = selectTargetSystemGroup.getVisible();
            boolean showTargetSystems = false;
            if (productReleaseProcessor != null) {
                IReleaseAndDeploymentOperation releaseAndDeploymentOperation = productReleaseProcessor
                        .getReleaseAndDeploymentOperation();
                if (releaseAndDeploymentOperation != null) {
                    List<ITargetSystem> availableTargetSystems = releaseAndDeploymentOperation
                            .getAvailableTargetSystems(ipsProject);
                    if (!availableTargetSystems.isEmpty()) {
                        targetSystemViewer.setInput(availableTargetSystems);
                        showTargetSystems = true;
                    }
                    List<ITargetSystem> selected = updateSelectedTargetSystem(availableTargetSystems);
                    targetSystemViewer.setCheckedElements(selected.toArray());
                }
            }
            if (targetSystemWasVisible != showTargetSystems) {
                selectTargetSystemGroup.setVisible(showTargetSystems);
            }
        }
    }

    private List<ITargetSystem> updateSelectedTargetSystem(List<ITargetSystem> availableTargetSystems) {
        List<ITargetSystem> selected = new ArrayList<>();
        String[] prevSelected = getDialogSettings()
                .getArray(SELECTED_TARGET_SYSTEMS_SETTING + "@" + getSelectedProject().getName()); //$NON-NLS-1$
        if (prevSelected != null) {
            for (String name : prevSelected) {
                for (ITargetSystem aTargetSystem : availableTargetSystems) {
                    if (aTargetSystem.getName().equals(name)) {
                        selected.add(aTargetSystem);
                    }
                }
            }
        }
        return selected;
    }

    private void updateMessage() {
        setMessage("", DialogPage.NONE); //$NON-NLS-1$

        if (ipsProject == null) {
            setMessage(Messages.ReleaserBuilderWizardSelectionPage_info_selectProject, DialogPage.INFORMATION);
            return;
        } else if (productReleaseProcessor == null) {
            setMessage(Messages.ReleaserBuilderWizardSelectionPage_error_noDeploymentExtension, DialogPage.ERROR);
            return;
        }
        if (newVersionText != null && !newVersionText.isDisposed()) {
            String newVersion = newVersionText.getText();
            IVersionFormat versionFormat = ipsProject.getVersionProvider();
            if (versionFormat == null) {
                setMessage(Messages.ReleaserBuilderWizardSelectionPage_error_couldNotDetermineFormat, DialogPage.ERROR);
                return;
            }

            correctVersionFormat = versionFormat.isCorrectVersionFormat(newVersion);
            if (!correctVersionFormat) {
                setMessage(NLS.bind(Messages.ReleaserBuilderWizardSelectionPage_error_illegalVersion, newVersion,
                        ipsProject.getVersionProvider().getVersionFormat()), DialogPage.ERROR);
            }

            correctNewVersion = !versionChangeRequired
                    || !newVersion.equals(ipsProject.getVersionProvider().getProjectVersion().asString());
            if (!correctNewVersion) {
                setMessage(Messages.ReleaserBuilderWizardSelectionPage_warning_sameVersion, DialogPage.ERROR);
            }
        }
    }

    private void updatePageComplete() {
        boolean complete = ipsProject != null && correctVersionFormat && isCorrectReleaseProcessorSet()
                && correctNewVersion;
        setPageComplete(complete);
    }

    private boolean isVersionChangeRequired() {
        IConfigurationElement configuration = ProductReleaseProcessor.getReleaseExtensionElement(ipsProject);
        if (configuration == null) {
            return true;
        }

        String versionMustChange = configuration.getAttribute(EXTENSION_ATTRIBUTE_VERSION_MUST_CHANGE);
        if (versionMustChange != null) {
            return Boolean.parseBoolean(versionMustChange);
        } else {
            return true;
        }
    }

    private boolean isCorrectReleaseProcessorSet() {
        return productReleaseProcessor != null && productReleaseProcessor.getReleaseAndDeploymentOperation() != null;
    }

    public ProductReleaseProcessor getProductReleaseProcessor() {
        return productReleaseProcessor;
    }

    public String getNewVersion() {
        return newVersionText.getText();
    }

    public List<ITargetSystem> getSelectedTargetSystems() {
        List<ITargetSystem> result = new ArrayList<>();
        for (Object checkedItem : targetSystemViewer.getCheckedElements()) {
            if (checkedItem instanceof ITargetSystem) {
                ITargetSystem targetSystem = (ITargetSystem)checkedItem;
                result.add(targetSystem);
            }
        }
        return result;
    }

    public IIpsProject getSelectedProject() {
        return ipsProject;
    }

}
