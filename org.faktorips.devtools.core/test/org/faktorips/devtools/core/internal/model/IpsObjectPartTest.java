package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;


/**
 *
 */
public class IpsObjectPartTest extends IpsObjectTestCase {
    
    private IIpsObject pdObject;
    private IIpsObjectPart part;
    private IIpsObjectPart subpart;

    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.PRODUCT_CMPT);
    }
    
    protected void createObjectAndPart() {
        IProductCmpt productCmpt = new ProductCmpt(pdSrcFile);
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(); 
        pdObject = productCmpt;
        part = generation;  
        subpart = generation.newConfigElement();
    }
    

    public void testGetPdObject() {
        assertEquals(pdObject, part.getIpsObject());
        assertEquals(pdObject, subpart.getIpsObject());
    }

    public void testSetDescription() {
        part.setDescription("newDescription");
        assertEquals("newDescription", part.getDescription());
        assertTrue(pdSrcFile.isDirty());
    }

}
