/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.test;

import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestSelectionTab extends AbstractLaunchConfigurationTab implements ITestConfigurationChangeListener {

    private UIToolkit toolkit = new UIToolkit(null);

    private TestSelectionComposite testSuiteSelectionComposite;

    private Text parameterText;

    private Text projectText;

    private IIpsProject project;

    /**
     * Modify listener that simply updates the owning launch configuration dialog.
     */
    private ModifyListener basicModifyListener = $ -> updateLaunchConfigurationDialog();

    @Override
    public void createControl(Composite parent) {
        Composite main = toolkit.createComposite(parent);
        main.setLayout(new GridLayout(1, true));
        main.setFont(parent.getFont());
        setControl(main);

        Group projectGroup = toolkit.createGroup(main, Messages.TestSelectionTab_labelGroupProject);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite projectDescr = toolkit.createLabelEditColumnComposite(projectGroup);
        toolkit.createLabel(projectDescr, Messages.TestSelectionTab_labelProject);
        projectText = toolkit.createText(projectDescr);
        projectText.setEditable(false);

        Group tocGroup = toolkit.createGroup(main, Messages.TestSelectionTab_labelGroupTestSelection);
        tocGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        testSuiteSelectionComposite = new TestSelectionComposite(tocGroup);
        testSuiteSelectionComposite.addChangeListener(this);
        testSuiteSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Group parameterGroup = toolkit.createGroup(main, Messages.TestSelectionTab_groupParameter);
        parameterGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite parameterComp = toolkit.createLabelEditColumnComposite(parameterGroup);
        toolkit.createLabel(parameterComp, Messages.TestSelectionTab_labelMaxHeapSize);
        parameterText = toolkit.createText(parameterComp);
        parameterText.addModifyListener(basicModifyListener);
    }

    @Override
    public String getName() {
        return Messages.TestSelectionTab_title;
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            String packageFragmentRoot = configuration.getAttribute(IpsTestRunner.ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
            String testCases = configuration.getAttribute(IpsTestRunner.ATTR_TESTCASES, ""); //$NON-NLS-1$
            String maxHeapSize = configuration.getAttribute(IpsTestRunner.ATTR_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
            String projectName = configuration.getAttribute(IpsTestRunner.ATTR_PROJECT, ""); //$NON-NLS-1$

            project = Objects.requireNonNullElse(IpsTestRunner.getIpsProject(projectName),
                    IpsTestRunner.getIpsProjectFromTocPath(packageFragmentRoot));
            if (project != null) {
                projectText.setText(project.getName());
                testSuiteSelectionComposite.initContent(project, testCases);
            } else {
                projectText.setText(Messages.TestSelectionTab_ErrorUnknownProjekt);
                new UIToolkit(null).setDataChangeable(testSuiteSelectionComposite, false);
                setErrorMessage(Messages.TestSelectionTab_ErrorUnknownProjekt);
            }
            parameterText.setText(maxHeapSize);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        if (project != null) {
            configuration.setAttribute(IpsTestRunner.ATTR_PACKAGEFRAGMENTROOT,
                    testSuiteSelectionComposite.getPackageFragmentRootText());
            configuration.setAttribute(IpsTestRunner.ATTR_TESTCASES, testSuiteSelectionComposite.getTestCasesText());
            configuration.setAttribute(IpsTestRunner.ATTR_MAX_HEAP_SIZE, parameterText.getText());
            configuration.setAttribute(IpsTestRunner.ATTR_PROJECT, project.getName());
        }
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IpsTestRunner.ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
        configuration.setAttribute(IpsTestRunner.ATTR_TESTCASES, ""); //$NON-NLS-1$
        configuration.setAttribute(IpsTestRunner.ATTR_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
        if (project != null) {
            configuration.setAttribute(IpsTestRunner.ATTR_PROJECT, project.getName());
        }
    }

    @Override
    public void testConfigurationHasChanged() {
        updateLaunchConfigurationDialog();
    }
}
