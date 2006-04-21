package org.faktorips.devtools.stdbuilder.test;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.faktorips.devtools.stdbuilder.MutableClProductCmptRegistryTocTest;
import org.faktorips.devtools.stdbuilder.ParameterIdentifierResolverTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSetTest;
import org.faktorips.devtools.stdbuilder.TocFileBuilderTest;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilderTest;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilderTest;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilderTest;

public class AutomatedSuite extends TestSuite {

	public static Test suite(){
		
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ParameterIdentifierResolverTest.class);
		suite.addTestSuite(TocFileBuilderTest.class             );
		suite.addTestSuite(StandardBuilderSetTest.class         );
		suite.addTestSuite(MutableClProductCmptRegistryTocTest.class );
		suite.addTestSuite(XmlContentFileCopyBuilderTest.class       );
		suite.addTestSuite(ProductCmptBuilderTest.class              );
		suite.addTestSuite(TableImplBuilderTest.class                );
		

		return suite;
	}
	
}
