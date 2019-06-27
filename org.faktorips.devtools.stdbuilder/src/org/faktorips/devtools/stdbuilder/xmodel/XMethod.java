/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.method.BaseMethod;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet.FormulaCompiling;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;

public class XMethod extends AbstractGeneratorModelNode {

    public XMethod(IBaseMethod method, GeneratorModelContext context, ModelService modelService) {
        super(method, context, modelService);
    }

    @Override
    public IMethod getIpsObjectPartContainer() {
        IIpsObjectPartContainer ipsObjectPartContainer = super.getIpsObjectPartContainer();
        if (ipsObjectPartContainer instanceof BaseMethod) {
            ipsObjectPartContainer = (IIpsObjectPartContainer)ipsObjectPartContainer.getParent();
        }
        return (IMethod)ipsObjectPartContainer;
    }

    public IMethod getMethod() {
        return getIpsObjectPartContainer();
    }

    /**
     * The returned modifier is the java modifier derived from the method. But if we currently
     * generate an interface we do not want to have the modifier <i>abstract</i> included (removed
     * by XOR '^').
     * 
     * @param generateInterface <code>true</code> if we currently generates an interface,
     *            <code>false</code> if we generate an implementation
     */
    public String getModifier(boolean generateInterface) {
        int javaModifier = getMethod().getJavaModifier();
        if (generateInterface) {
            // Note: There is a ~ that means the compliment of abstract!
            javaModifier &= ~Modifier.ABSTRACT;
        } else if (!isGenerateMethodBody(generateInterface)) {
            javaModifier |= Modifier.ABSTRACT;
        }
        return Modifier.toString(javaModifier);
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
        boolean useInterface = (getMethod().getModifier().isPublished()
                || getMethod() instanceof IPolicyCmptTypeMethod);
        boolean useGeneration = getMethod() instanceof IProductCmptTypeMethod;
        return getJavaClassName(datatype, useGeneration, !useInterface);
    }

    public Set<XParameter> getParameters() {
        if (isCached(XParameter.class)) {
            return getCachedObjects(XParameter.class);
        } else {
            Set<XParameter> nodesForParts = initNodesForParts(Arrays.asList(getMethod().getParameters()),
                    XParameter.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
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

    public boolean isGenerateMethodBody(boolean genInterface) {
        if (genInterface) {
            return false;
        }
        if (isAbstract()) {
            return false;
        }
        if (isFormulaSignature()) {
            if (getGeneratorConfig().getFormulaCompiling() == FormulaCompiling.Subclass) {
                return isFormulaOptional();
            }
        }
        return true;
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
            IProductCmptTypeMethod overloadedFormulaMethod = ((IProductCmptTypeMethod)getMethod())
                    .findOverloadedFormulaMethod(getIpsProject());
            if (overloadedFormulaMethod == null) {
                throw new CoreRuntimeException("Cannot find overloaded formula for method " + getName());
            }
            XMethod overloadedMethod = getModelNode(overloadedFormulaMethod, XMethod.class);
            return overloadedMethod;
        } else {
            throw new RuntimeException("The method " + getName() + " is no formula signature.");
        }
    }

    public boolean isFormulaOptional() {
        if (getMethod() instanceof IProductCmptTypeMethod) {
            return !((IProductCmptTypeMethod)getMethod()).isFormulaMandatory();
        } else {
            return false;
        }
    }

    public String getMethodNameIsFormulaAvailable() {
        return "isFormula" + StringUtils.capitalize(getFormularName()) + "Available";
    }

    public String getFormularName() {
        if (getMethod() instanceof IProductCmptTypeMethod) {
            return ((IProductCmptTypeMethod)getMethod()).getFormulaName();
        } else {
            throw new RuntimeException("The method " + getName() + " is no formula signature.");
        }
    }

    public boolean isChangingOverTime() {
        if (getMethod() instanceof IProductCmptTypeMethod) {
            return ((IProductCmptTypeMethod)getMethod()).isChangingOverTime();
        }
        return true;
    }
}
