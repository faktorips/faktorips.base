/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productrelease;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;
import org.faktorips.devtools.core.productrelease.IReleaseAndDeploymentOperation;
import org.faktorips.devtools.core.productrelease.ITargetSystem;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class ProductReleaserBuilderWizardPage extends WizardPage {

    static final String SELECTED_PROJECT_SETTING = "selectedProject"; //$NON-NLS-1$

    static final String SELECTED_TARGET_SYSTEMS_SETTING = "selectedTargetSystems"; //$NON-NLS-1$

    private IIpsProject ipsProject;
    private Label currentVersionLabel;
    private Text newVersionText;

    private boolean correctVersionFormat = false;
    private CheckboxTableViewer targetSystemViewer;
    private ProductReleaseProcessor productReleaseProcessor;
    private Label versionFormatLabel;
    private Group selectTargetSystemGroup;
    private final ObservableProgressMessages observableProgressMessages;

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
        projectSelectComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                TypedSelection<IIpsProject> typedSelection = new TypedSelection<IIpsProject>(IIpsProject.class, event
                        .getSelection());
                if (typedSelection.isValid()) {
                    IIpsProject ipsProject = typedSelection.getFirstElement();
                    setIpsProject(ipsProject);
                } else {
                    setIpsProject(null);
                }
                updatePageComplete();
            }
        });

        newVersionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                if (ipsProject != null) {
                    updateMessage();
                }
                updatePageComplete();
            }
        });

        IIpsProject[] projects = new IIpsProject[0];
        try {
            projects = IpsPlugin.getDefault().getIpsModel().getIpsProductDefinitionProjects();
            ArrayList<IIpsProject> sortedProjectList = new ArrayList<IIpsProject>(Arrays.asList(projects));
            Collections.sort(sortedProjectList, new Comparator<IIpsProject>() {

                @Override
                public int compare(IIpsProject o1, IIpsProject o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });
            projectSelectComboViewer.setInput(sortedProjectList);
        } catch (CoreException e) {
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
        projectSelectComboViewer.setSelection(new StructuredSelection(ipsProject));

        setControl(pageControl);
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        String oldVersion = ""; //$NON-NLS-1$
        productReleaseProcessor = null;
        if (ipsProject != null) {
            oldVersion = ipsProject.getProperties().getVersion();
            try {
                productReleaseProcessor = new ProductReleaseProcessor(ipsProject, observableProgressMessages);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        if (currentVersionLabel != null && !currentVersionLabel.isDisposed()) {
            String latestVersionText = currentVersionLabel.getText();
            if (newVersionText != null && !newVersionText.isDisposed()) {
                if (latestVersionText.equals(newVersionText.getText())) {
                    // only overwrite the text if the label has the default value
                    newVersionText.setText(oldVersion);
                    IVersionFormat versionFormat;
                    try {
                        if (ipsProject != null) {
                            versionFormat = ipsProject.getVersionFormat();
                            if (versionFormat != null) {
                                versionFormatLabel.setText(versionFormat.getVersionFormat());
                            }
                        }
                    } catch (CoreException e) {
                        setMessage(Messages.ReleaserBuilderWizardSelectionPage_error_versionFormat + e.getMessage(),
                                DialogPage.ERROR);
                        IpsPlugin.log(e);
                    }
                }
            }
            currentVersionLabel.setText(oldVersion);
        }
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
                    List<ITargetSystem> selected = new ArrayList<ITargetSystem>();
                    String[] prevSelected = getDialogSettings().getArray(
                            SELECTED_TARGET_SYSTEMS_SETTING + "@" + getSelectedProject().getName()); //$NON-NLS-1$
                    for (String name : prevSelected) {
                        for (ITargetSystem aTargetSystem : availableTargetSystems) {
                            if (aTargetSystem.getName().equals(name)) {
                                selected.add(aTargetSystem);
                            }
                        }
                    }
                    targetSystemViewer.setCheckedElements(selected.toArray());
                }
            }
            if (targetSystemWasVisible != showTargetSystems) {
                selectTargetSystemGroup.setVisible(showTargetSystems);
            }
        }
        updateMessage();
        updatePageComplete();
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
            try {
                IVersionFormat versionFormat = ipsProject.getVersionFormat();
                if (versionFormat == null) {
                    setMessage(Messages.ReleaserBuilderWizardSelectionPage_error_couldNotDetermineFormat,
                            DialogPage.ERROR);
                    return;
                }
                correctVersionFormat = versionFormat.isCorrectVersionFormat(newVersion);
                if (!correctVersionFormat) {
                    setMessage(NLS.bind(Messages.ReleaserBuilderWizardSelectionPage_error_illegalVersion, newVersion,
                            ipsProject.getVersionFormat().getVersionFormat()), DialogPage.ERROR);
                } else if (newVersion.equals(ipsProject.getProperties().getVersion())) {
                    setMessage(Messages.ReleaserBuilderWizardSelectionPage_warning_sameVersion, DialogPage.ERROR);
                    return;
                }
            } catch (CoreException e) {
                setMessage(Messages.ReleaserBuilderWizardSelectionPage_error_versionFormat + e.getMessage(),
                        DialogPage.ERROR);
                IpsPlugin.log(e);
            }
        }
    }

    private void updatePageComplete() {
        boolean complete = ipsProject != null && correctVersionFormat && productReleaseProcessor != null
                && productReleaseProcessor.getReleaseAndDeploymentOperation() != null
                && !newVersionText.getText().equals(ipsProject.getProperties().getVersion());
        setPageComplete(complete);
    }

    public ProductReleaseProcessor getProductReleaseProcessor() {
        return productReleaseProcessor;
    }

    public String getNewVersion() {
        return newVersionText.getText();
    }

    public List<ITargetSystem> getSelectedTargetSystems() {
        List<ITargetSystem> result = new ArrayList<ITargetSystem>();
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
