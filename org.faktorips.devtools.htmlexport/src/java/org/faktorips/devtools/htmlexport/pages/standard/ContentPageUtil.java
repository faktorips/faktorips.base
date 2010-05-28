package org.faktorips.devtools.htmlexport.pages.standard;

import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;

/**
 * Utility for Content-Pages 
 * @author dicker
 *
 */
public class ContentPageUtil {

	public static AbstractRootPageElement createObjectContentPageElement(IIpsObject object, DocumentorConfiguration config) {
		if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
			return new PolicyCmptTypeContentPageElement((IPolicyCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
			return new ProductCmptTypeContentPageElement((IProductCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT)
			return new ProductCmptContentPageElement((IProductCmpt) object, config);
		if (object.getIpsObjectType() == IpsObjectType.ENUM_TYPE)
			return new EnumTypeContentPageElement((IEnumType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.ENUM_CONTENT)
			return new EnumContentContentPageElement((IEnumContent) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TABLE_STRUCTURE)
			return new TableStructureContentPageElement((ITableStructure) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS)
			return new TableContentsContentPageElement((ITableContents) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TEST_CASE_TYPE)
			return new TestCaseTypeContentPageElement((ITestCaseType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TEST_CASE)
			return new TestCaseContentPageElement((ITestCase) object, config);
		
		// TODO Businessfunction
		return new IpsObjectContentPageElement(object, config);
	}

}
