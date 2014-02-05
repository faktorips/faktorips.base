/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * This interface is used for code that is generated for table access functions called within the
 * formula language. Since the fl-language is an expression language the java code that is generated
 * is also an expression. A problem arises when a call to a table within the generated java
 * expression returns null instead of a table row. Since this causes a NullPointerException when a
 * getter-method is called upon the row instance special care has to be taken to circumvent this
 * situation. Therefor a table function call within the formula language is generated as java code
 * within the execute method of an anonymous class of this interface. Within the execute method the
 * null situation can be handled within an if block. Example:
 * <p>
 * <code>
      public Decimal computePremium(final Integer age) {
        return ((Decimal)(new TableFunctionExecution() {
           public Object execute() {
               TableWithMultipleContentsRow row = TableWithMultipleContents.getInstance(getRepository(),
                        "tables.TableWithMultipleContents1").findRow(age);
                if (row != null) {
                    return row.getRate();
                }
                return Decimal.NULL;
            }
        }).execute());
    }
    </code>
 * 
 * 
 * @author Peter Erzberger
 */
public interface TableFunctionExecution {

    /**
     * Executes the java code that is generated for a table function call of the formula language.
     * 
     * @return the return value of the function or null or a null special case value if if the no
     *         row was found in the table for the parameters that have been provided to the table
     *         function.
     */
    public Object execute();
}
