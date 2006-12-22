/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestSelectionTab extends AbstractLaunchConfigurationTab implements  ITestConfigurationChangeListener{
    
    private UIToolkit toolkit = new UIToolkit(null);
    
    private TestSelectionComposite testSuiteSelectionComposite;
    
    private Text parameterText;

    private Text projectText;

    private IIpsProject project;

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return Messages.TestSelectionTab_title;
    }

    /**
     * {@inheritDoc}
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            String packageFragmentRoot = configuration.getAttribute(IpsTestRunnerDelegate.ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
            String testCases = configuration.getAttribute(IpsTestRunnerDelegate.ATTR_TESTCASES, ""); //$NON-NLS-1$
            String maxHeapSize = configuration.getAttribute(IpsTestRunnerDelegate.ATTR_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
            
            project = IpsTestRunner.getIpsProjectFromTocPath(packageFragmentRoot);
            projectText.setText(project.getName());
            testSuiteSelectionComposite.initContent(project, packageFragmentRoot, testCases);
            parameterText.setText(maxHeapSize);
        }
        catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        try {
            configuration.setAttribute(IpsTestRunnerDelegate.ATTR_PACKAGEFRAGMENTROOT, testSuiteSelectionComposite.getPackageFragmentRootText());
            configuration.setAttribute(IpsTestRunnerDelegate.ATTR_TESTCASES, testSuiteSelectionComposite.getTestCasesText());
            configuration.setAttribute(IpsTestRunnerDelegate.ATTR_MAX_HEAP_SIZE, parameterText.getText());       
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_TESTCASES, ""); //$NON-NLS-1$
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_MAX_HEAP_SIZE, ""); //$NON-NLS-1$        
    }

    /**
     * {@inheritDoc}
     */
    public void testConfigurationHasChanged() {
        updateLaunchConfigurationDialog();
    }
    
    /**
     * Modify listener that simply updates the owning launch configuration dialog.
     */
    private ModifyListener basicModifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent evt) {
            updateLaunchConfigurationDialog();
        }
    };    
}
