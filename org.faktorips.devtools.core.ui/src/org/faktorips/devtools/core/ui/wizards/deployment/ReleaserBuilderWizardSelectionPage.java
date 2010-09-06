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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class ReleaserBuilderWizardSelectionPage extends WizardPage {

    private IIpsProject ipsProject;
    private Label latestVersionLabel;
    private Text newVersionText;

    protected ReleaserBuilderWizardSelectionPage() {
        super("Releaser Builder");
        setTitle("Releaser Builder");
    }

    @Override
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage() && ipsProject != null
                && ipsProject.getVersionFormat().isCorrectVersionFormat(newVersionText.getText());
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

        projectSelectComboViewer.setContentProvider(new ArrayContentProvider());
        projectSelectComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                TypedSelection<IIpsProject> typedSelection = new TypedSelection<IIpsProject>(IIpsProject.class, event
                        .getSelection());
                if (typedSelection.isValid()) {
                    IIpsProject ipsProject = typedSelection.getFirstElement();
                    checkPrerequisite(ipsProject);
                    setIpsProject(ipsProject);
                } else {
                    setIpsProject(null);
                }
            }
        });

        newVersionText.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                if (ipsProject != null) {
                    String newVersion = newVersionText.getText();
                    if (!ipsProject.getVersionFormat().isCorrectVersionFormat(newVersion)) {
                        setMessage("The format of version \"" + newVersion + "\" is incorrect.", DialogPage.ERROR);
                    } else {
                        setMessage("", DialogPage.NONE);
                    }
                }
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

    protected void checkPrerequisite(IIpsProject ipsProject2) {
        // TODO Auto-generated method stub

    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        if (latestVersionLabel != null && newVersionText != null) {
            String oldVersion = ipsProject.getProperties().getVersion();
            if (oldVersion == null) {
                oldVersion = "";
            }
            if (latestVersionLabel.getText().equals(newVersionText.getText())) {
                newVersionText.setText(oldVersion);
            }
            latestVersionLabel.setText(oldVersion);
        }
        setPageComplete(ipsProject != null);
    }
}
