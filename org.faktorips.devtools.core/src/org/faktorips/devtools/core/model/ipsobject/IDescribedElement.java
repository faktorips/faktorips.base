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

package org.faktorips.devtools.core.model.ipsobject;

import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

/**
 * A described element is an element that supports attaching {@link IDescription}s in different
 * languages to it. Descriptions can be attached for all languages that are supported by the IPS
 * project the described element belongs to.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 * 
 * @see IDescription
 * @see IIpsProjectProperties#getSupportedLanguages()
 */
public interface IDescribedElement extends IIpsElement {

    /**
     * Returns the {@link IDescription} for the given {@link Locale}. If no description for the
     * locale exists, <tt>null</tt> is returned.
     * 
     * @param locale The locale to retrieve the description for.
     * 
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public IDescription getDescription(Locale locale);

    /**
     * Returns an unmodifiable view on the set of descriptions of this element.
     */
    public Set<IDescription> getDescriptions();

    /**
     * Creates a new description for this element.
     */
    public IDescription newDescription();

}
