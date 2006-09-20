/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * Test rule violation type. Supported types are violated or not violated.
 * 
 * @author Joerg Ortmann
 */
public class TestRuleViolationType extends DefaultEnumValue {
    
    public final static TestRuleViolationType VIOLATED;
    
    public final static TestRuleViolationType NOT_VIOLATED;

    public final static TestRuleViolationType UNKNOWN;
    
    private final static DefaultEnumType enumType; 

    private final static DefaultEnumType nonDefaultenumType; 

    static {
        enumType = new DefaultEnumType("TestRuleViolationType", TestRuleViolationType.class); //$NON-NLS-1$
        VIOLATED = new TestRuleViolationType(enumType, "violated", Messages.TestRuleViolationType_Violated); //$NON-NLS-1$
        NOT_VIOLATED = new TestRuleViolationType(enumType, "notViolated", Messages.TestRuleViolationType_NotViolated); //$NON-NLS-1$

        nonDefaultenumType = new DefaultEnumType("NonDefaultTestRuleViolationType", TestRuleViolationType.class); //$NON-NLS-1$
        UNKNOWN = new TestRuleViolationType(nonDefaultenumType, "unknown", Messages.TestRuleViolationType_Unknown); //$NON-NLS-1$
    }

    public TestRuleViolationType(DefaultEnumType type, String id, String name){
        super(type, id, name);
    }
    
    /**
     * Returns the test rule violation type by the given id or <code>UNKNOWN</code> if the id not represent a
     * test violation type.
     */
    public final static TestRuleViolationType getTestRuleViolationType(String id) {
        TestRuleViolationType type = (TestRuleViolationType) enumType.getEnumValue(id);
        return type == null?UNKNOWN:type;
    }
    
    /**
     * Returns the enum type which contains all test parameter types.
     */
    public final static EnumType getEnumType() {
        return enumType;
    }
}
