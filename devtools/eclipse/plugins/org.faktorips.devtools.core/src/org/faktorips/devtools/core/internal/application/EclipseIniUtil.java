/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.ui.internal.Workbench;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Reads VM arguments from the {@code eclipse.vmargs} system property to preserve them across
 * restarts.
 */
class EclipseIniUtil {

    private static final String ECLIPSE_VMARGS_PROPERTY = "eclipse.vmargs"; //$NON-NLS-1$
    private static final String JAR_ARG = "-jar"; //$NON-NLS-1$

    private EclipseIniUtil() {
        // utility
    }

    /**
     * Returns the VM arguments from the {@code eclipse.vmargs} system property as a
     * newline-prefixed string (e.g. {@code "\n-Xms256m\n-Xmx1024m"}), excluding any {@code -jar}
     * argument and its value. Returns an empty string if the property is not set.
     */
    static String readVmArgs() {
        String vmargs = System.getProperty(ECLIPSE_VMARGS_PROPERTY);
        if (vmargs == null || vmargs.isBlank()) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = vmargs.split("\\r?\\n"); //$NON-NLS-1$
        int i = 0;
        while (i < lines.length) {
            String line = lines[i++].strip();
            if (JAR_ARG.equals(line)) {
                // skip the following path value too
                i++;
            } else if (!line.isBlank()) {
                sb.append('\n').append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Sets the {@link IApplicationContext#EXIT_DATA_PROPERTY} so that Eclipse restarts into the
     * given workspace {@code selection}.
     * <p>
     * Appends {@code -vmargs} followed by the property to skip the workspace dialog on the next
     * start and all VM arguments from the {@code eclipse.vmargs} system property (excluding
     * {@code -jar} and its value).
     *
     * @param selection the workspace path to restart into
     * @return the restart arguments from {@link Workbench#setRestartArguments(String)}, or
     *             {@code null} if the workbench did not provide any
     */
    @SuppressWarnings("restriction")
    static Object setCmdLineParams(String selection) {
        Object restartArguments = Workbench.setRestartArguments(selection);
        String existingExitData = System.getProperty(IApplicationContext.EXIT_DATA_PROPERTY, ""); //$NON-NLS-1$
        String cmdLine = existingExitData + "\n-vmargs\n-D" + IpsApplication.SKIP_WORKSPACE_DIALOG_PROPERTY + "=true" //$NON-NLS-1$ //$NON-NLS-2$
                + readVmArgs() + "\n"; //$NON-NLS-1$
        System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, cmdLine);
        IpsPlugin.log(Status.info("Re-Starting Eclipse with: " + cmdLine)); //$NON-NLS-1$

        return restartArguments;
    }
}
