package org.faktorips.devtools.stdbuilder.test;
import org.faktorips.devtools.stdbuilder.ParameterIdentifierResolverTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomatedSuite extends TestSuite {

	public static Test suite(){
		
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ParameterIdentifierResolverTest.class);
		

		return suite;
	}
	
}
