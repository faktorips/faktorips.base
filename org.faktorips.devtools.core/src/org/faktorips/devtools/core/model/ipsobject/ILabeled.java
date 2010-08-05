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

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

/**
 * A labeled object is an object that supports attaching {@link ILabel}s in different languages to
 * it. Labels can be attached for all languages that are supported by the IPS project the labeled
 * object belongs to.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 * 
 * @see ILabel
 * @see IIpsProjectProperties#getSupportedLanguages()
 */
public interface ILabeled {

    /**
     * Returns the {@link ILabel} for the given {@link Locale}. If no label for the locale exists,
     * <tt>null</tt> is returned.
     * 
     * @param locale The locale to retrieve the label for.
     * 
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public ILabel getLabel(Locale locale);

    /**
     * Returns an unmodifiable view on the set of labels this object currently has attached.
     */
    public Set<ILabel> getLabels();

    /**
     * Returns whether this element has a plural label. Only if this method returns <tt>true</tt> is
     * it allowed to call {@link #getCurrentLocalePluralLabelValue()}.
     */
    public boolean isPluralLabelSupported();

    /**
     * Directly returns the value of the label for the current locale. The current locale is the
     * locale that Faktor-IPS uses at the time this operation is called to internationalize
     * Faktor-IPS models.
     * <p>
     * If there is no label for that locale, <tt>null</tt> is returned.
     * 
     * @see IpsPlugin#getIpsModelLocale()
     */
    public String getCurrentLocaleLabelValue();

    /**
     * Directly returns the value of the plural label for the current locale. The current locale is
     * the locale that Faktor-IPS uses at the time this operation is called to internationalize
     * Faktor-IPS models.
     * <p>
     * If there is no plural label for that locale, <tt>null</tt> is returned.
     * 
     * @throws UnsupportedOperationException If this object does not support plural labels.
     * 
     * @see IpsPlugin#getIpsModelLocale()
     * @see #isPluralLabelSupported()
     */
    public String getCurrentLocalePluralLabelValue();

    /**
     * Creates a new label for this object.
     */
    public ILabel newLabel();

}
