/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Test;

public class PageElementUtilsTest extends AbstractHtmlExportPluginTest {
    private static final TargetType TARGET = TargetType.CONTENT;

    @Test
    public void testCreateLinkPageElementAllTypesDocumented() {
        createMassivProjekt();

        context.setDocumentedIpsObjectTypes(ipsProject.getIpsModel().getIpsObjectTypes());

        List<IIpsSrcFile> srcFiles = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(srcFiles);

        for (IIpsSrcFile srcFile : srcFiles) {
            IPageElement pageElement = new PageElementUtils(getContext()).createLinkPageElement(context, srcFile,
                    TARGET,
                    srcFile.getName(), false);
            assertIsLink(pageElement);
        }

    }

    private void assertIsLink(IPageElement pageElement) {
        assertEquals(LinkPageElement.class, pageElement.getClass());
    }

    private void assertIsDeadLink(IPageElement pageElement) {
        assertEquals(TextPageElement.class, pageElement.getClass());
        assertTrue(pageElement.hasStyle(Style.DEAD_LINK));
    }

    @Test
    public void testCreateLinkPageElementSomeTypesDocumented() {
        createMassivProjekt();

        IpsObjectType testedIpsObjectType = IpsObjectType.POLICY_CMPT_TYPE;

        context.setDocumentedIpsObjectTypes(testedIpsObjectType);

        List<IIpsSrcFile> srcFiles = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(srcFiles);

        for (IIpsSrcFile srcFile : srcFiles) {
            IPageElement pageElement = new PageElementUtils(getContext()).createLinkPageElement(context, srcFile,
                    TARGET,
                    srcFile.getName(), false);

            if (srcFile.getIpsObjectType() == testedIpsObjectType) {
                assertIsLink(pageElement);
            } else {
                assertIsDeadLink(pageElement);
            }
        }
    }

    @Test
    public void testCreateTextPageElements() {
        List<String> texts = new ArrayList<>();
        texts.add("text 1");
        texts.add("text 2");
        texts.add("text 3");
        texts.add("text 4");

        IPageElement[] textPageElements = new PageElementUtils(getContext()).createTextPageElements(texts);

        assertEquals(texts.size(), textPageElements.length);

        for (int i = 0; i < textPageElements.length; i++) {
            texts.get(i).equals(((TextPageElement)textPageElements[i]).getText());
        }

    }
}
