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

public interface IIpsObjectPart extends IIpsObjectPartContainer {

    /**
     * The description is no longer a property of {@IIpsObjectPart} but an
     * {@link IIpsObjectPart} itself.
     * 
     * @deprecated Since version 3.1
     */
    @Deprecated
    public final static String PROPERTY_DESCRIPTION = "description"; //$NON-NLS-1$

    public final static String PROPERTY_ID = "id"; //$NON-NLS-1$

    /**
     * The part's id that uniquely identifies it in it's parent.
     */
    public String getId();

    /**
     * Deletes the part.
     */
    public void delete();

    /**
     * Returns whether the part was deleted (<code>true</code>) or not.
     */
    public boolean isDeleted();

}
