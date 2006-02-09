package org.faktorips.devtools.core.builder;

import java.util.List;

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
        builder = new DumyJavaSourceFileBuilder(new DumyPackageStructure(), "dumy",
            new LocalizedStringsSet(JavaSourceFileBuilderTest.class));
        generator = new TestJetJavaContentGenerator();
        generator.setJavaSourceFileBuilder(builder);
    }
    
    public void testAppendClass(){
        StringBuffer buf = new StringBuffer();
        generator.appendClass(List.class);
        generator.addImports(buf);
        assertTrue(buf.toString().indexOf(List.class.getName()) != -1);
    }
    
    public void testAppendClassName(){
        StringBuffer buf = new StringBuffer();
        generator.appendClassName(List.class.getName());
        generator.addImports(buf);
        assertTrue(buf.toString().indexOf(List.class.getName()) != -1);
    }
    
    public void testMarkImportLocation(){
        StringBuffer buf = new StringBuffer();
        buf.append("hello");
        int length = buf.length();
        generator.markImportLocation(buf);
        generator.appendClass(List.class);
        generator.addImports(buf);
        String content = buf.toString();
        String importEntry = content.substring(length, content.length());
        assertEquals("import java.util.List;", importEntry);
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
