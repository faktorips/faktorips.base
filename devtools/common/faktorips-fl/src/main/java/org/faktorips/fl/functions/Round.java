/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.math.RoundingMode;
import java.util.Objects;

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

    private RoundingMode roundingMode;

    /**
     * Constructs a new round function with the given name and rounding mode.
     * 
     * @param name The function name.
     * @param roundingMode One of the rounding modes defined by {@link RoundingMode}.
     * 
     * @throws IllegalArgumentException if the roundingMode is null.
     */
    public Round(String name, String description, RoundingMode roundingMode) {
        super(name, description, getSignature(Objects.requireNonNull(roundingMode, "roundingMode cannot be null")));
        this.roundingMode = roundingMode;
    }

    /**
     * Constructs a new round function with the given name and rounding mode.
     * 
     * @deprecated since 21.6. Use {@link #Round(String, String, RoundingMode)} instead.
     * 
     * @param name The function name.
     * @param roundingMode One of the rounding modes defined by <code>BigDecimal</code>.
     * 
     * @throws IllegalArgumentException if roundingMode cannot be converted to {@link RoundingMode}.
     */
    @Deprecated
    public Round(String name, String description, int roundingMode) {
        this(name, description, RoundingMode.valueOf(roundingMode));
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(CompilationResult[])
     */
    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(".setScale("); //$NON-NLS-1$
        fragment.append(argResults[1].getCodeFragment());
        fragment.append(", "); //$NON-NLS-1$
        fragment.appendClassName(RoundingMode.class);
        fragment.append('.');
        fragment.append(roundingMode.name());
        fragment.append(')');
        return new CompilationResultImpl(fragment, Datatype.DECIMAL);
    }

    private static FunctionSignatures getSignature(RoundingMode mode) {
        return switch (mode) {
            case DOWN -> FunctionSignatures.RoundDown;
            case UP -> FunctionSignatures.RoundUp;
            default -> FunctionSignatures.Round;
        };
    }

}
