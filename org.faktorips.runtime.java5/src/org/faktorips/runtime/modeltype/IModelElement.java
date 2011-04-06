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

package org.faktorips.runtime.modeltype;

import java.util.Locale;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IRuntimeRepository;

/**
 * Base Interface for all model elements.
 * 
 * @author Daniel Hohenberger
 */
public interface IModelElement {

    public static final String PROPERTY_NAME = "name";

    public static final String EXTENSION_PROPERTIES_XML_TAG = "Value";

    public static final String EXTENSION_PROPERTIES_XML_WRAPPER_TAG = "ExtensionProperties";

    public static final String EXTENSION_PROPERTIES_PROPERTY_ID = "id";

    public static final String EXTENSION_PROPERTIES_PROPERTY_NULL = "isNull";

    /**
     * @param propertyId the id of the desired extension property. Returns the value of the
     *            extension property defined by the given <code>propertyId</code> or
     *            <code>null</code> if the extension property's <code>isNull</code> attribute is
     *            <code>true</code>.
     * @throws IllegalArgumentException if no such property exists.
     */
    public Object getExtensionPropertyValue(String propertyId) throws IllegalArgumentException;

    /**
     * Returns a set of the extension property ids defined for this element.
     */
    public Set<String> getExtensionPropertyIds();

    /**
     * Returns the name of this model type.
     */
    public String getName();

    /**
     * Initializes the model element's state with the data stored in the xml element at the parser's
     * current position.
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException;

    /**
     * Initializes the model element's extension properties with the data stored in the xml element
     * at the parser's current position. This method assumes that the element is
     * <code>&lt;ExtensionProperties&gt;</code>.
     */
    public void initExtPropertiesFromXml(XMLStreamReader parser) throws XMLStreamException;

    /**
     * Returns the repository this model element belongs to. This method never returns
     * <code>null</code>.
     */
    public IRuntimeRepository getRepository();

    /**
     * Returns the label for the given locale.
     * <p>
     * Returns the element's name if no label for the given locale exists.
     */
    public String getLabel(Locale locale);

    /**
     * Returns the plural label for the given locale.
     * <p>
     * Returns the element's name if no plural label for the given locale exists.
     */
    public String getLabelForPlural(Locale locale);

}
