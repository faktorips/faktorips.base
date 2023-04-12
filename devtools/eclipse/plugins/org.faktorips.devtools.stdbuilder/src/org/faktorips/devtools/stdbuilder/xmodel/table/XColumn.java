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

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.InternationalStringDatatype;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.util.DatatypeHelperUtil;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;

public class XColumn extends AbstractGeneratorModelNode {

    private int indexInList;

    public XColumn(IColumn column, GeneratorModelContext context, ModelService modelService) {
        super(column, context, modelService);
    }

    private IColumn getColumn() {
        return (IColumn)getIpsObjectPartContainer();
    }

    protected DatatypeHelper getDatatypeHelper() {
        Datatype datatype = getColumn().findValueDatatype(getIpsProject());
        return getIpsProject().getDatatypeHelper(datatype);
    }

    public String getAttributeName() {
        return getJavaNamingConvention().getMemberVarName(getIpsObjectPartContainer().getName());
    }

    public String getDatatypeName() {
        DatatypeHelper datatypeHelper = getDatatypeHelper();
        if (datatypeHelper != null) {
            return addImport(datatypeHelper.getJavaClassName());
        } else {
            return null;
        }
    }

    public int getColumnPosition() {
        return getIndexInList();
    }

    /**
     * from @TableImplBuilder
     * 
     */
    public boolean isExtensible() {
        EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)getDatatypeHelper();
        return enumHelper.getEnumType().isExtensible();
    }

    public boolean isMultilingual() {
        return getDatatypeHelper().getDatatype() instanceof InternationalStringDatatype;
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getAttributeName());
    }

    public String getNullExpression() {
        DatatypeHelper datatypeHelper = getDatatypeHelper();
        if (datatypeHelper != null) {
            return datatypeHelper.nullExpression().getSourcecode();
        } else {
            return null;
        }
    }

    public int getIndexInList() {
        return indexInList;
    }

    public void setIndexInList(int indexInList) {
        this.indexInList = indexInList;
    }

    public String getNewInstanceFromExpression(String expression, String repositoryExpression) {
        JavaCodeFragment newInstanceFromExpression = DatatypeHelperUtil.getNewInstanceFromExpression(
                getDatatypeHelper(), expression, repositoryExpression);
        addImport(newInstanceFromExpression.getImportDeclaration());
        return newInstanceFromExpression.getSourcecode();
    }

    public String getToStringExpression(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

}
