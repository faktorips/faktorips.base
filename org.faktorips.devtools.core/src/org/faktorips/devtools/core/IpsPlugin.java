/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.internal.model.versionmanager.IpsFeatureMigrationOperation;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.internal.refactor.IpsRefactoringFactory;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.core.refactor.IIpsRefactoringFactory;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IpsFeatureVersionManagerSorter;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class.
 * 
 * @author Jan Ortmann
 */
public class IpsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.faktorips.devtools.core"; //$NON-NLS-1$

    public static final boolean TRACE_UI = Boolean
            .valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/ui")).booleanValue(); //$NON-NLS-1$

    /** The shared instance. */
    private static IpsPlugin plugin;

    private final IIpsRefactoringFactory ipsRefactoringFactory = new IpsRefactoringFactory();

    private IpsPreferences preferences;

    /**
     * Contains the IPS test runner, which runs IPS tests and informs registered IPS test run
     * listener.
     */
    private IIpsTestRunner ipsTestRunner;

    private IpsCoreExtensions ipsCoreExtensions;

    public IpsPlugin() {
        super();
        plugin = this;
        ipsCoreExtensions = new IpsCoreExtensions(Platform.getExtensionRegistry());
    }

    /**
     * Returns the number of the installed Faktor-IPS version.
     */
    public static final String getInstalledFaktorIpsVersion() {
        return Platform.getBundle(PLUGIN_ID).getVersion().toString();
    }

    /**
     * Returns the shared instance.
     */
    public static IpsPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the full extension id. This is the plugin's id plus the plug-in relative extension id
     * separated by a dot.
     * 
     * @throws NullPointerException if pluginRelativeEnxtensionId is <code>null</code>.
     */
    public static final String getFullExtensionId(String pluginRelativeEnxtensionId) {
        ArgumentCheck.notNull(pluginRelativeEnxtensionId);
        return PLUGIN_ID + '.' + pluginRelativeEnxtensionId;
    }

    /**
     * Provides a factory that allows to create IPS refactorings.
     */
    public static final IIpsRefactoringFactory getIpsRefactoringFactory() {
        return getDefault().ipsRefactoringFactory;
    }

    /**
     * Logs the given core exception.
     */
    public static final void log(CoreException e) {
        IpsLog.log(e);
    }

    /**
     * This method is called upon plug-in activation.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        preferences = new IpsPreferences(getPreferenceStore());
    }

    /**
     * This method is called when the plug-in is stopped.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the plugin's version identifier.
     */
    public AVersion getVersionIdentifier() {
        String bundleVersion = getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
        return AVersion.parse(bundleVersion);
    }

    /**
     * Logs the status.
     */
    public static final void log(IStatus status) {
        IpsLog.log(status);
    }

    /**
     * Logs the exception.
     */
    public static final void log(Throwable t) {
        IpsLog.log(t);
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(final IStatus status) {
        IpsLog.logAndShowErrorDialog(status);
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(Exception e) {
        IpsLog.logAndShowErrorDialog(e);
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(CoreException e) {
        IpsLog.logAndShowErrorDialog(e);
    }

    /**
     * Does not log the status but show an error dialog with the status message
     */
    public static final void showErrorDialog(final IStatus status) {
        IIpsModelExtensions.get().getWorkspaceInteractions().showErrorDialog(status);
    }

    /**
     * Returns preferences for this plug-in.
     */
    public IpsPreferences getIpsPreferences() {
        return preferences;
    }

    /**
     * Returns whether the product definition perspective is currently active or not
     * 
     * @return <code>true</code> if the current active perspective is the product definition
     *         perspective, <code>false</code> if not.
     */
    public boolean isProductDefinitionPerspective() {
        IWorkbenchWindow activeWorkbenchWindow = getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return false;
        }
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null) {
            return false;
        }
        IPerspectiveDescriptor perspective = activePage.getPerspective();
        if (perspective != null) {
            return perspective.getId().equals(IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID);
        } else {
            return false;
        }
    }

    /**
     * Returns the locale used by the localization. The returned locale is not the locale the
     * localization <strong>should use</strong>, but it is the locale the localization
     * <strong>actually uses</strong>.
     * <p>
     * That means if the default locale this plug-in runs is for example de_DE, but no language pack
     * for German is installed, the localization uses the English language, and this method will
     * return the Locale for "en".
     */
    public Locale getUsedLanguagePackLocale() {
        Locale retValue = new Locale(Messages.IpsPlugin_languagePackLanguage, Messages.IpsPlugin_languagePackCountry,
                Messages.IpsPlugin_languagePackVariant);
        return retValue;
    }

    /**
     * Returns the IPS test runner.
     */
    public IIpsTestRunner getIpsTestRunner() {
        if (ipsTestRunner == null) {
            ipsTestRunner = IpsTestRunner.getDefault();
        }

        return ipsTestRunner;
    }

    /**
     * Returns an array of all available external table formats.
     */
    public ITableFormat[] getExternalTableFormats() {
        return ipsCoreExtensions.getExternalTableFormats();
    }

    /**
     * Returns a migration operation migrating the content of the given IPS project to match the
     * needs of the current version of Faktor-IPS.
     * 
     * @param projectToMigrate The project the migration operation should be returned for.
     */
    public AbstractIpsFeatureMigrationOperation getMigrationOperation(IIpsProject projectToMigrate) {
        IIpsFeatureVersionManager[] managers = IIpsModelExtensions.get().getIpsFeatureVersionManagers();

        /*
         * Sort the managers to match the required order given by the basedOnFeatureManager-property
         * of the extension-point.
         */
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        managers = sorter.sortForMigartionOrder(managers);
        IpsFeatureMigrationOperation operation = new IpsFeatureMigrationOperation(projectToMigrate);
        for (IIpsFeatureVersionManager manager : managers) {
            operation.addMigrationPath(manager.getMigrationOperations(projectToMigrate));
        }
        return operation;
    }

    /**
     * Returns the {@link ITeamOperationsFactory ITeamOperationsFactories} used to create
     * {@link ITeamOperations} for the {@link ProductReleaseProcessor}.
     */
    public Set<ITeamOperationsFactory> getTeamOperationsFactories() {
        return ipsCoreExtensions.getTeamOperationsFactories();
    }

    public IpsCoreExtensions getIpsCoreExtensions() {
        return ipsCoreExtensions;
    }

    /**
     * FOR TESTS ONLY
     */
    public void setIpsCoreExtensions(IpsCoreExtensions ipsCoreExtensions) {
        this.ipsCoreExtensions = ipsCoreExtensions;
    }

}
