/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.util.ArgumentCheck;

/**
 * Definition of extension points provided by Faktor-IPS.
 * 
 * @author Jan Ortmann
 */
public class ExtensionPoints {

    /**
     * IpsModelPlugin relative id of the extension point for IPS object path container types.
     * 
     * @see IIpsObjectPathContainerType
     * 
     * @since 3.4
     */
    public static final String IPS_OBJECT_PATH_CONTAINER_TYPE = "ipsObjectPathContainerType"; //$NON-NLS-1$
    /**
     * The name of the extension point property <code>containerType</code> in the extension point
     * {@value #IPS_OBJECT_PATH_CONTAINER_TYPE}.
     */
    public static final String CONFIG_ELEMENT_CONTAINER_TYPE = "containerType"; //$NON-NLS-1$

    /**
     * IpsModelPlugin relative id of the extension point for IpsObjectTypes.
     * 
     * @see IpsObjectType
     */
    public static final String IPS_OBJECT_TYPE = "ipsobjecttype"; //$NON-NLS-1$

    /**
     * IpsModelPlugin relative id of the extension point for custom validations.
     * 
     * @see ICustomValidation
     */
    public static final String CUSTOM_VALIDATION = "customValidation"; //$NON-NLS-1$

    /**
     * IpsModelPlugin relative id of the extension point for product component naming strategies.
     * 
     * @see IProductCmptNamingStrategy
     * @see IProductCmptNamingStrategyFactory
     */
    public static final String PRODUCT_COMPONENT_NAMING_STRATEGY = "productComponentNamingStrategy"; //$NON-NLS-1$

    /**
     * IpsModelPlugin relative id of the extension point for "Pull Up" refactoring participants.
     */
    public static final String PULL_UP_PARTICIPANTS = "pullUpParticipants"; //$NON-NLS-1$

    /**
     * IpsModelPlugin relative id of the extension point for configuring a Maven project as an IPS
     * project.
     */
    public static final String ADD_IPS_NATURE = "addIpsNature"; //$NON-NLS-1$

    /**
     * Name of the attribute that holds the name of the executable class
     */
    public static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

    /**
     * The name of the {@link IConfigurationElement configuration element} property
     * {@value #CONFIG_ELEMENT_PROPERTY_PROVIDER}.
     */
    public static final String CONFIG_ELEMENT_PROPERTY_PROVIDER = "provider"; //$NON-NLS-1$

    /**
     * The name of the {@link IConfigurationElement configuration element} property
     * {@value #CONFIG_ELEMENT_PROPERTY_CLASS}.
     */
    public static final String CONFIG_ELEMENT_PROPERTY_CLASS = "class"; //$NON-NLS-1$

    private static final String RELEASE_EXTENSION_POINT_NAME = "productReleaseExtension"; //$NON-NLS-1$

    private final IExtensionRegistry registry;

    private final String nameSpace;

    public ExtensionPoints(IExtensionRegistry registry, String nameSpace) {
        super();
        ArgumentCheck.notNull(registry, this);
        ArgumentCheck.notNull(nameSpace, this);
        this.registry = registry;
        this.nameSpace = nameSpace;
    }

    public ExtensionPoints(String nameSpace) {
        this(Platform.getExtensionRegistry(), nameSpace);
    }

    /**
     * Creates a new instance with the platforms extension registry and the IPS plugin ID as
     * namespace.
     * 
     * @see Platform#getExtensionRegistry()
     * @see IpsModelActivator#PLUGIN_ID
     */
    public ExtensionPoints() {
        this(Platform.getExtensionRegistry(), IpsModelActivator.PLUGIN_ID);
    }

    /**
     * Returns all extensions defined for the given point. The point id should be one of the
     * constants defined in this class.
     * 
     * @throws NullPointerException if pointId is <code>null</code>.
     * @throws IllegalArgumentException if no extension point with id pointId exists.
     */
    public final IExtension[] getExtension(String pointId) {
        ArgumentCheck.notNull(pointId);
        IExtensionPoint point = getRegistry().getExtensionPoint(getNameSpace(), pointId);
        if (point == null) {
            IpsLog.log(new IpsStatus("ExtensionPoint " + pointId + " not found!")); //$NON-NLS-1$ //$NON-NLS-2$
            throw new IllegalArgumentException("Unknown extension point " + pointId); //$NON-NLS-1$
        }
        return point.getExtensions();
    }

