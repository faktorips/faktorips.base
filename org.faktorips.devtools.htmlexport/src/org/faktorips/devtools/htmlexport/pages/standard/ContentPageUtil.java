/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;

/**
 * Utility for Content-Pages
 * 
 * @author dicker
 * 
 */
public class ContentPageUtil {

    private ContentPageUtil() {
        // Utility class
    }

    public static ICompositePageElement createObjectContentPageElement(IIpsSrcFile ipsSrcFile,
            DocumentationContext context) {

        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        return createObjectContentPageElement(ipsObject, context);
    }

    // CSOFF: CyclomaticComplexityCheck
    private static ICompositePageElement createObjectContentPageElement(IIpsObject ipsSrcFile,
            DocumentationContext context) {
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE) {
            return new PolicyCmptTypeContentPageElement((IPolicyCmptType)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE) {
            return new ProductCmptTypeContentPageElement((IProductCmptType)ipsSrcFile, context);
        }
        if ((ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT)
                || (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_TEMPLATE)) {
            return new ProductCmptContentPageElement((IProductCmpt)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.ENUM_TYPE) {
            return new EnumTypeContentPageElement((IEnumType)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.ENUM_CONTENT) {
            return new EnumContentContentPageElement((IEnumContent)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TABLE_STRUCTURE) {
            return new TableStructureContentPageElement((ITableStructure)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
            return new TableContentsContentPageElement((ITableContents)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE_TYPE) {
            return new TestCaseTypeContentPageElement((ITestCaseType)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE) {
            return new TestCaseContentPageElement((ITestCase)ipsSrcFile, context);
        }

        // TODO Businessfunction && ORMEXT???
        return new IpsObjectContentPageElement(ipsSrcFile, context);
    }
    // CSON: CyclomaticComplexityCheck

}
