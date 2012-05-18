/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

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
            throws CoreException {
        // nothing to do
    }

    @Override
    public void stop() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }
}
