/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

/**
 * A collection of utility methods for Strings. We don't use a class library like apache-commons
 * here to minimize the dependencies for the generated code.
 * 
 * @deprecated This class is deprecated due to import collisions with the StringUtils.class of the
 *             apache.common.lang package. Use {@link IpsStringUtils} instead. It provides the same
 *             functionality.
 */
@Deprecated
public class StringUtils {

    /**
     * @deprecated This method is deprecated due to import collisions with the StringUtils.class of
     *             the apache.common.lang package. Use the <code>EMPTY</code> constant of
     *             {@link IpsStringUtils} instead. It provides the same content.
     */
    @Deprecated
    public static final String EMPTY = "";

    /**
     * @deprecated This class is deprecated due to import collisions with the StringUtils.class of
     *             the apache.common.lang package. Use {@link IpsStringUtils} instead.
     */
    @Deprecated
    private StringUtils() {
        // Utility class not to be instantiated.
    }

    /**
     * @deprecated This method is deprecated due to import collisions with the StringUtils.class of
     *             the apache.common.lang package. Use the <code>isEmpty(String s)</code> method of
     *             {@link IpsStringUtils} instead. It has the same functionality.
     */
    @Deprecated
    public static final boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * @deprecated This method is deprecated due to import collisions with the StringUtils.class of
     *             the apache.common.lang package. Use the <code>isBlank(String s)</code> method of
     *             {@link IpsStringUtils} instead. It has the same functionality.
     */
    @Deprecated
    public static final boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
