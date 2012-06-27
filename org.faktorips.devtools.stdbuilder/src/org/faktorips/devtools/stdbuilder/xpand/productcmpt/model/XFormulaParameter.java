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
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

public class XFormulaParameter extends AbstractGeneratorModelNode {

    public XFormulaParameter(IParameter parameter, GeneratorModelContext context, ModelService modelService) {
        super(parameter, context, modelService);
    }

    @Override
    public IParameter getIpsObjectPartContainer() {
        return (IParameter)super.getIpsObjectPartContainer();
    }

    protected IParameter getParameter() {
        return getIpsObjectPartContainer();
    }

    @Override
    public String getName() {
        return getParameter().getName();
    }

    /**
     * Three different cases:
     * <ul>
     * <li>If the datatype is void "void" is returned without adding an import.</li>
     * <li>If the datatype is a {@link ValueDatatype} the value datatype's class name is returned
     * and an import is added. This method throws a {@link RuntimeException} though, in case no
     * datatype helper could be found.</li>
     * <li>If the datatype is a {@link ProductCmptType} or {@link PolicyCmptType} (see
     * StdBuilderHelper#transformDatatypeToJavaClassName() for further info) the corresponding
     * (generated) java class name is returned and an import is added.</li>
     * </ul>
     * 
     * @return the java class name for this parameter.
     * @see StdBuilderHelper#transformDatatypeToJavaClassName(String, boolean,
     *      org.faktorips.devtools.stdbuilder.StandardBuilderSet,
     *      org.faktorips.devtools.core.model.ipsproject.IIpsProject)
     */
    public String getTypeClassName() {
        Datatype datatype = getDatatype();
        if (datatype.isVoid()) {
            return "void";
        }
        if (datatype instanceof ValueDatatype) {
            String datatypeClassName = getClassNameForDatatypeOrThrowException(datatype);
            return addImport(datatypeClassName);
        }
        if (datatype instanceof PolicyCmptType || datatype instanceof ProductCmptType) {
            Type type = (Type)datatype;
            XClass xClass = getModelNode(type, XClass.class);
            String nameOfGeneratedClass = xClass.getQualifiedName(getBuilderAspectDependingOnSettings());
            return addImport(nameOfGeneratedClass);
        }
        throw new RuntimeException("Can't get Java class name for datatype " + datatype.getQualifiedName());
    }

    protected String getClassNameForDatatypeOrThrowException(Datatype datatype) {
        try {
            DatatypeHelper helper = getIpsProject().findDatatypeHelper(datatype.getQualifiedName());
            if (helper != null) {
                return helper.getJavaClassName();
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        throw new RuntimeException("Can't get datatype helper for datatype " + datatype.getQualifiedName());
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
                return helper.nullExpression().toString();
            } else {
                return "null";
            }
        }
    }
}
