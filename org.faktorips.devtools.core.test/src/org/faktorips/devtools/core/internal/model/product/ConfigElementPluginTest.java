/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class ConfigElementPluginTest extends AbstractIpsPluginTest {

    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IPolicyCmptType pcType;
    private IPolicyCmptType supertype;
    
    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject pdProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = pdProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment fragment = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile supertypeFile = fragment.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestSuperPolicy", true, null);
        supertype = (IPolicyCmptType)supertypeFile.getIpsObject();
        
        IIpsSrcFile pcTypeFile = fragment.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (IPolicyCmptType)pcTypeFile.getIpsObject();
        pcType.setSupertype(supertype.getQualifiedName());
        
        IIpsSrcFile productFile = fragment.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)productFile.getIpsObject();
        productCmpt.setPolicyCmptType(pcType.getQualifiedName());
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
    }
    
    public void testFindPcTypeAttribute() throws CoreException {
        IAttribute a1 = pcType.newAttribute();
        a1.setName("a1");
        IAttribute a2 = supertype.newAttribute();
        a2.setName("a2");
        
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce = generation.newConfigElement();
        ce.setPcTypeAttribute("a1");
        assertEquals(a1, ce.findPcTypeAttribute());
        ce.setPcTypeAttribute("a2");
        assertEquals(a2, ce.findPcTypeAttribute());
        ce.setPcTypeAttribute("unkown");
        assertNull(ce.findPcTypeAttribute());
    }
    
    public void testValidate_Formula() throws CoreException {
        // config element based on computed attribute with no parameters
        IAttribute a = pcType.newAttribute();
        a.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        a.setName("premium");
        a.setDatatype("unknown datatype");
        a.setFormulaParameters(new Parameter[0]);
        IConfigElement ce = generation.newConfigElement();
        ce.setType(ConfigElementType.FORMULA);
        ce.setPcTypeAttribute("premium");

        // formula syntax error
        ce.setValue("42EUR12"); 
        MessageList list = ce.validate();
        assertEquals(1, list.getNoOfMessages());
        Message msg = list.getMessage(0);
        assertEquals(ExprCompiler.SYNTAX_ERROR, msg.getCode());
        assertEquals(ce, msg.getInvalidObjectProperties()[0].getObject());
        assertEquals(IConfigElement.PROPERTY_VALUE, msg.getInvalidObjectProperties()[0].getProperty());

        // attribute's datatype can't be resolved
        ce.setValue("42EUR"); // formula is the constant 42EUR
        list = ce.validate();
        assertEquals(1, list.getNoOfMessages());
        msg = list.getMessage(0);
        assertEquals(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_FORMULA, msg.getCode());
        assertEquals(ce, msg.getInvalidObjectProperties()[0].getObject());
        assertEquals(IConfigElement.PROPERTY_VALUE, msg.getInvalidObjectProperties()[0].getProperty());
        
        // datatype mismatch
        a.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        list = ce.validate();
        assertEquals(1, list.getNoOfMessages());
        msg = list.getMessage(0);
        assertEquals(IConfigElement.MSGCODE_WRONG_FORMULA_DATATYPE, msg.getCode());
        assertEquals(ce, msg.getInvalidObjectProperties()[0].getObject());
        assertEquals(IConfigElement.PROPERTY_VALUE, msg.getInvalidObjectProperties()[0].getProperty());
        
        // result datatype same as formula datatype => no error
        a.setDatatype(Datatype.MONEY.getQualifiedName());
        list = ce.validate();
        assertEquals(0, list.getNoOfMessages());
        
        // formula's datatype can be converted to the expected datatype
        a.setDatatype(Datatype.DECIMAL.getQualifiedName());
        ce.setValue("42");
        list = ce.validate();
        assertEquals(0, list.getNoOfMessages());
    }

}
