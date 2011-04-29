/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * Dialog for editing base or specific package names of IPS source folder entries (or global for the
 * whole IPS object path)
 * 
 * @author Roman Grutza
 */
public class PackageNameEditDialog extends StatusDialog {

    private IIpsObjectPathEntryAttribute attribute;
    private IIpsSrcFolderEntry srcFolderEntry;
    private Button buttonDefaultPackageNameSelected;
    private Button buttonCustomPackageNameSelected;
    private String selectedPackageName;
    private Text text;

    /**
     * @param parent Composite
     * @param srcFolderEntry parent entry for which to alter an attribute
     * @param attribute attribute to be changed, must be of type DEFAULT_BASE_PACKAGE_MERGABLE,
     *            DEFAULT_BASE_PACKAGE_DERIVED, SPECIFIC_BASE_PACKAGE_MERGABLE or
     *            SPECIFIC_BASE_PACKAGE_DERIVED
     * @see IIpsObjectPathEntryAttribute
     */
    public PackageNameEditDialog(Shell parent, IIpsSrcFolderEntry srcFolderEntry, IIpsObjectPathEntryAttribute attribute) {
        super(parent);

        this.setTitle(Messages.PackageNameEditDialog_dialog_title);
        this.setHelpAvailable(false);
        this.attribute = attribute;
        this.srcFolderEntry = srcFolderEntry;

        if (!(attribute.isPackageNameForDerivedSources() || attribute.isPackageNameForMergableSources())) {
            throw new IllegalArgumentException("Attribute is not of type 'package name'."); //$NON-NLS-1$
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);

        Group group = new Group(parent, SWT.NONE);

        buttonDefaultPackageNameSelected = new Button(group, SWT.RADIO);
        String defaultPackageName = getDefaultPackageName();
        buttonDefaultPackageNameSelected.setText(Messages.PackageNameEditDialog_button_text_use_default_folder
                + " (" + defaultPackageName + ")"); //$NON-NLS-1$//$NON-NLS-2$ 
        buttonCustomPackageNameSelected = new Button(group, SWT.RADIO);
        buttonCustomPackageNameSelected.setText(Messages.PackageNameEditDialog_button_text_use_specific_folder);

        text = new Text(group, SWT.BORDER | SWT.SINGLE);

        final TextField specificPackageNameTextField = new TextField(text);
        specificPackageNameTextField.addChangeListener(new ValueChangeListener() {

            @Override
            public void valueChanged(FieldValueChangedEvent e) {
                selectedPackageName = e.field.getText();

            }
        });

        text.setLayoutData(new GridData(SWT.HORIZONTAL));

        selectedPackageName = getSpecificPackageName();
        if (selectedPackageName.equals("")) { //$NON-NLS-1$
            // default IPS object path package name is used
            selectedPackageName = getDefaultPackageName();
            buttonCustomPackageNameSelected.setSelection(false);
            buttonDefaultPackageNameSelected.setSelection(true);
            text.setEnabled(false);
        } else {
            // entry specific package name
            buttonCustomPackageNameSelected.setSelection(true);
            buttonDefaultPackageNameSelected.setSelection(false);
            text.setEnabled(true);
        }

        specificPackageNameTextField.setText(selectedPackageName);

        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 10;

        buttonDefaultPackageNameSelected.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                text.setEnabled(false);
                selectedPackageName = getDefaultPackageName();
            }
        });

        buttonCustomPackageNameSelected.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                text.setEnabled(true);
            }
        });

        group.setLayout(layout);

        return composite;
    }

    /**
     * Returns the package name selected in this dialog.
     * 
     * @return the package name
     */
    public String getPackageName() {
        return selectedPackageName;
    }

    private String getSpecificPackageName() {
        String specificPackageName = ""; //$NON-NLS-1$
        if (attribute.isPackageNameForDerivedSources()) {
            specificPackageName = srcFolderEntry.getSpecificBasePackageNameForDerivedJavaClasses();
        } else {
            specificPackageName = srcFolderEntry.getSpecificBasePackageNameForMergableJavaClasses();
        }
        return specificPackageName;
    }

    private String getDefaultPackageName() {
        String projectName = ""; //$NON-NLS-1$
        if (attribute.isPackageNameForDerivedSources()) {
            projectName = srcFolderEntry.getIpsObjectPath().getBasePackageNameForDerivedJavaClasses();
        } else {
            projectName = srcFolderEntry.getIpsObjectPath().getBasePackageNameForMergableJavaClasses();
        }
        return projectName;
    }

}
