/**
 * 
 */
package org.faktorips.runtime.internal;

/**
 * @author ortmann
 * 
 */
public class TestEnumValue {

    private String id;

    public TestEnumValue(String id) {
        this.id = id;
    }

    public String getEnumValueId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
