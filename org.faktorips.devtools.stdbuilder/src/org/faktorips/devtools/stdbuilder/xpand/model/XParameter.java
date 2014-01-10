/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;

public class XParameter extends AbstractGeneratorModelNode {

    public XParameter(IParameter parameter, GeneratorModelContext context, ModelService modelService) {
        super(parameter, context, modelService);
    }

    @Override
    public IParameter getIpsObjectPartContainer() {
        return (IParameter)super.getIpsObjectPartContainer();
    }

    protected IParameter getParameter() {
        return getIpsObjectPartContainer();
    }

    /**
     * Returns the java class name for this parameter
     */
    public String getJavaClassName() {
        XMethod xMethod = getModelNode(getParameter().getParameterContainer(), XMethod.class);
        return xMethod.getJavaClassName(getDatatype());
    }

    protected Datatype getDatatype() {
        try {
            return getIpsProject().findDatatype(getParameter().getDatatype());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getNullExpression() {
        Datatype paramDataype = getDatatype();
        DatatypeHelper helper = getIpsProject().getDatatypeHelper(paramDataype);
        if (paramDataype.isPrimitive()) {
            return ((ValueDatatype)paramDataype).getDefaultValue();
        } else {
            if (helper != null) {
                JavaCodeFragment nullExpression = helper.nullExpression();
                addImport(nullExpression.getImportDeclaration());
                return nullExpression.getSourcecode();
            } else {
                return "null";
            }
        }
    }
}
