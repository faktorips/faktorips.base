/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.functions.Messages;
import org.faktorips.util.message.Message;
import org.faktorips.values.ObjectUtil;

/**
 * Equals operation for none primitive datatypes that are tested for equality with the equals()
 * Method.
 */
public class EqualsObjectDatatype extends AbstractBinaryOperation {

    protected static String getErrorMessageCode() {
        return ExprCompiler.PREFIX + "EQUALS-OPERATION"; //$NON-NLS-1$
    }

    protected EqualsObjectDatatype(String operator, Datatype lhsDatatype, Datatype rhsDatatype) {
        super(operator, lhsDatatype, rhsDatatype);
    }

    public EqualsObjectDatatype(Datatype type) {
        super("=", type, type); //$NON-NLS-1$
    }

    public EqualsObjectDatatype(Datatype lhsDatatype, Datatype rhsDatatype) {
        super("=", lhsDatatype, rhsDatatype); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {

        ConversionCodeGenerator ccg = getCompiler().getConversionCodeGenerator();
        Datatype datatype1 = lhs.getDatatype();
        Datatype datatype2 = rhs.getDatatype();

        if (!datatype1.equals(datatype2)) {
            if (ccg.canConvert(datatype1, datatype2)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype1, datatype2, lhs.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype2, lhs.getMessages(),
                        lhs.getIdentifiersUsedAsSet());
                lhs = newResult;
            } else if (ccg.canConvert(datatype2, datatype1)) {
                JavaCodeFragment converted = ccg.getConversionCode(datatype2, datatype1, rhs.getCodeFragment());
                CompilationResultImpl newResult = new CompilationResultImpl(converted, datatype1, rhs.getMessages(),
                        rhs.getIdentifiersUsedAsSet());
                rhs = newResult;
            } else {
                String text = Messages.INSTANCE.getString(getErrorMessageCode(), new Object[] { datatype1, datatype2 });
                Message msg = Message.newError(getErrorMessageCode(), text);
                return new CompilationResultImpl(msg);
            }
        }
        CompilationResultImpl result = new CompilationResultImpl();
        result.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        JavaCodeFragment frag = result.getCodeFragment();
        frag.appendClassName(ObjectUtil.class);
        frag.append(".equals("); //$NON-NLS-1$
        frag.append(rhs.getCodeFragment());
        frag.append(", ");
        frag.append(lhs.getCodeFragment());
        frag.append(')');
        return result;
    }

}
