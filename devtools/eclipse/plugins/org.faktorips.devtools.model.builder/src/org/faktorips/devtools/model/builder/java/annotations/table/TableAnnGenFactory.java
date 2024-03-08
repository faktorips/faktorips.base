/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.table;

import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGeneratorFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class TableAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        if (type == AnnotatedJavaElementType.TABLE_CLASS) {
            return new TableClassAnnGen();
        } else if (type == AnnotatedJavaElementType.TABLE_ROW_CLASS_COLUMN_GETTER) {
            return new TableRowClassColumnGetterAnnGen();
        } else {
            return null;
        }
    }
}
