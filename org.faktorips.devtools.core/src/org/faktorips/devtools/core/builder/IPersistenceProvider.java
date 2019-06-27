/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypePartInfo;

/**
 * Allows to specify several vendor specific JPA annotations.
 * 
 * @author Joerg Ortmann
 */
public interface IPersistenceProvider {

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
    public JavaCodeFragment getConverterAnnotations(IPersistentAttributeInfo persistentAttributeInfo);

    /**
     * Returns <code>true</code> if the persistent provider supports the @Index annotation
     */
    public boolean isSupportingIndex();

    /**
     * If index annotations are supported, this method returns the generated index annotation.
     */
    public JavaCodeFragment getIndexAnnotations(IPersistentTypePartInfo persistentAttributeInfo);

}
