/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.devtools.core.builder.DependencyGraphPersistenceManager;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.ProductRelevantIcon;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.internal.model.versionmanager.IpsFeatureMigrationOperation;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IpsFeatureVersionManagerSorter;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * The main plugin class.
 * 
 * @author Jan Ortmann
 */
public class IpsPlugin extends AbstractUIPlugin {

    public final static String PLUGIN_ID = "org.faktorips.devtools.core"; //$NON-NLS-1$

    public final static String PROBLEM_MARKER = PLUGIN_ID + ".problemmarker"; //$NON-NLS-1$

    public final static boolean TRACE_UI = Boolean.valueOf(
            Platform.getDebugOption("org.faktorips.devtools.core/trace/ui")).booleanValue(); //$NON-NLS-1$

    /**
     * Returns the full extension id. This is the plugin's id plus the plugin relative extension id
     * separated by a dot.
     * 
     * @throws NullPointerException if pluginRelativeEnxtensionId is <code>null</code>.
     */
    public final static String getFullExtensionId(String pluginRelativeEnxtensionId) {
        ArgumentCheck.notNull(pluginRelativeEnxtensionId);
        return PLUGIN_ID + '.' + pluginRelativeEnxtensionId;
    }

    // The shared instance.
    private static IpsPlugin plugin;

    // registry for image descriptors
    private ImageDescriptorRegistry imageDescriptorRegistry;

    // the document builder factory provides the DocumentBuilder for this plugin
    private DocumentBuilderFactory docBuilderFactory;

    private IpsPreferences preferences;

    private IpsModel model;

    // Contains the ips test runner, which runs ips test and informs registered ips test run
    // listener
    private IIpsTestRunner ipsTestRunner;

    // All available external table formats
    private ITableFormat[] externalTableFormats;

    // All available feature version managers
    private IIpsFeatureVersionManager[] featureVersionManagers;

    private IIpsLoggingFrameworkConnector[] loggingFrameworkConnectors;

    private IFunctionResolverFactory[] flFunctionResolvers;

    private boolean testMode = false;
    private ITestAnswerProvider testAnswerProvider;

    private DependencyGraphPersistenceManager dependencyGraphPersistenceManager;

