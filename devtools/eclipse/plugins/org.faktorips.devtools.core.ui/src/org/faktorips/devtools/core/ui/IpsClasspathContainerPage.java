/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension2;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.eclipse.internal.IpsClasspathContainerInitializer;

public class IpsClasspathContainerPage extends NewElementWizardPage implements IClasspathContainerPage,
        IClasspathContainerPageExtension2, IClasspathContainerPageExtension {

    private static final String WIZARDS_ADD_LIBRARY_WIZARD_PNG = "wizards/AddLibraryWizard.png"; //$NON-NLS-1$

    private IClasspathEntry entry;
    private IJavaProject javaProject;

    private Checkbox includeJodaCheckbox;
    private Checkbox includeGroovyCheckbox;

    private Checkbox includeJaxbSupportCheckbox;
    private Combo jaxbSelectionCombo;
    private JaxbSelectionPmo jaxbSelectionPmo = new JaxbSelectionPmo(JaxbSupportVariant.ClassicJAXB);

    public IpsClasspathContainerPage() {
        super(Messages.IpsClasspathContainerPage_title);
        setTitle(Messages.IpsClasspathContainerPage_title);
        setDescription(Messages.IpsClasspathContainerPage_description);
        setImageDescriptor(IpsUIPlugin.getImageHandling()
                .getSharedImageDescriptor(WIZARDS_ADD_LIBRARY_WIZARD_PNG, true));
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(1, false));

        UIToolkit toolkit = new UIToolkit(null);
        createJodaSupportCheckbox(composite, toolkit);

        createGroovySupportCheckbox(composite, toolkit);

        createJaxbSupportCheckboxAndSelection(composite, toolkit);

        toolkit.createVerticalSpacer(composite, 8);
        createLabel(composite, Messages.IpsClasspathContainerPage_disclaimer1);
        createLabel(composite, Messages.IpsClasspathContainerPage_disclaimer2);

        setControl(composite);
    }

    private void createJodaSupportCheckbox(Composite composite, UIToolkit toolkit) {
        String text = Messages.IpsClasspathContainerPage_includeJoda;
        boolean jodaAvailable = IpsClasspathContainerInitializer.isJodaSupportAvailable();
        if (!jodaAvailable) {
            text = text + Messages.IpsClasspathContainerPage_bundleNotInstalled;
        }
        includeJodaCheckbox = toolkit.createCheckbox(composite, text);
        includeJodaCheckbox.setEnabled(jodaAvailable);
        if (entry == null) {
            includeJodaCheckbox.setChecked(jodaAvailable);
        } else {
            includeJodaCheckbox.setChecked(IpsClasspathContainerInitializer.isJodaSupportIncluded(entry));
        }
    }

    private void createGroovySupportCheckbox(Composite composite, UIToolkit toolkit) {
        String text = Messages.IpsClasspathContainerPage_includeGroovy;
        boolean groovyAvailable = IpsClasspathContainerInitializer.isGroovySupportAvailable();
        if (!groovyAvailable) {
            text = text + Messages.IpsClasspathContainerPage_bundleNotInstalled;
        }
        includeGroovyCheckbox = toolkit.createCheckbox(composite, text);
        includeGroovyCheckbox.setEnabled(groovyAvailable);
        if (entry == null) {
            includeGroovyCheckbox.setChecked(groovyAvailable);
        } else {
            includeGroovyCheckbox.setChecked(IpsClasspathContainerInitializer.isGroovySupportIncluded(entry));
        }
    }

    private void createJaxbSupportCheckboxAndSelection(Composite composite, UIToolkit toolkit) {
        String text = Messages.IpsClasspathContainerPage_includeJaxbSupport;
        boolean jaxbAvailable = IpsClasspathContainerInitializer.isJaxbSupportAvailable();
        if (!jaxbAvailable) {
            text = text + Messages.IpsClasspathContainerPage_bundleNotInstalled;
        }
        includeJaxbSupportCheckbox = toolkit.createCheckbox(composite, text);
        includeJaxbSupportCheckbox.setEnabled(jaxbAvailable);
        if (entry == null) {
            includeJaxbSupportCheckbox.setChecked(jaxbAvailable);
        } else {
            includeJaxbSupportCheckbox.setChecked(IpsClasspathContainerInitializer.isClassicJaxbSupportIncluded(entry)
                    || IpsClasspathContainerInitializer.isJakartaSupportIncluded(entry));
        }

        BindingContext bc = new BindingContext();
        jaxbSelectionCombo = toolkit.createCombo(composite);
        bc.bindContent(jaxbSelectionCombo, jaxbSelectionPmo, JaxbSelectionPmo.PROPERTY_SELECTED,
                JaxbSupportVariant.class);

        if (IpsClasspathContainerInitializer.isClassicJaxbSupportIncluded(entry)) {
            jaxbSelectionPmo.setSelectedValue(JaxbSupportVariant.ClassicJAXB);
        } else if (IpsClasspathContainerInitializer.isJakartaSupportIncluded(entry)) {
            jaxbSelectionPmo.setSelectedValue(JaxbSupportVariant.JakartaXmlBinding3);
        }

        jaxbSelectionCombo.setEnabled(includeJaxbSupportCheckbox.isChecked());
        jaxbSelectionCombo.setVisible(includeJaxbSupportCheckbox.isChecked());

        includeJaxbSupportCheckbox.addListener(SWT.Selection, event -> {
            jaxbSelectionCombo.setEnabled(includeJaxbSupportCheckbox.isChecked());
            jaxbSelectionCombo.setVisible(includeJaxbSupportCheckbox.isChecked());
            if (!includeJaxbSupportCheckbox.isChecked()) {
                jaxbSelectionPmo.setSelectedValue(JaxbSupportVariant.None);
            }
        });

        bc.updateUI();
    }

    private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setFont(parent.getFont());
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
        label.setText(text);
        return label;
    }

    @Override
    public boolean finish() {
        return true;
    }

    @Override
    public IClasspathEntry getSelection() {
        IPath entryPath = IpsClasspathContainerInitializer.newEntryPath(includeJodaCheckbox.isChecked(),
                includeGroovyCheckbox.isChecked(),
                jaxbSelectionPmo.getSelectedValue());
        if (entry.getPath().equals(entryPath)) {
            return entry;
        } else {
            return JavaCore.newContainerEntry(entryPath, isExported());
        }
    }

    @Override
    public void setSelection(IClasspathEntry containerEntry) {
        entry = containerEntry;
    }

    @Override
    public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
        javaProject = project;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

    @Override
    public IClasspathEntry[] getNewContainers() {
        IClasspathEntry[] res = new IClasspathEntry[1];
        IPath entryPath = IpsClasspathContainerInitializer.newEntryPath(includeJodaCheckbox.isChecked(),
                includeGroovyCheckbox.isChecked(),
                jaxbSelectionPmo.getSelectedValue());
        res[0] = JavaCore.newContainerEntry(entryPath, isExported());
        return res;
    }

    private boolean isExported() {
        return entry == null ? false : entry.isExported();
    }

    public static class JaxbSelectionPmo {

        public static final String PROPERTY_SELECTED = "selectedValue"; //$NON-NLS-1$

        private JaxbSupportVariant selectedValue;

        public JaxbSelectionPmo(JaxbSupportVariant defaultValue) {
            selectedValue = defaultValue;
        }

        public void setSelectedValue(JaxbSupportVariant selected) {
            selectedValue = selected;
        }

        public JaxbSupportVariant getSelectedValue() {
            return selectedValue;
        }
    }
}
