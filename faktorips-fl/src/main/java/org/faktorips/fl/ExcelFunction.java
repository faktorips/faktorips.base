/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

/**
 * List of all supported Excel functions.
 */
public enum ExcelFunction {
    MIN("min"),
    MAX("max"),
    ISEMPTY("isempty"),
    WHOLENUMBER("wholenumber"),
    ROUNDDOWN("rounddown"),
    ROUNDUP("roundup"),
    ROUND("round"),
    IF("if"),
    ABS("abs"),
    NOT("not"),
    OR("or"),
    AND("and"),
    POWER("power"),
    SQRT("sqrt"),
    COUNT("count"),
    DAYS("days"),
    WEEKS("weeks"),
    MONTHS("months"),
    YEARS("years"),
    DATE("date"),
    DAYS360("days360"),
    TEXT("text");

    private final String propertyKey;

    ExcelFunction(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * Returns the key used to access name and description for the function in a localized
     * properties file.
     * 
     * @see LocalizedFunctionsResolver#getFctName(String)
     * @see LocalizedFunctionsResolver#getFctDescription(String)
     */
    public String getPropertyKey() {
        return propertyKey;
    }
}
