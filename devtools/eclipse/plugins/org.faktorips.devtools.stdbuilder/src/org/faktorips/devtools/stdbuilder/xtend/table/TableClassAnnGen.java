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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.table.XColumn;
import org.faktorips.devtools.stdbuilder.xmodel.table.XTable;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;

/**
 * Generates annotations for the IPS meta model information to the generated implementation class of
 * table structures.
 * 
 * @see AnnotatedJavaElementType#TABLE_CLASS
 */
public class TableClassAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XTable table = (XTable)modelNode;

        String tableStructureType = table.addImport(TableStructureKind.class) + ".";
        if (table.isSingleContentTable()) {
            tableStructureType += TableStructureKind.SINGLE_CONTENT.toString();
        } else {
            tableStructureType += TableStructureKind.MULTIPLE_CONTENTS.toString();
        }

        List<String> columnNames = new ArrayList<>();
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
