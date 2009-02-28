/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.enumtype;

import java.util.List;

import org.faktorips.util.ArgumentCheck;

/**
 * Utility methods for the enum type model of Faktor-IPS.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeUtil {

    /**
     * Checks whether the given enum attributes are equal in terms of their:
     * 
     * <ul>
     * <li>Name</li>
     * <li>Datatype</li>
     * <li>Identifier-Flag</li>
     * </ul>
     * 
     * @param enumAttribute1 The first enum attribute to compare.
     * @param enumAttribute2 The second enum attribute to compare.
     * 
     * @throws NullPointerException If <code>enumAttribute1</code> or <code>enumAttribute2</code> is
     *             <code>null</code>.
     */
    public static boolean equalEnumAttributes(IEnumAttribute enumAttribute1, IEnumAttribute enumAttribute2) {
        ArgumentCheck.notNull(new Object[] { enumAttribute1, enumAttribute2 });

        return enumAttribute1.getName().equals(enumAttribute2.getName())
                && enumAttribute1.getDatatype().equals(enumAttribute2.getDatatype())
                && enumAttribute1.isIdentifier() == enumAttribute2.isIdentifier();
    }

    /**
     * Checks whether the given enum attribute is contained in the given list in means of an equal
     * enum attribute.
     * 
     * @see #equalEnumAttributes(IEnumAttribute, IEnumAttribute)
     * 
     * @param listOfEnumAttributes The list of enum attributes that may or may not contain the
     *            specified enum attribute.
     * @param enumAttribute The enum attribute to search the list for an equal representative.
     * 
     * @throws NullPointerException If <code>listOfEnumAttributes</code> or
     *             <code>enumAttribute</code> is <code>null</code>.
     */
    public static boolean containsEqualEnumAttribute(List<IEnumAttribute> listOfEnumAttributes,
            IEnumAttribute enumAttribute) {

        ArgumentCheck.notNull(new Object[] { listOfEnumAttributes, enumAttribute });

        for (IEnumAttribute currentEnumAttribute : listOfEnumAttributes) {
            if (equalEnumAttributes(currentEnumAttribute, enumAttribute)) {
                return true;
            }
        }

        return false;
    }

}
