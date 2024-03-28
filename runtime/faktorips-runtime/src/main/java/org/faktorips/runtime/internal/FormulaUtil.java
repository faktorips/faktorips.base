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

/**
 * Utility class to handle and encapsulate access to formula texts.
 *
 * @since 24.7
 */
public class FormulaUtil {

    private FormulaUtil() {
        // utility class
    }

    public static String getFormula(ProductComponent product, String formulaSignature) {
        return product.getFormulaHandler().getFormula(formulaSignature);
    }

    public static void setFormula(ProductComponent product, String formulaSignature, String formulaText) {
        product.getFormulaHandler().setFormula(formulaSignature, formulaText);
    }
}
