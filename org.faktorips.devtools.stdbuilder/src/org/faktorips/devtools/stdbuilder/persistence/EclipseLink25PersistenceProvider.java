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
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypePartInfo;
import org.faktorips.devtools.model.util.PersistenceSupportNames;

public class EclipseLink25PersistenceProvider extends EclipseLink1PersistenceProvider {

    public static final String ID_ECLIPSE_LINK_2_5 = PersistenceSupportNames.ID_ECLIPSE_LINK_2_5;

    private static final String ANNOTATION_INDEX = "org.eclipse.persistence.annotations.Index"; //$NON-NLS-1$

    private static final String NAME_ATTRIBUTE = "name=\""; //$NON-NLS-1$

    @Override
    public boolean isSupportingIndex() {
        return true;
    }

    @Override
    public JavaCodeFragment getIndexAnnotations(IPersistentTypePartInfo persistentAttributeInfo) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (persistentAttributeInfo.isIndexNameDefined()) {
            String indexName = persistentAttributeInfo.getIndexName();
            builder.annotationLn(ANNOTATION_INDEX, NAME_ATTRIBUTE + indexName + "\"");
        }
        return builder.getFragment();
    }

}
