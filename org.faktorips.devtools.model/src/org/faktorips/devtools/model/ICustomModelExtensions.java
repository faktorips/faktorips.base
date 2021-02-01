/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.type.IAttribute;

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
     *            <code>org.faktorips.devtools.model.type.IAttribute</code>
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
     * @param type The published interface of the IPS object or part e.g. {@link IAttribute}
     * @param propertyId the extension property id
     * @param includeSupertypesAndInterfaces <code>true</code> if not only the extension properties
     *            defined for for the type itself should be returned, but also the ones registered
     *            for it's super type(s) and it's interfaces.
     */
    IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns a map {@link IExtensionPropertyDefinition} identified by their IDs, that are defined
     * and activated for the given {@link IIpsObjectPartContainer}.
     * 
     * @param object The {@link IIpsObjectPartContainer} for which you want to get the
     *            {@link IExtensionPropertyDefinition}
     * @return A map of {@link IExtensionPropertyDefinition} that are identified by their IDs
     */
    Map<String, IExtensionPropertyDefinition> getExtensionPropertyDefinitions(IIpsObjectPartContainer object);

    /**
     * Returns the custom validations for the given type. The result includes also validations that
     * are defined for any of the type's super types. Returns an empty list if no such validation
     * exists.
     * <p>
     * Errors that occur while initializing the custom validations are logged, no exception is
     * thrown.
     * 
     * @param type The published interface of the IPS object or part e.g.
     *            <code>org.faktorips.devtools.model.type.IAttribute</code>
     * 
     * @throws NullPointerException if type is <code>null</code>.
     */
    <T extends IIpsObjectPartContainer> Set<ICustomValidation<?>> getCustomValidations(Class<T> type);

    /**
     * Returns a factory to create product component naming strategies. The type of strategy is
     * identified by the given extension id.
     * 
     * @param extensionId The extension ID that identifies the type of naming strategy.
     * @return The naming strategy of <code>null</code> if none is found for the given extension id.
     * 
     * @throws NullPointerException if extension is <code>null</code>.
     * 
     * @see IProductCmptNamingStrategyFactory#getExtensionId()
     */
    public IProductCmptNamingStrategyFactory getProductCmptNamingStrategyFactory(String extensionId);
}