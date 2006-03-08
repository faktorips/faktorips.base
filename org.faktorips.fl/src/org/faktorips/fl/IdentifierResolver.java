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

package org.faktorips.fl;

import java.util.Locale;

/**
 * A <code>IdentifierResolver</code> resolves identifiers used in an expression.
 * <p>
 * Example:
 * <blockquote>
 * <pre>
 * 2 * a + 3
 * </pre>
 * </blockquote>
 * In the above example a is an identifier. The compiler needs to know a's datatype
 * and generate appropriate Java sourcecode that returns the value of a at runtime.
 * The compiler delegates this task to an identifier resolver.   
 */
public interface IdentifierResolver {
    
    /**
     * Returns the compilation result for the indicated identifier.
     * <P>
     * If the given string is a valid identifier the compilation result must contain
     * the Java sourcecode that can be inserted in the expression's Java sourcecode
     * along with the identifier's datatype. 
     * <P>
     * If the string is not a valid identifier the returned compilation result must contain 
     * an <strong>error</string> message with the code {@link ExprCompiler#UNDEFINED_IDENTIFIER UNDEFINED_IDENTIFIER}.
     * 
     * @param identifier	The identifier to resolve.
     * @param locale		The locale used for locale dependant messages. 
     */
    public CompilationResult compile(String identifier, Locale locale);
    
}
