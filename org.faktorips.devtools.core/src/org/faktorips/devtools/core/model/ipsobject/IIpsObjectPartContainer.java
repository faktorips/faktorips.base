/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.util.memento.MementoSupport;

/**
 * A container for {@link IIpsObjectPart}s.
 * 
 * @author Thorsten Guenther
 * @author Alexander Weickmann
 */
public interface IIpsObjectPartContainer extends IIpsElement, IExtensionPropertyAccess, Validatable, XmlSupport,
        MementoSupport, Described {

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "IPSOBJECTPARTCONTAINER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of descriptions this container has does
     * not correspond to the number of languages supported by the IPS project.
     */
    public final static String MSGCODE_INVALID_DESCRIPTION_COUNT = MSGCODE_PREFIX + "InvalidDescriptionCount"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of labels this container has does not
     * correspond to the number of languages supported by the IPS project.
     */
    public final static String MSGCODE_INVALID_LABEL_COUNT = MSGCODE_PREFIX + "InvalidLabelCount"; //$NON-NLS-1$

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
     * Returns the caption of this <tt>IIpsObjectPartContainer</tt> for the given {@link Locale}. A
     * caption is a string that describes the object itself.
     * <p>
     * Returns <tt>null</tt> if no caption for the given locale exists.
     * 
     * @param locale The locale to request the caption for.
     * 
     * @throws CoreException If any error occurs while retrieving the caption.
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public String getCaption(Locale locale) throws CoreException;

    /**
     * Returns the plural caption of this <tt>IIpsObjectPartContainer</tt> for the given
     * {@link Locale}. A caption is a string that describes the object itself.
     * <p>
     * Returns <tt>null</tt> if no plural caption for the given locale exists.
     * 
     * @param locale The locale to request the plural caption for.
     * 
     * @throws CoreException If any error occurs while retrieving the caption.
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public String getPluralCaption(Locale locale) throws CoreException;

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
