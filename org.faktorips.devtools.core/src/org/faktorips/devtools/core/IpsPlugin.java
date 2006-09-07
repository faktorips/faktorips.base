/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsModelManager;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.BooleanControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.DefaultControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.EnumDatatypeControlFactory;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class.
 * 
 * @author Jan Ortmann
 */
public class IpsPlugin extends AbstractUIPlugin {

    public final static String PLUGIN_ID = "org.faktorips.devtools.core"; //$NON-NLS-1$

    public final static String PROBLEM_MARKER = PLUGIN_ID + ".problemmarker"; //$NON-NLS-1$
    
    private boolean testMode = false;
    private ITestAnswerProvider testAnswerProvider; 

    /**
     * Returns the full extension id. This is the plugin's id plus the plugin
     * relative extension id separated by a dot.
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
    
    private IpsModelManager manager;

    // Contains the ips test runner, which runs ips test and informs registered ips test run listener
    private IIpsTestRunner ipsTestRunner;
    
    /**
     * All available external table formats
     */
    private AbstractExternalTableFormat[] externalTableFormats;
    
    // Factories for creating controls depending on the datatype
    private ValueDatatypeControlFactory[] controlFactories = new ValueDatatypeControlFactory[] {
    	new BooleanControlFactory(),
    	new EnumDatatypeControlFactory(),
    	new DefaultControlFactory()
    };
    
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
        manager = new IpsModelManager();
        ((IpsModel)getIpsModel()).startListeningToResourceChanges();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        ((IpsModel)getIpsModel()).stopListeningToResourceChanges();
        manager = null;
        if (imageDescriptorRegistry != null) {
            imageDescriptorRegistry.dispose();
        }
    }
    
    /**
     * Reinits the model (so all data in the cache is cleared). Should only be called in test cases to ensure
     * a clean environment.
     */
    public void reinitModel() {
        ((IpsModel)getIpsModel()).stopListeningToResourceChanges();
        manager = new IpsModelManager();
        ((IpsModel)getIpsModel()).startListeningToResourceChanges();
    }
    
    /**
     * Returns the plugin's version identifier.
     */
    public PluginVersionIdentifier getVersionIdentifier() {
        String version = (String) getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
        return new PluginVersionIdentifier(version); 
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
     * Returns the image with the indicated name form the <code>icons</code> folder. If no
     * image is found and <code>returnNull</code> is true, null is returned. Otherwise
     * (no image found, but <code>returnNull</code> is true), the missing image is returned.
     * 
     * @param name The name of the image.
     * @param returnNull <code>true</code> to get null as return value if the image is not found,
     * <code>false</code> to get the missing image in this case.
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
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Returns the IPS model.
     */
    public IIpsModel getIpsModel() {
        return getManager().getModel();
    }

    /**
     * Returns the IpsModelManager single instance.
     */
    public final IpsModelManager getManager() {
        return manager;
    }
    
    /**
     * Returns preferences for this plugin.
     */
    public IpsPreferences getIpsPreferences() {
    	return preferences;
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
     * Returns <code>true</code> when test mode is active. If so,
     * the method getTestAnswerProvider must not return null 
     * (which means that setTestAnswerProvider has to be called
     * with a non-null value).
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
     * Returns the locale used by the localization. The returned locale is not
     * the locale the localization <strong>should</strong> use, it is the locale
     * the localization <strong>can</strong> use. That means if the default locale
     * this plugin runs is for example de_DE, but no language pack for german is installed,
     * the localization uses the english language, and this method will return the
     * Locale for "en".
     */
    public Locale getUsedLanguagePackLocale() {
    	Locale retValue = new Locale(Messages.IpsPlugin_languagePackLanguage, Messages.IpsPlugin_languagePackCountry, Messages.IpsPlugin_languagePackVariant);
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
    public IIpsTestRunner getIpsTestRunner(){
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
    	IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.faktorips.devtools.core.externalTableFormat"); //$NON-NLS-1$
    	List result = new ArrayList();
    	for (int i = 0; i < elements.length; i++) {
			try {
				AbstractExternalTableFormat format = (AbstractExternalTableFormat)elements[i].createExecutableExtension("class"); //$NON-NLS-1$
				initExternalTableFormat(format, elements[i]);
				result.add(format);
			} catch (CoreException e) {
				log(e);
			}
		}
    	externalTableFormats = (AbstractExternalTableFormat[])result.toArray(new AbstractExternalTableFormat[result.size()]);
    }

	/**
	 * Initialize the given format (fill with values provided by the given formatElement and 
	 * with <code>IValueConverter</code>s configured in other extension points.
	 * 
	 * @param format The external table format to initialize.
	 * @param formatElement The configuration element which defines the given external table format.
	 */
	private void initExternalTableFormat(AbstractExternalTableFormat format, IConfigurationElement formatElement) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID, "externalValueConverter"); //$NON-NLS-1$
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
						format.addValueConverter((IValueConverter)elements[j]
								.createExecutableExtension("class")); //$NON-NLS-1$
					} catch (CoreException e) {
						IpsPlugin.log(e);
					}
				}
			}

		}
	}
    
	/**
     * Opens an editor for the IpsObject contained in the given IpsSrcFile.  
     * @param srcFile
	 */
    public void openEditor(IIpsSrcFile srcFile){
        if(srcFile==null){
            return;
        }
        openEditor(srcFile.getCorrespondingFile());
    }
    /**
     * Opens the given IpsObject in its editor.  
     * @param srcFile
     */
    public void openEditor(IIpsObject ipsObject){
        if(ipsObject==null){
            return;
        }
        openEditor(ipsObject.getIpsSrcFile());
    }
    /**
     * Opens the file referenced by the given IFile in an editor. The type of editor to be opened
     * is derived from the file-extension using the editor-registry. If no entry is existent, 
     * the workbench guesses the filetype by looking at the file's content and opens the corresponding
     * editor.
     * @see IDE#openEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.core.resources.IFile)
     * @param fileToEdit
     */
    public void openEditor(IFile fileToEdit){
        if(fileToEdit==null){
            return;
        }
        try {
            IWorkbench workbench= IpsPlugin.getDefault().getWorkbench();
            IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), fileToEdit, true, true);
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
}
