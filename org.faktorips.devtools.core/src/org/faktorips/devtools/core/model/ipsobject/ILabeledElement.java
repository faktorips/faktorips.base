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
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.type.IAttribute;

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

    /**
     * Returns the element's current label. That is primarily the value of the {@link ILabel} for
     * the IPS model locale as returned by {@link #getLabelForIpsModelLocale()}.
     * <p>
     * Should no {@link ILabel} exist for that locale, the next in question is the {@link ILabel}
     * for the default locale as returned by {@link #getLabelForDefaultLocale()}.
     * <p>
     * Should no {@link ILabel} exist for that locale as well, a last-resort label is returned that
     * is specific to the element (e.g. the capitalized name for attributes).
     * <p>
     * Note that it might also be the case that the element has to use the {@link ILabel}s of
     * another {@link ILabeledElement} to obtain it's own current label. For example,
     * {@link IAttributeValue} objects do not have {@link ILabel}s themselves but their current
     * label is obtained from the {@link ILabel}s of the {@link IAttribute} they are based upon.
     * <p>
     * This operation never returns <tt>null</tt>.
     * 
     * @see #getCurrentPluralLabel()
     */
    public String getCurrentLabel();

    /**
     * Returns the element's current plural label. See {@link #getCurrentLabel()} for more
     * information.
     * <p>
     * This operation never returns <tt>null</tt>.
     * 
     * @see #getCurrentLabel()
     */
    public String getCurrentPluralLabel();

}
