package org.faktorips.devtools.stdbuilder.productcmpt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;

public class ProductCmptXmlFileBuilderTest extends PluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IProductCmpt productCmpt;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("pnc.motor", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "MotorProduct2005", true, null);
        productCmpt = (IProductCmpt)srcFile.getIpsObject();
        productCmpt.setDescription("blabla");
        srcFile.save(true, null);
    }    
    
    /**
     * Tests if the product components xml file is copied.
     */
    public void testProductCmptXmlFile() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        IFolder outputFolder = entry.getOutputFolderForGeneratedJavaFiles();
        IPackageFragment pack = productCmpt.getIpsPackageFragment().getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION);
        IFolder folder = outputFolder.getFolder(new Path(pack.getElementName().replace('.', '/')));
        assertTrue(folder.exists());
        IFile file = folder.getFile("MotorProduct2005.xml");
        assertTrue(file.exists());
    }
    
    /**
     * Tests if the product component registry's toc file is written.
     */
    public void testTocFile() throws CoreException {
        srcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy", true, null);
        IPolicyCmptType pcType = (IPolicyCmptType)srcFile.getIpsObject(); 
        productCmpt.setPolicyCmptType(pcType.getQualifiedName());
        productCmpt.newGeneration();
        productCmpt.getIpsSrcFile().save(true, null);
        
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        IFile tocFile = ((IpsPackageFragmentRoot)entry.getIpsPackageFragmentRoot(ipsProject)).getTocFileInOutputFolder();
        assertTrue(tocFile.exists());
        
        // if the product component refers to a none existing policy component type, builder should not crash
        productCmpt.setPolicyCmptType("notExistingPcType");
        productCmpt.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }
   
}