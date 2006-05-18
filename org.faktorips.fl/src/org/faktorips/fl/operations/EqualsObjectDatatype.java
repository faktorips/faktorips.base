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

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.functions.Messages;
import org.faktorips.util.message.Message;


/**
 * Equals operation for none primitive datatypes that are tested for equality with
 * the equals() Method.
 */
public class EqualsObjectDatatype extends AbstractBinaryOperation {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "EQUALS-OPERATION"; //$NON-NLS-1$
    
    public EqualsObjectDatatype (Datatype type) {
        super("=", type, type); //$NON-NLS-1$
    }

    /** 
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {

        ConversionCodeGenerator ccg = getCompiler().getConversionCodeGenerator();
        Datatype datatype1 = lhs.getDatatype();
        Datatype datatype2 = rhs.getDatatype();
        
        if (!datatype1.equals(datatype2)) {
            if (ccg.canConvert(datatype1, datatype2)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype1, datatype2, lhs.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype2);
                newResult.addMessages(lhs.getMessages());
                lhs = newResult;
            } else if (ccg.canConvert(datatype2, datatype1)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype2, datatype1, rhs.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype1);
                newResult.addMessages(rhs.getMessages());
                rhs = newResult;
            } else {
                String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, new Object[]{datatype1, datatype2});
                Message msg = Message.newError(ERROR_MESSAGE_CODE, text);
                return new CompilationResultImpl(msg);
            }
        }
        lhs.getCodeFragment().append(".equals("); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        lhs.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        return lhs;
    }

}
