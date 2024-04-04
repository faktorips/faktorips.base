/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.values.InternationalString;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Utility class to handle and encapsulate access to formula texts.
 * <p>
 * <em>This class is not intended to be subclassed or used by clients.</em>
 *
 * @since 24.7
 */
// public sealed class FormulaUtil permits org.faktorips.runtime.model.type.Formula.FormulaUtilAccess
public class FormulaUtil {

    protected FormulaUtil() {
        // utility class
    }

    protected static String getFormula(ProductComponent product, String formulaSignature) {
        return product.getFormulaHandler().getFormula(formulaSignature);
    }

    protected static void setFormula(ProductComponent product, String formulaSignature, String formulaText) {
        product.getFormulaHandler().setFormula(formulaSignature, formulaText);
    }

    @CheckForNull
    protected static InternationalString getDescription(ProductComponent product, String formulaSignature) {
        return product.getFormulaHandler().getDescription(formulaSignature);
    }

    protected static void setDescription(ProductComponent product,
            String formulaSignature,
            @CheckForNull InternationalString description) {
        product.getFormulaHandler().setDescription(formulaSignature, description);
    }

    protected static String getFormula(ProductComponentGeneration productGeneration, String formulaSignature) {
        return productGeneration.getFormulaHandler().getFormula(formulaSignature);
    }

    protected static void setFormula(ProductComponentGeneration productGeneration,
            String formulaSignature,
            String formulaText) {
        productGeneration.getFormulaHandler().setFormula(formulaSignature, formulaText);
    }

    @CheckForNull
    protected static InternationalString getDescription(ProductComponentGeneration productGeneration,
            String formulaSignature) {
        return productGeneration.getFormulaHandler().getDescription(formulaSignature);
    }

    protected static void setDescription(ProductComponentGeneration productGeneration,
            String formulaSignature,
            @CheckForNull InternationalString description) {
        productGeneration.getFormulaHandler().setDescription(formulaSignature, description);
    }
}
