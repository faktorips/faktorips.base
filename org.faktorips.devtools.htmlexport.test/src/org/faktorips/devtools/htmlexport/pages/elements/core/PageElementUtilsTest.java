/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.junit.Test;

public class PageElementUtilsTest extends AbstractHtmlExportPluginTest {
    private static final String TARGET = "target"; //$NON-NLS-1$

    @Test
    public void testCreateLinkPageElementAllTypesDocumented() throws CoreException {
        createMassivProjekt();

        context.setDocumentedIpsObjectTypes(ipsProject.getIpsModel().getIpsObjectTypes());

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(srcFiles);

        for (IIpsSrcFile srcFile : srcFiles) {
            IPageElement pageElement = new PageElementUtils().createLinkPageElement(context, srcFile, TARGET,
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
    public void testCreateLinkPageElementSomeTypesDocumented() throws CoreException {
        createMassivProjekt();

        IpsObjectType testedIpsObjectType = IpsObjectType.POLICY_CMPT_TYPE;

        context.setDocumentedIpsObjectTypes(testedIpsObjectType);

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(srcFiles);

        for (IIpsSrcFile srcFile : srcFiles) {
            IPageElement pageElement = new PageElementUtils().createLinkPageElement(context, srcFile, TARGET,
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
        List<String> texts = new ArrayList<String>();
        texts.add("text 1");
        texts.add("text 2");
        texts.add("text 3");
        texts.add("text 4");

        IPageElement[] textPageElements = new PageElementUtils().createTextPageElements(texts);

        assertEquals(texts.size(), textPageElements.length);

        for (int i = 0; i < textPageElements.length; i++) {
            texts.get(i).equals(((TextPageElement)textPageElements[i]).getText());
        }

    }
}
