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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.MethodParameter;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;

public class XMethod extends AbstractGeneratorModelNode {

    private final Set<XParameter> methodParameters;

    public XMethod(IProductCmptTypeMethod method, GeneratorModelContext context, ModelService modelService) {
        super(method, context, modelService);
        methodParameters = initNodesForParts(new LinkedHashSet<IIpsObjectPart>(Arrays.asList(method.getParameters())),
                XParameter.class);
    }

    @Override
    public IProductCmptTypeMethod getIpsObjectPartContainer() {
        return (IProductCmptTypeMethod)super.getIpsObjectPartContainer();
    }

    public IProductCmptTypeMethod getMethod() {
        return getIpsObjectPartContainer();
    }

    public String getModifier() {
        return Modifier.toString(getMethod().getJavaModifier() | (isAbstract() ? Modifier.ABSTRACT : 0));
    }

    private boolean isAbstract() {
        return getMethod().isAbstract();
    }

    public String getMethodName() {
        return getMethod().getName();
    }

    public String getJavaClassName() {
        boolean resolveTypesToPublishedInterface = getMethod().getModifier().isPublished();
        return getJavaClassName(getDatatype(), resolveTypesToPublishedInterface);
    }

    public String getNotPrimitiveJavaClassName() {
        Datatype datatype = getDatatype();
        if (datatype instanceof ValueDatatype && datatype.isPrimitive()) {
            datatype = ((ValueDatatype)datatype).getWrapperType();
        }
        return getJavaClassName(datatype, getMethod().getModifier().isPublished());
    }

    public Set<XParameter> getParameters() {
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

}
