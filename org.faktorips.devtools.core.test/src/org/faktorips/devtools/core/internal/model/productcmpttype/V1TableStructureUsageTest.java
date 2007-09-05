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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Joerg Ortmann
 */
public class V1TableStructureUsageTest extends AbstractIpsPluginTest {
    private IIpsProject project;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private ITableStructureUsage tableStructureUsage;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        project = this.newIpsProject("TestProject");
        
        policyCmptType = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
        productCmptType = new ProductCmptType((PolicyCmptType)policyCmptType);
        
        tableStructureUsage = productCmptType.newTableStructureUsage();
        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "test.TableStructure1");
    }
  
    public void testSetRoleName() {
        tableStructureUsage.setRoleName("role100");
        assertEquals("role100", tableStructureUsage.getRoleName());
    }
    
    public void testDelete(){
        ITableStructureUsage tableStructureUsageNew = productCmptType.newTableStructureUsage();
        assertEquals(2, productCmptType.getTableStructureUsages().length);
        tableStructureUsageNew.delete();
        assertEquals(1, productCmptType.getTableStructureUsages().length);
        assertEquals(tableStructureUsage, productCmptType.getTableStructureUsages()[0]);
        tableStructureUsage.delete();
        assertEquals(0, productCmptType.getTableStructureUsages().length);
    }
    
    public void testAddRemoveTableStructure(){
        assertEquals(0, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureA");

        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
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
        assertNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
        
        tableStructureUsage.addTableStructure("test.TableStructureX");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
        
        tableStructureUsage.removeTableStructure("test.TableStructureX");
        tableStructureUsage.addTableStructure("test.TableStructure1");
        ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
    }
    
    public void testValidate_InvalidRoleName() throws CoreException{
        tableStructureUsage.setRoleName("role1");
        MessageList ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
        
        tableStructureUsage.setRoleName("1role");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
        
        tableStructureUsage.setRoleName("role 1");
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
    }
    
    public void testValidate_MustReferenceAtLeast1TableStructure() throws CoreException{
        MessageList ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));
        
        tableStructureUsage.addTableStructure("tableStructure1");
        ml = tableStructureUsage.validate();
        assertNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));
        
    }
    
    public void testvalidate_PolicycmpttypeIsNotConfigurableByProduct() throws CoreException{
        policyCmptType.setConfigurableByProductCmptType(true);
        MessageList ml = tableStructureUsage.validate();
        tableStructureUsage.addTableStructure("tableStructure1");
        assertNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_POLICYCMPTTYPE_IS_NOT_CONFIGURABLE_BY_PRODUCT));
        
        policyCmptType.setConfigurableByProductCmptType(false);
        ml = tableStructureUsage.validate();
        assertNotNull(ml.getMessageByCode(org.faktorips.devtools.core.model.pctype.ITableStructureUsage.MSGCODE_POLICYCMPTTYPE_IS_NOT_CONFIGURABLE_BY_PRODUCT));
    }
    
    public void testGetId(){
        ITableStructureUsage tcu2 = productCmptType.newTableStructureUsage();
        assertFalse(tableStructureUsage.getId() == tcu2.getId());
    }
    
    public void testSetIsMandatoryTableContent() {
        tableStructureUsage.setMandatoryTableContent(false);
        assertFalse(tableStructureUsage.isMandatoryTableContent());
        tableStructureUsage.setMandatoryTableContent(true);
        assertTrue(tableStructureUsage.isMandatoryTableContent());
    }    
}
