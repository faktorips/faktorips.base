/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.table.XTable;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.ITable;
import org.faktorips.util.StringUtil;

public class XTableUsage extends AbstractGeneratorModelNode {

    public XTableUsage(ITableStructureUsage ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    @Override
    public ITableStructureUsage getIpsObjectPartContainer() {
        return (ITableStructureUsage)super.getIpsObjectPartContainer();
    }

    public ITableStructureUsage getTableStructureUsage() {
        return getIpsObjectPartContainer();
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName() + "Name");
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName() + "Name");
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName());
    }

    public String getTableClassName() {
        if (getTableStructureUsage().getTableStructures().length > 1) {
            return addImport(ITable.class) + "<?>";
        } else {
            String tableStructureName = getTableStructureUsage().getTableStructures()[0];
            ITableStructure tableStructure;
            tableStructure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE,
                    tableStructureName);
            XTable xTable = getModelNode(tableStructure, XTable.class);
            return xTable.getSimpleName(BuilderAspect.IMPLEMENTATION);
        }
    }

    public String getConstantNameTable() {
        return "TABLE_" + StringUtil.camelCaseToUnderscore(getName()).toUpperCase();
    }

    public boolean isChangingOverTime() {
        return getTableStructureUsage().isChangingOverTime();
    }
}
