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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;

/**
 * Launch configuration delegate to delegate the launch to the ips test runner.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunnerDelegate extends LaunchConfigurationDelegate {

    public static final boolean TRACE_IPS_TEST_RUNNER;

    private static DateFormat debugFormat;

    static {
        TRACE_IPS_TEST_RUNNER = Boolean
                .valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/testrunner")).booleanValue(); //$NON-NLS-1$
    }

    @Override
    public void launch(final ILaunchConfiguration configuration,
            final String mode,
            final ILaunch launch,
            IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        if (IpsPlugin.getDefault().getIpsTestRunner().isRunningTestRunner()) {
            monitor.worked(1);
            monitor.done();
            monitor.setCanceled(true);
            return;
        }

        if (IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
            // the current thread is an ui thread, thus we can start the test directly
            trace("Launch in existing UI Thread."); //$NON-NLS-1$
            startTest(configuration, mode, launch);
            return;
        }

        // it is necessary that we execute the test in an ui thread (e.g. check for open editors in
        // DebugUiTools or open view is only possible if we run in an ui thread)
        trace("Launch in new UI Thread."); //$NON-NLS-1$
        UIJob uiJob = new UIJob("IPS Testrunner delegate") { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                trace("Lauch configuration (" + configuration.getName() + ") in UI Job 'IPS Testrunner delegate'"); //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    startTest(configuration, mode, launch);
                } catch (IpsException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                return Job.ASYNC_FINISH;
            }
        };
        uiJob.setSystem(true);
        uiJob.schedule();
    }

    /*
     * Delegate the test start to the ips test runner
     */
    private void startTest(final ILaunchConfiguration configuration, final String mode, final ILaunch launch)
            {
        String packageFragment;
        String testCases;
        try {
            packageFragment = configuration.getAttribute(IpsTestRunner.ATTR_PACKAGEFRAGMENTROOT, ""); //$NON-NLS-1$
            testCases = configuration.getAttribute(IpsTestRunner.ATTR_TESTCASES, ""); //$NON-NLS-1$
        } catch (CoreException e) {
            throw new IpsException(e);
        }

        IpsTestAction runTestAction = new IpsTestAction(null, mode);
        runTestAction.setLauch(launch);
        runTestAction.run(packageFragment, testCases);
    }

    private void trace(String line) {
        if (TRACE_IPS_TEST_RUNNER) {
            if (debugFormat == null) {
                debugFormat = new SimpleDateFormat("(HH:mm:ss.SSS): "); //$NON-NLS-1$
            }
            StringBuffer msgBuf = new StringBuffer(line.length() + 40);
            msgBuf.append("IpsTestRunnerDelegate "); //$NON-NLS-1$
            debugFormat.format(new Date(), msgBuf, new FieldPosition(0));
            msgBuf.append(line);
            System.out.println(msgBuf.toString());
        }
    }
}
