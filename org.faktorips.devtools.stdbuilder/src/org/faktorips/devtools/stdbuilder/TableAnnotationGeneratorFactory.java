/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class TableAnnotationGeneratorFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        if (type == AnnotatedJavaElementType.TABLE_CLASS) {
            return new TableClassAnnotationGenerator();
        } else if (type == AnnotatedJavaElementType.TABLE_ROW_CLASS) {
            return new TableRowClassAnnotationGenerator();
        } else if (type == AnnotatedJavaElementType.TABLE_ROW_CLASS_COLUMN_GETTER) {
            return new TableRowClassColumnGetterAnnotationGenerator();
        } else {
            return null;
        }
    }
}
