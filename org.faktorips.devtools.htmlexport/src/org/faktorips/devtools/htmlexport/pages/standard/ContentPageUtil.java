/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;

/**
 * Utility for Content-Pages
 * 
 * @author dicker
 * 
 */
public class ContentPageUtil {

    public static ICompositePageElement createObjectContentPageElement(IIpsSrcFile ipsSrcFile,
            DocumentationContext context) throws CoreException {

        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        return createObjectContentPageElement(ipsObject, context);
    }

    // CSOFF: CyclomaticComplexityCheck
    private static ICompositePageElement createObjectContentPageElement(IIpsObject ipsSrcFile,
            DocumentationContext context) throws CoreException {
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE) {
            return new PolicyCmptTypeContentPageElement((IPolicyCmptType)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE) {
            return new ProductCmptTypeContentPageElement((IProductCmptType)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
            return new ProductCmptContentPageElement((IProductCmpt)ipsSrcFile, context);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_TEMPLATE) {
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
