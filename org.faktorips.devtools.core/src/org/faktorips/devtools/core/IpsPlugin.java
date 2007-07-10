/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ArchiveIpsSrcFile;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.ProductRelevantIcon;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.internal.model.versionmanager.IpsContentMigrationOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsContentMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IpsFeatureVersionManagerSorter;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.BooleanControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.DefaultControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.EnumDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditFieldChangesBroadcaster;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.IpsArchiveEditorInput;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.devtools.extsystems.IValueConverter;
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
    private AbstractExternalTableFormat[] externalTableFormats;

    // All available feature version managers
    private IIpsFeatureVersionManager[] featureVersionManagers;
    
    private IIpsLoggingFrameworkConnector[] loggingFrameworkConnectors;

    // Factories for creating controls depending on the datatype
    private ValueDatatypeControlFactory[] controlFactories = new ValueDatatypeControlFactory[] {
            new BooleanControlFactory(), new EnumDatatypeControlFactory(), new DefaultControlFactory() };

    // Broadcaster for broadcasting delayed change events triggerd by edit fields
    private EditFieldChangesBroadcaster editFieldChangeBroadcaster;

    private IpsObjectEditorSettings ipsEditorSettings;
    
    private boolean testMode = false;
    private ITestAnswerProvider testAnswerProvider;

    // Manager to update ips problem marker
    private IpsProblemMarkerManager ipsProblemMarkerManager;
    
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
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        preferences = new IpsPreferences(getPreferenceStore());
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        ipsEditorSettings = new IpsObjectEditorSettings();
        ResourcesPlugin.getWorkspace().addSaveParticipant(this, ipsEditorSettings);
        ipsEditorSettings.load(getStateLocation());
        model = new IpsModel();
        model.startListeningToResourceChanges();
    }

    /**
     * This method is called when the plug-in is stopped
     */
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
     * Returns the image with the indicated name from the <code>icons</code> folder and overlays
     * it with the product relevant image. If the given image is not found return the missing image
     * overlayed with the product relevant image.
     * 
     * @see IpsPlugin#getImage(String)
     * 
     * @param baseImageName The name of the image which will be overlayed with the product relevant
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
     * Returns the image with the indicated name from the <code>icons</code> folder. If no image
     * is found and <code>returnNull</code> is true, null is returned. Otherwise (no image found,
     * but <code>returnNull</code> is true), the missing image is returned.
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
        // must use lazy initilization, as the current display is not neccessarily
        // available when the plugin is started.
        if (this.imageDescriptorRegistry == null) {
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
    public final static void log(Exception e) {
        log(new IpsStatus(e));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public final static void logAndShowErrorDialog(IStatus status) {
        plugin.getLog().log(status);
        ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.IpsPlugin_titleErrorDialog,
                Messages.IpsPlugin_msgUnexpectedError, status);
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
        }
        catch (ParserConfigurationException e) {
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
     * Returns the settings for ips object editors.
     * @return
     */
    public IIpsObjectEditorSettings getIpsEditorSettings() {
        return ipsEditorSettings;
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
     * localization <strong>should</strong> use, it is the locale the localization <strong>can</strong>
     * use. That means if the default locale this plugin runs is for example de_DE, but no language
     * pack for german is installed, the localization uses the english language, and this method
     * will return the Locale for "en".
     */
    public Locale getUsedLanguagePackLocale() {
        Locale retValue = new Locale(Messages.IpsPlugin_languagePackLanguage, Messages.IpsPlugin_languagePackCountry,
                Messages.IpsPlugin_languagePackVariant);
        return retValue;
    }

    /**
     * Returns a control factory that can create controls (and edit fields) for the given datatype.
     * 
     * @throws RuntimeException if no factory is found for the given datatype.
     */
    public ValueDatatypeControlFactory getValueDatatypeControlFactory(ValueDatatype datatype) {
        ValueDatatypeControlFactory[] factories = getValueDatatypeControlFactories();
        for (int i = 0; i < factories.length; i++) {
            if (factories[i].isFactoryFor(datatype)) {
                return factories[i];
            }
        }
        throw new RuntimeException(Messages.IpsPlugin_errorNoDatatypeControlFactoryFound + datatype);
    }

    /**
     * Returns all controls factories.
     */
    // TODO control factories sollten ueber einen extension point definiert sein und geladen werden.
    private ValueDatatypeControlFactory[] getValueDatatypeControlFactories() {
        return controlFactories;
    }

    /**
     * Returns the ips test runner.
     */
    public IIpsTestRunner getIpsTestRunner() {
        if (ipsTestRunner == null)
            ipsTestRunner = IpsTestRunner.getDefault();

        return ipsTestRunner;
    }

    /**
     * @return An array of all available external table formats.
     */
    public AbstractExternalTableFormat[] getExternalTableFormats() {
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
        List result = new ArrayList();
        for (int i = 0; i < elements.length; i++) {
            try {
                AbstractExternalTableFormat format = (AbstractExternalTableFormat)elements[i]
                        .createExecutableExtension("class"); //$NON-NLS-1$
                initExternalTableFormat(format, elements[i]);
                result.add(format);
            }
            catch (CoreException e) {
                log(e);
            }
        }
        externalTableFormats = (AbstractExternalTableFormat[])result.toArray(new AbstractExternalTableFormat[result
                .size()]);
    }

    /**
     * Initialize the given format (fill with values provided by the given formatElement and with
     * <code>IValueConverter</code>s configured in other extension points.
     * 
     * @param format The external table format to initialize.
     * @param formatElement The configuration element which defines the given external table format.
     */
    private void initExternalTableFormat(AbstractExternalTableFormat format, IConfigurationElement formatElement) {
        IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID,
                "externalValueConverter"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        format.setName(formatElement.getAttribute("name")); //$NON-NLS-1$
        format.setDefaultExtension(formatElement.getAttribute("defaultExtension")); //$NON-NLS-1$

        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] elements = extensions[i].getConfigurationElements();
            boolean found = false;
            for (int j = 0; j < elements.length && !found; j++) {
                found = elements[j].getAttribute("id").equals(formatElement.getAttribute("id")); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
            }

            for (int j = 0; j < elements.length && found; j++) {
                if (elements[j].getName().equals("externalValueConverter")) { //$NON-NLS-1$
                    try {
                        format.addValueConverter((IValueConverter)elements[j].createExecutableExtension("class")); //$NON-NLS-1$
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
            ArrayList builders = new ArrayList();
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
            loggingFrameworkConnectors = (IIpsLoggingFrameworkConnector[])builders
                    .toArray(new IIpsLoggingFrameworkConnector[builders.size()]);
        }
        return loggingFrameworkConnectors;
    }
    
    /**
     * Opens an editor for the IpsObject contained in the given IpsSrcFile.
     * 
     * @param srcFile
     */
    public void openEditor(IIpsSrcFile srcFile) {
        if (srcFile == null) {
            return;
        }
        if (srcFile instanceof ArchiveIpsSrcFile) {
            IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
            IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(srcFile.getName());
            IpsArchiveEditorInput input = new IpsArchiveEditorInput(srcFile);
            try {
                IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), input, editor.getId());
            }
            catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        else {
            openEditor(srcFile.getCorrespondingFile());
        }
    }

    /**
     * Opens the given IpsObject in its editor.
     * 
     * @param srcFile
     */
    public void openEditor(IIpsObject ipsObject) {
        if (ipsObject == null) {
            return;
        }
        openEditor(ipsObject.getIpsSrcFile());
    }

    /**
     * Opens the file referenced by the given IFile in an editor. The type of editor to be opened is
     * derived from the file-extension using the editor-registry. If no entry is existent, the
     * workbench opens the default editor as defined in the preferences/file-associations. If none
     * is specified the workbench guesses the filetype by looking at the file's content and opens
     * the corresponding editor.
     * <p>
     * <code>IFile</code>s containing <code>IpsSrcFiles</code>s and thus
     * <code>IpsObject</code>s are always opened in the corresponding IpsObjectEditor.
     * 
     * @see IDE#openEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.core.resources.IFile)
     * @param fileToEdit
     */
    public void openEditor(IFile fileToEdit) {
        if (fileToEdit == null) {
            return;
        }
        // check if the file can be edit with a corresponding ips object editor,
        // if the file is outside an ips package then the ips object editor couldn't be used
        // - the ips object could not be retrieved from the ips src file - 
        // therefore open the default text editor (to edit the ips src file as xml)
        IIpsModel model = IpsPlugin.getDefault().getIpsModel();
        IIpsElement ipsElement = model.getIpsElement(fileToEdit);
        if (ipsElement instanceof IIpsSrcFile && !((IIpsSrcFile)ipsElement).exists()) {
            try {
                openWithDefaultIpsSrcTextEditor(fileToEdit);
            }
            catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        else {
            try {
                IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
                IFileEditorInput editorInput = new FileEditorInput(fileToEdit);
                /*
                 * For known filetypes always use the registered editor, NOT the editor specified by
                 * the preferences/file-associations. This ensures that, when calling this method,
                 * IpsObjects are always opened in their IpsObjectEditor and never in an xml-editor
                 * (which might be the default editor for the given file).
                 */
                IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(fileToEdit.getName());
                if (editor != null & editorInput != null) {
                    workbench.getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editor.getId());
                }
                else {
                    /*
                     * For unknown files let IDE open the corresponding editor. This method searches
                     * the preferences/file-associations for an editor (default editor) and if none
                     * is found guesses the filetype by looking at the contents of the given file.
                     */
                    IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), fileToEdit, true, true);
                }
            }
            catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /*
     * Open the given file with the default text editor. And show an information message in the editors
     * status bar to inform the user about using the text editor instead of the ips object editor.
     */
    private void openWithDefaultIpsSrcTextEditor(IFile fileToEdit) throws CoreException {
        String defaultContentTypeOfIpsSrcFilesId = "org.faktorips.devtools.core.ipsSrcFile"; //$NON-NLS-1$
        IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
        IFileEditorInput editorInput = new FileEditorInput(fileToEdit);

        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        IContentType contentType = contentTypeManager.getContentType(defaultContentTypeOfIpsSrcFilesId);

        IEditorDescriptor[] editors = workbench.getEditorRegistry().getEditors("", contentType); //$NON-NLS-1$
        if (editors.length != 1) {
            throw new CoreException(new IpsStatus(NLS.bind(
                    "No registered editors (or more then one) for content-type id {0} found!", //$NON-NLS-1$
                    defaultContentTypeOfIpsSrcFilesId)));
        }
        try {
            IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
            if (page == null){
                return;
            }
            IEditorPart editorPart = page.openEditor(editorInput,
                    editors[0].getId());
            if (editorPart == null) {
                throw new CoreException(new IpsStatus("Error opening the default text editor!!")); //$NON-NLS-1$
            }
            // show information in the status bar about using the default text editor instead of
            // using the default ips object editor
            ((IEditorSite)editorPart.getSite()).getActionBars().getStatusLineManager().setMessage(
                    IpsPlugin.getDefault().getImage("size8/InfoMessage.gif"), //$NON-NLS-1$
                    Messages.IpsPlugin_infoDefaultTextEditorWasOpened);
        }
        catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * @return All installed ips feature version managers.
     */
    public IIpsFeatureVersionManager[] getIpsFeatureVersionManagers() {
        if (featureVersionManagers == null) {
            IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    "org.faktorips.devtools.core.faktorIpsFeatureVersionManager"); //$NON-NLS-1$
            List result = new ArrayList();
            for (int i = 0; i < elements.length; i++) {
                try {
                    IIpsFeatureVersionManager manager = (IIpsFeatureVersionManager)elements[i]
                            .createExecutableExtension("class"); //$NON-NLS-1$
                    manager.setFeatureId(elements[i].getAttribute("featureId")); //$NON-NLS-1$
                    manager.setId(elements[i].getAttribute("id")); //$NON-NLS-1$
                    manager.setPredecessorId(elements[i].getAttribute("basedOnFeatureManager")); //$NON-NLS-1$
                    result.add(manager);
                }
                catch (CoreException e) {
                    log(e);
                }
            }
            featureVersionManagers = (IIpsFeatureVersionManager[])result.toArray(new IIpsFeatureVersionManager[result
                    .size()]);
        }

        return featureVersionManagers;
    }

    /**
     * @param featureId The id of the feature the manager has to be returned.
     * @return The manager for the feature with the given id or <code>null</code> if no manager
     *         was found.
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
     * @param projectToMigrate The project the migration operation should be returned for.
     * @return A migration operation migrating the content of the given IpsProject to match the
     *         needs of the current version of FaktorIps.
     * @throws CoreException
     */
    public AbstractIpsContentMigrationOperation getMigrationOperation(IIpsProject projectToMigrate)
            throws CoreException {
        
        
        IIpsFeatureVersionManager[] managers = getIpsFeatureVersionManagers();
        if (isTestMode()) {
            Object obj = getTestAnswerProvider().getAnswer();
            if (obj instanceof IIpsFeatureVersionManager[]) {
                managers = (IIpsFeatureVersionManager[])obj;
            }
        }

        // sort the managers to match the required order given by the basedOnFeatureManager-property
        // of the extension-point.
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        managers = sorter.sortForMigartionOrder(managers);
        
        IpsContentMigrationOperation operation = new IpsContentMigrationOperation(projectToMigrate);
        for (int i = 0; i < managers.length; i++) {
            operation.addMigrationPath(managers[i].getMigrationOperations(projectToMigrate));
        }
        return operation;
    }
    
    /**
     * Returns the edit field change broadcaster.
     */
    public EditFieldChangesBroadcaster getEditFieldChangeBroadcaster(){
        if (editFieldChangeBroadcaster == null){
            editFieldChangeBroadcaster = new EditFieldChangesBroadcaster();
        }
        return editFieldChangeBroadcaster;
    }

    /**
     * Returns the ips problem marker manager which manages ips marker updates.
     */
    public IpsProblemMarkerManager getIpsProblemMarkerManager() {
        if (ipsProblemMarkerManager == null){
            ipsProblemMarkerManager = new IpsProblemMarkerManager();
        }
        return ipsProblemMarkerManager;
    }
}
