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

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.devtools.core.builder.DependencyGraphPersistenceManager;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.internal.model.versionmanager.IpsFeatureMigrationOperation;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.internal.refactor.IpsRefactoringFactory;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.core.model.versionmanager.IpsFeatureVersionManagerSorter;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.core.refactor.IIpsRefactoringFactory;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * The main plug-in class.
 * 
 * @author Jan Ortmann
 */
public class IpsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.faktorips.devtools.core"; //$NON-NLS-1$

    public static final String PROBLEM_MARKER = PLUGIN_ID + ".problemmarker"; //$NON-NLS-1$

    /**
     * The extension point id of the extension point <tt>ipsMigrationOperation</tt>.
     */
    public static final String EXTENSION_POINT_ID_MIGRATION_OPERATION = "org.faktorips.devtools.core.ipsMigrationOperation"; //$NON-NLS-1$

    /**
     * The extension point id of the extension point property <tt>migrationOperation</tt> in the
     * extension point ipsMigrationOperation.
     */
    public static final String CONFIG_ELEMENT_ID_MIGRATION_OPERATION = "migrationOperation"; //$NON-NLS-1$

    public static final boolean TRACE_UI = Boolean.valueOf(
            Platform.getDebugOption("org.faktorips.devtools.core/trace/ui")).booleanValue(); //$NON-NLS-1$

    private static final String EXTENSION_POINT_ID_TEAM_OPERATIONS_FACTORY = "teamOperationsFactory"; //$NON-NLS-1$

    /** The shared instance. */
    private static IpsPlugin plugin;

    private final MultiLanguageSupport multiLanguageSupport = new MultiLanguageSupport();

    private final IIpsRefactoringFactory ipsRefactoringFactory = new IpsRefactoringFactory();

    /** The document builder factory that provides the document builder for this plug-in. */
    private DocumentBuilderFactory docBuilderFactory;

    private IpsPreferences preferences;

    private IpsModel model;

    /**
     * Contains the IPS test runner, which runs IPS tests and informs registered IPS test run
     * listener.
     */
    private IIpsTestRunner ipsTestRunner;

    /** All available external table formats. */
    private ITableFormat[] externalTableFormats;

    /** All available feature version managers. */
    private IIpsFeatureVersionManager[] featureVersionManagers;

    private IIpsLoggingFrameworkConnector[] loggingFrameworkConnectors;

    private IFunctionResolverFactory[] flFunctionResolvers;

    private boolean testMode = false;

    private ITestAnswerProvider testAnswerProvider;

    private boolean suppressLoggingDuringTestExecution = false;

    private DependencyGraphPersistenceManager dependencyGraphPersistenceManager;

    private Set<ITeamOperationsFactory> teamOperationsFactories;

    private final ExtensionFactory extensionFactory;

    public IpsPlugin() {
        super();
        plugin = this;
        extensionFactory = new ExtensionFactory(getExtensionRegistry());
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
     * Provides access to operations related to multi-language support.
     */
    public static final MultiLanguageSupport getMultiLanguageSupport() {
        return getDefault().multiLanguageSupport;
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
        log(e.getStatus());
    }

    /**
     * This method is called upon plug-in activation.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        preferences = new IpsPreferences(getPreferenceStore());
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        dependencyGraphPersistenceManager = new DependencyGraphPersistenceManager();

        IpsCompositeSaveParticipant saveParticipant = new IpsCompositeSaveParticipant();
        saveParticipant.addSaveParticipant(dependencyGraphPersistenceManager);
        ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);

        // force loading of class before model is created!
        IpsObjectType.POLICY_CMPT_TYPE.getId();
        // ensure that this class is loaded in time
        BFElementType.ACTION_BUSINESSFUNCTIONCALL.getClass();
        model = new IpsModel();
        model.startListeningToResourceChanges();
    }

    /**
     * This method is called when the plug-in is stopped.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        ((IpsModel)getIpsModel()).stopListeningToResourceChanges();
        model = null;
    }

    /**
     * Re-initializes the model (so all data in the cache is cleared). Should only be called in test
     * cases to ensure a clean environment.
     */
    public void reinitModel() {
        model.stopListeningToResourceChanges();
        model = new IpsModel();
        model.startListeningToResourceChanges();
    }

    /**
     * Returns the plugin's version identifier.
     */
    public Version getVersionIdentifier() {
        String version = (String)getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
        return Version.parseVersion(version);
    }

    /**
     * Logs the status.
     */
    public static final void log(IStatus status) {
        if (plugin != null && !plugin.suppressLoggingDuringTestExecution) {
            plugin.getLog().log(status);
        }
    }

    /**
     * Logs the exception.
     */
    public static final void log(Throwable t) {
        log(new IpsStatus(t));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(final IStatus status) {
        plugin.getLog().log(status);
        Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.IpsPlugin_titleErrorDialog,
                        Messages.IpsPlugin_msgUnexpectedError, status);
            }
        });
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(Exception e) {
        logAndShowErrorDialog(new IpsStatus(e));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(CoreException e) {
        logAndShowErrorDialog(e.getStatus());
    }

    /**
     * Does not log the status but show an error dialog with the status message
     */
    public static final void showErrorDialog(final IStatus status) {
        Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.IpsPlugin_titleErrorDialog, null,
                        status);
            }
        });
    }

    /**
     * Getting the platform extension registry. Use this method instead of the static method of
     * {@link Platform} to have the ability to overwrite or mock the extension registry.
     */
    public IExtensionRegistry getExtensionRegistry() {
        return Platform.getExtensionRegistry();
    }

    /**
     * Returns a new document builder.
     * 
     * @throws RuntimeException if the factory throws a ParserConfigurationException. The
     *             ParserConfigurationException is wrapped in a runtime exception as we can't do
     *             anything to resolve it.
     * @deprecated It is not useful to create a new document builder every time you need one. The
     *             only problem is that {@link DocumentBuilder} is not thread safe. To avoid multi
     *             threading problems, use {@link #getDocumentBuilder()} instead.
     */
    @Deprecated
    public DocumentBuilder newDocumentBuilder() {
        try {
            return docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a thread local document builder. This method does not create a new document builder
     * for every call but creating a new document builder for every thread.
     * 
     * @throws RuntimeException if the factory throws a ParserConfigurationException. The
     *             ParserConfigurationException is wrapped in a runtime exception as we can't do
     *             anything to resolve it.
     */
    public DocumentBuilder getDocumentBuilder() {
        return XmlUtil.getDefaultDocumentBuilder();
        //
    }

    /**
     * Returns the IPS model.
     */
    public IIpsModel getIpsModel() {
        return model;
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
     * <strong>FOR INTNERNAL TEST USE ONLY.</strong>
     * <p>
     * Activate or deactivate test mode.
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * <strong>FOR INTNERNAL TEST USE ONLY.</strong>
     * 
     * @param suppress <code>true</code> if logging should be disabled during test execution. The
     *            default behavior is not to suppress logging. However, in some test cases we use
     *            test data with an invalid state which results in exceptions in the error log which
     *            is the correct behavior. However if looking at the error log after all tests have
     *            been run, these "correct" exceptions make it difficult, to see the unexpected
     *            exceptions. You see the exception, and think something has gone wrong. In this
     *            test cases it is appropriate to turn off logging. In the setup of the
     *            <code>AbstractIpsPluginTest</code> logging is explicitly turned on, so there is no
     *            need to reset this flag, after your test method.
     * 
     * @see #log(CoreException)
     * @see #log(IStatus)
     */
    public void setSuppressLoggingDuringTest(boolean suppress) {
        suppressLoggingDuringTestExecution = suppress;
    }

    /**
     * <strong>FOR INTNERNAL TEST USE ONLY.</strong>
     * <p>
     * Returns <code>true</code> when test mode is active. If so, the method getTestAnswerProvider
     * must not return null (which means that setTestAnswerProvider has to be called with a non-null
     * value).
     */
    public boolean isTestMode() {
        return testMode;
    }

    /**
     * <strong>FOR INTNERNAL TEST USE ONLY.</strong>
     * <p>
     * Returns the answer provider for testing purpose.
     */
    public ITestAnswerProvider getTestAnswerProvider() {
        return testAnswerProvider;
    }

    /**
     * <strong>FOR INTNERNAL TEST USE ONLY.</strong>
     * <p>
     * Returns the answer provider for testing purpose.
     */
    public void setTestAnswerProvider(ITestAnswerProvider testAnswerProvider) {
        this.testAnswerProvider = testAnswerProvider;
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
        if (externalTableFormats == null) {
            initExternalTableFormats();
        }
        return externalTableFormats;
    }

    /**
     * Initializes the array of all available table formats.
     */
    private void initExternalTableFormats() {
        IConfigurationElement[] elements = getExtensionRegistry().getConfigurationElementsFor(
                "org.faktorips.devtools.core.externalTableFormat"); //$NON-NLS-1$
        List<ITableFormat> result = new ArrayList<ITableFormat>();
        for (IConfigurationElement element : elements) {
            try {
                ITableFormat format = (ITableFormat)element.createExecutableExtension("class"); //$NON-NLS-1$
                initExternalTableFormat(format, element);
                result.add(format);
            } catch (CoreException e) {
                log(e);
            }
        }
        externalTableFormats = result.toArray(new ITableFormat[result.size()]);
    }

    /**
     * Initialize the given format (fill with values provided by the given formatElement and with
     * <code>IValueConverter</code>s configured in other extension points.
     * 
     * @param format The external table format to initialize.
     * @param formatElement The configuration element which defines the given external table format.
     */
    private void initExternalTableFormat(ITableFormat format, IConfigurationElement formatElement) {
        format.setName(formatElement.getAttribute("name")); //$NON-NLS-1$
        format.setDefaultExtension(formatElement.getAttribute("defaultExtension")); //$NON-NLS-1$

        IConfigurationElement[] elements = getExtensionRegistry().getConfigurationElementsFor(
                "org.faktorips.devtools.core.externalValueConverter"); //$NON-NLS-1$

        for (IConfigurationElement element : elements) {
            String tableFormatId = formatElement.getAttribute("id"); //$NON-NLS-1$
            if (element.getAttribute("tableFormatId").equals(tableFormatId)) { //$NON-NLS-1$") 
                // Converter found for current table format id.
                IConfigurationElement[] valueConverters = element.getChildren();
                for (IConfigurationElement valueConverter : valueConverters) {
                    try {
                        IValueConverter converter = (IValueConverter)valueConverter.createExecutableExtension("class"); //$NON-NLS-1$
                        format.addValueConverter(converter);
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }
            }
        }
    }

    /**
     * Returns the <code>IIpsLoggingFrameworkConnector</code> for the provided id. If no
     * <code>IIpsLoggingFrameworkConnector</code> with the provided id is found <code>null</code>
     * will be returned.
     */
    public IIpsLoggingFrameworkConnector getIpsLoggingFrameworkConnector(String id) {
        IIpsLoggingFrameworkConnector[] builders = getIpsLoggingFrameworkConnectors();
        for (IIpsLoggingFrameworkConnector builder : builders) {
            if (id.equals(builder.getId())) {
                return builder;
            }
        }
        return null;
    }

    /**
     * Returns the <code>IIpsLoggingFrameworkConnector</code> that are registered at the according
     * extension-point.
     */
    public IIpsLoggingFrameworkConnector[] getIpsLoggingFrameworkConnectors() {
        if (loggingFrameworkConnectors == null) {
            ArrayList<IIpsLoggingFrameworkConnector> builders = new ArrayList<IIpsLoggingFrameworkConnector>();
            IExtensionPoint extensionPoint = getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID,
                    "loggingFrameworkConnector"); //$NON-NLS-1$
            IExtension[] extensions = extensionPoint.getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    if ("loggingFrameworkConnector".equals(configElement.getName())) { //$NON-NLS-1$
                        try {
                            IIpsLoggingFrameworkConnector connector = (IIpsLoggingFrameworkConnector)configElement
                                    .createExecutableExtension("class"); //$NON-NLS-1$
                            connector.setId(extension.getUniqueIdentifier() == null ? "" : extension //$NON-NLS-1$
                                    .getUniqueIdentifier());
                            builders.add(connector);

                        } catch (CoreException e) {
                            log(new IpsStatus("Unable to create the log statement builder with the id " //$NON-NLS-1$
                                    + extension.getUniqueIdentifier(), e));
                        }
                    }
                }
            }
            loggingFrameworkConnectors = builders.toArray(new IIpsLoggingFrameworkConnector[builders.size()]);
        }
        return loggingFrameworkConnectors;
    }

    /**
     * Returns the <code>org.faktorips.fl.FunctionResolver</code>s that are registered at the
     * according extension-point.
     */
    public IFunctionResolverFactory[] getFlFunctionResolverFactories() {
        if (flFunctionResolvers == null) {
            ArrayList<IFunctionResolverFactory> flFunctionResolverFactoryList = new ArrayList<IFunctionResolverFactory>();
            IExtensionPoint extensionPoint = getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID,
                    "flFunctionResolverFactory"); //$NON-NLS-1$
            IExtension[] extensions = extensionPoint.getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    if ("functionResolverFactory".equals(configElement.getName())) { //$NON-NLS-1$
                        try {
                            IFunctionResolverFactory functionResolverFactory = (IFunctionResolverFactory)configElement
                                    .createExecutableExtension("class"); //$NON-NLS-1$
                            flFunctionResolverFactoryList.add(functionResolverFactory);

                        } catch (CoreException e) {
                            log(new IpsStatus(
                                    "Unable to create the flfunctionResolverFactory identified by the extension unique identifier: " //$NON-NLS-1$
                                            + extension.getUniqueIdentifier(), e));
                        }
                    }
                }
            }
            flFunctionResolvers = flFunctionResolverFactoryList
                    .toArray(new IFunctionResolverFactory[flFunctionResolverFactoryList.size()]);
        }
        return flFunctionResolvers;
    }

    /**
     * Returns all installed IPS feature version managers.
     */
    public IIpsFeatureVersionManager[] getIpsFeatureVersionManagers() {
        if (featureVersionManagers == null) {
            featureVersionManagers = extensionFactory.createIpsFeatureVersionManagers();
        }
        return featureVersionManagers;
    }

    /**
     * Returns the manager for the feature with the given id or <code>null</code> if no manager was
     * found.
     * 
     * @param featureId The id of the feature the manager has to be returned.
     */
    public IIpsFeatureVersionManager getIpsFeatureVersionManager(String featureId) {
        IIpsFeatureVersionManager[] managers = getIpsFeatureVersionManagers();
        for (IIpsFeatureVersionManager manager : managers) {
            if (manager.getFeatureId().equals(featureId)) {
                return manager;
            }
        }
        return null;
    }

    /**
     * THIS METHOD SHOULD ONLY BE CALLED FROM TEST CASES.
     * <p>
     * Sets the feature version managers. This method overwrites all feature managers registered via
     * extension points.
     */
    public void setFeatureVersionManagers(IIpsFeatureVersionManager[] managers) {
        featureVersionManagers = managers;
    }

    /**
     * Returns a migration operation migrating the content of the given IPS project to match the
     * needs of the current version of Faktor-IPS.
     * 
     * @param projectToMigrate The project the migration operation should be returned for.
     */
    public AbstractIpsFeatureMigrationOperation getMigrationOperation(IIpsProject projectToMigrate)
            throws CoreException {

        IIpsFeatureVersionManager[] managers = getIpsFeatureVersionManagers();
        if (isTestMode()) {
            Object obj = getTestAnswerProvider().getAnswer();
            if (obj instanceof IIpsFeatureVersionManager[]) {
                managers = (IIpsFeatureVersionManager[])obj;
            }
        }

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
     * Get all registered migration operations for a specified contributor name. The contributor
     * name is the symbolic name of the bundle that provides the registered migration operations.
     * 
     * @param contributorName The name of the contributor which provides the requested migration
     *            operations
     * 
     * @return A map containing all registered migration operations. The key of the map is the
     *         target version of the operation
     */
    public Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredMigrationOperations(String contributorName) {
        Map<Version, IIpsProjectMigrationOperationFactory> result = new HashMap<Version, IIpsProjectMigrationOperationFactory>();
        IConfigurationElement[] configurationElements = getExtensionRegistry().getConfigurationElementsFor(
                EXTENSION_POINT_ID_MIGRATION_OPERATION);
        for (IConfigurationElement configElement : configurationElements) {
            if (configElement == null || !configElement.getName().equals(CONFIG_ELEMENT_ID_MIGRATION_OPERATION)) {
                continue;
            }
            if (!contributorName.equals(configElement.getContributor().getName())) {
                continue;
            }
            try {
                IIpsProjectMigrationOperationFactory operation = (IIpsProjectMigrationOperationFactory)configElement
                        .createExecutableExtension("class"); //$NON-NLS-1$
                String targetVersion = configElement.getAttribute("targetVersion"); //$NON-NLS-1$
                result.put(Version.parseVersion(targetVersion), operation);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }

        return result;
    }

    /**
     * Returns the persistence manager of the dependency graphs.
     */
    public DependencyGraphPersistenceManager getDependencyGraphPersistenceManager() {
        return dependencyGraphPersistenceManager;
    }

    /**
     * Returns the {@link ITeamOperationsFactory ITeamOperationsFactories} used to create
     * {@link ITeamOperations} for the {@link ProductReleaseProcessor}.
     */
    public Set<ITeamOperationsFactory> getTeamOperationsFactories() {
        if (teamOperationsFactories == null) {
            teamOperationsFactories = new HashSet<ITeamOperationsFactory>();
            ExtensionPoints extensionPoints = new ExtensionPoints(getExtensionRegistry(), PLUGIN_ID);
            IExtension[] extensions = extensionPoints.getExtension(EXTENSION_POINT_ID_TEAM_OPERATIONS_FACTORY);
            for (IExtension extension : extensions) {
                for (IConfigurationElement configElement : extension.getConfigurationElements()) {
                    ITeamOperationsFactory factory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, "class", ITeamOperationsFactory.class); //$NON-NLS-1$
                    if (factory != null) {
                        teamOperationsFactories.add(factory);
                    }
                }
            }
        }
        return teamOperationsFactories;
    }

}
