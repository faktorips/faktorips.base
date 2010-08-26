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
 * A labeled element is an element that supports attaching {@link ILabel}s in different languages to
 * it. Labels can be attached for all languages that are supported by the IPS project the labeled
 * element belongs to.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 * 
 * @see ILabel
 * @see IIpsProjectProperties#getSupportedLanguages()
 */
public interface ILabeledElement {

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
     * Returns an unmodifiable view on the set of labels this element currently has attached.
     */
    public Set<ILabel> getLabels();

    /**
     * Returns whether this element has a plural label.
     */
    public boolean isPluralLabelSupported();

    /**
     * Returns the label for the locale that Faktor-IPS uses at the time this operation is called to
     * internationalize Faktor-IPS models.
     * <p>
     * If there is no label for that locale, <tt>null</tt> is returned.
     * 
     * @see IpsPlugin#getIpsModelLocale()
     */
    public ILabel getLabelForIpsModelLocale();

    /**
     * Returns the label for the default language. The default language is specified trough the IPS
     * project. Returns <tt>null</tt> if no label for the default language exists or no default
     * language is specified.
     */
    public ILabel getLabelForDefaultLocale();

    /**
     * Creates a new label for this element.
     */
    public ILabel newLabel();

}
