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
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsModelManager;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.BooleanControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.DefaultControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.EnumDatatypeControlFactory;
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

    /** Factories for creating controls depending on the datatype */
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

    public IEditorPart openEditor(IIpsSrcFile srcFile) throws PartInitException {
        IFile file = srcFile.getCorrespondingFile();
        IFileEditorInput editorInput = new FileEditorInput(file);
        IEditorDescriptor editor = getWorkbench().getEditorRegistry().getDefaultEditor(
            file.getName());
        return getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
            editor.getId());
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
}
