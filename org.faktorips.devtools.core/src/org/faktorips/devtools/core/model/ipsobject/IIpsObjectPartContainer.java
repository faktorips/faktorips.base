/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
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
        MementoSupport {

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "IPSOBJECTPARTCONTAINER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of descriptions this container has does
     * not correspond to the number of languages supported by the IPS project.
     */
    public static final String MSGCODE_INVALID_DESCRIPTION_COUNT = MSGCODE_PREFIX + "InvalidDescriptionCount"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of labels this container has does not
     * correspond to the number of languages supported by the IPS project.
     */
    public static final String MSGCODE_INVALID_LABEL_COUNT = MSGCODE_PREFIX + "InvalidLabelCount"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the entered version does not correspond to the valid
     * version format
     */
    public static final String MSGCODE_INVALID_VERSION_FORMAT = "InvalidVersionFormat"; //$NON-NLS-1$

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

    /**
     * Copies the properties of the given source container to this container.
     * 
     * @param source The source container from where the properties of this container shall be
     *            copied from
     * 
     * @throws IllegalArgumentException If the class of the source container is not the same as the
     *             class of this container
     */
    public void copyFrom(IIpsObjectPartContainer source);

}
