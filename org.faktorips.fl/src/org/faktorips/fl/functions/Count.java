/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * A function that counts the instances of the path
 * 
 * @author frank
 * @since 3.11.0
 */
public class Count extends AbstractFlFunction {

    public static final String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "COUNT"; //$NON-NLS-1$

    /**
     * @param name the function name
     */
    public Count(String name, String description) {
        super(name, description, FunctionSignatures.Count);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        Datatype argType = argResults[0].getDatatype();
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (argType instanceof ListOfTypeDatatype) {
            fragment.append(argResults[0].getCodeFragment());
            fragment.append(".size()"); //$NON-NLS-1$
        } else {
            String text = Messages.INSTANCE.getString(ERROR_MESSAGE_CODE, new Object[] { argType });
            Message msg = Message.newError(ERROR_MESSAGE_CODE, text);
            return new CompilationResultImpl(msg);
        }
        return new CompilationResultImpl(fragment, getType());
    }
}
