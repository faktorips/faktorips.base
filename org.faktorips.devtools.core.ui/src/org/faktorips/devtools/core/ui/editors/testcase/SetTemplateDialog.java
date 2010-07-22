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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.devtools.core.ui.editors.EditDialog;

/**
 * A dialog to edit the product component type (template) of a product compnent.
 */
public class SetTemplateDialog extends EditDialog {

    private ITestCase testCase;
    private TestCaseTypeRefControl template;
    private String message;

    /**
     * Set template for tese case dialog
     * 
     * @param testCase The test case for which the type will be setdit
     * @param parentShell The shell to be used as parent for the dialog
     * @param message The message to be displayed to the user if no error message is set.
     */
    public SetTemplateDialog(ITestCase testCase, Shell parentShell, String message) {
        super(parentShell, Messages.SetTemplateDialog_DialogTemplate_Title, false);
        this.message = message;
        this.testCase = testCase;
    }

    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(workArea, Messages.SetTemplateDialog_DialogTemplate_LabelTemplate);
        template = new TestCaseTypeRefControl(testCase.getIpsProject(), workArea, uiToolkit);
        template.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        template.getTextControl().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    if (StringUtils.isEmpty(getTestCaseType())
                            || null == testCase.getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE,
                                    getTestCaseType())) {
                        getButton(OK).setEnabled(false);
                        String msg = NLS.bind(Messages.SetTemplateDialog_DialogTemplate_Error_TemplateNotExists,
                                template.getText());
                        setMessage(msg, IMessageProvider.ERROR);
                    } else {
                        getButton(OK).setEnabled(true);
                        setMessage(message);
                    }
                } catch (CoreException exception) {
                    IpsPlugin.logAndShowErrorDialog(exception);
                }
            }
        });
        super.setMessage(message);

        return workArea;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        super.getButton(OK).setEnabled(false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == OK) {
            testCase.setTestCaseType(getTestCaseType());
        }
        super.buttonPressed(buttonId);
    }

    private String getTestCaseType() {
        return template.getText();
    }

}
