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

package org.faktorips.fl.operations;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the multiplication of two decimals. 
 */
public class DivideDecimalDecimal extends AbstractBinaryOperation {

    // the default scale used for rounding
    private int scale = 10;
    
    public DivideDecimalDecimal() {
        super("/", Datatype.DECIMAL, Datatype.DECIMAL);
    }
    
    /**
     * Sets the rounding scale used.
     */
    public void setRoundingScale(int scale) {
        this.scale = scale;
    }

    /**
     * Returns the rounding scale used.
     */
    public int getRoundingScale() {
        return scale;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        JavaCodeFragment fragment = lhs.getCodeFragment();
        fragment.append(".divide(");
        lhs.add(rhs);
        fragment.append(", ");
        fragment.append(scale);
        fragment.append(", ");
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_HALF_UP)");
        lhs.setDatatype(Datatype.DECIMAL);
        return lhs;
    }

}
