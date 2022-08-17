/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcase;

import java.util.Arrays;

import org.faktorips.devtools.model.INamedValue;
import org.faktorips.util.ArgumentCheck;

/**
 * Test rule violation type. Supported types are violated or not violated.
 * 
 * @author Joerg Ortmann
 */
public enum TestRuleViolationType implements INamedValue {

    VIOLATED("violated", Messages.TestRuleViolationType_Violated, Messages.TestRuleViolationType_TextViolated), //$NON-NLS-1$
    NOT_VIOLATED("notViolated", Messages.TestRuleViolationType_NotViolated, //$NON-NLS-1$
            Messages.TestRuleViolationType_TextNotViolated),
    UNKNOWN("unknown", Messages.TestRuleViolationType_Unknown, Messages.TestRuleViolationType_Unknown); //$NON-NLS-1$

    private final String id;
    private final String name;
    private final String text;

    TestRuleViolationType(String id, String name, String text) {
        ArgumentCheck.notNull(id);
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(text);
        this.id = id;
        this.name = name;
        this.text = text;
    }

    /**
     * Returns the test rule violation type by the given id or <code>UNKNOWN</code> if the id not
     * represent a test violation type.
     */
    public static final TestRuleViolationType getTestRuleViolationType(String id) {
        return Arrays.stream(TestRuleViolationType.values()).filter(s -> s.id.equals(id)).findAny().orElse(UNKNOWN);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the text representation of the rule violation type.
     */
    public String getText() {
        return text;
    }

    /**
     * Maps the rule values id to the corresponding name, if the given string is no id return the
     * given string without mapping.
     */
    public static String mapRuleValueTest(String id) {
        if (id.equals(TestRuleViolationType.VIOLATED.getId())) {
            return TestRuleViolationType.VIOLATED.getText();
        } else if (id.equals(TestRuleViolationType.NOT_VIOLATED.getId())) {
            return TestRuleViolationType.NOT_VIOLATED.getText();
        }
        return id;
    }

}
