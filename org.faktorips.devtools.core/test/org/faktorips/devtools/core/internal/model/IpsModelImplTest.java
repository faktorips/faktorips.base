package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.DefaultEnumTypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumType;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsModelImplTest extends PluginTest {

    private IIpsProject ipsProject;
    private IpsModel model;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        model = (IpsModel)ipsProject.getIpsModel();
    }

    public void testGetIpsObjectPath() throws CoreException{
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForGeneratedJavaClasses("newpackage");
        ipsProject.setIpsObjectPath(path);
        
        //path is created in the first call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());

        //path is read from the cache in the second call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());

        IIpsProject secondProject = newIpsProject("TestProject2");
        IIpsObjectPath secondPath = secondProject.getIpsObjectPath();
        secondPath.getSourceFolderEntries()[0].setSpecificBasePackageNameForGeneratedJavaClasses("secondpackage");
        secondProject.setIpsObjectPath(secondPath);
        
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());
        assertEquals("secondpackage", secondPath.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());
    }
    
    
    public void testGetValueDatatypes() throws CoreException, IOException {
        updateDatatypesDefinition();
        addFaktorIpsCommonJarToIpsProject();        
        addPaymentModeClassToIpsProject();
        SortedSet datatypes = new TreeSet();
        model.getValueDatatypes(ipsProject, datatypes);
        assertEquals(2, datatypes.size());

        Iterator it = datatypes.iterator();
        assertEquals(Datatype.DECIMAL, it.next());
        EnumType paymentMode = (EnumType)it.next();
        assertEquals("PaymentMode", paymentMode.getName());
    }

    public void testGetDatatypeHelpers() throws IOException, CoreException {
        updateDatatypesDefinition();
        addFaktorIpsCommonJarToIpsProject();        
        addPaymentModeClassToIpsProject();
        DatatypeHelper helper = model.getDatatypeHelper(ipsProject, Datatype.DECIMAL);
        assertEquals(DecimalHelper.class, helper.getClass());
        
        ValueDatatype paymentMode = (ValueDatatype)ipsProject.findDatatype("PaymentMode");
        assertNotNull(paymentMode);
        helper = model.getDatatypeHelper(ipsProject, paymentMode);
        assertEquals(DefaultEnumTypeHelper.class, helper.getClass());
        
        assertNull(model.getDatatypeHelper(ipsProject, Datatype.MONEY));
    }
    
    private void updateDatatypesDefinition() throws CoreException, IOException {
	    IFile typeFile = ipsProject.getDatatypesDefinitionFile();
	    String contents = 
	        "<?xml version=\"1.0\"?>" + 
	        "<DatatypesDefinition>" + 
	        	"<Datatype id=\"Decimal\"/>" + 
	        	"<Datatype class=\"org.PaymentMode\" helperClass=\"org.faktorips.codegen.dthelpers.DefaultEnumTypeHelper\"" +
	    			" valueOfMethod=\"getPaymentMode\" getEnumTypeMethod=\"getEnumType\" />" + 	
	        "</DatatypesDefinition>";
	    ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes());
	    typeFile.setContents(is, true, false, null);
	    is.close();
    }
    
    /*
     * Adds the PaymentMode.java file defining an enum type PaymentMode to the ips project.
     * This is done by copying the PaymentMode.txt file.
     */
    private void addPaymentModeClassToIpsProject() throws CoreException, IOException {
        IFolder sourceFolder = ipsProject.getProject().getFolder("src");
        IFolder orgFolder = sourceFolder.getFolder("org");
        orgFolder.create(true, true, null);
        IFile srcFile = orgFolder.getFile("PaymentMode.java");
        InputStream is = this.getClass().getResourceAsStream("PaymentMode.txt");
        srcFile.create(is, true, null);
        is.close();
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }
    
    private void addFaktorIpsCommonJarToIpsProject() throws IOException, CoreException {
        IFolder libFolder = ipsProject.getProject().getFolder("lib");
        libFolder.create(true, true, null);
        IFile jarFile = libFolder.getFile("faktorips-common.jar");
        InputStream is = this.getClass().getResourceAsStream("faktorips-common.jar");
        jarFile.create(is, true, null);
        is.close();
        
        IClasspathEntry[] entries = ipsProject.getJavaProject().getRawClasspath();
        IClasspathEntry libEntry = JavaCore.newLibraryEntry(jarFile.getFullPath(), null, null);
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length+1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length-1] = libEntry;
        ipsProject.getJavaProject().setRawClasspath(newEntries, null);
    }

}
