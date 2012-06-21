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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;

public class XFormulaSignature extends AbstractGeneratorModelNode {

    private final List<XFormulaParameter> parameters;

    public XFormulaSignature(IProductCmptTypeMethod method, GeneratorModelContext context, ModelService modelService) {
        super(method, context, modelService);
        parameters = initNodesForParts(Arrays.asList(method.getParameters()), XFormulaParameter.class);
    }

    @Override
    public IProductCmptTypeMethod getIpsObjectPartContainer() {
        return (IProductCmptTypeMethod)super.getIpsObjectPartContainer();
    }

    public IProductCmptTypeMethod getMethod() {
        return getIpsObjectPartContainer();
    }

    public String getMethodName() {
        return getMethod().getName();
    }

    public List<XFormulaParameter> getParameters() {
        return new CopyOnWriteArrayList<XFormulaParameter>(parameters);
    }

}
