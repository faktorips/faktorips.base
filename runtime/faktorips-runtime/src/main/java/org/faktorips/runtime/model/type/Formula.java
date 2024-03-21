/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.runtime.internal.FormulaUtil;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsFormula;

/**
 * Represents a formula in a {@link ProductCmptType}.
 */
public class Formula extends TypePart {

    private final IpsFormula annotation;

    private final Method getter;

    public Formula(Type type, Method getter) {
        super(getter.getAnnotation(IpsFormula.class).name(), type,
                getter.getAnnotation(IpsExtensionProperties.class), Deprecation.of(getter));
        annotation = getter.getAnnotation(IpsFormula.class);
        this.getter = getter;
    }

    /**
     * Returns the formula text of this formula in the given product component.
     *
     * @param productComponent a product component based on the product component type this formula
     *            belongs to.
     */
    public String getFormulaText(ProductComponent productComponent) {
        return FormulaUtil.getFormula(productComponent, getFormulaName());
    }

    /**
     * Sets the formula text of this formula in the given product component.
     *
     * @param productComponent a product component based on the product component type this formula
     *            belongs to.
     * @param formulaText the new formula text
     */
    public void setFormulaText(ProductComponent productComponent, String formulaText) {
        FormulaUtil.setFormula(productComponent, getFormulaName(), formulaText);
    }

    /**
     * Returns the name of the formula.
     */
    public String getFormulaName() {
        return annotation.name();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getFormulaName());
        sb.append("(");
        sb.append(Stream.of(getter.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ")));
        sb.append(")");
        sb.append(": ");
        sb.append(getter.getReturnType());
        return sb.toString();
    }
}
