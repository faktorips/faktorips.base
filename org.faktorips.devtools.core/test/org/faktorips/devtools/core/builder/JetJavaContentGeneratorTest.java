/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.LocalizedStringsSet;

public class JetJavaContentGeneratorTest extends IpsPluginTest {
    
    private DumyJavaSourceFileBuilder  builder;
    private TestJetJavaContentGenerator generator;
    private IIpsProject project;
    private IIpsObject ipsObject;

    
    public void setUp() throws Exception{
        super.setUp();
        project = newIpsProject("TestProject");
        ipsObject = newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy");
        builder = new DumyJavaSourceFileBuilder(new TestIpsArtefactBuilderSet(), "dumy",
            new LocalizedStringsSet(JavaSourceFileBuilderTest.class));
        generator = new TestJetJavaContentGenerator();
        generator.setJavaSourceFileBuilder(builder);
    }
    
    public void testAppendClass() throws CoreException{
        StringBuffer buf = new StringBuffer();
        generator.appendClass(List.class);
        generator.addImports(buf);
        assertTrue(buf.toString().indexOf(List.class.getName()) != -1);
    }
    
    public void testAppendClassName() throws CoreException{
        StringBuffer buf = new StringBuffer();
        generator.appendClassName(List.class.getName());
        generator.addImports(buf);
        assertTrue(buf.toString().indexOf(List.class.getName()) != -1);
    }
    
    public void testMarkImportLocation() throws CoreException{
        StringBuffer buf = new StringBuffer();
        buf.append("hello");
        int length = buf.length();
        generator.markImportLocation(buf);
        generator.appendClass(List.class);
        generator.addImports(buf);
        String content = buf.toString();
        String importEntry = content.substring(length, content.length());
        assertEquals("import java.util.List;" + SystemUtils.LINE_SEPARATOR, importEntry);
    }
    
    public void testGetDocumentText() throws CoreException{
        builder.beforeBuild(ipsObject.getIpsSrcFile(), null);
        String value = generator.getLocalizedText(ipsObject.getIpsSrcFile(), "key");
        assertNotNull(value);
    }
    
    private static class TestJetJavaContentGenerator extends JetJavaContentGenerator{

        public String generate(IIpsSrcFile ipsSrcFile) throws CoreException {
            return null;
        }
        
    }
}
