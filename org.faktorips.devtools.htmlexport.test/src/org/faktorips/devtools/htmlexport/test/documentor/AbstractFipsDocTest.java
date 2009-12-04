package org.faktorips.devtools.htmlexport.test.documentor;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.Documentor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public abstract class AbstractFipsDocTest extends AbstractIpsPluginTest {

    protected static final String FIPSDOC_GENERIERT_HOME = "/home/dicker/fipsdoc/generiert";
    protected IIpsProject ipsProject;
    protected DocumentorConfiguration documentorConfig;
    protected Documentor documentor;

    public AbstractFipsDocTest() {
        super();
    }

    public AbstractFipsDocTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception  {
        super.setUp();

        ipsProject = newIpsProject("TestProjekt");

        documentorConfig = new DocumentorConfiguration();
        documentorConfig.setPath(FIPSDOC_GENERIERT_HOME);
        documentorConfig.setIpsProject(ipsProject);

        documentor = new Documentor(documentorConfig);
    }

    protected void createStandardProjekt() {
        try {
            newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
            newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
            newPolicyCmptType(ipsProject, "BVB");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected void createMassivProjekt()  {
        try {
            newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
            newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
            newPolicyCmptType(ipsProject, "base.BVB");
            newPolicyCmptType(ipsProject, "base.sub.SubBVB");
            PolicyCmptType newPolicyCmptType = newPolicyCmptType(ipsProject, "kranken.KrankenBVB");
            newPolicyCmptType.setDescription("blablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");
            
            addAttribute(newPolicyCmptType.newPolicyCmptTypeAttribute(), "Attribut 1", "String", Modifier.PUBLISHED);
            addAttribute(newPolicyCmptType.newPolicyCmptTypeAttribute(), "Attribut 2", "Money", Modifier.PUBLIC);
            addAttribute(newPolicyCmptType.newPolicyCmptTypeAttribute(), "Attribut 3", "Integer", Modifier.PUBLIC);
            
            
            newPolicyCmptType(ipsProject, "kranken.sub.KrankenSubBVB");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void addAttribute(IAttribute newAttribute, String name, String datatype, Modifier modifier) {
        newAttribute.setName(name);
        newAttribute.setDatatype(datatype);
        newAttribute.setModifier(modifier);
    }

    protected void deletePreviousGeneratedFiles() {
        File file = new File(documentorConfig.getPath());
        if (file.exists()) {
            file.delete();
        }
    }

}