package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class StandardBuilderSetTest extends IpsPluginTest {

    private IIpsProject project;
    private IPolicyCmptType type;
    
    /*
     * @see IpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        type = newPolicyCmptType(project, "Policy");
        IAttribute a = type.newAttribute();
        a.setAttributeType(AttributeType.COMPUTED);
        a.setDatatype(Datatype.INTEGER.getQualifiedName());
        a.setName("age");
        a.setProductRelevant(true);
        assertFalse(type.validate().containsErrorMsg());
        type.getIpsSrcFile().save(true, null);
    }
    
    public void testDeleteProductRelevantPolicyCmptType() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        // this should not throw an exception (this has once been a bug)
        type.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

}