    /**
     * Creates executable extensions for all extensions defined for the given point. The point id
     * should be one of the constants defined in this class.
     * 
     * @param pointId The id of the extension point.
     * @param elementName Name of the config element that contains a property that specifies the
     *            qualified name of the class to instantiate.
     * @param propertyName Name of the property that contains the qualified name of the class to
     *            instantiate.
     * @param expectedType The expected class/type of the instance.
     * 
     * @throws NullPointerException if any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException if no extension point with id pointId exists.
     * 
     * @see IConfigurationElement#createExecutableExtension(String)
     */
    public final <T> List<T> createExecutableExtensions(String pointId,
            String elementName,
            String propertyName,
            Class<T> expectedType) {
        return createExecutableExtensions(pointId, elementName, propertyName, expectedType, ($1, $2) -> {
            /* NOP */ });
    }

    /**
     * Creates executable extensions for all extensions defined for the given point. The point id
     * should be one of the constants defined in this class.
     * <p>
     * The given {@link BiConsumer initializer} is used to configure the created executables with
     * additional information from the extension point.
     * 
     * @param pointId The id of the extension point.
     * @param elementName Name of the config element that contains a property that specifies the
     *            qualified name of the class to instantiate.
     * @param propertyName Name of the property that contains the qualified name of the class to
     *            instantiate.
     * @param expectedType The expected class/type of the instance.
     * @param initializer The initializer is used to configure the created executables with
     *            additional information from the extension point.
     * 
     * @throws NullPointerException if any of the arguments is <code>null</code>.
     * @throws IllegalArgumentException if no extension point with id pointId exists.
     * 
     * @see IConfigurationElement#createExecutableExtension(String)
     */
    public <T> List<T> createExecutableExtensions(String pointId,
            String elementName,
            String propertyName,
            Class<T> expectedType,
            BiConsumer<IExtension, T> initializer) {
        ArgumentCheck.notNull(pointId);
        return Arrays.stream(getExtension(pointId)).flatMap(
                extension -> createAndInitialize(elementName, propertyName, expectedType, initializer, extension)
                        .stream())
                .collect(Collectors.toList());
    }

    private static <T> List<T> createAndInitialize(String elementName,
            String propertyName,
            Class<T> expectedType,
            BiConsumer<IExtension, T> initializer,
            IExtension extension) {
        List<T> executables = createExecutableExtensions(extension, elementName, propertyName, expectedType);
        executables.forEach(ex -> initializer.accept(extension, ex));
        return executables;
    }

    /**
     * Creates the objects defined by the given extension. A lot of extension points allow to define
     * more than one object with one extension. Examples are Eclipse's
     * <code>org.eclipse.jdt.core.classpathVariableInitializer</code> or Faktor-IPS's
     * <code>datatypeDefinition</code> extension points.
     * 
     * @param extension The extension.
     * @param elementName Name of the config elements that contains a property that specifies the
     *            qualified name of the class to instantiate. The extension contains at least one
     *            element with the given name, but can contain more than one.
     * @param propertyName Name of the config element's property that contains the qualified name of
     *            the class to instantiate.
     * @param expectedType The expected class/type of the instance.
     * 
     * @see IConfigurationElement#createExecutableExtension(String)
     * 
     * @throws NullPointerException if any of the arguments is <code>null</code>.
     */
    public static final <T> List<T> createExecutableExtensions(IExtension extension,
            String elementName,
            String propertyName,
            Class<T> expectedType) {

        IConfigurationElement[] configElements = extension.getConfigurationElements();
        List<T> executables = new ArrayList<>(configElements.length);
        if (configElements.length == 0) {
            String text = "No config elements with name " + elementName + " found in extension "//$NON-NLS-1$ //$NON-NLS-2$
                    + extension.getUniqueIdentifier() + "."; //$NON-NLS-1$
            IpsLog.log(new IpsStatus(IStatus.WARNING, text));
            return executables;
        }
        for (IConfigurationElement configElement : configElements) {
            if (elementName.equalsIgnoreCase(configElement.getName())) {
                T newExecutable = createExecutableExtension(extension, configElement, propertyName, expectedType);
                if (newExecutable != null) {
                    executables.add(newExecutable);
                }
            }
        }
        return executables;
    }

