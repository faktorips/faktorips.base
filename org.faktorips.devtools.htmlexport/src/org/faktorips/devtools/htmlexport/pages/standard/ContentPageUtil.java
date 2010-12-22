/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;

/**
 * Utility for Content-Pages
 * 
 * @author dicker
 * 
 */
public class ContentPageUtil {

    public static AbstractPageElement createObjectContentPageElement(IIpsSrcFile ipsSrcFile,
            DocumentationContext context) throws CoreException {

        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        return createObjectContentPageElement(ipsObject, context);
    }

    private static AbstractPageElement createObjectContentPageElement(IIpsObject ipsSrcFile,
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

}
