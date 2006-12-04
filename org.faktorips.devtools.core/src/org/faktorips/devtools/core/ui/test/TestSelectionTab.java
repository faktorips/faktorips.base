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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Launch configuration tab to select tests to execute with the ips test runner.
 * 
 * @author Joerg Ortmann
 */
public class TestSelectionTab extends AbstractLaunchConfigurationTab {
    
    private UIToolkit toolkit = new UIToolkit(null);

    private Text packageFragmentRootText;
    private Text testCasesText;
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite main = toolkit.createComposite(parent);
        main.setLayout(new GridLayout(1, true));
        main.setFont(parent.getFont());
        setControl(main);
        
        Group tocGroup = toolkit.createGroup(main, Messages.TestSelectionTab_goupPackageFragmentRoots);
        Composite tocComp = toolkit.createLabelEditColumnComposite(tocGroup);
        toolkit.createLabel(tocComp, Messages.TestSelectionTab_labelTocFiles);
        packageFragmentRootText = toolkit.createText(tocComp);
        packageFragmentRootText.addModifyListener(basicModifyListener);
        
        Group testCaseGroup = toolkit.createGroup(main, Messages.TestSelectionTab_groupTestSuites);
        Composite testCaseComp = toolkit.createLabelEditColumnComposite(testCaseGroup);
        toolkit.createLabel(testCaseComp, Messages.TestSelectionTab_labelTestCasesPackages);
        testCasesText = toolkit.createText(testCaseComp);
        testCasesText.addModifyListener(basicModifyListener);
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
            
            packageFragmentRootText.setText(packageFragmentRoot);
            testCasesText.setText(testCases);
        }
        catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_PACKAGEFRAGMENTROOT, packageFragmentRootText.getText());
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_TESTCASES, testCasesText.getText());
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
        configuration.setAttribute(IpsTestRunnerDelegate.ATTR_TESTCASES, ""); //$NON-NLS-1$
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
