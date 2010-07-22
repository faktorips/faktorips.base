/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * An interface that marks an object as being able to read via SAX.
 * 
 * @author Joerg Ortmann
 */
public interface XmlSaxSupport {

    /**
     * (Re)Initializes the object's state with the input stream.
     * 
     * @param is input stream
     */
    public void initFromInputStream(InputStream is) throws CoreException;

    /**
     * Adds the given extension property. If the extension property not exists as definitions then
     * the property will be ignored.
     * 
     * @param propertyId The id of the extension property
     * @param extPropertyValue The value of the extension property
     */
    public void addExtensionProperty(String propertyId, String extPropertyValue);

}
