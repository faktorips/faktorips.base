/**
 * 
 */
package org.faktorips.runtime.test;

import java.util.List;

import org.faktorips.runtime.IEnumValueLookupService;
import org.faktorips.runtime.internal.TestEnumValue;

/**
 * @author ortmann
 *
 */
public class TestEnumValueLookup implements IEnumValueLookupService<TestEnumValue> {

	public TestEnumValueLookup() {
	}

	public Class<TestEnumValue> getEnumTypeClass() {
		return TestEnumValue.class;
	}

	public TestEnumValue getEnumValue(Object id) {
		return null;
	}

	public List<TestEnumValue> getEnumValues() {
		return null;
	}


}
