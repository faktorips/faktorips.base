/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TableStructureUsagePcTypeTest extends AbstractIpsPluginTest {
    private IIpsProject project;
    private IPolicyCmptType pcType;
    private ITableStructureUsage tableStructureUsage;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        project = this.newIpsProject("TestProject");
        
        pcType = newPolicyCmptType(project, "test.policyCmptType");
        tableStructureUsage = pcType.newTableStructureUsage();
        pcType.getIpsSrcFile().save(true, null);
        
        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "test.TableStructure1");
    }
    
    public void testRemove() {
        tableStructureUsage.delete();
        assertEquals(0, pcType.getTableStructureUsages().length);
        assertTrue(pcType.getIpsSrcFile().isDirty());
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "TableStructureUsage", 0);
        tableStructureUsage.initFromXml(paramEl);
        assertEquals("role1", tableStructureUsage.getRoleName());
        assertEquals(3, tableStructureUsage.getTableStructures().length);
        for (int i = 0; i < 3; i++) {
            assertEquals("tableStructure"+(i+1), tableStructureUsage.getTableStructures()[i]);
        }
    }
    
    public void testToXml(){
        tableStructureUsage.setRoleName("roleA");
        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
        Element element = tableStructureUsage.toXml(this.newDocument());
        
        ITableStructureUsage copy = new TableStructureUsage();
        copy.initFromXml(element);
        
        assertEquals("roleA", copy.getRoleName());
        assertEquals(2, copy.getTableStructures().length);
        assertEquals("tableStructureA", copy.getTableStructures()[0]);
        assertEquals("tableStructureB", copy.getTableStructures()[1]);
    }
    
    public void testSetRoleName() {
        tableStructureUsage.setRoleName("role100");
        assertEquals("role100", tableStructureUsage.getRoleName());
        assertTrue(pcType.getIpsSrcFile().isDirty());
    }
    
    public void testAddRemoveTableStructure(){
        assertEquals(0, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureA");
        assertFalse(pcType.getIpsSrcFile().isDirty());

        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
        assertTrue(pcType.getIpsSrcFile().isDirty());
        assertEquals(2, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureC");
        assertEquals(2, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureA");
        assertEquals(1, tableStructureUsage.getTableStructures().length);
        assertEquals("tableStructureB", tableStructureUsage.getTableStructures()[0]);
        tableStructureUsage.removeTableStructure("tableStructureB");
        assertEquals(0, tableStructureUsage.getTableStructures().length);
    }
    
    public void testValidate_TableStructureNotFound() throws CoreException{
        MessageList ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
        
        tableStructureUsage.addTableStructure("test.TableStructureX");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
        
        tableStructureUsage.removeTableStructure("test.TableStructureX");
        tableStructureUsage.addTableStructure("test.TableStructure1");
        ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
    }
    
    public void testValidate_InvalidRoleName() throws CoreException{
        tableStructureUsage.setRoleName("role1");
        MessageList ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
        
        tableStructureUsage.setRoleName("1role");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
        
        tableStructureUsage.setRoleName("role 1");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
    }
    
    public void testValidate_MustReferenceAtLeast1TableStructure() throws CoreException{
        MessageList ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));
        
        tableStructureUsage.addTableStructure("tableStructure1");
        ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));
        
    }
    
    public void testValidate_SameRolename() throws CoreException{
        tableStructureUsage.setRoleName("role1");
        MessageList ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_SAME_ROLENAME));
        
        pcType.newTableStructureUsage().setRoleName("role1");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_SAME_ROLENAME));
        
        tableStructureUsage.setRoleName("roleA");
        ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_SAME_ROLENAME));
        
        // check for same role names in one of the supertype of the policy cmpt
        IPolicyCmptType pcTypeSuper = newPolicyCmptType(project, "test.policyCmptTypeSuper");
        pcType.setSupertype(pcTypeSuper.getQualifiedName());
        pcTypeSuper.newTableStructureUsage().setRoleName("roleA");
        
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_SAME_ROLENAME));
    }
}
