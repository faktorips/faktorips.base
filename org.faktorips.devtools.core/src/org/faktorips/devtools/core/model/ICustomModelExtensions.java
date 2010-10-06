/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model;

import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;

/**
 * The single instance of this class is owned by the IpsModel and gives access to extensions of the
 * Faktor-IPS meta model. Currently three types of extension are supported.
 * <ol>
 * <li>Extension Properties</li>
 * <li>Custom Validations</li>
 * <li>Naming Strategies for Product Components</li>
 * </ol>
 * 
 * Extensions to the Faktor-IPS meta model can be contributed by PlugIn extensions.
 * 
 * @see IExtensionPropertyDefinition
 * @see ICustomValidation
 * @see IProductCmptNamingStrategy
 * 
 * @author Jan Ortmann
 * 
 * @since 3.1.0
 */
public interface ICustomModelExtensions {

    /**
     * Returns the extension properties for the given type. Returns an empty array if no extension
     * property is defined.
     * 
     * @param type The published interface of the IPS object or part e.g.
     *            <code>org.faktorips.devtools.core.model.type.IAttribute</code>
     * @param includeSupertypesAndInterfaces <code>true</code> if not only the extension properties
     *            defined for for the type itself should be returned, but also the ones registered
     *            for it's super type(s) and it's interfaces.
     */
    Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the extension property with the given id that belongs to the given type. Returns
     * <code>null</code> if no such extension property is defined.
     * 
     * @param type The published interface of the IPS object or part e.g.
     *            <code>or.faktorips.devtools.core.model.type.IAttribute</code>
     * @param propertyId the extension property id
     * @param includeSupertypesAndInterfaces <code>true</code> if not only the extension properties
     *            defined for for the type itself should be returned, but also the ones registered
     *            for it's super type(s) and it's interfaces.
     */
    IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the custom validations for the given type. The result includes also validations that
     * are defined for any of the type's super types. Returns an empty list if no such validation
     * exists.
     * <p>
     * Errors that occur while initializing the custom validations are logged, no exception is
     * thrown.
     * 
     * @param type The published interface of the IPS object or part e.g.
     *            <code>org.faktorips.devtools.core.model.type.IAttribute</code>
     * 
     * @throws NullPointerException if type is <code>null</code>.
     */
    <T extends IIpsObjectPartContainer> Set<ICustomValidation<T>> getCustomValidations(Class<T> type);

    /**
     * Returns the product component naming stratgey identified by the given extension id.
     * 
     * @param extensionId The extension ID that identifies the naming strategy.
     * @return The naming strategy of <code>null</code> if none is found for the given extension id.
     * 
     * @throws NullPointerException if extension id is <code>null</code>.
     * 
     * @see IProductCmptNamingStrategy#getExtensionId()
     */
    public IProductCmptNamingStrategy getProductCmptNamingStrategy(String extensionId);
}