    /**
     * Wrapper around IConfigurationElement.createExecutableExtension(propertyName) with detailed
     * logging. If the executable extension couldn't be created, the reason is logged, no exception
     * is thrown. The returned object is of the expected type.
     * 
     * @param extension The extension.
     * @param elementName Name of the config element that contains a property that specifies the
     *            qualified name of the class to instantiate.
     * @param propertyName Name of the property that contains the qualified name of the class to
     *            instantiate.
     * @param expectedType The expected class/type of the instance.
     * 
     * @see IConfigurationElement#createExecutableExtension(String)
     * 
     * @throws NullPointerException if any of the arguments is <code>null</code>.
     */
    public static final <T> T createExecutableExtension(IExtension extension,
            String elementName,
            String propertyName,
            Class<T> expectedType) {

        IConfigurationElement configElement = null;
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        for (IConfigurationElement configElement2 : configElements) {
            if (elementName.equalsIgnoreCase(configElement2.getName())) {
                configElement = configElement2;
            }
        }
        if (configElement == null) {
            String text = "Can't cerate executable extension " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + ". No config element with name " + elementName + " found."; //$NON-NLS-1$ //$NON-NLS-2$
            IpsLog.log(new IpsStatus(text));
            return null;
        }
        return createExecutableExtension(extension, configElement, propertyName, expectedType);
    }

    /**
     * Wrapper around IConfigurationElement.createExecutableExtension(propertyName) with detailed
     * logging. If the executable extension couldn't be created, the reason is logged, no exception
     * is thrown. The returned object is of the expected type.
     * 
     * @param extension The extension.
     * @param element A config element of the extension that contains a property that specifies the
     *            qualified name of the class to instantiate.
     * @param propertyName Name of the property that contains the qualified name of the class to
     *            instantiate.
     * @param expectedType The expected class/type of the instance.
     * 
     * @see IConfigurationElement#createExecutableExtension(String)
     */
    public static final <T> T createExecutableExtension(IExtension extension,
            IConfigurationElement element,
            String propertyName,
            Class<T> expectedType) {
        return createExecutableExtension(extension.getUniqueIdentifier(), element, propertyName, expectedType);
    }

    /**
     * Wrapper around IConfigurationElement.createExecutableExtension(propertyName) with detailed
     * logging. If the executable extension couldn't be created, the reason is logged, no exception
     * is thrown. The returned object is of the expected type.
     * 
     * @param extensionId The unique id of the extension for logging purposes
     * @param element A {@link IConfigurationElement} of the extension that contains a property that
     *            specifies the qualified name of the class to instantiate.
     * @param propertyName Name of the property that contains the qualified name of the class to
     *            instantiate.
     * @param expectedType The expected class/type of the instance.
     * 
     * @return the created extension or {@code null} if it couldn't be created
     * 
     * @see IConfigurationElement#createExecutableExtension(String)
     */
    public static final <T> T createExecutableExtension(String extensionId,
            IConfigurationElement element,
            String propertyName,
            Class<T> expectedType) {

        Object object = null;
        try {
            object = element.createExecutableExtension(propertyName);
            if (object == null) {
                IpsLog.log(new IpsStatus("Unable to create extension " //$NON-NLS-1$
                        + extensionId + ". Reason: " //$NON-NLS-1$
                        + propertyName + " is null")); //$NON-NLS-1$
                return null;
            }
            // CSOFF: IllegalCatchCheck
        } catch (Exception e) {
            IpsLog.log(new IpsStatus("Unable to create extension " //$NON-NLS-1$
                    + extensionId + ". Reason: Can't instantiate " //$NON-NLS-1$
                    + element.getAttribute(propertyName), e));
            return null;
        }
        // CSON: IllegalCatchCheck
        if (!(expectedType.isAssignableFrom(object.getClass()))) {
            IpsLog.log(new IpsStatus("Unable to create extension " //$NON-NLS-1$
                    + extensionId + "Reason: " //$NON-NLS-1$
                    + element.getAttribute(propertyName) + " is not of type " //$NON-NLS-1$
                    + expectedType));
            return null;
        }
        return expectedType.cast(object);
    }

    public static IConfigurationElement getReleaseExtensionElement(IIpsProject ipsProject) {
        String releaseExtensionId = ipsProject.getReadOnlyProperties().getReleaseExtensionId();
        IConfigurationElement[] configElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(IpsModelActivator.PLUGIN_ID, RELEASE_EXTENSION_POINT_NAME);
        for (IConfigurationElement confElement : configElements) {
            if (confElement.getAttribute("id").equals(releaseExtensionId)) { //$NON-NLS-1$
                return confElement;
            }
        }
        return null;
    }

    public static final Stream<IConfigurationElement> getConfigurationElements(IExtension extension,
            String elementName) {
        return Arrays.stream(extension.getConfigurationElements())
                .filter(configElement -> elementName.equalsIgnoreCase(configElement.getName()));
    }

    public IExtensionRegistry getRegistry() {
        return registry;
    }

    public String getNameSpace() {
        return nameSpace;
    }

}
