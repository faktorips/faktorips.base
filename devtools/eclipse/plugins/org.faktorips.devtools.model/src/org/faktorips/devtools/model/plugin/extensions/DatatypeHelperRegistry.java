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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.datatype.DatatypeDefinition;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/** Registry for looking up the {@link DatatypeHelper} for a {@link Datatype}. */
public class DatatypeHelperRegistry extends
        LazyCollectionExtension<DatatypeDefinition, Map<Datatype, DatatypeHelper>> {

    private static final String DATATYPE_DEFINITION_EXTENSION_POINT = "datatypeDefinition";

    public DatatypeHelperRegistry(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                DATATYPE_DEFINITION_EXTENSION_POINT,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                DatatypeDefinition.class,
                LinkedHashMap::new,
                DatatypeHelperRegistry::createDatatype);
    }

    @Override
    protected DatatypeDefinition create(IConfigurationElement configElement) {
        return new DatatypeDefinition((IExtension)configElement.getParent(), configElement);
    }

    /**
     * @param configElement unused
     */
    private static void createDatatype(IConfigurationElement configElement,
            DatatypeDefinition datatypeDefinition,
            Map<Datatype, DatatypeHelper> registry) {
        if (datatypeDefinition.hasDatatype() && datatypeDefinition.hasHelper()) {
            registry.put(datatypeDefinition.getDatatype(), datatypeDefinition.getHelper());
        }
    }
}
