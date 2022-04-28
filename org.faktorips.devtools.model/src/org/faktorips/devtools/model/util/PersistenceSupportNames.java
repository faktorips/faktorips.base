/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.List;

/**
 * Provides all IDs required for configuring the persistence support using the standard builder.
 * 
 * @since 21.6
 * @author Florian Orendi
 */
public class PersistenceSupportNames {

    /**
     * The persistence support ID used by the standard builder.
     */
    public static final String STD_BUILDER_PROPERTY_PERSISTENCE_PROVIDER = "persistenceProvider"; //$NON-NLS-1$

    /**
     * The ID if no persistence technology has been selected.
     */
    public static final String ID_NONE = "None"; //$NON-NLS-1$

    /**
     * The ID for using Eclipse Link 1.1 for persistence support.
     */
    public static final String ID_ECLIPSE_LINK_1_1 = "EclipseLink 1.1"; //$NON-NLS-1$

    /**
     * The ID for using Eclipse Link 2.5 for persistence support.
     */
    public static final String ID_ECLIPSE_LINK_2_5 = "EclipseLink 2.5"; //$NON-NLS-1$

    /**
     * The ID for using Eclipse Link 3.0 for persistence support.
     */
    public static final String ID_ECLIPSE_LINK_3_0 = "EclipseLink 3.0"; //$NON-NLS-1$

    /**
     * The ID for using Generic JPA 2.0 for persistence support.
     */
    public static final String ID_GENERIC_JPA_2 = "Generic JPA 2.0"; //$NON-NLS-1$

    /**
     * The ID for using Generic JPA 2.1 for persistence support.
     */
    public static final String ID_GENERIC_JPA_2_1 = "Generic JPA 2.1"; //$NON-NLS-1$

    /**
     * The ID for using Jakarta Persistence 2.2 for persistence support.
     */
    public static final String ID_JAKARTA_PERSISTENCE_2_2 = "Jakarta Persistence 2.2"; //$NON-NLS-1$

    /**
     * The ID for using Jakarta Persistence 3.0 for persistence support.
     */
    public static final String ID_JAKARTA_PERSISTENCE_3_0 = "Jakarta Persistence 3.0"; //$NON-NLS-1$

    private PersistenceSupportNames() {
        // Utility class
    }

    /**
     * Returns all known persistence provider option IDs including {@value #ID_NONE} as the first
     * option.
     *
     * @since 21.12
     */
    public static final List<String> getAllPersistenceProviderOptions() {
        return List.of(ID_NONE,
                ID_ECLIPSE_LINK_1_1, ID_ECLIPSE_LINK_2_5, ID_ECLIPSE_LINK_3_0,
                ID_GENERIC_JPA_2, ID_GENERIC_JPA_2_1,
                ID_JAKARTA_PERSISTENCE_2_2, ID_JAKARTA_PERSISTENCE_3_0);
    }

}
