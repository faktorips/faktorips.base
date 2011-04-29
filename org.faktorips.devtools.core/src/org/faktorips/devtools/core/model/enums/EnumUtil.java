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

package org.faktorips.devtools.core.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides utility methods concerning enumerations.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class EnumUtil {

    /**
     * Returns whether the given <tt>IEnumAttribute</tt> is unique. In contrast to the method
     * <tt>findIsUnique(IIpsProject)</tt> from <tt>IEnumAttribute</tt> this operation never returns
     * <tt>null</tt>. If the <tt>IEnumAttribute</tt> is inherited and the super type attribute
     * cannot be found, <tt>false</tt> instead of <tt>null</tt> will be returned.
     * 
     * @see org.faktorips.devtools.core.model.enums.IEnumAttribute#findIsUnique(IIpsProject)
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to obtain the information for.
     * @param ipsProject The IPS project that is used to the search the super type hierarchy.
     * 
     * @throws CoreException If an error occurs while searching for the super enumeration if the
     *             given <tt>IEnumAttribute</tt> is inherited.
     * @throws NullPointerException If <tt>enumAttribute</tt> or <tt>ipsProject</tt> is
     *             <tt>null</tt>.
     */
    public static boolean findEnumAttributeIsUnique(IEnumAttribute enumAttribute, IIpsProject ipsProject)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { enumAttribute, ipsProject });
        Boolean value = enumAttribute.findIsUnique(ipsProject);
        return (value == null) ? false : value.booleanValue();
    }

    /**
     * Returns whether the given <tt>IEnumAttribute</tt> is the (default) identifier. In contrast to
     * the method <tt>findIsIdentifier(IIpsProject)</tt> from <tt>IEnumAttribute</tt> this operation
     * never returns <tt>null</tt>. If the <tt>IEnumAttribute</tt> is inherited and the super type
     * attribute cannot be found, <tt>false</tt> instead of <tt>null</tt> will be returned.
     * 
     * @see org.faktorips.devtools.core.model.enums.IEnumAttribute#findIsIdentifier(IIpsProject)
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to obtain the information for.
     * @param ipsProject The IPS project that is used to the search the super type hierarchy.
     * 
     * @throws CoreException If an error occurs while searching for the super enumeration if the
     *             given <tt>IEnumAttribute</tt> is inherited.
     * @throws NullPointerException If <tt>enumAttribute</tt> or <tt>ipsProject</tt> is
     *             <tt>null</tt>.
     */
    public static boolean findEnumAttributeIsIdentifier(IEnumAttribute enumAttribute, IIpsProject ipsProject)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { enumAttribute, ipsProject });
        Boolean value = enumAttribute.findIsIdentifier(ipsProject);
        return (value == null) ? false : value.booleanValue();
    }

    /**
     * Returns whether the given <tt>IEnumAttribute</tt> is used as name in the Faktor-IPS UI. In
     * contrast to the method <tt>findIsUsedAsNameInFaktorIpsUi(IIpsProject)</tt> from
     * <tt>IEnumAttribute</tt> this operation never returns <tt>null</tt>. If the
     * <tt>IEnumAttribute</tt> is inherited and the super type attribute cannot be found,
     * <tt>false</tt> instead of <tt>null</tt> will be returned.
     * 
     * @see org.faktorips.devtools.core.model.enums.IEnumAttribute#findIsUsedAsNameInFaktorIpsUi(IIpsProject)
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to obtain the information for.
     * @param ipsProject The IPS project that is used to the search the super type hierarchy.
     * 
     * @throws CoreException If an error occurs while searching for the super enumeration if the
     *             given <tt>IEnumAttribute</tt> is inherited.
     * @throws NullPointerException If <tt>enumAttribute</tt> or <tt>ipsProject</tt> is
     *             <tt>null</tt>.
     */
    public static boolean findEnumAttributeIsUsedAsNameInFaktorIpsUi(IEnumAttribute enumAttribute,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { enumAttribute, ipsProject });
        Boolean value = enumAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject);
        return (value == null) ? false : value.booleanValue();
    }

}
