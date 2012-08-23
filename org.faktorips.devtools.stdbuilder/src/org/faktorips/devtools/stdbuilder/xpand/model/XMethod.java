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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;

public class XMethod extends AbstractGeneratorModelNode {

    private Set<XParameter> methodParameters;

    public XMethod(IMethod method, GeneratorModelContext context, ModelService modelService) {
        super(method, context, modelService);
    }

    @Override
    public IMethod getIpsObjectPartContainer() {
        return (IMethod)super.getIpsObjectPartContainer();
    }

    public IMethod getMethod() {
        return getIpsObjectPartContainer();
    }

    public String getModifier() {
        return Modifier.toString(getMethod().getJavaModifier() | (isAbstract() ? Modifier.ABSTRACT : 0));
    }

    public String getMethodName() {
        return getMethod().getName();
    }

    public String getJavaClassName() {
        Datatype datatype = getDatatype();
        return getJavaClassName(datatype);
    }

    public String getNotPrimitiveJavaClassName() {
        Datatype datatype = getDatatype();
        if (datatype instanceof ValueDatatype && datatype.isPrimitive()) {
            datatype = ((ValueDatatype)datatype).getWrapperType();
        }
        return getJavaClassName(datatype);
    }

    protected String getJavaClassName(Datatype datatype) {
        boolean resolveTypesToPublishedInterface = (getMethod().getModifier().isPublished() || getMethod() instanceof IPolicyCmptTypeMethod)
                && isGeneratingPublishedInterfaces();
        boolean useGeneration = (getMethod() instanceof IProductCmptTypeMethod)
                && ((IProductCmptTypeMethod)getMethod()).isChangingOverTime();
        return getJavaClassName(datatype, useGeneration, resolveTypesToPublishedInterface);
    }

    public Set<XParameter> getParameters() {
        checkForUpdate();
        if (methodParameters == null) {
            synchronized (this) {
                if (methodParameters == null) {
                    methodParameters = initNodesForParts(Arrays.asList(getMethod().getParameters()), XParameter.class);
                }
            }
        }
        return new CopyOnWriteArraySet<XParameter>(methodParameters);
    }

    public List<MethodParameter> getMethodParameters() {
        List<MethodParameter> result = new ArrayList<MethodParameter>();
        for (XParameter param : getParameters()) {
            result.add(new MethodParameter(param.getJavaClassName(), param.getName()));
        }
        return result;
    }

    public Datatype getDatatype() {
        try {
            return getMethod().findDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getDefaultReturnValue() {
        Datatype datatype = getDatatype();
        if (isReturnVoid()) {
            throw new RuntimeException("Cannot give default return value for void in method " + getName());
        } else if (datatype.isValueDatatype()) {
            String defaultValue = ((ValueDatatype)datatype).getDefaultValue();
            // getDefaultValue returns null if the default value should be "null"
            if (defaultValue == null) {
                return "null";
            } else {
                return defaultValue;
            }
        } else {
            return "null";
        }
    }

    public boolean isOverrides() {
        try {
            return getMethod().findOverriddenMethod(getIpsProject()) != null;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isPublished() {
        return getMethod().getModifier().isPublished();
    }

    public boolean isAbstract() {
        return getMethod().isAbstract();
    }

    public boolean isReturnVoid() {
        return getDatatype().isVoid();
    }

    public boolean isFormulaSignature() {
        if (getMethod() instanceof IProductCmptTypeMethod) {
            return ((IProductCmptTypeMethod)getMethod()).isFormulaSignatureDefinition();
        } else {
            return false;
        }
    }

    public boolean isOverloadsFormula() {
        if (getMethod() instanceof IProductCmptTypeMethod) {
            return ((IProductCmptTypeMethod)getMethod()).isOverloadsFormula();
        } else {
            return false;
        }
    }

    public XMethod getOverloadedFormulaMethod() {
        if (getMethod() instanceof IProductCmptTypeMethod) {
            try {
                IProductCmptTypeMethod overloadedFormulaMethod = ((IProductCmptTypeMethod)getMethod())
                        .findOverloadedFormulaMethod(getIpsProject());
                if (overloadedFormulaMethod == null) {
                    throw new CoreRuntimeException("Cannot find overloaded formula for method " + getName());
                }
                XMethod overloadedMethod = getModelNode(overloadedFormulaMethod, XMethod.class);
                return overloadedMethod;
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        } else {
            throw new RuntimeException("The method " + getName() + " is no formula signature.");
        }
    }

}
