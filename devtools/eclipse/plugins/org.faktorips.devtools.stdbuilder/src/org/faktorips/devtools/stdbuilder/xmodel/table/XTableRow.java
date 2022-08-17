/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.xmodel.MethodParameter;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.table.TableRowBuilderClassNameProvider;

public class XTableRow extends XTableStructure {

    private TableRowBuilderClassNameProvider nameProvider;

    public XTableRow(ITableStructure row, GeneratorModelContext context, ModelService modelService) {
        super(row, context, modelService);
        nameProvider = new TableRowBuilderClassNameProvider(
                context.getGeneratorConfig(row.getIpsObject()).isGeneratePublishedInterfaces(row.getIpsProject()));
    }

    @Override
    protected String getBaseSuperclassName() {
        return "";
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        return new LinkedHashSet<>();
    }

    @Override
    public String getName() {
        return super.getName() + "Row";
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return nameProvider;
    }

    /**
     * @return joined string list of the null expressions of the columns, separated with ","
     */
    public String getNullExpressions() {
        List<String> nulls = new ArrayList<>();
        for (XColumn column : getValidColumns()) {
            if (column.getNullExpression() != null) {
                nulls.add(column.getNullExpression());
            }
        }
        return StringUtils.join(nulls, ",");
    }

    public List<MethodParameter> getConstructorParameters() {
        List<MethodParameter> params = new ArrayList<>();
        for (XColumn column : getValidColumns()) {
            params.add(new MethodParameter(column.getDatatypeName(), column.getAttributeName()));
        }
        return params;
    }

    /**
     * @return attribute names separated with "|"
     */
    public String getAttributeNames() {
        List<String> atts = new ArrayList<>();
        for (XColumn column : getValidColumns()) {
            atts.add(column.getAttributeName());
        }
        if (atts.size() > 0) {
            return "\"\" +" + StringUtils.join(atts, " + \"|\" + ");
        } else {
            return "\"\"";
        }

    }
}
