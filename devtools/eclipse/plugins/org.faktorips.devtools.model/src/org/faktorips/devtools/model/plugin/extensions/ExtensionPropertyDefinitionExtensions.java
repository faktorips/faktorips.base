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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.devtools.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition.RetentionPolicy;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link IExtensionPropertyDefinition}-by-{@link Class}-{@link Map}-supplier for all
 * implementations of the extension point {@value #EXTENSION_POINT_ID_OBJECT_EXTENSION_PROPERTY}.
 */
public class ExtensionPropertyDefinitionExtensions extends
        LazyCollectionExtension<IExtensionPropertyDefinition, Map<Class<?>, List<IExtensionPropertyDefinition>>> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_OBJECT_EXTENSION_PROPERTY}.
     */
    public static final String EXTENSION_POINT_ID_OBJECT_EXTENSION_PROPERTY = "objectExtensionProperty"; //$NON-NLS-1$

    public ExtensionPropertyDefinitionExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_OBJECT_EXTENSION_PROPERTY,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IExtensionPropertyDefinition.class,
                HashMap::new,
                (configElement, extProperty, typeExtensionPropertiesMap) -> {
                    initExtensionProperty((ExtensionPropertyDefinition)extProperty, configElement);
                    typeExtensionPropertiesMap.computeIfAbsent(extProperty.getExtendedType(), $ -> new ArrayList<>())
                            .add(extProperty);
                },
                map -> map.values().forEach(Collections::sort));
    }

    /* private */ static void initExtensionProperty(ExtensionPropertyDefinition extProperty,
            IConfigurationElement element) {
        IExtension extension = element.getDeclaringExtension();
        extProperty.setPropertyId(extension.getUniqueIdentifier());
        extProperty.setName(extension.getLabel());
        extProperty.setDefaultValue(element.getAttribute("defaultValue")); //$NON-NLS-1$
        extProperty.setPosition(element.getAttribute("position")); //$NON-NLS-1$
        String retentionString = element.getAttribute("retention"); //$NON-NLS-1$
        if (IpsStringUtils.isEmpty(retentionString)) {
            extProperty.setRetention(RetentionPolicy.RUNTIME);
        } else {
            extProperty.setRetention(RetentionPolicy.valueOf(retentionString));
        }
        if (IpsStringUtils.isNotEmpty(element.getAttribute("order"))) { //$NON-NLS-1$
            extProperty.setSortOrder(Integer.parseInt(element.getAttribute("order"))); //$NON-NLS-1$
        }
        String extType = element.getAttribute("extendedType"); //$NON-NLS-1$
        try {
            extProperty.setExtendedType(extProperty.getClass().getClassLoader().loadClass(extType));
        } catch (ClassNotFoundException e) {
            IpsLog.log(new IpsStatus("Extended type " + extType //$NON-NLS-1$
                    + " not found for extension property " //$NON-NLS-1$
                    + extProperty.getPropertyId(), e));
        }
    }

}
