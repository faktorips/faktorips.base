/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsCompositeSaveParticipant;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.Messages;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.controlfactories.DefaultControlFactory;
import org.faktorips.devtools.core.ui.controller.EditFieldChangesBroadcaster;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog.IpsObjectSelectionHistory;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.IpsArchiveEditorInput;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.filter.IProductCmptPropertyFilter;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.inputformat.DatatypeInputFormatRegistry;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DefaultDeepCopySmartModeBehavior;
import org.faktorips.devtools.core.ui.wizards.deepcopy.IAdditionalDeepCopyWizardPage;
import org.faktorips.devtools.core.ui.wizards.deepcopy.IDeepCopySmartModeBehavior;
import org.faktorips.devtools.core.ui.workbenchadapters.IWorkbenchAdapterProvider;
import org.faktorips.devtools.core.ui.workbenchadapters.IpsElementWorkbenchAdapter;
import org.faktorips.devtools.core.ui.workbenchadapters.IpsElementWorkbenchAdapterAdapterFactory;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class IpsUIPlugin extends AbstractUIPlugin {

    /** The plug-in ID. */
    public static final String PLUGIN_ID = "org.faktorips.devtools.core.ui"; //$NON-NLS-1$

    public static final String EXTENSION_POINT_ID_PRODUCT_CMPT_PROPERTY_FILTER = "productCmptPropertyFilter"; //$NON-NLS-1$

    /**
     * The simple extension point id of the extension point
     * <code>extensionPropertyEditFieldFactory</code>.
     */
    public static final String EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY = "extensionPropertyEditFieldFactory"; //$NON-NLS-1$

    /**
     * The simple extension point id of the extension point <code>inputFormat</code>.
     */
    public static final String EXTENSION_POINT_INPUT_FORMAT = "inputFormat"; //$NON-NLS-1$

    /**
     * The simple extension point id of the extension point <code>extensionPropertySectionFactory</code>
     * .
     */
    public static final String EXTENSION_POINT_ID_EXTENSION_PROPERTY_SECTION_FACTORY = "extensionPropertySectionFactory"; //$NON-NLS-1$

    /**
     * The extension point id of the extension point <code>adapterprovider</code>.
     */
    public static final String EXTENSION_POINT_ID_ADAPTER_PROVIDER = "adapterprovider"; //$NON-NLS-1$

    /**
     * Extension point id for adding external drop support (see {@link IIpsDropAdapterProvider}
     */
    public static final String EXTENSION_POINT_ID_IPS_DROP_ADAPTER_PROVIDER = "ipsDropAdapterProvider"; //$NON-NLS-1$

    /**
     * The extension point id of the extension point property <code>workbenchadapter</code> in the
     * extension point adapterprovider.
     */
    public static final String CONFIG_ELEMENT_ID_WORKBENCHADAPTER_PROVIDER = "workbenchadapter"; //$NON-NLS-1$

    /**
     * Class property in extension point config elements
     */
    public static final String CONFIG_PROPERTY_CLASS = "class"; //$NON-NLS-1$

    /**
     * The suffix that is used for the expanded state preference of {@link IpsSection}s. The full ID
     * is constructed using the section ID plus this suffix.
     */
    public static final String PREFERENCE_ID_SUFFIX_SECTION_EXPANDED = "_expanded"; //$NON-NLS-1$

    /**
     * Preference key for the current working date, stored as milliseconds since start of the Unix
     * epoch.
     */
    public static final String PREFERENCE_ID_DEFAULT_VALIDITY_DATE = "defaultValidityDate"; //$NON-NLS-1$

    /**
     * Setting key for the open ips object history
     */
    private static final String OPEN_IPS_OBJECT_HISTORY_SETTINGS = PLUGIN_ID + "OpenTypeHistory"; //$NON-NLS-1$

    /** key for the history setting entry in the open ips object history settings */
    private static final String HISTORY_SETTING = "History"; //$NON-NLS-1$

    /** The shared instance. */
    private static IpsUIPlugin plugin;

    private static IExtensionRegistry registry;

    private static List<IWorkbenchAdapterProvider> workbenchAdapterProviders;

    /** Factories for creating controls depending on the datatype. */
    private ValueDatatypeControlFactory[] controlFactories;

    /** The default value datatype control factory */
    private ValueDatatypeControlFactory defaultControlFactory;

    /** Broadcaster for broadcasting delayed change events triggered by edit fields. */
    private EditFieldChangesBroadcaster editFieldChangeBroadcaster;

    private IpsObjectEditorSettings ipsEditorSettings;

    /** Manager to update IPS problem marker. */
    private IpsProblemMarkerManager ipsProblemMarkerManager;

    private Map<String, IExtensionPropertyEditFieldFactory> extensionPropertyEditFieldFactoryMap;

    private Map<String, IExtensionPropertySectionFactory> extensionPropertySectionFactoriesMap;

    private IpsObjectSelectionHistory openIpsObjectHistory;

    private IpsElementWorkbenchAdapterAdapterFactory ipsElementWorkbenchAdapterAdapterFactory;

    private UIDatatypeFormatter datatypeFormatter;

    private List<IIpsDropAdapterProvider> productCmptDnDHandler;

    private GregorianCalendar defaultValidityDate;

    private DatatypeInputFormatRegistry datatypeInputFormat;

    private final ImageHandling images = new ImageHandling();

    /**
     * List of global property visibility filters.
     */
    private List<IProductCmptPropertyFilter> propertyVisibilityFilters;

    private IDeepCopySmartModeBehavior deepCopySmartModeBehavior;

    /**
     * This method is for test purposes only.
     * <p>
     * Note: Always reset the registry in test tear down!!!
     * 
     * @return the previous used registry to reset after the test
     */
    protected IExtensionRegistry setExtensionRegistry(IExtensionRegistry registry) {
        IExtensionRegistry oldRegistry = IpsUIPlugin.registry;
        IpsUIPlugin.registry = registry;
        extensionPropertyEditFieldFactoryMap = null;
        return oldRegistry;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        registry = Platform.getExtensionRegistry();
        ipsEditorSettings = new IpsObjectEditorSettings();
        ipsEditorSettings.load(getStateLocation());
        IpsCompositeSaveParticipant saveParticipant = new IpsCompositeSaveParticipant();
        saveParticipant.addSaveParticipant(ipsEditorSettings);
        ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID, saveParticipant);
        productCmptDnDHandler = initProductCmptDnDHandler();
        controlFactories = initValueDatatypeControlFactories();
        defaultControlFactory = new DefaultControlFactory();
        ipsElementWorkbenchAdapterAdapterFactory = new IpsElementWorkbenchAdapterAdapterFactory();
        datatypeFormatter = new UIDatatypeFormatter();
        Platform.getAdapterManager().registerAdapters(ipsElementWorkbenchAdapterAdapterFactory, IIpsElement.class);
        initDefaultValidityDate();
        propertyVisibilityFilters = Collections.unmodifiableList(loadPropertyFilters());
    }

    private void initDefaultValidityDate() {
        defaultValidityDate = new GregorianCalendar();

        IPreferencesService preferencesService = Platform.getPreferencesService();
        String pluginId = getBundle().getSymbolicName();
        long timeInMillis = preferencesService.getLong(pluginId, PREFERENCE_ID_DEFAULT_VALIDITY_DATE,
                new GregorianCalendar().getTimeInMillis(), null);

        defaultValidityDate.setTimeInMillis(timeInMillis);
    }

    /**
     * @return the list of global property filters, added through the extension point
     *         "productCmptPropertyFilter".
     */
    public List<IProductCmptPropertyFilter> getPropertyVisibilityFilters() {
        return propertyVisibilityFilters;
    }

    private List<IProductCmptPropertyFilter> loadPropertyFilters() {
        List<IProductCmptPropertyFilter> filters = new ArrayList<IProductCmptPropertyFilter>();
        ExtensionPoints extensionPoints = new ExtensionPoints(IpsUIPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension(EXTENSION_POINT_ID_PRODUCT_CMPT_PROPERTY_FILTER);
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            IProductCmptPropertyFilter filter = ExtensionPoints.createExecutableExtension(extension, configElements[0],
                    "class", IProductCmptPropertyFilter.class); //$NON-NLS-1$
            filters.add(filter);
        }
        return filters;
    }

    private List<IIpsDropAdapterProvider> initProductCmptDnDHandler() {
        List<IIpsDropAdapterProvider> dndHandler = new ArrayList<IIpsDropAdapterProvider>();

        ExtensionPoints extensionPoints = new ExtensionPoints(registry, PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension(EXTENSION_POINT_ID_IPS_DROP_ADAPTER_PROVIDER);
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                String configElClass = configElement.getAttribute(CONFIG_PROPERTY_CLASS);
                if (StringUtils.isEmpty(configElClass)) {
                    throw new RuntimeException("A problem occured while trying to load the extension: " //$NON-NLS-1$
                            + extension.getExtensionPointUniqueIdentifier() + ". The attribute \"" //$NON-NLS-1$
                            + CONFIG_PROPERTY_CLASS + "\" is not specified."); //$NON-NLS-1$
                } else {
                    IIpsDropAdapterProvider handler = ExtensionPoints.createExecutableExtension(extension,
                            configElement, CONFIG_PROPERTY_CLASS, IIpsDropAdapterProvider.class);
                    if (handler != null) {
                        dndHandler.add(handler);
                    }
                }
            }
        }
        return dndHandler;
    }

    /**
     * Returns the input format for the given datatype. The project is used to retrieve a default
     * configuration for the format (depending on the datatype), as for example the default currency
     * for money values. The project can be <code>null</code>. If it is null, the input format tries
     * to guess a default configuration.
     * 
     * @param datatype The datatype for which you need the input format
     * @param ipsProject The IIps Project used to retrieve a default configuration
     * 
     * @return The input format for formatting any values in the ui depending on the datatype and
     *         the current locale.
     */
    public IInputFormat<String> getInputFormat(ValueDatatype datatype, IIpsProject ipsProject) {
        if (datatypeInputFormat == null) {
            datatypeInputFormat = new DatatypeInputFormatRegistry();
            datatypeInputFormat.initDatatypeInputFormatMap(registry);
        }
        return datatypeInputFormat.getDatatypeInputFormat(datatype, ipsProject);
    }

    private ValueDatatypeControlFactory[] initValueDatatypeControlFactories() throws CoreException {
        List<ValueDatatypeControlFactory> factories = new ArrayList<ValueDatatypeControlFactory>();

        ExtensionPoints extensionPoints = new ExtensionPoints(registry, PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension("valueDatatypeControlFactory"); //$NON-NLS-1$
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                String configElClass = configElement.getAttribute(CONFIG_PROPERTY_CLASS);
                if (StringUtils.isEmpty(configElClass)) {
                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "A problem occured while trying to load the extension: " //$NON-NLS-1$
                                    + extension.getExtensionPointUniqueIdentifier() + ". The attribute \"" //$NON-NLS-1$
                                    + CONFIG_PROPERTY_CLASS + "\" is not specified.")); //$NON-NLS-1$
                } else {
                    ValueDatatypeControlFactory factory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, CONFIG_PROPERTY_CLASS, ValueDatatypeControlFactory.class);
                    if (factory != null) {
                        factories.add(factory);
                    }
                }
            }
        }
        return factories.toArray(new ValueDatatypeControlFactory[factories.size()]);
    }

    /* private */ void initDeepCopySmartModeBehavior() {
        ExtensionPoints extensionPoints = new ExtensionPoints(registry, IpsUIPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints
                .getExtension(IAdditionalDeepCopyWizardPage.EXTENSION_POINT_ID_DEEP_COPY_WIZARD);
        Map<IDeepCopySmartModeBehavior, String> behaviors = new HashMap<IDeepCopySmartModeBehavior, String>();
        for (IExtension extension : extensions) {
            List<IDeepCopySmartModeBehavior> executableExtensions = ExtensionPoints.createExecutableExtensions(
                    extension, IDeepCopySmartModeBehavior.CONFIG_ELEMENT_ID_SMART_MODE_BEHAVIOR,
                    IAdditionalDeepCopyWizardPage.CONFIG_ELEMENT_ATTRIBUTE_CLASS, IDeepCopySmartModeBehavior.class);
            for (IDeepCopySmartModeBehavior executableExtension : executableExtensions) {
                behaviors.put(executableExtension, extension.getUniqueIdentifier());
            }
        }
        if (behaviors.size() > 1) {
            IpsPlugin.log(new IpsStatus(MessageFormat.format(
                    "Only one extension for the extension point {0}.{1} may define a {2} but it is defined in {3}", //$NON-NLS-1$
                    IpsUIPlugin.PLUGIN_ID, IAdditionalDeepCopyWizardPage.EXTENSION_POINT_ID_DEEP_COPY_WIZARD,
                    IDeepCopySmartModeBehavior.CONFIG_ELEMENT_ID_SMART_MODE_BEHAVIOR,
                    StringUtils.join(behaviors.values(), ", ")))); //$NON-NLS-1$
        }
        if (behaviors.size() == 1) {
            deepCopySmartModeBehavior = behaviors.keySet().iterator().next();
        } else {
            deepCopySmartModeBehavior = new DefaultDeepCopySmartModeBehavior();
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        images.dispose();
        super.stop(context);
    }

    /**
     * Returns the shared instance
     */
    public static IpsUIPlugin getDefault() {
        return plugin;
    }

    @Override
    public ImageRegistry getImageRegistry() {
        IpsPlugin.log(new CoreException(
                new Status(IStatus.WARNING, PLUGIN_ID, "Image Registry is used - please use resource manager"))); //$NON-NLS-1$
        return super.getImageRegistry();
    }

    /**
     * Returns the settings for ips object editors.
     */
    public IIpsObjectEditorSettings getIpsEditorSettings() {
        return ipsEditorSettings;
    }

    /**
     * Return true if this IpsSrcFile file is editable. This includes checking the ipsSrcFile
     * mutable state and the state of the workbench
     */
    public static boolean isEditable(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile == null) {
            return false;
        }
        if (!ipsSrcFile.isMutable()) {
            return false;
        }
        if (!IpsPlugin.getDefault().getIpsPreferences().isWorkingModeEdit()) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the given generation is editable.
     * <p>
     * This method is in ui module although it only uses information from core module. We put it in
     * here because the ips preferences should be an ui aspect and may be moved to ui.
     * 
     * @param generation The generation you want to check
     * 
     * @return true if the generation is editable
     */
    public boolean isGenerationEditable(IProductCmptGeneration generation) {
        if (generation == null) {
            return false;
        }
        return isEditable(generation.getIpsSrcFile());
    }

    /**
     * Returns a control factory that can create controls (and edit fields) for the given datatype.
     * Returns a {@link DefaultControlFactory} if datatype is <code>null</code> or no factory was
     * found.
     * 
     */
    public ValueDatatypeControlFactory getValueDatatypeControlFactory(ValueDatatype datatype) {
        ValueDatatypeControlFactory[] factories = getValueDatatypeControlFactories();
        for (ValueDatatypeControlFactory factorie : factories) {
            if (factorie.isFactoryFor(datatype)) {
                return factorie;
            }
        }
        return defaultControlFactory;
    }

    /**
     * Returns all controls factories.
     */
    private ValueDatatypeControlFactory[] getValueDatatypeControlFactories() {
        return controlFactories;
    }

    /**
     * Adds drop support to the given viewer. All {@link IIpsDropAdapterProvider} instances provided
     * as extensions to #are registered as drop listener.
     */
    public void addDropSupport(StructuredViewer viewer) {
        Control control = viewer.getControl();
        DropTarget dropTarget = new DropTarget(control, getSupportedOperations());
        dropTarget.setTransfer(getSupportedTransferTypes());
        addDropListener(dropTarget, viewer);
    }

    private Transfer[] getSupportedTransferTypes() {
        Set<Transfer> result = new HashSet<Transfer>();

        for (IIpsDropAdapterProvider handler : productCmptDnDHandler) {
            result.addAll(handler.getSupportedTransferTypes());
        }
        return result.toArray(new Transfer[result.size()]);
    }

    private void addDropListener(DropTarget dropTarget, StructuredViewer viewer) {
        Set<IpsViewerDropAdapter> adapters = new HashSet<IpsViewerDropAdapter>();
        for (IIpsDropAdapterProvider handler : productCmptDnDHandler) {
            IpsViewerDropAdapter adapter = handler.getDropAdapter(viewer);
            dropTarget.addDropListener(adapter);
            adapters.add(adapter);
        }

        for (IpsViewerDropAdapter adapter : adapters) {
            adapter.setPartnerDropAdapters(adapters);
        }
    }

    private int getSupportedOperations() {
        int result = 0;
        for (IIpsDropAdapterProvider handler : productCmptDnDHandler) {
            result = result | handler.getSupportedOperations();
        }
        return result;
    }

    /**
     * Returns a factory for creating table format controls/widgets.
     * 
     * @param tableFormat ITableFormat to test whether it has custom properties.
     * @return A Factory class which can be used to create the controls for configuring the custom
     *         properties, or <code>null</code> if the table format has no custom properties.
     * 
     * @throws CoreException if the factory class could not be created.
     */
    public TableFormatConfigurationCompositeFactory getTableFormatPropertiesControlFactory(ITableFormat tableFormat)
            throws CoreException {

        ArgumentCheck.notNull(tableFormat);

        Map<ITableFormat, TableFormatConfigurationCompositeFactory> tableFormatToPropertiesCompositeMap = null;
        tableFormatToPropertiesCompositeMap = new HashMap<ITableFormat, TableFormatConfigurationCompositeFactory>();
        ExtensionPoints extensionPoints = new ExtensionPoints(registry, IpsPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension("externalTableFormat"); //$NON-NLS-1$
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                String configElClass = configElement.getAttribute(CONFIG_PROPERTY_CLASS);
                if (StringUtils.isEmpty(configElClass)) {
                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "A problem occured while trying to load the extension: " //$NON-NLS-1$
                                    + extension.getExtensionPointUniqueIdentifier()
                                    + ". The attribute 'class' is not specified.")); //$NON-NLS-1$
                }
                if (tableFormat.getClass().getName().equals(configElClass)) {
                    // the current configuration element corresponds to the given table format
                    String configElGuiClass = configElement.getAttribute("guiClass"); //$NON-NLS-1$
                    if (!StringUtils.isEmpty(configElGuiClass)) {
                        TableFormatConfigurationCompositeFactory factory = ExtensionPoints.createExecutableExtension(
                                extension, configElement, "guiClass", //$NON-NLS-1$
                                TableFormatConfigurationCompositeFactory.class);

                        // assign the given table format to the created factory
                        factory.setTableFormat(tableFormat);
                        tableFormatToPropertiesCompositeMap.put(tableFormat, factory);
                    }
                    break;
                }
            }
        }

        return tableFormatToPropertiesCompositeMap.get(tableFormat);
    }

    /**
     * Test if the given table format class has custom properties. This method returns true if the
     * optional <code>guiClass</code> property is defined for the externalTableFormat extension
     * belonging to the given table format and if the guiClass can be instantiated.
     * 
     * @param tableFormat ITableFormat to test whether it has custom properties.
     * @return true if the given table format has custom properties, false otherwise.
     */
    public boolean hasTableFormatCustomProperties(ITableFormat tableFormat) {
        try {
            TableFormatConfigurationCompositeFactory tableFormatPropertiesControlFactory = getTableFormatPropertiesControlFactory(
                    tableFormat);
            return (tableFormatPropertiesControlFactory != null);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * Opens the given IpsObject in its editor.<br>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     */
    public IEditorPart openEditor(IIpsObject ipsObject) {
        if (ipsObject == null) {
            return null;
        }
        return openEditor(ipsObject.getIpsSrcFile());
    }

    /**
     * Opens an editor for the IpsObject contained in the given IpsSrcFile.<br>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     */
    public IEditorPart openEditor(IIpsSrcFile srcFile) {
        if (srcFile == null) {
            return null;
        }
        if (srcFile instanceof LibraryIpsSrcFile) {
            IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
            IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(srcFile.getName());
            IpsArchiveEditorInput input = new IpsArchiveEditorInput(srcFile);
            try {
                IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                if (page == null) {
                    return null;
                }
                return page.openEditor(input, editor.getId());
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        } else {
            return openEditor(srcFile.getCorrespondingFile());
        }
        return null;
    }

    /**
     * Opens the file referenced by the given IFile in an editor. The type of editor to be opened is
     * derived from the file-extension using the editor-registry. If no entry is existent, the
     * workbench opens the default editor as defined in the preferences/file-associations. If none
     * is specified the workbench guesses the filetype by looking at the file's content and opens
     * the corresponding editor.
     * <p>
     * <code>IFile</code>s containing <code>IpsSrcFiles</code>s and thus <code>IpsObject</code>s are
     * always opened in the corresponding IpsObjectEditor.
     * <p>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     * 
     * @see IDE#openEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.core.resources.IFile)
     */
    public IEditorPart openEditor(final IFile fileToEdit) {
        if (fileToEdit == null) {
            return null;
        }
        RunnableFuture<IEditorPart> runnable = new FutureTask<IEditorPart>(new CallableImplementation(fileToEdit));
        BusyIndicator.showWhile(Display.getDefault(), runnable);
        try {
            return runnable.get();
        } catch (InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (ExecutionException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return null;
    }

    /**
     * Opens an editor for the given file editor input.
     * <p>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     */
    public IEditorPart openEditor(IFileEditorInput editorInput) {
        if (editorInput == null) {
            return null;
        }
        try {
            IFile file = editorInput.getFile();
            IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
            /*
             * For known file types always use the registered editor, NOT the editor specified by
             * the preferences/file-associations. This ensures that, when calling this method,
             * IpsObjects are always opened in their IpsObjectEditor and never in an XML editor
             * (which might be the default editor for the given file).
             */
            IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(file.getName());
            if (editor != null) {
                return workbench.getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editor.getId());
            } else {
                /*
                 * For unknown files let IDE open the corresponding editor. This method searches the
                 * preferences/file-associations for an editor (default editor) and if none is found
                 * guesses the filetype by looking at the contents of the given file.
                 */
                return IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file, true, true);
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return null;
    }

    /**
     * Opens an editor for the given generation.
     * <p>
     * If an editor for the given {@link IProductCmptGeneration} is already open, this opened editor
     * is advised to show the given generation.
     * <p>
     * Note that this is different from the standard Eclipse behavior, where a new editor would be
     * opened for each different generation.
     * 
     * @param productCmptGeneration the generation to open in an editor or to show in an already
     *            opened editor
     */
    public IEditorPart openEditor(IProductCmptGeneration productCmptGeneration) {
        if (productCmptGeneration == null) {
            return null;
        }
        // Open the editor
        IEditorPart openedEditor = openEditor(productCmptGeneration.getIpsObject());
        if (openedEditor == null) {
            return null;
        }
        // Update shown generation if product component editor was opened
        if (openedEditor instanceof ProductCmptEditor) {
            ProductCmptEditor productCmptEditor = (ProductCmptEditor)openedEditor;
            if (!productCmptEditor.getActiveGeneration().equals(productCmptGeneration)) {
                productCmptEditor.setActiveGeneration(productCmptGeneration);
            }
        }
        return openedEditor;
    }

    /**
     * Returns whether an {@link IEditorPart} for the provided {@link IIpsSrcFile} is currently
     * opened.
     * 
     * @param ipsSrcFile the {@link IIpsSrcFile} to check whether an editor is opened for
     */
    public boolean isOpenEditor(IIpsSrcFile ipsSrcFile) {
        IEditorInput editorInput = new FileEditorInput(ipsSrcFile.getCorrespondingFile());
        IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
        return workbench.getActiveWorkbenchWindow().getActivePage().findEditor(editorInput) != null;
    }

    /**
     * Returns the edit field change broadcaster.
     */
    public EditFieldChangesBroadcaster getEditFieldChangeBroadcaster() {
        if (editFieldChangeBroadcaster == null) {
            editFieldChangeBroadcaster = new EditFieldChangesBroadcaster();
        }
        return editFieldChangeBroadcaster;
    }

    /**
     * Returns the IPS problem marker manager which manages IPS marker updates.
     */
    public IpsProblemMarkerManager getIpsProblemMarkerManager() {
        if (ipsProblemMarkerManager == null) {
            ipsProblemMarkerManager = new IpsProblemMarkerManager();
        }
        return ipsProblemMarkerManager;
    }

    /**
     * Returns the registered {@link IExtensionPropertyEditFieldFactory} for the provided
     * propertyId. If no factory is explicitly registered for the provided <code>propertyId</code> a
     * {@link DefaultExtensionPropertyEditFieldFactory} will be associated with the propertyId and
     * returned.
     * 
     * @param propertyId the id that identifies an extension property. For it the edit field factory
     *            will be returned.
     */
    public IExtensionPropertyEditFieldFactory getExtensionPropertyEditFieldFactory(String propertyId)
            throws CoreException {

        if (extensionPropertyEditFieldFactoryMap == null) {
            extensionPropertyEditFieldFactoryMap = new HashMap<String, IExtensionPropertyEditFieldFactory>();
            ExtensionPoints extensionPoints = new ExtensionPoints(registry, IpsUIPlugin.PLUGIN_ID);
            IExtension[] extensions = extensionPoints
                    .getExtension(EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                if (configElements.length > 0) {
                    String configElPropertyId = configElements[0].getAttribute("propertyId"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(configElPropertyId)) {
                        throw new CoreException(new IpsStatus(IStatus.ERROR,
                                "A problem occured while trying to load the extension: " //$NON-NLS-1$
                                        + extension.getExtensionPointUniqueIdentifier()
                                        + ". The attribute propertyId is not specified.")); //$NON-NLS-1$
                    }
                    IExtensionPropertyEditFieldFactory factory = ExtensionPoints.createExecutableExtension(extension,
                            configElements[0], CONFIG_PROPERTY_CLASS, IExtensionPropertyEditFieldFactory.class);
                    if (factory != null) {
                        extensionPropertyEditFieldFactoryMap.put(configElPropertyId, factory);
                    }
                }
            }
        }
        IExtensionPropertyEditFieldFactory factory = extensionPropertyEditFieldFactoryMap.computeIfAbsent(propertyId, $ -> new DefaultExtensionPropertyEditFieldFactory());
        return factory;
    }

    /**
     * Returns the registered {@link IExtensionPropertyEditFieldFactory} for the provided
     * propertyId. If no factory is explicitly registered for the provided {@code propertyId},
     * {@code null} will be associated with the propertyId and returned.
     * 
     * @param propertyId the id that identifies an extension property for which the section factory
     *            will be returned.
     */
    public IExtensionPropertySectionFactory getExtensionPropertySectionFactory(String propertyId) throws CoreException {

        if (extensionPropertySectionFactoriesMap == null) {
            extensionPropertySectionFactoriesMap = new HashMap<String, IExtensionPropertySectionFactory>();
            ExtensionPoints extensionPoints = new ExtensionPoints(registry, IpsUIPlugin.PLUGIN_ID);
            IExtension[] extensions = extensionPoints
                    .getExtension(EXTENSION_POINT_ID_EXTENSION_PROPERTY_SECTION_FACTORY);
            for (IExtension extension : extensions) {
                for (IConfigurationElement configElement : extension.getConfigurationElements()) {
                    String configElPropertyId = configElement.getAttribute("propertyId"); //$NON-NLS-1$
                    if (StringUtils.isBlank(configElPropertyId)) {
                        throw new CoreException(new IpsStatus(IStatus.ERROR,
                                "A problem occured while trying to load the extension: " //$NON-NLS-1$
                                        + extension.getExtensionPointUniqueIdentifier()
                                        + ". The attribute propertyId is not specified.")); //$NON-NLS-1$
                    }
                    IExtensionPropertySectionFactory factory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, CONFIG_PROPERTY_CLASS, IExtensionPropertySectionFactory.class);
                    if (factory != null) {
                        extensionPropertySectionFactoriesMap.put(configElPropertyId, factory);
                    }
                }
            }
        }
        IExtensionPropertySectionFactory factory = extensionPropertySectionFactoriesMap.get(propertyId);
        return factory;
    }

    /**
     * This method collects all registered {@link IWorkbenchAdapterProvider}s registered in any
     * plugin extension. The {@link IWorkbenchAdapterProvider} are used to register additional
     * {@link IWorkbenchAdapter} that have to be registered for {@link IIpsElement}. The provider is
     * necessary because it is not possible to register multiple {@link IWorkbenchAdapter} for
     * {@link IIpsElement}, but we do not want to register the adapters for every specific object.
     * 
     * @return The list of registered {@link IWorkbenchAdapterProvider}
     */
    public static List<IWorkbenchAdapterProvider> getWorkbenchAdapterProviders() {
        List<IWorkbenchAdapterProvider> result = new ArrayList<IWorkbenchAdapterProvider>();
        if (workbenchAdapterProviders == null) {
            try {
                ExtensionPoints extPoints = new ExtensionPoints(registry, IpsUIPlugin.PLUGIN_ID);
                IExtension[] adapterProviders = extPoints.getExtension(EXTENSION_POINT_ID_ADAPTER_PROVIDER);
                for (IExtension extension : adapterProviders) {
                    if (extension != null) {
                        IConfigurationElement[] configElements = extension.getConfigurationElements();
                        for (IConfigurationElement configElement : configElements) {
                            if (configElement != null
                                    && configElement.getName().equals(CONFIG_ELEMENT_ID_WORKBENCHADAPTER_PROVIDER)) {
                                IWorkbenchAdapterProvider executableExtension = ExtensionPoints
                                        .createExecutableExtension(extension, configElement, CONFIG_PROPERTY_CLASS,
                                                IWorkbenchAdapterProvider.class);
                                if (executableExtension != null) {
                                    result.add(executableExtension);
                                } else {
                                    throw new RuntimeException("error while initialize workbench adapter provider"); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
            // CSON: IllegalCatch
        } else {
            workbenchAdapterProviders = result;
        }
        return result;
    }

    /**
     * Adding an {@link IIpsSrcFile} to the object history. The history stores all used files to
     * give the user a hint which files he used recently.
     * 
     * @param ipsSrcFile The new file for the history
     */
    public void addHistoryItem(IIpsSrcFile ipsSrcFile) {
        getOpenIpsObjectHistory().accessed(ipsSrcFile);
        saveOpenIpsObjectHistory();
    }

    /**
     * Get the history of the recently used ips objects.
     * 
     * @return the history containing all recenty used objects
     */
    public IpsObjectSelectionHistory getOpenIpsObjectHistory() {
        if (openIpsObjectHistory == null) {
            loadOpenIpsObjectHistory();
        }
        return openIpsObjectHistory;

    }

    /**
     * Load the history with the recently used {@link IIpsSrcFile}s. This history is used to give a
     * hint for the user which objects he recently used.
     */
    public void loadOpenIpsObjectHistory() {
        openIpsObjectHistory = new IpsObjectSelectionHistory();

        IDialogSettings settings = getHistorySettings();
        try {
            String setting = settings.get(HISTORY_SETTING);
            if (setting != null) {
                IMemento memento = XMLMemento.createReadRoot(new StringReader(setting));
                openIpsObjectHistory.load(memento);
            }
        } catch (WorkbenchException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, IpsUIPlugin.PLUGIN_ID, IStatus.ERROR,
                    "Could not load OpenIpsObjecHistory", e)); //$NON-NLS-1$
        }
    }

    /**
     * Save the history containing the recently used {@link IIpsSrcFile}s. You could specify the
     * history you want to safe. Normally you should use the method
     * {@link #saveOpenIpsObjectHistory()} to save the history of this {@link IpsUIPlugin}.
     * 
     * @param selectionHistory the history with the recently used objects, @see
     *            {@link #loadOpenIpsObjectHistory()}
     */
    public void saveOpenIpsObjectHistory(IpsObjectSelectionHistory selectionHistory) {
        XMLMemento memento = XMLMemento.createWriteRoot(HISTORY_SETTING);
        selectionHistory.save(memento);
        StringWriter writer = new StringWriter();
        IDialogSettings settings = getHistorySettings();
        try {
            memento.save(writer);
            settings.put(HISTORY_SETTING, writer.getBuffer().toString());
        } catch (IOException e) {
            // Simply don't store the settings
            StatusManager.getManager().handle(new Status(IStatus.ERROR, IpsUIPlugin.PLUGIN_ID, IStatus.ERROR,
                    "Could not write OpenIpsObjecHistory", e)); //$NON-NLS-1$
        }
    }

    /**
     * Save the history containing the recently used {@link IIpsSrcFile}s.
     * 
     * @see #saveOpenIpsObjectHistory(IpsObjectSelectionHistory)
     */
    public void saveOpenIpsObjectHistory() {
        saveOpenIpsObjectHistory(openIpsObjectHistory);
    }

    private IDialogSettings getHistorySettings() {
        IDialogSettings settings = IpsUIPlugin.getDefault().getDialogSettings()
                .getSection(OPEN_IPS_OBJECT_HISTORY_SETTINGS);
        if (settings == null) {
            settings = IpsUIPlugin.getDefault().getDialogSettings().addNewSection(OPEN_IPS_OBJECT_HISTORY_SETTINGS);
        }
        return settings;
    }

    /**
     * Creates a {@link DecoratingLabelProvider} for the specified {@link ILabelProvider}
     * 
     * @param labelProvider the label provider you want to decorate
     * @return the decorated label provider
     */
    public static LabelProvider getDecoratedLabelProvider(ILabelProvider labelProvider) {
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        return new DecoratingLabelProvider(labelProvider, decoManager.getLabelDecorator());
    }

    /**
     * Returning the default label of an {@link IIpsElement}. The default label is the label
     * provided by the {@link IWorkbenchAdapter}. If there is no {@link IWorkbenchAdapter} for the
     * specified {@link IIpsElement} then the empty String "" is returned.
     * 
     * @param ipsElement The {@link IIpsElement} you want to get the default label for
     * @return the default label of the {@link IIpsElement} returned by the
     *         {@link IWorkbenchAdapter}
     */
    public static final String getLabel(IIpsElement ipsElement) {
        IWorkbenchAdapter adapter = (IWorkbenchAdapter)ipsElement.getAdapter(IWorkbenchAdapter.class);
        if (adapter == null) {
            return ""; //$NON-NLS-1$
        } else {
            return adapter.getLabel(ipsElement);
        }
    }

    /**
     * Saves all dirty editors in the workbench that are editing one of the provided
     * {@link IIpsSrcFile}s.
     * <p>
     * Opens a dialog to prompt the user. Returns true if successful or false if the user has
     * canceled the command.
     * 
     * @return true if the command succeeded, false if the operation was canceled by the user or an
     *         error occurred while saving
     */
    public boolean saveEditors(List<IIpsSrcFile> ipsSrcFiles) {
        // Collect dirty parts and abort if there are none
        List<IEditorPart> dirtyEditorParts = collectDirtyEditorParts();
        if (dirtyEditorParts.size() == 0) {
            return true;
        }

        // Filter dirty editor parts according to the provided source files
        List<IEditorPart> filteredEditorParts = new ArrayList<IEditorPart>(dirtyEditorParts.size());
        if (ipsSrcFiles == null) {
            filteredEditorParts.addAll(dirtyEditorParts);
        } else {
            for (IEditorPart part : dirtyEditorParts) {
                if (part instanceof IpsObjectEditor) {
                    IpsObjectEditor ipsObjectEditor = (IpsObjectEditor)part;
                    for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                        if (ipsObjectEditor.getIpsSrcFile().equals(ipsSrcFile)) {
                            filteredEditorParts.add(ipsObjectEditor);
                            break;
                        }
                    }
                }
            }
        }

        return saveEditorsInternal(filteredEditorParts);
    }

    /**
     * Saves all dirty editors in the workbench.
     * <p>
     * Opens a dialog to prompt the user. Returns true if successful or false if the user has
     * canceled the command.
     * 
     * @return true if the command succeeded, false if the operation was canceled by the user or an
     *         error occurred while saving
     */
    public boolean saveAllEditors() {
        // Collect dirty parts and abort if there are none
        List<IEditorPart> dirtyEditorParts = collectDirtyEditorParts();
        if (dirtyEditorParts.size() == 0) {
            return true;
        }

        return saveEditorsInternal(dirtyEditorParts);
    }

    // Only allows to save or cancel the current operation ('no' is not an option)
    private boolean saveEditorsInternal(final List<IEditorPart> editorParts) {
        boolean okPressed = editorParts.size() == 1 ? saveEditorsInternalOnePart(editorParts)
                : saveEditorsInternalMultipleParts(editorParts);
        if (!okPressed) {
            return false;
        }

        ISaveableFilter fileFilter = new ISaveableFilter() {
            @Override
            public boolean select(Saveable saveable, IWorkbenchPart[] containingParts) {
                for (IWorkbenchPart part : containingParts) {
                    if (editorParts.contains(part)) {
                        return true;
                    }
                }
                return false;
            }
        };

        // No need to confirm because we already ask the user
        boolean confirm = false;
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return PlatformUI.getWorkbench().saveAll(activeWorkbenchWindow, activeWorkbenchWindow, fileFilter, confirm);
    }

    private boolean saveEditorsInternalOnePart(List<IEditorPart> editorParts) {
        // Use a simple dialog
        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        return MessageDialog.openConfirm(activeShell, Messages.IpsPlugin_dialogSaveDirtyEditorTitle,
                NLS.bind(Messages.IpsPlugin_dialogSaveDirtyEditorMessageSimple, editorParts.get(0).getTitle()));
    }

    private boolean saveEditorsInternalMultipleParts(List<IEditorPart> editorParts) {
        /*
         * Use same behavior like RefactoringSaveHelper#askSaveAllDirtyEditors but disable double
         * click event in list.
         */
        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ListDialog dialog = new ListDialog(activeShell) {
            {
                setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
            }

            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                setAddCancelButton(true);
                super.createButtonsForButtonBar(parent);
                // If no cancel button is there the double click event will be disabled
                setAddCancelButton(false);
            }
        };
        dialog.setInput(editorParts);
        dialog.setLabelProvider(new WorkbenchPartLabelProvider());
        dialog.setContentProvider(new ArrayContentProvider());
        dialog.setInitialSelections(editorParts.toArray());
        dialog.setTitle(Messages.IpsPlugin_dialogSaveDirtyEditorTitle);
        dialog.setMessage(Messages.IpsPlugin_dialogSaveDirtyEditorMessageMany);
        dialog.setInitialSelections(new Object[0]);
        return dialog.open() == IDialogConstants.OK_ID;
    }

    /**
     * Collect dirtyParts. Note this code is based on the "collect dirtyParts" part of the eclipse
     * method PlatformUI.getWorkbench().saveAllEditors(), because there is no API method we can use
     * instead.
     */
    private List<IEditorPart> collectDirtyEditorParts() {
        ArrayList<IEditorPart> dirtyParts = new ArrayList<IEditorPart>();
        ArrayList<IEditorInput> dirtyEditorsInput = new ArrayList<IEditorInput>();
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow window : windows) {
            IWorkbenchPage[] pages = window.getPages();
            for (IWorkbenchPage page : pages) {
                IEditorPart[] dirtyEditors = page.getDirtyEditors();
                for (int k = 0; k < dirtyEditors.length; k++) {
                    if (dirtyEditors[k].isSaveOnCloseNeeded()) {
                        if (!dirtyEditorsInput.contains(dirtyEditors[k].getEditorInput())) {
                            dirtyParts.add(dirtyEditors[k]);
                            dirtyEditorsInput.add(dirtyEditors[k].getEditorInput());
                        }
                    }
                }
            }
        }
        return dirtyParts;
    }

    /**
     * Returns the currently active {@link IpsObjectEditor} or null if no such editor is currently
     * active.
     */
    public IpsObjectEditor getActiveIpsObjectEditor() {
        IWorkbenchWindow activeWorkbenchWindow = getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return null;
        }

        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null) {
            return null;
        }

        IEditorPart activeEditor = activePage.getActiveEditor();
        if (!(activeEditor instanceof IpsObjectEditor)) {
            return null;
        }

        return (IpsObjectEditor)activeEditor;
    }

    /**
     * This method calls
     * {@link IpsModel#runAndQueueChangeEvents(IWorkspaceRunnable, IProgressMonitor)} to perform
     * multiple change operations and queue all change events. In addition it shows a busy indicator
     * and starts a progress dialog if the operation takes too long.
     * 
     * The whole workspace is blocked while the operation runs.
     * 
     * IMPORTANT NOTES:
     * 
     * The action will NOT run in UI thread! If you need to run in display thread use
     * {@link BusyIndicator#showWhile(Display, Runnable)}
     * 
     * You cannot call this method from another modal dialog like a wizard because the wizard has
     * its own progress and this one will wait until the dialog is finished.
     * 
     * @param action The {@link IWorkspaceRunnable} that should be startet-
     */
    public void runWorkspaceModification(final IWorkspaceRunnable action) {
        IWorkbench wb = PlatformUI.getWorkbench();
        IProgressService ps = wb.getProgressService();
        try {
            ps.busyCursorWhile(new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IpsPlugin.getDefault().getIpsModel().runAndQueueChangeEvents(action, monitor);
                }

            });
        } catch (InvocationTargetException e) {
            logAndShowErrorDialog(e);
        } catch (InterruptedException e) {
            logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns the current default validity date which should be used as default date for the
     * creation of new generations.
     */
    public GregorianCalendar getDefaultValidityDate() {
        GregorianCalendar dateWithoutTime = new GregorianCalendar();
        dateWithoutTime.setTimeInMillis(defaultValidityDate.getTimeInMillis());
        dateWithoutTime.set(Calendar.SECOND, 0);
        dateWithoutTime.set(Calendar.MINUTE, 0);
        dateWithoutTime.set(Calendar.HOUR, 0);
        dateWithoutTime.set(Calendar.HOUR_OF_DAY, 0);
        dateWithoutTime.set(Calendar.MILLISECOND, 0);
        return dateWithoutTime;
    }

    /**
     * Sets the default validity date to be used as default date for the creation of new generations
     * to the given date.
     */
    public void setDefaultValidityDate(GregorianCalendar workingDate) {
        String pluginId = getBundle().getSymbolicName();
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(pluginId);
        node.putLong(PREFERENCE_ID_DEFAULT_VALIDITY_DATE, workingDate.getTimeInMillis());
        defaultValidityDate.setTimeInMillis(workingDate.getTimeInMillis());
    }

    // ************************************************
    // IMAGE HANDLING
    // ************************************************

    public static ImageHandling getImageHandling() {
        return getDefault().images;
    }

    /**
     * Returns a {@link Color} instance for the given symbolic name. If no color is found for the
     * symbolic name the {@link RGB} object is used to create a new color-instance. From this moment
     * on the new color instance is accessible via the same symbolic name.
     * <p>
     * Color instances retrieved/created via this method will be disposed when the corresponding
     * display is disposed.
     * 
     * @param symbolicColorName a name that identifies the color
     * @param rgb the RGB information of the requested color, so that it can be created in case it
     *            hasn't been registered yet.
     */
    public Color getColor(String symbolicColorName, RGB rgb) {
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        if (!colorRegistry.hasValueFor(symbolicColorName)) {
            colorRegistry.put(symbolicColorName, rgb);
        }
        return colorRegistry.get(symbolicColorName);
    }

    public UIDatatypeFormatter getDatatypeFormatter() {
        return datatypeFormatter;
    }

    /**
     * Returns the {@link IDeepCopySmartModeBehavior} to be used by the {@link DeepCopyWizard} in
     * {@link IpsPreferences#isCopyWizardModeSmartmode() Smart Mode}. The behavior can be provided
     * via the
     * {@link #PLUGIN_ID}.{@link IAdditionalDeepCopyWizardPage#EXTENSION_POINT_ID_DEEP_COPY_WIZARD}
     * extension point's {@link IDeepCopySmartModeBehavior#CONFIG_ELEMENT_ID_SMART_MODE_BEHAVIOR}
     * element. If none is configured, the {@link DefaultDeepCopySmartModeBehavior} will be used.
     * 
     * @return the {@link IDeepCopySmartModeBehavior} to be used by the {@link DeepCopyWizard} in
     *         {@link IpsPreferences#isCopyWizardModeSmartmode() Smart Mode}.
     */
    public IDeepCopySmartModeBehavior getDeepCopySmartModeBehavior() {
        if (deepCopySmartModeBehavior == null) {
            initDeepCopySmartModeBehavior();
        }
        return deepCopySmartModeBehavior;
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

    private static class CallableImplementation implements Callable<IEditorPart> {
        private final IFile fileToEdit;

        private CallableImplementation(IFile fileToEdit) {
            this.fileToEdit = fileToEdit;
        }

        /**
         * Check if the file can be edit with a corresponding IPS object editor, if the file is
         * outside an IPS package then the IPS object editor couldn't be used - the IPS object could
         * not be retrieved from the IPS source file - therefore open the default text editor (to
         * edit the IPS source file as XML).
         */
        @Override
        public IEditorPart call() throws Exception {
            IIpsModel model = IpsPlugin.getDefault().getIpsModel();
            IIpsElement ipsElement = model.getIpsElement(fileToEdit);
            if (ipsElement instanceof IIpsSrcFile && !((IIpsSrcFile)ipsElement).exists()) {
                try {
                    return openWithDefaultIpsSrcTextEditor(fileToEdit);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            } else {
                return IpsUIPlugin.getDefault().openEditor(new FileEditorInput(fileToEdit));
            }

            return null;
        }

        /**
         * Open the given file with the default text editor. And show an information message in the
         * editors status bar to inform the user about using the text editor instead of the IPS
         * object editor.
         */
        private IEditorPart openWithDefaultIpsSrcTextEditor(IFile fileToEdit) throws CoreException {
            String defaultContentTypeOfIpsSrcFilesId = "org.faktorips.devtools.core.ipsSrcFile"; //$NON-NLS-1$
            IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
            IFileEditorInput editorInput = new FileEditorInput(fileToEdit);

            IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
            IContentType contentType = contentTypeManager.getContentType(defaultContentTypeOfIpsSrcFilesId);

            IEditorDescriptor[] editors = workbench.getEditorRegistry().getEditors("", contentType); //$NON-NLS-1$
            if (editors.length != 1) {
                throw new CoreException(new IpsStatus(
                        NLS.bind("No registered editors (or more then one) for content-type id {0} found!", //$NON-NLS-1$
                                defaultContentTypeOfIpsSrcFilesId)));
            }
            try {
                IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                if (page == null) {
                    return null;
                }
                IEditorPart editorPart = page.openEditor(editorInput, editors[0].getId());
                if (editorPart == null) {
                    throw new CoreException(new IpsStatus("Error opening the default text editor!!")); //$NON-NLS-1$
                }
                /*
                 * show information in the status bar about using the default text editor instead of
                 * using the default IPS object editor.
                 */
                ((IEditorSite)editorPart.getSite()).getActionBars().getStatusLineManager().setMessage(
                        getImageHandling().getSharedImage("size8/InfoMessage.gif", true), //$NON-NLS-1$
                        Messages.IpsPlugin_infoDefaultTextEditorWasOpened);
                return editorPart;
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }

            return null;
        }

    }

    /**
     * Images in eclipse is not so easy as it looks like. If you are not familiar with the basics of
     * image handling in eclipse, read this short article <a href=
     * "http://www.eclipse.org/articles/Article-Using%20Images%20In%20Eclipse/Using%20Images%20In%20Eclipse.html"
     * >Using Images in the Eclipse UI</a>
     * <p>
     * In Faktor-IPS we have a two kinds of images handled by the image handling. The first kind of
     * image is a plugin shared image. Only use shared images for those icons that are realy
     * important for several components of the plugin and more important, those does not change over
     * time.
     * <p>
     * The second kind of images are not shared images. You have to look for the
     * 
     * @author dirmeier
     */
    public static class ImageHandling {

        private static final Map<ImageDescriptor, ImageDescriptor> ENABLE_DISABLE_MAP = new HashMap<ImageDescriptor, ImageDescriptor>();

        private ResourceManager resourceManager;

        /**
         * used to map image names (also composit names for overlays) to descriptors
         */
        private Map<String, ImageDescriptor> descriptorMap = new HashMap<String, ImageDescriptor>();

        public ResourceManager getResourceManager() {
            if (resourceManager == null) {
                resourceManager = createResourceManager();
            }
            return resourceManager;
        }

        private ResourceManager createResourceManager() {
            // If we are in the UI Thread use that
            if (Display.getCurrent() != null) {
                return new LocalResourceManager(JFaceResources.getResources(Display.getCurrent()));
            }

            if (PlatformUI.isWorkbenchRunning()) {
                return new LocalResourceManager(JFaceResources.getResources(PlatformUI.getWorkbench().getDisplay()));
            }

            // Invalid thread access if it is not the UI Thread
            // and the workbench is not created.
            throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
        }

        /**
         * Returns the image with the indicated name from the <code>icons</code> folder. If no image
         * with the indicated name is found and createIfAbsent is false null is returned.
         * 
         * @param name The image name, e.g. <code>IpsProject.gif</code>
         * @param createIfAbsent true to create a new image if not already registered
         */
        public Image getSharedImage(String name, boolean createIfAbsent) {
            ImageDescriptor descriptor = getSharedImageDescriptor(name, createIfAbsent);
            if (createIfAbsent) {
                return getImage(descriptor);
            } else {
                if (descriptor != null) {
                    return (Image)getResourceManager().find(descriptor);
                } else {
                    return null;
                }
            }
        }

        /**
         * To get an image descriptor to a specified name. If the image descriptor is not already
         * registered in the plugin's image registry and the flag createIfAbsent is true, this
         * method does. Only use this method for images you want to share for the whole plugin.
         * 
         * @see ImageHandling
         * 
         * @param name the name of the image equal to the filename in the subfolder icons
         * 
         * @return the shared image descriptor
         */
        public ImageDescriptor getSharedImageDescriptor(String name, boolean createIfAbsent) {
            ImageDescriptor descriptor = descriptorMap.get(name);
            if (descriptor == null && createIfAbsent) {
                descriptor = createImageDescriptor(name);
                registerSharedImageDescriptor(name, descriptor);
            }
            return descriptor;
        }

        /**
         * To register an image descriptor in the image registry. The name of the image is the
         * filename in the subfolder <i>icons</i> that means the path to the image is
         * IpsUIPlugin/icons/name
         */
        public void registerSharedImageDescriptor(String name, ImageDescriptor descriptor) {
            if (descriptor != null && descriptor != ImageDescriptor.getMissingImageDescriptor()) {
                descriptorMap.put(name, descriptor);
            }
        }

        /**
         * Get the disabled version of a shared image for an ips element
         */
        public Image getDisabledImage(IAdaptable adaptable) {
            return getImage(getDisabledImageDescriptor(adaptable));
        }

        /**
         * Get the shared descriptor for disable image with the descriptor of an enabled image
         */
        public ImageDescriptor getDisabledImageDescriptor(IAdaptable adaptable) {
            ImageDescriptor enabledImageDescriptor = getImageDescriptor(adaptable);
            ImageDescriptor disabledImageDescriptor = ENABLE_DISABLE_MAP.get(enabledImageDescriptor);
            if (disabledImageDescriptor == null) {
                disabledImageDescriptor = createDisabledImageDescriptor(enabledImageDescriptor);
            }
            return disabledImageDescriptor;
        }

        /**
         * Return a shared image which is the disabled version of the given image descriptor
         */
        public Image getDisabledSharedImage(ImageDescriptor enabledImage) {
            ImageDescriptor disabledID = createDisabledImageDescriptor(enabledImage);
            return getImage(disabledID);
        }

        /**
         * Create the disabled version of a shared image descriptor
         */
        public ImageDescriptor createDisabledImageDescriptor(ImageDescriptor enabledImageDescriptor) {
            ImageDescriptor disabledImageDesc = ImageDescriptor.createWithFlags(enabledImageDescriptor,
                    SWT.IMAGE_DISABLE);
            return disabledImageDesc;
        }

        /**
         * Just create a image descriptor with the specified name as image filename in the icons
         * subfolder does not register anything in the image registry or the image description
         * registry. Only use for images of this plugin!
         * <p>
         * Use this method when you only want to have an image descriptor for any eclipse object
         * e.g. an Action or a Wizard Normally eclipse does instantiate and dispose the image
         * 
         * @return the new created image descriptor
         */
        public ImageDescriptor createImageDescriptor(String name) {
            URL url = getDefault().getBundle().getEntry("icons/" + name); //$NON-NLS-1$
            return ImageDescriptor.createFromURL(url);
        }

        /**
         * To get an image for an image descriptor from resource manager. If no such resource
         * already exists the resource manager creates a new one. The image will remain allocated
         * for the lifetime of the plugin. If the image is not potentially needed by other classes
         * use the methods {@link #createImage(ImageDescriptor)} and
         * {@link #disposeImage(ImageDescriptor)} or even better use your own LocalResourceManager.
         * <p/>
         * If descriptor is null, the missing image is returned
         */
        public Image getImage(ImageDescriptor descriptor) {
            return getImage(descriptor, true);
        }

        /**
         * To get an image for an image descriptor from resource manager. If no such resource
         * already exists the resource manager creates a new one. The image will remain allocated
         * for the lifetime of the plugin. If the image is not potentially needed by other classes
         * use the methods {@link #createImage(ImageDescriptor)} and
         * {@link #disposeImage(ImageDescriptor)} or even better use your own LocalResourceManager.
         * 
         * @param returnMissingImage if true, the MissingImage is returned instead of null
         */
        public Image getImage(ImageDescriptor descriptor, boolean returnMissingImage) {
            if (descriptor != null) {
                return (Image)getResourceManager().get(descriptor);
            }
            if (returnMissingImage) {
                return (Image)getResourceManager().get(ImageDescriptor.getMissingImageDescriptor());
            }
            return null;
        }

        /**
         * Create an image in the resource manager. You have to dispose the image by calling
         * {@link #disposeImage(ImageDescriptor)} if you do not need it any longer. If you want to
         * share the image with other components, use one of the shared image methods. If the image
         * descriptor is already registered as a shared image, the descriptor is not registered
         * twice. You do not have to worry about calling the disposeImage method because a shared
         * image also would not be disposed
         */
        public Image createImage(ImageDescriptor descriptor) {
            if (descriptor != null) {
                return getResourceManager().createImage(descriptor);
            }
            return (Image)getResourceManager().get(ImageDescriptor.getMissingImageDescriptor());
        }

        /**
         * To dispose a self registered image. Do not dispose shared images (in fact this method
         * wouldn't do).
         * 
         * @param descriptor the descriptor of the image you want to dispose
         * 
         */
        public void disposeImage(ImageDescriptor descriptor) {
            getResourceManager().destroyImage(descriptor);
        }

        /**
         * Getting an image descriptor by calling the {@link IWorkbenchAdapter} of the ips element
         * If there is no registered adapter this method returns null. If the registered adapter has
         * no image, this method returns the missing image
         * 
         * @return the image descriptor or null if there is no image or no registered adapter
         */
        public ImageDescriptor getImageDescriptor(IAdaptable adaptable) {
            if (adaptable == null) {
                return getSharedImageDescriptor("IpsElement_broken.gif", true); //$NON-NLS-1$
            }
            IWorkbenchAdapter adapter = (IWorkbenchAdapter)adaptable.getAdapter(IWorkbenchAdapter.class);
            if (adapter != null) {
                ImageDescriptor descriptor = adapter.getImageDescriptor(adaptable);
                if (descriptor != null) {
                    return descriptor;
                } else {
                    return ImageDescriptor.getMissingImageDescriptor();
                }
            }
            return null;
        }

        /**
         * Getting the image for an ips element by calling the {@link IWorkbenchAdapter} for the
         * specified ips element. The image is either a shared image (if someone already registered
         * the corresponding image descriptor) or a not shared one if no one registered the image
         * before. If it is a no shared image, someone (maybe you - normally the workbench adapter)
         * have dispose the image.
         */
        public Image getImage(IAdaptable adaptable) {
            return getImage(getImageDescriptor(adaptable), false);
        }

        /**
         * Get the enabled or disabled image for the given element.
         * 
         * @see #getImage(IAdaptable) and @see {@link #getDisabledImage(IAdaptable)}
         */
        public Image getImage(IAdaptable adaptable, boolean enabled) {
            if (enabled) {
                return getImage(adaptable);
            } else {
                return getDisabledImage(adaptable);
            }
        }

        /**
         * Get the default image descriptor for an ips element class. May return null. Note: The
         * workbench adapters are registered for concrete implementations not for interfaces
         */
        public ImageDescriptor getDefaultImageDescriptor(Class<? extends IpsElement> ipsElementClass) {
            IpsElementWorkbenchAdapter adapter = getDefault().ipsElementWorkbenchAdapterAdapterFactory
                    .getAdapterByClass(ipsElementClass);
            if (adapter != null) {
                IpsElementWorkbenchAdapter ipsWA = adapter;
                return ipsWA.getDefaultImageDescriptor();
            } else {
                return null;
            }
        }

        /**
         * Get the default image for an ips element class. May return null. Note: The workbench
         * adapters are registered for concrete implementations not for interfaces
         */
        public Image getDefaultImage(Class<? extends IpsElement> ipsElementClass) {
            ImageDescriptor descriptor = getDefaultImageDescriptor(ipsElementClass);
            if (descriptor != null) {
                return getImage(descriptor);
            } else {
                return null;
            }
        }

        /**
         * Returns the image with the indicated name from the <code>icons</code> folder and overlays
         * it with the specified overlay image. If the given image is not found return the missing
         * image overlaid with the product relevant image.
         * 
         * @param baseImageName The name of the image which will be overlaid with the overlay image.
         * @param overlayImageName The name of the overlay image
         * @param quadrant the quadrant where the overlay is painted, one of
         *            {@link IDecoration#TOP_LEFT} {@link IDecoration#TOP_RIGHT},
         *            {@link IDecoration#BOTTOM_LEFT} or {@link IDecoration#BOTTOM_RIGHT}
         */
        public ImageDescriptor getSharedOverlayImage(String baseImageName, String overlayImageName, int quadrant) {
            if (StringUtils.isEmpty(overlayImageName)) {
                return getSharedImageDescriptor(baseImageName, true);
            }
            Image baseImage = getSharedImage(baseImageName, true);
            return getSharedOverlayImageDescriptor(baseImage, overlayImageName, quadrant);
        }

        /**
         * Returns the image with the indicated name from the <code>icons</code> folder and overlays
         * it with the specified overlay image. If the given image is not found return the missing
         * image overlaid with the product relevant image.
         * 
         * @param baseImage The image which will be overlaid with the overlay image
         * @param overlayImageName The name of the overlay image
         * @param quadrant the quadrant where the overlay is painted, one of
         *            {@link IDecoration#TOP_LEFT} {@link IDecoration#TOP_RIGHT},
         *            {@link IDecoration#BOTTOM_LEFT} or {@link IDecoration#BOTTOM_RIGHT}
         */
        public ImageDescriptor getSharedOverlayImageDescriptor(Image baseImage, String overlayImageName, int quadrant) {
            String overlayedImageName = overlayImageName + "_" + baseImage.hashCode(); //$NON-NLS-1$
            ImageDescriptor imageDescriptor = getSharedImageDescriptor(overlayedImageName, false);
            if (imageDescriptor == null) {
                ImageDescriptor overlay = createImageDescriptor(overlayImageName);
                imageDescriptor = new DecorationOverlayIcon(baseImage, overlay, quadrant);
                registerSharedImageDescriptor(overlayedImageName, imageDescriptor);
            }
            return imageDescriptor;
        }

        /**
         * Returns the image with the indicated name from the <code>icons</code> folder and overlaid
         * by the specified overlay images. The array contains the names of the overlays, sorted in
         * following order: top-left, top-right, bottom-left, bottom-right. If the given image is
         * not found return the missing image overlaid with the product relevant image.
         * 
         * 
         * @param baseImageName The name of the image which will be overlaid with the overlay image.
         * @param overlayImageNames The names of the overlay images
         */
        public ImageDescriptor getSharedOverlayImage(String baseImageName, String[] overlayImageNames) {
            String overlayedImageName = Arrays.toString(overlayImageNames) + "_" + baseImageName; //$NON-NLS-1$
            ImageDescriptor imageDescriptor = getSharedImageDescriptor(overlayedImageName, false);
            if (imageDescriptor == null) {
                Image baseImage = getSharedImage(baseImageName, true);
                ImageDescriptor[] overlays = new ImageDescriptor[overlayImageNames.length];
                for (int i = 0; i < overlayImageNames.length; i++) {
                    if (overlayImageNames[i] != null) {
                        overlays[i] = createImageDescriptor(overlayImageNames[i]);
                    }
                }
                imageDescriptor = new DecorationOverlayIcon(baseImage, overlays);
                registerSharedImageDescriptor(overlayedImageName, imageDescriptor);
            }
            return imageDescriptor;
        }

        public IpsElementWorkbenchAdapter getWorkbenchAdapterFor(Class<? extends IpsElement> class1) {
            return getDefault().ipsElementWorkbenchAdapterAdapterFactory.getAdapterByClass(class1);
        }

        public void dispose() {
            if (resourceManager != null) {
                resourceManager.dispose();
            }
        }
    }

}
