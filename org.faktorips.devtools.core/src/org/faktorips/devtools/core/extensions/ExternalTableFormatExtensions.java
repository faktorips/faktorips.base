/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.extensions.LazyCollectionExtension;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.IValueConverter;

public class ExternalTableFormatExtensions extends LazyCollectionExtension<ITableFormat, List<ITableFormat>> {

    private static final String EXTENSION_POINT_ID_EXTERNAL_TABLE_FORMAT = "externalTableFormat"; //$NON-NLS-1$
    private static final String EXTENSION_POINT_ID_EXTERNAL_VALUE_CONVERTER = "externalValueConverter"; //$NON-NLS-1$

    public ExternalTableFormatExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_EXTERNAL_TABLE_FORMAT,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                ITableFormat.class,
                ArrayList::new,
                (formatElement, format, list) -> {
                    initExternalTableFormat(format, formatElement, extensionPoints.getRegistry());
                    list.add(format);
                });
    }

    /**
     * Initialize the given format (fill with values provided by the given formatElement and with
     * <code>IValueConverter</code>s configured in other extension points.
     * 
     * @param format The external table format to initialize.
     * @param formatElement The configuration element which defines the given external table format.
     */
    private static void initExternalTableFormat(ITableFormat format,
            IConfigurationElement formatElement,
            IExtensionRegistry extensionRegistry) {
        format.setName(formatElement.getAttribute("name")); //$NON-NLS-1$
        format.setDefaultExtension(formatElement.getAttribute("defaultExtension")); //$NON-NLS-1$

        IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor(IpsPlugin.PLUGIN_ID,
                EXTENSION_POINT_ID_EXTERNAL_VALUE_CONVERTER);

        for (IConfigurationElement element : elements) {
            String tableFormatId = formatElement.getAttribute("id"); //$NON-NLS-1$
            if (element.getAttribute("tableFormatId").equals(tableFormatId)) { //$NON-NLS-1$
                // Converter found for current table format id.
                IConfigurationElement[] valueConverters = element.getChildren();
                for (IConfigurationElement valueConverter : valueConverters) {
                    try {
                        IValueConverter converter = (IValueConverter)valueConverter
                                .createExecutableExtension(ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS);
                        format.addValueConverter(converter);
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }
            }
        }
    }

}
