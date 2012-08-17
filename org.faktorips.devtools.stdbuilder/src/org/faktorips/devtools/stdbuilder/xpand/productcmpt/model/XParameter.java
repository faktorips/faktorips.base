/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;

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
        return getJavaClassName(getDatatype(), isGeneratingPublishedInterfaces());
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
