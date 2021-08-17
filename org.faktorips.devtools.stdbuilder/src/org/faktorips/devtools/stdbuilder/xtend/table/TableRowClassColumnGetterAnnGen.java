/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.table;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.table.XColumn;
import org.faktorips.runtime.model.annotation.IpsTableColumn;

/**
 * Generates annotations for the IPS meta model information to the generated getters in table rows.
 * 
 * @see AnnotatedJavaElementType#TABLE_ROW_CLASS_COLUMN_GETTER
 */
public class TableRowClassColumnGetterAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XColumn column = (XColumn)modelNode;
        return new JavaCodeFragment("@" + column.addImport(IpsTableColumn.class) + "(name=" + "\"" + column.getName()
                + "\")");
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XColumn;
    }

}
