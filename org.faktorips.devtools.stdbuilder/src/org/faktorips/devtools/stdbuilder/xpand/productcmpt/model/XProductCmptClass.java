/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.ProductComponent;

public class XProductCmptClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = false;

    public XProductCmptClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
    }

    @Override
    public boolean isChangeOverTimeClass() {
        return CHANGE_OVER_TIME;
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponent.class);
    }

    @Override
    public Set<XProductCmptClass> getClassHierarchy() {
        return super.getClassHierarchy(XProductCmptClass.class);
    }

    public XProductCmptGenerationClass getProductCmptGenerationNode() {
        return getModelNode(getType(), XProductCmptGenerationClass.class);
    }

    public String getMethodNameGetProductCmpt() {
        return getJavaNamingConvention().getGetterMethodName(getName());
    }

    public String getMethodNameSetProductCmpt() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        LinkedHashSet<String> extendedInterfaces = super.getExtendedInterfaces();
        if (!hasSupertype()) {
            extendedInterfaces.add(addImport(IProductComponent.class));
        }
        return extendedInterfaces;
    }

    /**
     * Returns whether access methods for generations should be generated.
     * <p>
     * This is the case if the <em>changing over time</em> flag of the product component type is
     * set.
     */
    public boolean isGenerateGenerationAccessMethods() {
        return isChangingOverTime();
    }

    /**
     * Returns whether the access method to retrieve the changing over time flag should be
     * generated.
     * <p>
     * This is the case if the product component type does not have a super type.
     */
    public boolean isGenerateIsChangingOverTimeAccessMethod() {
        return !hasSupertype();
    }

    /**
     * Returns whether the product component type is changing over time.
     * <p>
     * Not to be confused with {@link #isChangeOverTimeClass()}.
     */
    public boolean isChangingOverTime() {
        return getType().isChangingOverTime();
    }

}
