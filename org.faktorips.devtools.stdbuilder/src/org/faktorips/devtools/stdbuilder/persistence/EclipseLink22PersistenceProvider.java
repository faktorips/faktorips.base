/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;

public class EclipseLink22PersistenceProvider extends EclipseLink1PersistenceProvider {

    public static final String ID_ECLIPSE_LINK_2_2 = "EclipseLink 2.2"; //$NON-NLS-1$

    private static final String ANNOTATION_INDEX = "org.eclipse.persistence.annotations.Index";

    private static final String NAME_ATTRIBUTE = "name=\"";

    @Override
    public boolean isSupportingIndex() {
        return true;
    }

    @Override
    public JavaCodeFragment getIndexAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (persistentAttributeInfo.isIndexNameDefined()) {
            String indexName = persistentAttributeInfo.getIndexName();
            builder.annotationLn(ANNOTATION_INDEX, NAME_ATTRIBUTE + indexName + "\"");
        }
        return builder.getFragment();
    }

}
