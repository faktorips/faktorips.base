/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.util.PersistenceSupportNames;

/**
 * Persistence provider for standard generic JPA 2.1 support
 */
public class GenericJPA2_1PersistenceProvider extends GenericJPA2PersistenceProvider {

    /**
     * @deprecated Use {@link PersistenceSupportNames#ID_GENERIC_JPA_2_1} instead.
     */
    @Deprecated(forRemoval = true, since = "21.12")
    public static final String ID_GENERIC_JPA_2_1 = PersistenceSupportNames.ID_GENERIC_JPA_2_1;

    @Override
    public boolean isSupportingConverters() {
        return true;
    }

    @Override
    public JavaCodeFragment getConverterAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder();
        fragmentBuilder.annotationClassValueLn(getPackagePrefix() + PersistenceAnnotation.Convert.toString(),
                "converter",
                persistentAttributeInfo.getConverterQualifiedClassName());

        return fragmentBuilder.getFragment();
    }

    @Override
    public boolean isSupportingIndex() {
        // of course, JPA 2.1 supports indices, but not with a simple annotation on the attribute as
        // Eclipselink does. As this feature is only useable for simple indices and complex indices
        // must be configured by hand anyways we choose to currently not support this feature, as it
        // would need unproportionally large refactorings
        return false;
    }

}
