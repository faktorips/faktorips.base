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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Application for Faktor-IPS to be used with eclipse. Provides reduced functionality in comparison
 * with the use as plug-in within an eclipse running the IDE-Product. Used for department-workers.
 * 
 * @author Thorsten Guenther
 */
public class IpsApplication implements IApplication, IExecutableExtension {

    // see org.eclipse.ui.internal.ide.application.IDEApplication.PROP_EXIT_CODE
    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    @Override
    public Object start(IApplicationContext appContext) throws Exception {
        Display display = PlatformUI.createDisplay();

        // look and see if there's a splash shell we can parent off of
        Shell shell = display.getActiveShell();
        if (shell != null) {
            // should set the icon and message for this shell to be the
            // same as the chooser dialog - this will be the guy that lives in
            // the task bar and without these calls you'd have the default icon
            // with no message.
            shell.setText(Messages.IpsWorkbenchAdvisor_title);
            shell.setImages(Dialog.getDefaultImages());
        }

        if (!checkInstanceLocation(shell)) {
            return EXIT_OK;
        }

        int returnCode = PlatformUI.createAndRunWorkbench(display, new IpsWorkbenchAdvisor());

        // fix to restart product, see
        // org.eclipse.ui.internal.ide.application.IDEApplication.start(IApplicationContext)

        // the workbench doesn't support relaunch yet (bug 61809) so
        // for now restart is used, and exit data properties are checked
        // here to substitute in the relaunch return code if needed
        if (returnCode != PlatformUI.RETURN_RESTART) {
            return EXIT_OK;
        }
        // if the exit code property has been set to the relaunch code, then
        // return that code now, otherwise this is a normal restart
        return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH : EXIT_RESTART;
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
            {
        // nothing to do
    }

    /**
     * Return true if a valid workspace path has been set and false otherwise. Prompt for and set
     * the path if possible and required.
     * 
     * @return true if a valid instance location has been set and false otherwise
     */
    private boolean checkInstanceLocation(Shell shell) {
        // -data @none was specified but an ide requires workspace
        Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc == null || !instanceLoc.isSet()) {
            MessageDialog.openError(shell, Messages.IpsApplication_workspaceNotSet_title,
                    Messages.IpsApplication_workspaceNotSet_msg);
            return false;
        }

        // at this point its valid, so try to lock it and update the
        // metadata version information if successful
        try {
            if (instanceLoc.lock()) {
                return true;
            }

            // we failed to create the directory.
            // Two possibilities:
            // 1. directory is already in use
            // 2. directory could not be created
            File workspaceDirectory = new File(instanceLoc.getURL().getFile());
            if (workspaceDirectory.exists()) {
                MessageDialog.openError(shell, Messages.IpsApplication_cannotLockWorkspace_title,
                        Messages.IpsApplication_cannotLockWorkspace_msg);
            } else {
                MessageDialog.openError(
                        shell,
                        Messages.IpsApplication_cannotCreateWorkspace_title,
                        NLS.bind(Messages.IpsApplication_cannotCreateWorkspace_msg,
                                workspaceDirectory.getCanonicalPath()));
            }
        } catch (IOException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return false;
    }

    @Override
    public void stop() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        final Display display = workbench.getDisplay();
        display.syncExec(() -> {
            if (!display.isDisposed()) {
                workbench.close();
            }
        });
    }
}
