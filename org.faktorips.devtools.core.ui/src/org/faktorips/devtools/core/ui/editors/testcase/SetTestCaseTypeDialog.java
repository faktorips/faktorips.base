/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;

/**
 * A dialog to edit the test case type of a test case.
 */
public class SetTestCaseTypeDialog extends EditDialog {

    private ITestCase testCase;
    private TestCaseTypeRefControl testCaseType;
    private String message;

    /**
     * Set {@link ITestCaseType} for the {@link ITestCase} dialog
     * 
     * @param testCase The test case for which the type will be set
     * @param parentShell The shell to be used as parent for the dialog
     * @param message The message to be displayed to the user if no error message is set.
     */
    public SetTestCaseTypeDialog(ITestCase testCase, Shell parentShell, String message) {
        super(parentShell, Messages.SetTemplateDialog_DialogTemplate_Title, false);
        this.message = message;
        this.testCase = testCase;
    }

    @Override
    protected Composite createWorkArea(Composite parent) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(workArea, Messages.SetTemplateDialog_DialogTemplate_LabelTemplate);
        testCaseType = new TestCaseTypeRefControl(testCase.getIpsProject(), workArea, getToolkit());
        testCaseType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        testCaseType.getTextControl().addModifyListener($ -> {
            if (StringUtils.isEmpty(getTestCaseType())
                    || null == testCase.getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE,
                            getTestCaseType())) {
                getButton(OK).setEnabled(false);
                String msg = NLS.bind(Messages.SetTemplateDialog_DialogTemplate_Error_TemplateNotExists,
                        testCaseType.getText());
                setMessage(msg, IMessageProvider.ERROR);
            } else {
                getButton(OK).setEnabled(true);
                setMessage(message);
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
            testCase.getIpsSrcFile().save(null);
        }
        super.buttonPressed(buttonId);
    }

    private String getTestCaseType() {
        return testCaseType.getText();
    }

}
