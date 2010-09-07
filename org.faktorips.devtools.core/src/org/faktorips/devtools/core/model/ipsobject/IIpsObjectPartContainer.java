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

package org.faktorips.devtools.core.model.ipsobject;

import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.util.memento.MementoSupport;

/**
 * A container for {@link IIpsObjectPart}s that may have {@link IDescription}s and {@link ILabel}s
 * attached to it.
 * <p>
 * Whether or not descriptions and / or labels are supported by this
 * <tt>IIpsObjectPartContainer</tt> can be queried trough the operations
 * {@link #hasDescriptionSupport()} and {@link #hasLabelSupport()} respectively.
 * 
 * @author Thorsten Guenther
 * @author Alexander Weickmann
 */
public interface IIpsObjectPartContainer extends IIpsElement, IExtensionPropertyAccess, Validatable, XmlSupport,
        MementoSupport, Described, IDescribedElement, ILabeledElement {

    /**
     * Returns the IPS object this part belongs to if this <tt>IIpsObjectPartContainer</tt> is a
     * part, or the IPS object itself, if this <tt>IIpsObjectPartContainer</tt> is the IPS object.
     */
    public IIpsObject getIpsObject();

    /**
     * Returns the IPS source file this <tt>IIpsObjectPartContainer</tt> belongs to.
     */
    public IIpsSrcFile getIpsSrcFile();

    /**
     * Returns whether this <tt>IIpsObjectPartContainer</tt> does support descriptions. This
     * operation is called by the constructor so it's crucial that the return value does not depend
     * on any instance state.
     */
    public boolean hasDescriptionSupport();

    /**
     * Returns whether this <tt>IIpsObjectPartContainer</tt> does support labels. This operation is
     * called by the constructor so it's crucial that the return value does not depend on any
     * instance state.
     */
    public boolean hasLabelSupport();

    /**
     * @throws UnsupportedOperationException If this <tt>IIpsObjectPartContainer</tt> does not
     *             support labels.
     * 
     * @see #hasLabelSupport()
     */
    @Override
    public ILabel newLabel();

    /**
     * @throws UnsupportedOperationException If this <tt>IIpsObjectPartContainer</tt> does not
     *             support labels.
     * 
     * @see #hasLabelSupport()
     */
    @Override
    public ILabel getLabel(Locale locale);

    /**
     * @throws UnsupportedOperationException If this <tt>IIpsObjectPartContainer</tt> does not
     *             support labels.
     * 
     * @see #hasLabelSupport()
     */
    @Override
    public List<ILabel> getLabels();

    /**
     * @throws UnsupportedOperationException If this <tt>IIpsObjectPartContainer</tt> does not
     *             support descriptions.
     * 
     * @see #hasDescriptionSupport()
     */
    @Override
    public IDescription newDescription();

    /**
     * @throws UnsupportedOperationException If this <tt>IIpsObjectPartContainer</tt> does not
     *             support descriptions.
     * 
     * @see #hasDescriptionSupport()
     */
    @Override
    public IDescription getDescription(Locale locale);

    /**
     * @throws UnsupportedOperationException If this <tt>IIpsObjectPartContainer</tt> does not
     *             support descriptions.
     * 
     * @see #hasDescriptionSupport()
     */
    @Override
    public List<IDescription> getDescriptions();

    /**
     * Returns the caption of this <tt>IIpsObjectPartContainer</tt> for the given {@link Locale}. A
     * caption is a string that describes the object itself.
     * <p>
     * Returns <tt>null</tt> if no caption for the given locale exists.
     * 
     * @param locale The locale to request the caption for.
     * 
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public String getCaption(Locale locale);

    /**
     * Returns the plural caption of this <tt>IIpsObjectPartContainer</tt> for the given
     * {@link Locale}. A caption is a string that describes the object itself.
     * <p>
     * Returns <tt>null</tt> if no plural caption for the given locale exists.
     * 
     * @param locale The locale to request the plural caption for.
     * 
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public String getPluralCaption(Locale locale);

    /**
     * Returns the last resort caption of this <tt>IIpsObjectPartContainer</tt>. The last resort
     * caption is used in case that neither the caption for the requested locale nor a caption for
     * the default locale exists.
     * <p>
     * Never returns <tt>null</tt>.
     */
    public String getLastResortCaption();

    /**
     * Returns the last resort plural caption of this <tt>IIpsObjectPartContainer</tt>. The last
     * resort plural caption is used in case that neither the plural caption for the requested
     * locale nor a plural caption for the default locale exists.
     * <p>
     * Never returns <tt>null</tt>.
     */
    public String getLastResortPluralCaption();

}
