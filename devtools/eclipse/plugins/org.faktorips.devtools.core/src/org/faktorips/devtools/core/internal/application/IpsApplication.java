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
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.ChooseWorkspaceDialog;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Application for Faktor-IPS to be used with eclipse. Provides reduced functionality in comparison
 * with the use as plug-in within an eclipse running the IDE-Product. Used for department-workers.
 *
 * @author Thorsten Guenther
 */
public class IpsApplication implements IApplication, IExecutableExtension {

    static final String SKIP_WORKSPACE_DIALOG_PROPERTY = "org.faktorips.modeller.skipWorkspaceDialog"; //$NON-NLS-1$

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
        Object workspaceResult = checkInstanceLocation(shell);
        if (workspaceResult != null) {
            return workspaceResult;
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
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
        // nothing to do
    }

    /**
     * Enables workspace selection at start.
     *
     * @return a non-null exit code ({@link IApplication#EXIT_OK} or
     *             {@link IApplication#EXIT_RELAUNCH}) if the application should exit, or
     *             {@code null} if the workspace is ready and the application should start normally
     */
    // CSOFF: CyclomaticComplexity
    @SuppressWarnings("restriction")
    private Object checkInstanceLocation(Shell shell) {
        Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc == null || !instanceLoc.isSet()) {
            MessageDialog.openError(shell, Messages.IpsApplication_workspaceNotSet_title,
                    Messages.IpsApplication_workspaceNotSet_msg);
            return EXIT_OK;
        }

        boolean skipDialog = System.getProperty(SKIP_WORKSPACE_DIALOG_PROPERTY) != null;
        if (!skipDialog) {
            ChooseWorkspaceData data = new ChooseWorkspaceData(instanceLoc.getURL());
            String selection;
            if (data.getShowDialog()) {
                ChooseWorkspaceDialog dialog = new ChooseWorkspaceDialog(shell, data, true, true);
                dialog.prompt(true);
                selection = data.getSelection();
                if (selection == null) {
                    return EXIT_OK;
                }
                data.writePersistedData();
            } else {
                String[] recentWorkspaces = data.getRecentWorkspaces();
                selection = (recentWorkspaces != null && recentWorkspaces.length > 0
                        && recentWorkspaces[0] != null && !recentWorkspaces[0].isBlank())
                                ? recentWorkspaces[0]
                                : data.getInitialDefault();
            }
            try {
                if (selection != null) {
                    File currentWorkspace = new File(instanceLoc.getURL().getFile()).getCanonicalFile();
                    File selectedWorkspace = new File(selection).getCanonicalFile();
                    if (!currentWorkspace.equals(selectedWorkspace)) {
                        Object restartArguments = EclipseIniUtil.setCmdLineParams(selection);
                        return restartArguments != null ? restartArguments : EXIT_OK;
                    }
                }
            } catch (IOException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return EXIT_OK;
            }
        }
        try {
            if (instanceLoc.lock()) {
                // workspace ready, proceed normally
                return null;
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
        return EXIT_OK;
    }
    // CSON: CyclomaticComplexity

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
