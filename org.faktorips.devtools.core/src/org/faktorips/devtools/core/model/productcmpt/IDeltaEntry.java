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

package org.faktorips.devtools.core.model.productcmpt;

/**
 * A delta entry describes a single difference between a product component generation and the type
 * it is based on. For example the type might contain a new attribute but the product component has
 * not matching attribute value.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaEntry {

    /**
     * Returns the entry's type.
     */
    public DeltaType getDeltaType();

    /**
     * Returns a detailed description, especially for mismatches.
     */
    public String getDescription();

    /**
     * Fixes the difference between the type and the product component.
     * <p>
     * For example if the type contains a new attribute but the product component generation. has
     * not matching attribute value, this method creates the attribute value.
     */
    public void fix();

}
