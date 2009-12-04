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
     * IpsPlugin relative id of the extension point for IpsObjectTypes.
     * 
     * @see IpsObjectType
     */
    public final static String IPS_OBJECT_TYPE = "ipsobjecttype"; //$NON-NLS-1$

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
     * Wrapper around IConfigurationElement.createExecutableExtension(propertyName) with detailed
     * logging. If the executable extension couldn't be created, the reason is logged, no exception
     * is thrown. The returned object is of the expected type.
     * 
     * @param extension The extension to create an
     * @param element
     * @param propertyName
     * @param expectedType
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
