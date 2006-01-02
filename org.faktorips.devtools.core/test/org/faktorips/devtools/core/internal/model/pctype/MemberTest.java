package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IMember;


/**
 *
 */
public class MemberTest extends IpsObjectTestCase {

    private PolicyCmptType pcType;
    private IMember member;

    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.POLICY_CMPT_TYPE);
    }
    
    protected void createObjectAndPart() {
        pcType = new PolicyCmptType(pdSrcFile);
        member = pcType.newAttribute();
    }
    
    public void testSetName() {
        member.setName("premium");
        assertEquals("premium", member.getName());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testSetDescription() {
        member.setDescription("blabla");
        assertEquals("blabla", member.getDescription());
        assertTrue(pdSrcFile.isDirty());
    }
    
}
