/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.datatype.DatatypeDefinition;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsModelActivator;

/**
 * {@link Datatype}-by-id-qualified-name-supplier for all implementations of the extension point
 * {@value #EXTENSION_POINT_ID_DATATYPE_DEFINITION}.
 */
public class PredefinedDatatypesExtensions extends
        LazyIntermediateCollectionExtension<DatatypeDefinition, Map<IConfigurationElement, Datatype>, Map<String, Datatype>> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_DATATYPE_DEFINITION}.
     */
    public static final String EXTENSION_POINT_ID_DATATYPE_DEFINITION = "datatypeDefinition"; //$NON-NLS-1$

    public static final String EXTENSION_ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    public PredefinedDatatypesExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_DATATYPE_DEFINITION,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                DatatypeDefinition.class,
                LinkedHashMap::new,
                PredefinedDatatypesExtensions::createDatatype,
                PredefinedDatatypesExtensions::sortByIpsAndOtherProviders);
    }

    @Override
    protected DatatypeDefinition create(IConfigurationElement configElement) {
        return new DatatypeDefinition((IExtension)configElement.getParent(), configElement);
    }

    private static void createDatatype(IConfigurationElement configElement,
            DatatypeDefinition datatypeDefinition,
            Map<IConfigurationElement, Datatype> datatypesByConfigElement) {
        if (datatypeDefinition.hasDatatype()) {
            var datatype = datatypeDefinition.getDatatype();
            datatypesByConfigElement.put(configElement, datatype);
        }
    }

    private static Map<String, Datatype> sortByIpsAndOtherProviders(
            Map<IConfigurationElement, Datatype> datatypesByConfigElement) {
        Map<String, Datatype> datatypesByQualifiedName = new LinkedHashMap<>();
        addAll(datatypesByConfigElement, datatypesByQualifiedName, Predicate.isEqual(IpsModelActivator.PLUGIN_ID));
        addAll(datatypesByConfigElement, datatypesByQualifiedName, not(isEqual(IpsModelActivator.PLUGIN_ID)));
        return datatypesByQualifiedName;
    }

    private static void addAll(Map<IConfigurationElement, Datatype> datatypesByConfigElement,
            Map<String, Datatype> datatypesByQualifiedName,
            Predicate<String> namespaceFilter) {
        datatypesByConfigElement.entrySet().stream()
                .filter(e -> namespaceFilter.test(((IExtension)e.getKey().getParent()).getNamespaceIdentifier()))
                .map(e -> e.getValue())
                .forEach(d -> datatypesByQualifiedName.put(d.getQualifiedName(), d));
    }

}