    /**
     * Returns the number of the installed Faktor-IPS version.
     */
    public final static String getInstalledFaktorIpsVersion() {
        return (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the shared instance.
     */
    public static IpsPlugin getDefault() {
        return plugin;
    }

    /**
     * The constructor.
     */
    public IpsPlugin() {
        super();
        plugin = this;
    }

    /**
     * Logs the core exception
     */
    public final static void log(CoreException e) {
        log(e.getStatus());
    }

    /**
     * Returns the version of the Faktor-IPS feature. If the Faktor-IPS feature is not found
     * <code>null</code> will be returned.
     */
    public String getIpsFeatureVersion() {
        IBundleGroupProvider[] bundleGroupProviders = Platform.getBundleGroupProviders();
        for (int i = 0; i < bundleGroupProviders.length; i++) {
            IBundleGroup[] bundleGroups = bundleGroupProviders[i].getBundleGroups();
            for (int j = 0; j < bundleGroups.length; j++) {
                if (bundleGroups[i].getIdentifier().equals("org.faktorips.feature")) { //$NON-NLS-1$
                    return bundleGroups[i].getVersion();
                }
            }
        }
        return null;
    }

    /**
     * This method is called upon plug-in activation
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

        IpsObjectType.POLICY_CMPT_TYPE.getId(); // force loading of class before model is created!
        // ensure that this class is loaded in time
        BFElementType.ACTION_BUSINESSFUNCTIONCALL.getClass();
        model = new IpsModel();
        model.startListeningToResourceChanges();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        ((IpsModel)getIpsModel()).stopListeningToResourceChanges();
        model = null;
        if (imageDescriptorRegistry != null) {
            imageDescriptorRegistry.dispose();
        }
    }

    /**
     * Reinits the model (so all data in the cache is cleared). Should only be called in test cases
     * to ensure a clean environment.
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
     * Returns the image with the indicated name from the <code>icons</code> folder. If no image
     * with the indicated name is found, a missing image is returned.
     * 
     * @param name The image name, e.g. <code>IpsProject.gif</code>
     */
    public Image getImage(String name) {
        return getImage(name, false);
    }

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder and overlays it
     * with the product relevant image. If the given image is not found return the missing image
     * overlaid with the product relevant image.
     * 
     * @see IpsPlugin#getImage(String)
     * 
     * @param baseImageName The name of the image which will be overlaid with the product relevant
     *            image.
     */
    public Image getProductRelevantImage(String baseImageName) {
        String overlayedImageName = "ProductRelevantOverlay.gif_" + baseImageName; //$NON-NLS-1$
        Image image = getImageRegistry().get(overlayedImageName);
        if (image == null) {
            image = ProductRelevantIcon.createProductRelevantImage(getImage(baseImageName));
            ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(image);
            getImageRegistry().put(overlayedImageName, imageDescriptor);
        }
        return image;
    }

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder. If no image is
     * found and <code>returnNull</code> is true, null is returned. Otherwise (no image found, but
     * <code>returnNull</code> is false), the missing image is returned.
     * 
     * @param name The name of the image.
     * @param returnNull <code>true</code> to get null as return value if the image is not found,
     *            <code>false</code> to get the missing image in this case.
     */
    public Image getImage(String name, boolean returnNull) {
        Image image = getImageRegistry().get(name);
        if (image == null) {
            URL url = getBundle().getEntry("icons/" + name); //$NON-NLS-1$
            if (url == null && returnNull) {
                return null;
            }
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            getImageRegistry().put(name, descriptor);
            image = getImageRegistry().get(name);
        }
        return image;
    }

    public Image getImage(ImageDescriptor descriptor) {
        return getImageDescriptorRegistry().get(descriptor);
    }

    private ImageDescriptorRegistry getImageDescriptorRegistry() {
        // must use lazy initialization, as the current display is not necessarily
        // available when the plugin is started.
        if (imageDescriptorRegistry == null) {
            imageDescriptorRegistry = new ImageDescriptorRegistry(Display.getCurrent());
        }
        return imageDescriptorRegistry;
    }

    public ImageDescriptor getImageDescriptor(String name) {
        URL url = getBundle().getEntry("icons/" + name); //$NON-NLS-1$
        return ImageDescriptor.createFromURL(url);
    }

    /**
     * Logs the status.
     */
    public final static void log(IStatus status) {
        plugin.getLog().log(status);
    }

    /**
     * Logs the exception
     */
    public final static void log(Throwable t) {
        log(new IpsStatus(t));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public final static void logAndShowErrorDialog(final IStatus status) {
        plugin.getLog().log(status);
        Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        display.asyncExec(new Runnable() {
            public void run() {
                ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.IpsPlugin_titleErrorDialog,
                        Messages.IpsPlugin_msgUnexpectedError, status);
            }
        });
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public final static void logAndShowErrorDialog(Exception e) {
        logAndShowErrorDialog(new IpsStatus(e));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public final static void logAndShowErrorDialog(CoreException e) {
        logAndShowErrorDialog(e.getStatus());
    }

    /**
     * Returns a new document builder.
     * 
     * @throws RuntimeException if the factory throws a ParserConfigurationException. The
     *             ParserConfigurationException is wrapped in a runtime exception as we can't do
     *             anything to resolve it.
     */
    public DocumentBuilder newDocumentBuilder() {
        try {
            return docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Returns the IPS model.
     */
    public IIpsModel getIpsModel() {
        return model;
    }

    /**
     * Returns preferences for this plugin.
     */
    public IpsPreferences getIpsPreferences() {
        return preferences;
    }

    /**
     * /** <strong>FOR INTNERNAL TEST USE ONLY.</strong>
     * <p>
     * Activate or deactivate test mode.
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
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
     * localization <strong>should</strong> use, it is the locale the localization
     * <strong>can</strong> use. That means if the default locale this plugin runs is for example
     * de_DE, but no language pack for german is installed, the localization uses the english
     * language, and this method will return the Locale for "en".
     */
    public Locale getUsedLanguagePackLocale() {
        Locale retValue = new Locale(Messages.IpsPlugin_languagePackLanguage, Messages.IpsPlugin_languagePackCountry,
                Messages.IpsPlugin_languagePackVariant);
        return retValue;
    }

    /**
     * Returns the ips test runner.
     */
    public IIpsTestRunner getIpsTestRunner() {
        if (ipsTestRunner == null) {
            ipsTestRunner = IpsTestRunner.getDefault();
        }

        return ipsTestRunner;
    }

    /**
     * @return An array of all available external table formats.
     */
    public ITableFormat[] getExternalTableFormats() {
        if (externalTableFormats == null) {
            initExternalTableFormats();
        }
        return externalTableFormats;
    }

    /**
     * Initialize the array of all available table formats
     */
    private void initExternalTableFormats() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "org.faktorips.devtools.core.externalTableFormat"); //$NON-NLS-1$
        List<ITableFormat> result = new ArrayList<ITableFormat>();
        for (int i = 0; i < elements.length; i++) {
            try {
                ITableFormat format = (ITableFormat)elements[i].createExecutableExtension("class"); //$NON-NLS-1$
                initExternalTableFormat(format, elements[i]);
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

        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "org.faktorips.devtools.core.externalValueConverter"); //$NON-NLS-1$

        IConfigurationElement tableFormatElement = null;
        for (int i = 0; i < elements.length; i++) {
            String tableFormatId = formatElement.getAttribute("id");
            if (elements[i].getAttribute("tableFormatId").equals(tableFormatId)) { //$NON-NLS-1$") 
                // converter found for current table format id
                tableFormatElement = elements[i];
                break;
            }
        }

        IConfigurationElement[] valueConverters = tableFormatElement.getChildren();
        for (int i = 0; i < valueConverters.length; i++) {
            try {
                IValueConverter converter = (IValueConverter)valueConverters[i].createExecutableExtension("class"); //$NON-NLS-1$
                format.addValueConverter(converter);
            } catch (CoreException e) {
                IpsPlugin.log(e);
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
        for (int i = 0; i < builders.length; i++) {
            if (id.equals(builders[i].getId())) {
                return builders[i];
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
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID,
                    "loggingFrameworkConnector"); //$NON-NLS-1$
            IExtension[] extensions = extensionPoint.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                IExtension extension = extensions[i];
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (int j = 0; j < configElements.length; j++) {
                    IConfigurationElement configElement = configElements[j];
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
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID,
                    "flFunctionResolverFactory"); //$NON-NLS-1$
            IExtension[] extensions = extensionPoint.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                IExtension extension = extensions[i];
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (int j = 0; j < configElements.length; j++) {
                    IConfigurationElement configElement = configElements[j];
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
     * @return All installed ips feature version managers.
     */
    public IIpsFeatureVersionManager[] getIpsFeatureVersionManagers() {
        if (featureVersionManagers == null) {
            IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    "org.faktorips.devtools.core.faktorIpsFeatureVersionManager"); //$NON-NLS-1$
            List<IIpsFeatureVersionManager> result = new ArrayList<IIpsFeatureVersionManager>();
            for (int i = 0; i < elements.length; i++) {
                try {
                    IIpsFeatureVersionManager manager = (IIpsFeatureVersionManager)elements[i]
                            .createExecutableExtension("class"); //$NON-NLS-1$
                    manager.setFeatureId(elements[i].getAttribute("featureId")); //$NON-NLS-1$
                    manager.setId(elements[i].getAttribute("id")); //$NON-NLS-1$
                    manager.setPredecessorId(elements[i].getAttribute("basedOnFeatureManager")); //$NON-NLS-1$
                    result.add(manager);
                } catch (CoreException e) {
                    log(e);
                }
            }
            featureVersionManagers = result.toArray(new IIpsFeatureVersionManager[result.size()]);
            if (featureVersionManagers.length == 0) {
                featureVersionManagers = new IIpsFeatureVersionManager[] { EmptyIpsFeatureVersionManager.INSTANCE };
            }
        }
        return featureVersionManagers;
    }

    /**
     * @param featureId The id of the feature the manager has to be returned.
     * @return The manager for the feature with the given id or <code>null</code> if no manager was
     *         found.
     */
    public IIpsFeatureVersionManager getIpsFeatureVersionManager(String featureId) {
        IIpsFeatureVersionManager[] managers = getIpsFeatureVersionManagers();
        for (int i = 0; i < managers.length; i++) {
            if (managers[i].getFeatureId().equals(featureId)) {
                return managers[i];
            }
        }
        return null;
    }

    /**
     * THIS METHOD SHOULD ONLY BE CALLED FROM TEST CASES.
     * 
     * Sets the feature version managers. This method overwrites all feature managers registered via
     * extension points.
     * 
     * @param managers
     */
    public void setFeatureVersionManagers(IIpsFeatureVersionManager[] managers) {
        featureVersionManagers = managers;
    }

    /**
     * @param projectToMigrate The project the migration operation should be returned for.
     * @return A migration operation migrating the content of the given IpsProject to match the
     *         needs of the current version of FaktorIps.
     * @throws CoreException
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
        for (int i = 0; i < managers.length; i++) {
            operation.addMigrationPath(managers[i].getMigrationOperations(projectToMigrate));
        }
        return operation;
    }

    /**
     * Returns the persistence manager of the dependency graphs.
     */
    public DependencyGraphPersistenceManager getDependencyGraphPersistenceManager() {
        return dependencyGraphPersistenceManager;
    }

}
