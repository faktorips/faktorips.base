/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;
import org.faktorips.fl.UnaryOperation;

/**
 * Abstract implementation of {@link UnaryOperation} for {@link JavaCodeFragment Java code}
 * generating operations.
 */
public abstract class AbstractUnaryJavaOperation extends AbstractUnaryOperation<JavaCodeFragment> {

    /**
     * Creates a new unary operation for the indicated {@link Operation}.
     */
    public AbstractUnaryJavaOperation(Operation operation) {
        super(operation);
    }

    /**
     * Creates a new unary operation for the indicated operator and {@link Datatype data type}.
     */
    public AbstractUnaryJavaOperation(Datatype datatype, String operator) {
        super(datatype, operator);
    }

    public CompilationResult<JavaCodeFragment> generate(CompilationResult<JavaCodeFragment> arg) {
        CompilationResultImpl result = generate((CompilationResultImpl)arg);
        result.addIdentifiersUsed(((CompilationResultImpl)arg).getIdentifiersUsedAsSet());
        return result;
    }

    /**
     * Generates the {@link CompilationResult} for the given operand.
     * 
     * @param arg the operand
     * @return the given operand combined with this operation's operator
     */
    public abstract CompilationResultImpl generate(CompilationResultImpl arg);

}
