/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class Round extends AbstractFlFunction {

    private final static String[] ROUNDING_MODES = new String[7];

    static {
        ROUNDING_MODES[BigDecimal.ROUND_CEILING] = "ROUND_CEILING"; //$NON-NLS-1$
        ROUNDING_MODES[BigDecimal.ROUND_DOWN] = "ROUND_DOWN"; //$NON-NLS-1$
        ROUNDING_MODES[BigDecimal.ROUND_FLOOR] = "ROUND_FLOOR"; //$NON-NLS-1$
        ROUNDING_MODES[BigDecimal.ROUND_HALF_DOWN] = "ROUND_HALF_DOWN"; //$NON-NLS-1$
        ROUNDING_MODES[BigDecimal.ROUND_HALF_EVEN] = "ROUND_HALF_EVEN"; //$NON-NLS-1$
        ROUNDING_MODES[BigDecimal.ROUND_HALF_UP] = "ROUND_HALF_UP"; //$NON-NLS-1$
        ROUNDING_MODES[BigDecimal.ROUND_UP] = "ROUND_UP"; //$NON-NLS-1$
    }

    private int roundingMode;

    /**
     * Constructs a new round function with the given name and rounding mode.
     * 
     * @param name The function name.
     * @param roundingMode One of the rounding modes defined by <code>BigDecimal</code>.
     * 
     * @throws IllegalArgumentException if name is <code>null</code> or the roundingMode is illegal.
     */
    public Round(String name, String description, int roundingMode) {
        super(name, description, roundingMode == BigDecimal.ROUND_DOWN ? FunctionSignatures.RoundDown
                : roundingMode == BigDecimal.ROUND_UP ? FunctionSignatures.RoundUp : FunctionSignatures.Round);
        if (roundingMode < 0 || roundingMode > ROUNDING_MODES.length - 1) {
            throw new IllegalArgumentException("Illegal rounding mode " + roundingMode); //$NON-NLS-1$
        }
        this.roundingMode = roundingMode;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(CompilationResult[])
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(".setScale("); //$NON-NLS-1$
        fragment.append(argResults[1].getCodeFragment());
        fragment.append(", "); //$NON-NLS-1$
        fragment.appendClassName(BigDecimal.class);
        fragment.append('.');
        fragment.append(ROUNDING_MODES[roundingMode]);
        fragment.append(')');
        return new CompilationResultImpl(fragment, Datatype.DECIMAL);
    }

}
