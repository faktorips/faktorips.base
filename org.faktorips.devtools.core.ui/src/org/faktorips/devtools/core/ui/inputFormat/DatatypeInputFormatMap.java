/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.inputFormat;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class DatatypeInputFormatMap {

    private Map<ValueDatatype, IDatatypeInputFormatFactory> inputFormatMap = new ConcurrentHashMap<ValueDatatype, IDatatypeInputFormatFactory>();

    /**
     * This method retrieve and save the Datatypes and the related Formats that are registered at
     * the according extension-point. It is in the responsibility of the user if this method is
     * considered.
     * 
     */
    public void initDatatypeInputFormatMap(IExtensionRegistry registry) {
        getInputFormatMap().clear();
        ExtensionPoints extensionPoints = new ExtensionPoints(registry, IpsUIPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension(IpsUIPlugin.EXTENSION_POINT_INPUT_FORMAT);
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    IDatatypeInputFormatFactory inputFormatFactory = (IDatatypeInputFormatFactory)configElement
                            .createExecutableExtension(IpsUIPlugin.CONFIG_PROPERTY_CLASS);

                    for (IConfigurationElement datatypeElement : configElement.getChildren()) {
                        ValueDatatype datatype = (ValueDatatype)datatypeElement
                                .createExecutableExtension(IpsUIPlugin.CONFIG_PROPERTY_CLASS);

                        getInputFormatMap().put(datatype, inputFormatFactory);
                    }

                } catch (CoreException e) {
                    throw new CoreRuntimeException(new IpsStatus(
                            "Unable to create the InputFormatFactory identified by the extension unique identifier: " //$NON-NLS-1$
                                    + extension.getUniqueIdentifier(), e));
                }
            }
        }
    }

    public IInputFormat<String> getDatatypeInputFormat(ValueDatatype datatype) {
        Class<? extends ValueDatatype> datatypeClass = datatype.getClass();
        IDatatypeInputFormatFactory inputformatFactory = getInputFormatMap().get(datatype);
        if (inputformatFactory == null) {
            IDatatypeInputFormatFactory supertypeOfDatatype = getAllowedSupertype(datatypeClass);
            // if(supertypeOfDatatype== null){
            // return new Default
            // }
            return supertypeOfDatatype.newInputFormat(datatype);
        }
        return inputformatFactory.newInputFormat(datatype);
    }

    private IDatatypeInputFormatFactory getAllowedSupertype(Class<? extends ValueDatatype> requiredClass) {
        Map<ValueDatatype, IDatatypeInputFormatFactory> inputFormatMap = getInputFormatMap();
        for (Entry<ValueDatatype, IDatatypeInputFormatFactory> entry : inputFormatMap.entrySet()) {
            ValueDatatype registeredDatatype = entry.getKey();
            Class<? extends ValueDatatype> registeredClass = registeredDatatype.getClass();
            if (registeredClass.isAssignableFrom(requiredClass)) {
                return entry.getValue();
            }
        }
        return null;
    }

    protected Map<ValueDatatype, IDatatypeInputFormatFactory> getInputFormatMap() {
        return inputFormatMap;
    }
}
