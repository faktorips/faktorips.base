package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;


/**
 *
 */
public class ParameterIdentifierResolverTest extends IpsPluginTest {

    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IAttribute attribute;
    private ParameterIdentifierResolver resolver;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newAttribute();
        attribute.setName("tax");
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        resolver = new ParameterIdentifierResolver(pdProject);
    }
    

    public void testCompile() throws CoreException {
        Locale locale = Locale.getDefault();
        
        // no parameter registered => undefined identifier
        CompilationResult result = resolver.compile("identifier", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        // parameter with a value datatype
        Parameter p1 = new Parameter(0, "rate", Datatype.MONEY.getQualifiedName());
        resolver.add(p1);
        result = resolver.compile("rate", locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.MONEY, result.getDatatype());
        assertEquals("rate", result.getCodeFragment().getSourcecode());
        
        // parameter with the datatype being a policy component type
        // => resolver can resolve identifiers with form paramName.attributeName
        //    with attributeName is the name of one of the type's attributes
        Parameter p2 = new Parameter(1, "policy", pcType.getQualifiedName());
        resolver.add(p2);
        result = resolver.compile("policy.tax", locale);
        assertTrue(result.successfull());
        assertEquals(Datatype.DECIMAL, result.getDatatype());
        String expected = "policy." + attribute.getJavaMethod(IAttribute.JAVA_GETTER_METHOD_IMPLEMENATION).getElementName() + "()";
        assertEquals(expected, result.getCodeFragment().getSourcecode());
        
        // unkown parameter
        result = resolver.compile("unkownParameter", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        // parameter with unkown datatype
        Parameter p3 = new Parameter(0, "p3", "UnkownDatatype");
        resolver.add(p3);
        result = resolver.compile("p3", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        // unkown attribute
        result = resolver.compile("policy.unkownAttribute", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        // attribute with unkown datatype
        attribute.setDatatype("UnknownDatatype");
        result = resolver.compile("policy.tax", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        // no attributename given
        result = resolver.compile("policy.", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        // unkown policy component type
        result = resolver.compile("unkownType.tax", locale);
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().getNoOfMessages());
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, result.getMessages().getMessage(0).getCode());
        
        
        
    }

}
