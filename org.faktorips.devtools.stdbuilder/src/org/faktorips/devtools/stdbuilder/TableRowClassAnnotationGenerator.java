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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.table.model.XTableRow;
import org.faktorips.runtime.modeltype.annotation.IpsTableRow;

public class TableRowClassAnnotationGenerator implements IAnnotationGenerator {

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.TABLE_ROW_CLASS;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XTableRow tableRow = (XTableRow)modelNode;
        return new JavaCodeFragment("@" + tableRow.addImport(IpsTableRow.class) + "(tableClass="
                + tableRow.getIpsObjectPartContainer().getName() + ".class)");
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XTableRow;
    }

}
