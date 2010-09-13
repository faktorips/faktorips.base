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

package org.faktorips.devtools.core.ui.wizards.deployment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.deployment.IDeploymentOperation;
import org.faktorips.devtools.core.deployment.ReleaseAndDeploymentOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class ReleaserBuilderWizardSelectionPage extends WizardPage {

    private IIpsProject ipsProject;
    private Label latestVersionLabel;
    private Text newVersionText;

    private boolean correctVersionFormat = false;
    private TableViewer targetSystemViewer;
    private ReleaseAndDeploymentOperation releaseAndDeploymentOperation;

    protected ReleaserBuilderWizardSelectionPage() {
        super("Releaser Builder");
        setTitle("Releaser Builder");
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, true));
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        pageControl.setLayoutData(data);

        Group selectProjectGroup = toolkit.createGroup(pageControl, "Project");
        Composite selectProjectControl = toolkit.createLabelEditColumnComposite(selectProjectGroup);

        toolkit.createLabel(selectProjectControl, "Product Definition Project:");

        Combo projectSelectCombo = toolkit.createCombo(selectProjectControl);
        ComboViewer projectSelectComboViewer = new ComboViewer(projectSelectCombo);

        Group selectVersionGroup = toolkit.createGroup(pageControl, "Version");
        Composite selectVersionControl = toolkit.createLabelEditColumnComposite(selectVersionGroup);

        toolkit.createLabel(selectVersionControl, "Latest Version:");
        latestVersionLabel = toolkit.createLabel(selectVersionControl, "");

        toolkit.createLabel(selectVersionControl, "New Version:");
        newVersionText = new Text(selectVersionControl, SWT.SINGLE | SWT.BORDER);
        newVersionText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Group selectTargetSystemGroup = toolkit.createGroup(pageControl, "Target Systems");
        Composite selectTargetSystemControl = toolkit.createLabelEditColumnComposite(selectTargetSystemGroup);
        // Label targetSystemLabel = toolkit.createLabel(selectTargetSystemControl, "Select:");
        // targetSystemLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        Table table = toolkit.createTable(selectTargetSystemControl, SWT.CHECK | SWT.BORDER);
        table.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
        targetSystemViewer = new TableViewer(table);
        targetSystemViewer.setContentProvider(new ArrayContentProvider());

        // TODO get list from extension
        targetSystemViewer.setInput(new String[] { "Test", "Praxis", "Supi" });

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
                    String newVersion = newVersionText.getText();
                    try {
                        IVersionFormat versionFormat = ipsProject.getVersionFormat();
                        if (versionFormat == null) {
                            setMessage("Could not determine version format", DialogPage.ERROR);
                            return;
                        }
                        correctVersionFormat = versionFormat.isCorrectVersionFormat(newVersion);
                        if (!correctVersionFormat) {
                            setMessage("The format of version \"" + newVersion + "\" is incorrect. Format: "
                                    + ipsProject.getVersionFormat().getVersionFormat(), DialogPage.ERROR);
                        }
                        if (newVersion.equals(ipsProject.getProperties().getVersion())) {
                            setMessage("The new version is the same as the last one", DialogPage.WARNING);
                        } else {
                            setMessage("", DialogPage.NONE);
                        }
                    } catch (CoreException e) {
                        setMessage("Could not determine version format: " + e.getMessage(), DialogPage.ERROR);
                    }

                }
                updatePageComplete();
            }
        });

        IIpsProject[] projects;
        try {
            projects = IpsPlugin.getDefault().getIpsModel().getIpsProductDefinitionProjects();
            projectSelectComboViewer.setInput(projects);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (ipsProject != null) {
            projectSelectComboViewer.setSelection(new StructuredSelection(ipsProject));
        }

        setControl(pageControl);
    }

    public void setIpsProject(IIpsProject ipsProject) {
        setMessage("", DialogPage.NONE);
        this.ipsProject = ipsProject;
        String oldVersion = "";
        releaseAndDeploymentOperation = null;
        if (ipsProject != null) {
            oldVersion = ipsProject.getProperties().getVersion();
            try {
                releaseAndDeploymentOperation = new ReleaseAndDeploymentOperation(ipsProject);
            } catch (CoreException e) {
                setMessage("No deployment Extension found for this project", DialogPage.ERROR);
            }
        }
        if (latestVersionLabel != null && !latestVersionLabel.isDisposed()) {
            String nextVersion = latestVersionLabel.getText();
            if (newVersionText != null && !newVersionText.isDisposed()) {
                if (nextVersion.equals(newVersionText.getText())) {
                    // only overwrite the text if the label has the default value
                    newVersionText.setText(nextVersion);
                }
            }
            latestVersionLabel.setText(oldVersion);
        }
        if (targetSystemViewer != null && !targetSystemViewer.getTable().isDisposed()) {
            if (releaseAndDeploymentOperation != null) {
                IDeploymentOperation deploymentOperation = releaseAndDeploymentOperation.getDeploymentOperation();
                if (deploymentOperation != null) {
                    targetSystemViewer.setInput(deploymentOperation.getAvailableTargetSystems());
                }
            } else {
                targetSystemViewer.setInput(new String[0]);
            }
        }

        updatePageComplete();
    }

    private void updatePageComplete() {
        boolean complete = ipsProject != null && correctVersionFormat && releaseAndDeploymentOperation != null
                && releaseAndDeploymentOperation.getDeploymentOperation() != null;
        setPageComplete(complete);
    }

    public ReleaseAndDeploymentOperation getReleaseBuilderOpertation() {
        return releaseAndDeploymentOperation;
    }

    public String getNewVersion() {
        return newVersionText.getText();
    }

    public List<String> getSelectedTargetSystems() {
        List<String> result = new ArrayList<String>();
        for (TableItem item : targetSystemViewer.getTable().getItems()) {
            if (item.getChecked()) {
                result.add(item.getText());
            }
        }
        return result;
    }
}
