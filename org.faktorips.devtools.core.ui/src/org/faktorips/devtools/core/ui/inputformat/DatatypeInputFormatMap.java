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

package org.faktorips.devtools.core.ui.inputformat;

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

    private Map<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> inputFormatMap = new ConcurrentHashMap<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory>();

    /**
     * This method retrieves the Datatypes and the related Formats that are registered at the
     * according extension-point. It is in the responsibility of the user if this method is
     * considered.
     * <p>
     * Note: if multiple factories are registered for the same datatype only the factory loaded the
     * last will be used.
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
                        Class<? extends ValueDatatype> datatypeClass = resolveDatatypeClass(extension, datatypeElement);
                        getInputFormatMap().put(datatypeClass, inputFormatFactory);
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(new IpsStatus(
                            "Unable to create the InputFormatFactory identified by the extension unique identifier: " //$NON-NLS-1$
                                    + extension.getUniqueIdentifier(), e));
                }
            }
        }
    }

    private Class<? extends ValueDatatype> resolveDatatypeClass(IExtension extension,
            IConfigurationElement datatypeElement) {
        String classAttribute = datatypeElement.getAttribute(IpsUIPlugin.CONFIG_PROPERTY_CLASS);
        try {
            @SuppressWarnings("unchecked")
            // defined safe by extension definition
            Class<? extends ValueDatatype> datatypeClass = (Class<? extends ValueDatatype>)Class
                    .forName(classAttribute);
            return datatypeClass;
        } catch (ClassNotFoundException e) {
            throw new CoreRuntimeException(new IpsStatus(
                    "Cannot load class " + classAttribute + " while loading extension " //$NON-NLS-1$ //$NON-NLS-2$
                            + extension.getUniqueIdentifier(), e));
        }
    }

    /**
     * Returns an {@link IInputFormat} for the given datatype. Uses the input format factories
     * registered via the extension point org.faktorips.devtools.core.ui.inputFormat.
     * <p>
     * If the requested datatype is not registered via extension point directly, the formatter for
     * the datatype's super-class is returned. If there are registered multiple super-classes the
     * nearest super-class is used.
     * <p>
     * If no format can be found regardlessly, a {@link DefaultInputFormat} is returned as a
     * fall-back. Thus this method never returns <code>null</code>.
     * <p>
     * If the parameter datatype is null, always the {@link DefaultInputFormat} is returned.
     * 
     * @param datatype the {@link ValueDatatype} to create an {@link IInputFormat} for.
     */
    public IInputFormat<String> getDatatypeInputFormat(ValueDatatype datatype) {
        IDatatypeInputFormatFactory inputformatFactory;
        if (datatype == null) {
            inputformatFactory = null;
        } else {
            inputformatFactory = getInputFormatMap().get(datatype);
            if (inputformatFactory == null) {
                inputformatFactory = getNearestSupertypeFactory(datatype);
            }
        }
        return createInputFormatOrReturnDefault(datatype, inputformatFactory);
    }

    /**
     * Examines the registered {@link IDatatypeInputFormatFactory factories} whether they are
     * responsible for a super-type of the given datatype. Returns <code>null</code> if no factory
     * can be found. If factories for multiple super-types of the given datatype have been
     * registered, the factory for the nearest super-type is returned. OIW always returns the format
     * factory for the most specific datatype possible.
     */
    private IDatatypeInputFormatFactory getNearestSupertypeFactory(ValueDatatype datatype) {
        IDatatypeInputFormatFactory inputformatFactory;
        Class<? extends ValueDatatype> datatypeClass = datatype.getClass();
        inputformatFactory = getNearestSupertypeFactoryByClass(datatypeClass);
        return inputformatFactory;
    }

    private IDatatypeInputFormatFactory getNearestSupertypeFactoryByClass(Class<? extends ValueDatatype> requiredClass) {
        Entry<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> previouslyFoundEntry = null;
        for (Entry<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> currentEntry : getInputFormatMap()
                .entrySet()) {
            if (qualifiesAsFactory(previouslyFoundEntry, currentEntry, requiredClass)) {
                previouslyFoundEntry = currentEntry;
            }
        }
        return previouslyFoundEntry == null ? null : previouslyFoundEntry.getValue();
    }

    private boolean qualifiesAsFactory(Entry<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> previousEntry,
            Entry<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> currentEntry,
            Class<? extends ValueDatatype> requiredClass) {
        Class<? extends ValueDatatype> registeredDatatypeClass = currentEntry.getKey();
        if (registeredDatatypeClass.isAssignableFrom(requiredClass)) {
            if (providesMoreSpecificFactoryThanPreviousEntry(previousEntry, currentEntry)) {
                return true;
            }
        }
        return false;
    }

    private boolean providesMoreSpecificFactoryThanPreviousEntry(Entry<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> previousEntry,
            Entry<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> currentEntry) {
        if (previousEntry != null) {
            Class<? extends ValueDatatype> previouslyFoundSuperDatatypeClass = previousEntry.getKey();
            Class<? extends ValueDatatype> currentSuperDatatypeClass = currentEntry.getKey();
            return previouslyFoundSuperDatatypeClass.isAssignableFrom(currentSuperDatatypeClass);
        }
        return true;
    }

    /**
     * Returns a new input format created by the given {@link IDatatypeInputFormatFactory}. If the
     * given factory is <code>null</code>, a {@link DefaultInputFormat} is returned as a fall-back.
     */
    private IInputFormat<String> createInputFormatOrReturnDefault(ValueDatatype datatype,
            IDatatypeInputFormatFactory inputformatFactory) {
        if (inputformatFactory != null) {
            return inputformatFactory.newInputFormat(datatype);
        } else {
            return new DefaultInputFormat();
        }
    }

    protected Map<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> getInputFormatMap() {
        return inputFormatMap;
    }
}
