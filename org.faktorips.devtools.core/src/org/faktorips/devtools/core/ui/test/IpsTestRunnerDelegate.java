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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;

/**
 * Launch configuration delegate to delegate the launch to the ips test runner.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunnerDelegate extends LaunchConfigurationDelegate {
    public static final String ID_IPSTEST_LAUNCH_CONFIGURATION_TYPE = "org.faktorips.devtools.core.ipsTestLaunchConfigurationType"; //$NON-NLS-1$
    public static final String ATTR_PACKAGEFRAGMENTROOT = IpsPlugin.PLUGIN_ID + ".ATTR_PACKAGEFRAGMENTROOT"; //$NON-NLS-1$
    public static final String ATTR_TESTCASES = IpsPlugin.PLUGIN_ID + ".ATTR_TESTCASES"; //$NON-NLS-1$
    
    /**
     * {@inheritDoc}
     */
    public void launch(final ILaunchConfiguration configuration,
            final String mode,
            final ILaunch launch,
            IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null){
            // the current thread is an ui thread, thus we can start the test directly
            startTest(configuration, mode, launch);
            return;
        }
        
        // it is necessary that we execute the test in an ui thread (e.g. check for open editors in
        // DebugUiTools or open view is only possible if we run in an ui thread)
        UIJob uiJob = new UIJob("IPS Testrunner delegate") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    startTest(configuration, mode, launch);
                }
                catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                return Job.ASYNC_FINISH;
            }
        };
        uiJob.setSystem(true);
        uiJob.run(monitor);
    }
    
    /*
     * Delegate the test start to the ips test runner
     */
    private void startTest(final ILaunchConfiguration configuration, final String mode, final ILaunch launch)
            throws CoreException {
        String packageFragment = configuration.getAttribute(ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
        String testCases = configuration.getAttribute(ATTR_TESTCASES, ""); //$NON-NLS-1$
        IpsTestAction runTestAction = new IpsTestAction(null, mode);
        runTestAction.setLauch(launch);
        runTestAction.run(packageFragment, testCases);
    }
}
