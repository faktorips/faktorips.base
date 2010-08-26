/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.util.ArgumentCheck;

/**
 * Definition of extension points provided by Faktor-IPS.
 * 
 * @author Jan Ortmann
 */
public class ExtensionPoints {

    /**
     * IpsPlugin relative id of the extension point for IpsObjectTypes.
     * 
     * @see IpsObjectType
     */
    public final static String IPS_OBJECT_TYPE = "ipsobjecttype"; //$NON-NLS-1$

    /**
     * IpsPlugin relative id of the extension point for custom validations.
     * 
     * @see IpsObjectType
     */
    public final static String CUSTOM_VALIDATION = "customValidation"; //$NON-NLS-1$

    private IExtensionRegistry registry;
    private String nameSpace;

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
     * Returns all extensions defined for the given point. The point id should be one of the
     * constants defined in this class.
     * 
     * @throws NullPointerException if pointId is <code>null</code>.
     * @throws IllegalArgumentException if no extension point with id pointId exists.
     */
    public final IExtension[] getExtension(String pointId) {
        ArgumentCheck.notNull(pointId);
        IExtensionPoint point = registry.getExtensionPoint(nameSpace, pointId);
        if (point == null) {
            IpsPlugin.log(new IpsStatus("ExtensionPoint " + pointId + " not found!")); //$NON-NLS-1$ //$NON-NLS-2$
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

        ArgumentCheck.notNull(pointId);
        IExtensionPoint point = registry.getExtensionPoint(nameSpace, pointId);
        if (point == null) {
            IpsPlugin.log(new IpsStatus("ExtensionPoint " + pointId + " not found!")); //$NON-NLS-1$ //$NON-NLS-2$
            throw new IllegalArgumentException("Unknown extension point " + pointId); //$NON-NLS-1$
        }
        IExtension[] extensions = point.getExtensions();
        List<T> execExtensions = new ArrayList<T>(extensions.length);
        for (int i = 0; i < extensions.length; i++) {
            T newExt = createExecutableExtension(extensions[i], elementName, propertyName, expectedType);
            if (newExt != null) {
                execExtensions.add(newExt);
            }
        }
        return execExtensions;

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
    public final static <T> T createExecutableExtension(IExtension extension,
            String elementName,
            String propertyName,
            Class<T> expectedType) {

        IConfigurationElement configElement = null;
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        for (int i = 0; i < configElements.length; i++) {
            if (elementName.equalsIgnoreCase(configElements[i].getName())) {
                configElement = configElements[i];
            }
        }
        if (configElement == null) {
            String text = "Can't cerate executable extension " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + ". No config element with name " + elementName + " found."; //$NON-NLS-1$ //$NON-NLS-2$
            IpsPlugin.log(new IpsStatus(text));
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
    public final static <T> T createExecutableExtension(IExtension extension,
            IConfigurationElement element,
            String propertyName,
            Class<T> expectedType) {

        Object object = null;
        try {
            object = element.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Unable to create extension " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + ". Reason: Can't instantiate " //$NON-NLS-1$
                    + element.getAttribute(propertyName), e));
            return null;
        }
        if (!(expectedType.isAssignableFrom(object.getClass()))) {
            IpsPlugin.log(new IpsStatus("Unable to create extension " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + "Reason: " //$NON-NLS-1$
                    + element.getAttribute(propertyName) + " is not of type " //$NON-NLS-1$
                    + expectedType));
            return null;
        }
        return expectedType.cast(object);
    }

}
