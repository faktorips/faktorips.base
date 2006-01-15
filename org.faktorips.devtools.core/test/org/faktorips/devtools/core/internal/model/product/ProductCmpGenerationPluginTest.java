package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmpGenerationPluginTest extends IpsPluginTest {

    private IPolicyCmptType pcType;
    private ProductCmpt productCmpt;
    private ProductCmptGeneration generation;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsPack;
    private IIpsSrcFile ipsSrcFile;
    
    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsPack = ipsRootFolder.createPackageFragment("products", true, null);
        ipsSrcFile = ipsPack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
        ipsSrcFile = ipsPack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)ipsSrcFile.getIpsObject();
        productCmpt.setPolicyCmptType(pcType.getQualifiedName());
        generation = (ProductCmptGeneration)productCmpt.createNewGeneration(0);
    }
    
    public void testGetJavaType() throws CoreException {
        // generation contains no formula element => IType is the general TestPolicyPkImpl.
        IType type = generation.getJavaType(IProductCmptGeneration.JAVA_IMPLEMENTATION_TYPE);
        assertNotNull(type);
        assertEquals("TestPolicyPkImpl", type.getElementName());
        
        // generation contains a formula element => IType is a special subclass for this generation only.
        generation.newConfigElement().setType(ConfigElementType.FORMULA);
        type = generation.getJavaType(IProductCmptGeneration.JAVA_IMPLEMENTATION_TYPE);
        assertNotNull(type);
        assertEquals("TestProduct", type.getElementName());
        
        try {
            type = generation.getJavaType(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetAllJavaTypes() throws CoreException {
        IType[] types = generation.getAllJavaTypes();
        assertEquals(1, types.length);
    }
}
