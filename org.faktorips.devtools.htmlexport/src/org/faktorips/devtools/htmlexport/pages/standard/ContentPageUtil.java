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
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;

/**
 * Utility for Content-Pages
 * 
 * @author dicker
 * 
 */
public class ContentPageUtil {

    public static AbstractPageElement createObjectContentPageElement(IIpsSrcFile ipsSrcFile,
            DocumentorConfiguration config) {

        IIpsObject ipsObject;
        try {
            ipsObject = ipsSrcFile.getIpsObject();
            return createObjectContentPageElement(ipsObject, config);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static AbstractPageElement createObjectContentPageElement(IIpsObject ipsSrcFile,
            DocumentorConfiguration config) {
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE) {
            return new PolicyCmptTypeContentPageElement((IPolicyCmptType)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE) {
            return new ProductCmptTypeContentPageElement((IProductCmptType)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
            return new ProductCmptContentPageElement((IProductCmpt)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.ENUM_TYPE) {
            return new EnumTypeContentPageElement((IEnumType)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.ENUM_CONTENT) {
            return new EnumContentContentPageElement((IEnumContent)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TABLE_STRUCTURE) {
            return new TableStructureContentPageElement((ITableStructure)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
            return new TableContentsContentPageElement((ITableContents)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE_TYPE) {
            return new TestCaseTypeContentPageElement((ITestCaseType)ipsSrcFile, config);
        }
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE) {
            return new TestCaseContentPageElement((ITestCase)ipsSrcFile, config);
        }

        // TODO Businessfunction
        return new IpsObjectContentPageElement(ipsSrcFile, config);
    }

}
