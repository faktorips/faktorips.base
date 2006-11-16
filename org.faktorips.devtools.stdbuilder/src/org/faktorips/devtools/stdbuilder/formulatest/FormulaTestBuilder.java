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

package org.faktorips.devtools.stdbuilder.formulatest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A builder that extracts formula test cases from product components and generates the
 * corresponding runtime xml to instantiate such formula tests.
 * 
 * @author Joerg Ortmann
 */
public class FormulaTestBuilder extends AbstractArtefactBuilder {
    private static final String RUNTIME_EXTENSION = "_formulaTest";

    public FormulaTestBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "FormulaTestBuilder";
    }

    /**
     * {@inheritDoc}
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (!productCmpt.isValid()){
            return;
        }
        if (!productCmpt.containsFormulaTest()){
            // build formula test if at least one formula and formula test is specified
            return;
        }
        
        // there is at least one formula in the product cmt, therefore build the formula test
        InputStream is = null;
        String content;
        try {
            Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
            Element element = addFormulaTestCases(doc, productCmpt);
            String encoding = ipsSrcFile.getIpsProject()==null?"UTF-8":productCmpt.getIpsProject().getXmlFileCharset(); //$NON-NLS-1$
            content = XmlUtil.nodeToString(element, encoding);
            is = convertContentAsStream(content, ipsSrcFile.getIpsProject().getProject().getDefaultCharset());
        } catch (TransformerException e) {
            throw new RuntimeException(e); 
            // This is a programing error, rethrow as runtime exception
        }

        IFile file = getXmlContentFile(ipsSrcFile);
        IFolder folder = (IFolder)file.getParent();
        if (!folder.exists()) {
            createFolder(folder);
        }
        if (!file.exists()) {
            file.create(is, true, null);
        }else{
            IFile copy = getXmlContentFile(ipsSrcFile);
            String charSet = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
            String currentContent = getContentAsString(copy.getContents(), charSet);
            if(content.equals(currentContent)){
                return;
            }            
            file.setContents(is, true, true, null);
        }
    }

    /*
     * Creates the root formula test node
     */
    private Element addFormulaTestCases(Document doc, IProductCmpt productCmpt) throws CoreException {
        Element formulaTestElem = doc.createElement("FormulaTest");
        Element inputElem = doc.createElement("Input");
        formulaTestElem.appendChild(inputElem);
        
        Element productCmptElem = doc.createElement("ProductCmpt");
        productCmptElem.setAttribute("runtimeId", productCmpt.getRuntimeId());
        inputElem.appendChild(productCmptElem);

        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
        for (int i = 0; i < generations.length; i++) {
            if (!generations[i].isValid()){
                continue;
            }
            addFormulaTestForGeneration(doc, productCmptElem, (IProductCmptGeneration) generations[i]);
        }
        return formulaTestElem;
    }

    /*
     * Creates nodes for generation
     */
    private void addFormulaTestForGeneration(Document doc, Element parent, IProductCmptGeneration generation) throws CoreException {
        IConfigElement[] formulas = generation.getConfigElements(ConfigElementType.FORMULA);
        if (formulas.length == 0){
            return;
        }
        
        Element generationElem = doc.createElement("Generation");
        parent.appendChild(generationElem);
        generationElem.setAttribute("validFrom", XmlUtil.gregorianCalendarToXmlDateString(generation.getValidFrom()));
        for (int i = 0; i < formulas.length; i++) {
            if (!formulas[i].isValid()){
                return;
            }
            addFormula(doc, generationElem, formulas[i]);
        }
    }
    
    /*
     * Creates nodes for each config elment of type formula
     */
    private void addFormula(Document doc, Element generationElem, IConfigElement element) throws CoreException {
        Element formulaElem = doc.createElement("Formula");
        generationElem.appendChild(formulaElem);
        formulaElem.setAttribute("formulaName", element.getPcTypeAttribute());
        IFormulaTestCase[] ftcs = element.getFormulaTestCases();
        for (int i = 0; i < ftcs.length; i++) {
            if (!ftcs[i].isValid()){
                return;
            }
            addFormulaTestCase(doc, formulaElem, ftcs[i]);
        }
    }

    /*
     * Creates nodes for each formula test case
     */
    private void addFormulaTestCase(Document doc, Element formulaElem, IFormulaTestCase formulaTestCase) throws CoreException {
        Element formulaTestCaseElem = doc.createElement("FormulaTestCase");
        formulaElem.appendChild(formulaTestCaseElem);
        formulaTestCaseElem.setAttribute("name", formulaTestCase.getName());
        ValueToXmlHelper.addValueToElement(formulaTestCase.getExpectedResult(), formulaTestCaseElem, "ExpectedResult");
        IFormulaTestInputValue[] ftivs = formulaTestCase.getFormulaTestInputValues();
        
        for (int i = 0; i < ftivs.length; i++) {
            Element formulaTestInputValueElem = doc.createElement("FormulaTestInputValue");
            formulaTestCaseElem.appendChild(formulaTestInputValueElem);
            formulaTestInputValueElem.setAttribute("identifier", ftivs[i].getIdentifier());
            ValueToXmlHelper.addValueToElement(ftivs[i].getValue(), formulaTestInputValueElem, "Value");
            Datatype datatype = ftivs[i].findDatatypeOfFormulaParameter();
            if (datatype != null){
                formulaTestInputValueElem.setAttribute("datatype", datatype.getJavaClassName());
            }
            Parameter formulaParam = ftivs[i].findFormulaParameter();
            if (formulaParam != null){
                formulaTestInputValueElem.setAttribute("parameterIndex", ""+formulaParam.getIndex());
            }
        }
    }

    /**
     * Returns all formula config elements of the given product cmpt or null if the product cmpt has no formulas
     * or no generation exists.
     */
    public IConfigElement[] getFormulas(IProductCmpt productCmpt) throws CoreException{
        IIpsObjectGeneration[] productCmptGenerations = productCmpt.getGenerations();
        if (productCmptGenerations.length == 0){
            return null;
        } else {
            List formulaList = new ArrayList();
            for (int i = 0; i < productCmptGenerations.length; i++) {
                IConfigElement[] formulas = ((IProductCmptGeneration)productCmptGenerations[i]).getConfigElements(ConfigElementType.FORMULA);
                if (formulas.length > 0){
                    formulaList.addAll(Arrays.asList(formulas));
                }
            }
            if (formulaList.size() == 0){
                return null;
            } else {
                return (IConfigElement[]) formulaList.toArray(new IConfigElement[formulaList.size()]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)){
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = getXmlContentFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(true, null);
        }
    }

    /*
     * Converts the given string content as ByteArrayInputStream.
     */
    private ByteArrayInputStream convertContentAsStream(String content, String charSet) throws CoreException{
        try {
            return new ByteArrayInputStream(content.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /*
     * Creates a folder if not existes.
     */
    private void createFolder(IFolder folder) throws CoreException {
        while (!folder.getParent().exists()) {
            createFolder((IFolder)folder.getParent());
        }
        folder.create(true, true, null);
    }   
    
    /*
     * Returns the file resource of the given ips source file.
     */
    private IFile getXmlContentFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        IFolder folder = getXmlContentFileFolder(ipsSrcFile);
        return folder.getFile(getFileName(StringUtil.getFilenameWithoutExtension(file.getName())) + ".xml");
    }

    /**
     * Returns the path to the xml resource as used by the Class.getResourceAsStream() Method.
     * 
     * @see Class#getResourceAsStream(java.lang.String) 
     */
    public String getXmlResourcePath(IProductCmpt productCmpt) throws CoreException {
        String internalPackage = getBuilderSet().getPackage(DefaultBuilderSet.KIND_FORMULA_TEST_XML,
                productCmpt.getIpsSrcFile());
        return internalPackage.replace('.', '/') + "/" + getFileName(productCmpt.getName()) + ".xml";
    }
    
    /*
     * Returns the qualified name of the formula test stored on the product cmpt
     */
    public String getFormulaTestQualifiedName(IProductCmpt productCmpt) throws CoreException{
        return productCmpt.getQualifiedName();
    }
    
    private String getFileName(String name){
        return name + RUNTIME_EXTENSION;
    }
    
    /*
     * Returns the package folder for the given ips sourcefile.
     */
    private IFolder getXmlContentFileFolder(IIpsSrcFile ipsSrcFile) throws CoreException {
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_FORMULA_TEST_XML, ipsSrcFile);
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination().getFolder(
            pathToPack);
    }
    
    private String getContentAsString(InputStream is, String charSet) throws CoreException{
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }     
}
