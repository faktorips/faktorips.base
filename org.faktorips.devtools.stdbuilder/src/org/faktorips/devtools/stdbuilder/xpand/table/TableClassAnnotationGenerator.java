/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.table.model.XColumn;
import org.faktorips.devtools.stdbuilder.xpand.table.model.XTable;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.table.TableStructureType;

public class TableClassAnnotationGenerator implements IAnnotationGenerator {

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.TABLE_CLASS;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XTable table = (XTable)modelNode;

        String tableStructureType = table.addImport(TableStructureType.class) + ".";
        if (table.isSingleContentTable()) {
            tableStructureType += TableStructureType.SINGLE_CONTENT.toString();
        } else {
            tableStructureType += TableStructureType.MULTIPLE_CONTENTS.toString();
        }

        List<String> columnNames = new ArrayList<String>();
        for (XColumn column : table.getValidColumns()) {
            columnNames.add("\"" + column.getName() + "\"");
        }
        String colNames = StringUtils.join(columnNames, ",");

        return new JavaCodeFragment("@" + table.addImport(IpsTableStructure.class) + "(name = \""
                + table.getIpsObjectPartContainer().getQualifiedName() + "\", type= " + tableStructureType
                + ", columns = {" + colNames + "})");
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XTable;
    }
}
