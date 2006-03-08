/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class Round extends AbstractFlFunction {
    
    private final static String[] ROUNDING_MODES = new String[7];
    
    static {
        ROUNDING_MODES[BigDecimal.ROUND_CEILING] = "ROUND_CEILING";
        ROUNDING_MODES[BigDecimal.ROUND_DOWN] = "ROUND_DOWN";
        ROUNDING_MODES[BigDecimal.ROUND_FLOOR] = "ROUND_FLOOR";
        ROUNDING_MODES[BigDecimal.ROUND_HALF_DOWN] = "ROUND_HALF_DOWN";
        ROUNDING_MODES[BigDecimal.ROUND_HALF_EVEN] = "ROUND_HALF_EVEN";
        ROUNDING_MODES[BigDecimal.ROUND_HALF_UP] = "ROUND_HALF_UP";
        ROUNDING_MODES[BigDecimal.ROUND_UP] = "ROUND_UP";
    }
    
    private int roundingMode;
    
    /**
     * Constructs a new round function with the given name and rounding mode.
     * 
     * @param name The function name.
     * @param roundingMode One of the rounding modes defined by <code>BigDecimal</code>.
     * 
     * @throws IllegalArgumentException if name is <code>null</code> or the roundingMode
     * is illegal.
     */
    public Round(String name, String description, int roundingMode) {
        super(name, description, Datatype.DECIMAL, new Datatype[] {Datatype.DECIMAL, Datatype.PRIMITIVE_INT});
        if (roundingMode<0 | roundingMode > ROUNDING_MODES.length-1) {
            throw new IllegalArgumentException("Illegal rounding mode " + roundingMode);
        }
        this.roundingMode = roundingMode;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.codegen.JavaCodeFragment[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(".setScale(");
        fragment.append(argResults[1].getCodeFragment());
        fragment.append(", ");
        fragment.appendClassName(BigDecimal.class);
        fragment.append('.');
        fragment.append(ROUNDING_MODES[roundingMode]);
        fragment.append(')');
        return new CompilationResultImpl(fragment, Datatype.DECIMAL);
    }

}
