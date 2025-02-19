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
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.FormulaUtil;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsFormula;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Represents a formula in a {@link ProductCmptType}.
 *
 * @since 24.7
 */
public class Formula extends TypePart {

    public static final String MSGCODE_REQUIRED_FORMULA_IS_EMPTY = "MSGCODE_REQUIRED_FORMULA_IS_EMPTY";
    public static final String MSGKEY_REQUIRED_FORMULA_IS_EMPTY = "Formula.RequiredFormulaIsEmpty";

    public static final String PROPERTY_REQUIRED_FORMULA = "requiredFormula";

    private final IpsFormula annotation;

    private final Method getter;

    private final boolean changingOverTime;

    public Formula(Type type, Method getter, boolean changingOverTime) {
        super(getter.getAnnotation(IpsFormula.class).name(), type,
                getter.getAnnotation(IpsExtensionProperties.class), Deprecation.of(getter));
        annotation = getter.getAnnotation(IpsFormula.class);
        this.getter = getter;
        this.changingOverTime = changingOverTime;
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    /**
     * Returns the formula text of this formula in the given product component.
     *
     * @param productComponent a product component based on the product component type this formula
     *            belongs to.
     * @param effectiveDate (optional) the date to use for selecting the product component's
     *            generation, if this formula {@link #isChangingOverTime()}
     */
    public String getFormulaText(IProductComponent productComponent, @CheckForNull Calendar effectiveDate) {
        Object relevantProductObject = getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime());
        if (relevantProductObject instanceof ProductComponent productCmpt) {
            return FormulaUtilAccess.getFormulaText(productCmpt, getFormulaName());
        } else {
            return FormulaUtilAccess.getFormulaText((ProductComponentGeneration)relevantProductObject,
                    getFormulaName());
        }
    }

    /**
     * Sets the formula text of this formula in the given product component.
     *
     * @param productComponent a product component based on the product component type this formula
     *            belongs to.
     * @param effectiveDate (optional) the date to use for selecting the product component's
     *            generation, if this formula {@link #isChangingOverTime()}
     * @param formulaText the new formula text
     */
    public void setFormulaText(IProductComponent productComponent,
            @CheckForNull Calendar effectiveDate,
            String formulaText) {
        setFormulaText(getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime()), formulaText);
    }

    /**
     * Sets the formula text of this formula in the given product component.
     * <p>
     * If the formula is not changing over time, the underlying product component is used directly;
     * otherwise, the given generation is used.
     * </p>
     *
     * @param generation the product component generation to base the formula text update on
     * @param formulaText the new formula text
     * @since 25.1
     */
    public void setFormulaText(IProductComponentGeneration generation, String formulaText) {
        setFormulaText(getRelevantProductObject(generation, isChangingOverTime()), formulaText);
    }

    private void setFormulaText(IProductObject productObject, String formulaText) {
        if (productObject instanceof ProductComponent productCmpt) {
            FormulaUtilAccess.setFormulaText(productCmpt, getFormulaName(), formulaText);
        } else {
            FormulaUtilAccess.setFormulaText((ProductComponentGeneration)productObject, getFormulaName(), formulaText);
        }
    }

    /**
     * Returns the name of the formula.
     */
    public String getFormulaName() {
        return annotation.name();
    }

    /**
     * @return {@code true} if the formula is required, {@code false} otherwise
     *
     * @since 25.1
     */
    public boolean isRequired() {
        return annotation.required();
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

    @Override
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent productComponent,
            Calendar effectiveDate) {
        ResourceBundle messages = ResourceBundle.getBundle(Formula.class.getName(), context.getLocale());
        String formatString = String.format(messages.getString(MSGKEY_REQUIRED_FORMULA_IS_EMPTY),
                getLabel(context.getLocale()), productComponent.getId());

        if (isRequired() && IpsStringUtils.isEmpty(getFormulaText(productComponent, effectiveDate))) {
            list.newError(MSGCODE_REQUIRED_FORMULA_IS_EMPTY, formatString,
                    new ObjectProperty(this, PROPERTY_REQUIRED_FORMULA),
                    new ObjectProperty(productComponent, getName()));
        }
    }

    private static class FormulaUtilAccess extends FormulaUtil {

        private static String getFormulaText(ProductComponent product, String formulaSignature) {
            return FormulaUtil.getFormula(product, formulaSignature);
        }

        private static void setFormulaText(ProductComponent product, String formulaSignature, String formulaText) {
            FormulaUtil.setFormula(product, formulaSignature, formulaText);
        }

        private static String getFormulaText(ProductComponentGeneration productGeneration, String formulaSignature) {
            return FormulaUtil.getFormula(productGeneration, formulaSignature);
        }

        private static void setFormulaText(ProductComponentGeneration productGeneration,
                String formulaSignature,
                String formulaText) {
            FormulaUtil.setFormula(productGeneration, formulaSignature, formulaText);
        }
    }
}
