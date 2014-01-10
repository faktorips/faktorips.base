/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;

/**
 * Allows to specify several vendor specific JPA annotations.
 * 
 * @author Joerg Ortmann
 */
public interface IPersistenceProvider {

    public static final String PROVIDER_IMPLEMENTATION_ECLIPSE_LINK_1_1 = "EclipseLink 1.1"; //$NON-NLS-1$
    public static final String PROVIDER_IMPLEMENTATION_GENERIC_JPA_2_0 = "Generic JPA 2.0"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if the persistent provider supports the orphan removal (private
     * owned) annotation.
     */
    public boolean isSupportingOrphanRemoval();

    /**
     * If orphan removal is supported then this method must be used to add the necessary annotation
     * to the given java code fragment (e.g. PrivateOwned)
     */
    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment);

    /**
     * If orphan removal is supported then this method must be used to get the attribute to the
     * relationship annotation (e.g. orphanRemoval=true). Returns an empty string ("" or
     * <code>null</code>) if no attribute is necessary.
     */
    public String getRelationshipAnnotationAttributeOrphanRemoval();

    /**
     * Returns <code>true</code> if the persistent provider supports converters.
     */
    public boolean isSupportingConverters();

    /**
     * If converters are supported then this method must be used to add the necessary annotation to
     * the given java code fragment.
     */
    public void addAnnotationConverter(JavaCodeFragment javaCodeFragment,
            IPersistentAttributeInfo persistentAttributeInfo);
}
