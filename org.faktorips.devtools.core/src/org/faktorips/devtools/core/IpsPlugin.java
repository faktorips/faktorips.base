package org.faktorips.devtools.core;

import java.net.URL;

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
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsModelManager;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class.
 * 
 * @author Jan Ortmann
 */
public class IpsPlugin extends AbstractUIPlugin {

    public final static String PLUGIN_ID = "org.faktorips.devtools.core";

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

    private IpsModelManager manager;

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
    void reinitModel() {
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
        Image image = getImageRegistry().get(name);
        if (image == null) {
            URL url = getBundle().getEntry("icons/" + name);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            if (descriptor == null) {
                descriptor = ImageDescriptor.getMissingImageDescriptor();
            }
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
        String iconPath = "icons/";
        URL url = getBundle().getEntry(iconPath + '/' + name);
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
        ErrorDialog.openError(Display.getCurrent().getActiveShell(), "OpenIPS",
            "An unexpected program error has occured!", status);
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
}
