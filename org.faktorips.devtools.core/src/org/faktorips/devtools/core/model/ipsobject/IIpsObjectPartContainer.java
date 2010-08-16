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

import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.util.memento.MementoSupport;

/**
 * A container for IPS object parts that may have {@link IDescription}s and {@link ILabel}s attached
 * to it.
 * <p>
 * Whether or not descriptions and / or labels are supported by this IPS object part container can
 * be queried trough the operations {@link #hasDescriptionSupport()} and {@link #hasLabelSupport()}
 * respectively.
 * 
 * @author Thorsten Guenther
 * @author Alexander Weickmann
 * 
 * @see ILabel
 * @see ILabeledElement
 * @see IDescription
 * @see IDescribedElement
 */
public interface IIpsObjectPartContainer extends IIpsElement, IExtensionPropertyAccess, Validatable, XmlSupport,
        MementoSupport, Described, IDescribedElement, ILabeledElement {

    /**
     * Returns the IPS object this part belongs to if the container is a part, or the IPS object
     * itself, if this container is the IPS object.
     */
    public IIpsObject getIpsObject();

    /**
     * Returns the IPS source file this container belongs to.
     */
    public IIpsSrcFile getIpsSrcFile();

    /**
     * Returns whether this IPS object part container does support descriptions.
     */
    public boolean hasDescriptionSupport();

    /**
     * Returns whether this IPS object part container does support labels.
     */
    public boolean hasLabelSupport();

    /**
     * @throws UnsupportedOperationException If this IPS object part container does not support
     *             descriptions.
     * 
     * @see #hasDescriptionSupport()
     */
    public IDescription newDescription();

    /**
     * @throws UnsupportedOperationException If this IPS object part container does not support
     *             labels.
     * 
     * @see #hasLabelSupport()
     */
    public ILabel newLabel();

}
