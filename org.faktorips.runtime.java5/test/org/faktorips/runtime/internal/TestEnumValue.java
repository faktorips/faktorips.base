/**
 * 
 */
package org.faktorips.runtime.internal;

import org.faktorips.runtime.IEnumValue;

/**
 * @author ortmann
 *
 */
public class TestEnumValue implements IEnumValue {

    private String id;
    
    public TestEnumValue(String id){
        this.id = id;
    }
    
    public String getEnumValueId() {
        return id;
    }

    public String toString() {
    	return id;
    }
}
