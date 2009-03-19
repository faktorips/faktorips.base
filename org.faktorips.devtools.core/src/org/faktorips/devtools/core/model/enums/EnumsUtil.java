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

package org.faktorips.devtools.core.model.enums;

import java.util.List;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Utility methods for Faktor-IPS enums.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumsUtil {

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

    /**
     * Takes a qualified fragment root name e.g. Project/model and extracts the source folder and
     * the ips project from it.
     * <p>
     * Returns an object array.
     * <ul>
     * <li>0 is the source folder name (might be an empty string).
     * <li>1 is the ips project (might be <code>null</code>).
     * </ul>
     * 
     * @throws NullPointerException If <code>qualifiedFragmentRoot</code> is <code>null</code>.
     */
    public static Object[] splitProjectAndSourceFolder(String fragmentRootQualifiedName) {
        ArgumentCheck.notNull(fragmentRootQualifiedName);

        Object[] array = new Object[2];

        if (fragmentRootQualifiedName.contains("/")) {
            array[0] = fragmentRootQualifiedName.substring(fragmentRootQualifiedName.indexOf('/') + 1);
            String projectName = fragmentRootQualifiedName.substring(0, fragmentRootQualifiedName.indexOf('/'));
            if (!(projectName.equals(""))) {
                IIpsProject foundProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
                if (foundProject.exists()) {
                    array[1] = foundProject;
                }
            }
        } else {
            array[0] = "";
        }

        return array;
    }

}
