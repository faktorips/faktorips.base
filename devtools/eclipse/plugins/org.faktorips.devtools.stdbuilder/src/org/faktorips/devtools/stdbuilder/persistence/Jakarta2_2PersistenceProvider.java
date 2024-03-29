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

/**
 * Persistence provider for standard generic Jakarta Persistence 2.2 support
 */
public class Jakarta2_2PersistenceProvider extends GenericJPA2PersistenceProvider {

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
        // of course, Jakarta Persistence 2.2 supports indices, but not with a simple annotation on
        // the attribute as
        // Eclipselink does. As this feature is only usable for simple indices and complex indices
        // must be configured by hand anyways we choose to currently not support this feature, as it
        // would need unproportionally large refactorings
        return false;
    }

}
