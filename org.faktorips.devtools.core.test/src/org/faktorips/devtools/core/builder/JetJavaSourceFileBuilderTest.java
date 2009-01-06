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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class JetJavaSourceFileBuilderTest extends AbstractIpsPluginTest {

    private JetJavaSourceFileBuilder builder;
    private IIpsProject ipsProject;

    public void setUp() throws Exception {
        super.setUp();
        builder = new JetJavaSourceFileBuilder(new TestIpsArtefactBuilderSet(), "test",
                JetJavaSourceFileBuilderTest.TestGenerator.class, IpsObjectType.POLICY_CMPT_TYPE,
                false, "Prefix", "Suffix");
        ipsProject = newIpsProject();
    }

    public void testGenerate() throws Exception {
        String content = builder.generate();
        assertEquals("hello", content);
    }

    public void testIsBuilderFor() throws Exception {
        IIpsObject policyCmpt = newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "TestCmpt");
        IIpsObject tableStructure = newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
            "TestTable");
        assertTrue(builder.isBuilderFor(policyCmpt.getIpsSrcFile()));
        assertFalse(builder.isBuilderFor(tableStructure.getIpsSrcFile()));
    }

    public void testGetUnqualifiedClassName() throws Exception {
        IIpsObject policyCmpt = newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "TestCmpt");
        String className = builder.getUnqualifiedClassName(policyCmpt.getIpsSrcFile());
        assertEquals("PrefixTestCmptSuffix", className);
    }

    public static class TestGenerator extends JetJavaContentGenerator {

        public String generate(IIpsSrcFile ipsSrcFile) throws CoreException {
            return "hello";
        }
    }
}
