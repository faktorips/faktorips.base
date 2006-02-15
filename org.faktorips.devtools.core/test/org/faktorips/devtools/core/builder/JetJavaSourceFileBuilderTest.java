package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;

public class JetJavaSourceFileBuilderTest extends IpsPluginTest {

    private JetJavaSourceFileBuilder builder;
    private IIpsProject ipsProject;

    public void setUp() throws Exception {
        super.setUp();
        builder = new JetJavaSourceFileBuilder(new TestIpsArtefactBuilderSet(), "test",
                JetJavaSourceFileBuilderTest.TestGenerator.class, IpsObjectType.POLICY_CMPT_TYPE,
                false, "Prefix", "Suffix");
        ipsProject = newIpsProject("TestProject");
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
