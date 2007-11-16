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

package org.faktorips.devtools.stdbuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.formulatest.FormulaTestBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.TocEntryObject;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TocFileBuilder extends AbstractArtefactBuilder {

    // a map contains the modification stamp for each ips package fragment root's runtime
    // repository table of contents.
    private Map packFrgmtRootTocModStamps = new HashMap();
    
	// a map that contains the table of contents ojects (value) for each table of contents file.
	private Map tocFileMap = new HashMap();

	// required builders
    private ProductCmptImplClassBuilder productCmptTypeImplClassBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;
    private ProductCmptBuilder productCmptBuilder;
    private TableImplBuilder tableImplClassBuilder;
    private TestCaseTypeClassBuilder testCaseTypeClassBuilder;
    private TestCaseBuilder testCaseBuilder;
    private FormulaTestBuilder formulaTestBuilder;
    
    public TocFileBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }

    public void setProductCmptTypeImplClassBuilder(ProductCmptImplClassBuilder builder) {
        this.productCmptTypeImplClassBuilder = builder;
    }
    
    public void setProductCmptGenImplClassBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplClassBuilder = builder;
    }

    public void setProductCmptBuilder(ProductCmptBuilder builder) {
        productCmptBuilder = builder;
    }
    
    public void setTableImplBuilder(TableImplBuilder builder) {
        this.tableImplClassBuilder = builder;
    }

    public void setTestCaseTypeClassBuilder(TestCaseTypeClassBuilder testCaseTypeClassBuilder) {
        this.testCaseTypeClassBuilder = testCaseTypeClassBuilder;
    }
    
    public void setTestCaseBuilder(TestCaseBuilder testCaseBuilder) {
        this.testCaseBuilder = testCaseBuilder;
    }


    public void setFormulaTestBuilder(FormulaTestBuilder formulaTestBuilder) {
        this.formulaTestBuilder = formulaTestBuilder;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "TocFileBuilder"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        IpsObjectType type = ipsSrcFile.getIpsObjectType();
        return IpsObjectType.PRODUCT_CMPT.equals(type)
                || IpsObjectType.TABLE_CONTENTS.equals(type)
                || IpsObjectType.TEST_CASE.equals(type);
    }
    
    /**
     * The toc file builder has to remember the modifcation stamp for each toc before
     * the build process starts.
     * 
     * {@inheritDoc}
     */
    public void beforeBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
    	if (buildKind==IncrementalProjectBuilder.FULL_BUILD) {
    		tocFileMap = new HashMap();
    	}
    	IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < srcRoots.length; i++) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoots[i];
            if (buildKind==IncrementalProjectBuilder.FULL_BUILD) {
                getToc(root).clear();
            }
            long modStamp = getToc(root).getModificationStamp();
            packFrgmtRootTocModStamps.put(root, new Long(modStamp));
            // next lines are a workaround for a bug in PDE
            // if we create the folder in afterBuildProcess, it is marked in the MANIFEST section
            // for exported packages as not existing (but it's there).
            IFile tocFile = getBuilderSet().getRuntimeRepositoryTocFile(root);
            if (tocFile==null) {
                continue;
            }
            createFolderIfNotThere((IFolder)tocFile.getParent());
        }
    }

    /**
     * Saves the tocs that have been modified during the build. 
     * 
     * {@inheritDoc}
     */
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < srcRoots.length; i++) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoots[i];
            Long oldModStamp = (Long)packFrgmtRootTocModStamps.get(root);
            if (oldModStamp.longValue() != getToc(root).getModificationStamp()) {
                saveToc(root);
            }
        }
    }
    
	/**
	 * Saves the repository's table of contents to a file. The table of contents file is needed
	 * by the FaktorIPS runtime to load the product components and table data.
	 * 
	 * @throws CoreException if an error occurs while writing the toc to the file.
	 */
	private void saveToc(IIpsPackageFragmentRoot root) throws CoreException {
        IFile tocFile = getBuilderSet().getRuntimeRepositoryTocFile(root);
        if (tocFile==null) {
            return;
        }
        String encoding = root.getIpsProject().getXmlFileCharset();
        if (encoding==null) {
            return;
        }
        String xml = null;
        try {
            Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
            Element tocElement = getToc(root).toXml(doc);
            doc.appendChild(tocElement);
            xml = XmlUtil.nodeToString(doc, encoding);
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus("Error transforming product component registry's table of contents to xml.", e)); //$NON-NLS-1$
        }
        
        boolean newlyCreated = createFileIfNotThere(tocFile);
        if (!newlyCreated) {
            replaceTocFileIfContentHasChanged(root.getIpsProject(), tocFile, xml);
        } else {
            try {
                tocFile.setContents(new ByteArrayInputStream(xml.getBytes(encoding)), true, true, null);
            } catch (UnsupportedEncodingException e1) {
                throw new CoreException(new IpsStatus(e1));
            }
        }
	}
    
    private void replaceTocFileIfContentHasChanged(IIpsProject ipsProject, IFile tocFile, String newContents) throws CoreException {
        String oldContents = null;
        String charset = ipsProject.getXmlFileCharset();
        try {
            oldContents = StringUtil.readFromInputStream(tocFile.getContents(), charset);
        } catch (Exception e) {
            // if an error occurs reading the old contents, we just write the new one
            // e.g. an error can occur if the toc file isn't synchronized 
        }
        if (newContents.equals(oldContents)) {
            return;
        }
        InputStream is;
        try {
            is = new ByteArrayInputStream(newContents.getBytes(charset));
        } catch (UnsupportedEncodingException e1) {
            throw new CoreException(new IpsStatus(e1));
        }
        tocFile.setContents(is, true, true, null);
    }
    
    private MutableClRuntimeRepositoryToc getToc(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsPackageFragmentRoot root = ipsSrcFile.getIpsObject().getIpsPackageFragment().getRoot();
        return getToc(root);
    }

	/**
	 * Returns the product component registry's table of contents for the
	 * indicated ips package fragment root.
	 * 
	 * @throws CoreException
	 *             if an error occurs while accessing the toc file.
	 */
	public MutableClRuntimeRepositoryToc getToc(IIpsPackageFragmentRoot root) throws CoreException {
		IIpsArtefactBuilderSet builderSet = root.getIpsProject().getIpsArtefactBuilderSet();
		IFile tocFile = builderSet.getRuntimeRepositoryTocFile(root);
		MutableClRuntimeRepositoryToc toc = (MutableClRuntimeRepositoryToc) tocFileMap.get(tocFile);
		if (toc == null) {
			toc = new MutableClRuntimeRepositoryToc();
			if (tocFile!=null && tocFile.exists()) {
				InputStream is = tocFile.getContents(true);
				Document doc;
				try {
					DocumentBuilder builder = IpsPlugin.getDefault()
							.newDocumentBuilder();
					doc = builder.parse(is);
				} catch (Exception e) {
				    // can happen if the file is deleted in the filesystem, but the workspace has not been synchronized
                    // nothing seriuos, we just write the file again
                    doc = null; 
                    tocFile.refreshLocal(1, null);
				}
                if (doc!=null) {
                    Element tocEl = doc.getDocumentElement();
                    try {
                        toc.initFromXml(tocEl);
                    } catch (Exception e) {
                        throw new CoreException(new IpsStatus(
                                "Error initializing toc from xml!", e)); //$NON-NLS-1$
                    }
                }
			}
			tocFileMap.put(tocFile, toc);
		}
		return toc;
	}

    /**
     * {@inheritDoc}
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsObject object = null;
        try {
            TocEntryObject entry;
            object = ipsSrcFile.getIpsObject();
            if (object.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
                // add entry for formula test, the formula test depends on the product cmpt 
                // source file, thus it will be implicit created or deleted here
                entry = createFormulaTestTocEntry((IProductCmpt)object);
                if (entry != null){
                    // a new formula toc entry was created
                    getToc(ipsSrcFile).addOrReplaceTocEntry(entry);
                } else {
                    // no entry for formula tests created,
                    // delete previous created toc entries for formula tests,
                    // this is the case e.g. if the type of the config element changed to a
                    // non formula type
                    removeFormulaTestEntry(ipsSrcFile);
                }
                
                // add entry for product cmpt
                entry = createTocEntry((IProductCmpt)object);
            } else if (object.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
                entry = createTocEntry((ITableContents)object);
            } else if (object.getIpsObjectType().equals(IpsObjectType.TEST_CASE)) {
                entry = createTocEntry((ITestCase)object);
            }
            else {
                throw new RuntimeException("Unknown ips object type " + object.getIpsObjectType()); //$NON-NLS-1$
            }
            if (entry != null){
                getToc(ipsSrcFile).addOrReplaceTocEntry(entry);
            } else {
                // no toc entry has been newly created, remove the previous toc entry 
                getToc(ipsSrcFile).removeEntry(object.getQualifiedName());
            }
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Unable to update the runtime repository toc file with the entry for: " //$NON-NLS-1$
                            + object.getQualifiedName(), e));
        }
    }

    public TocEntryObject createTocEntry(IProductCmpt productCmpt) throws CoreException {
        if (productCmpt.getNumOfGenerations() == 0) {
            return null;
        }
        IProductCmptType pcType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        if (pcType == null) {
            return null;
        }
        IProductCmptKind kind = productCmpt.findProductCmptKind();
        if (kind==null) {
            return null;
        }
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_PRODUCT_CMPT_TOCENTRY, productCmpt.getIpsSrcFile()).replace('.', '/');
        String xmlResourceName = packageString + '/' + productCmpt.getName() + ".xml"; //$NON-NLS-1$
        DateTime validTo = DateTime.createDateOnly(productCmpt.getValidTo());
        TocEntryObject entry = TocEntryObject.createProductCmptTocEntry(
            productCmpt.getRuntimeId(), 
            productCmpt.getQualifiedName(),
            productCmpt.findProductCmptKind().getRuntimeId(),
            productCmpt.getVersionId(),
            xmlResourceName, 
            productCmptTypeImplClassBuilder.getQualifiedClassName(pcType.getIpsSrcFile()),
            validTo);
        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
        TocEntryGeneration[] genEntries = new TocEntryGeneration[generations.length];
        for (int i = 0; i < generations.length; i++) {
            DateTime validFrom = DateTime.createDateOnly(generations[i].getValidFrom());
            IProductCmptGeneration gen = (IProductCmptGeneration)generations[i];
            String generationClassName;
            if (gen.getProductCmpt().containsFormula()) {
                generationClassName = productCmptBuilder.getQualifiedClassName((IProductCmptGeneration)generations[i]);
            } else {
                generationClassName = productCmptGenImplClassBuilder.getQualifiedClassName(pcType);
            }
            genEntries[i] = new TocEntryGeneration(entry, validFrom, generationClassName, xmlResourceName); 
        }
        entry.setGenerationEntries(genEntries);
        return entry;
    }

    /**
     * Creates a toc entry for the given formula test.
     */
    private TocEntryObject createFormulaTestTocEntry(IProductCmpt productCmpt) throws CoreException {
        if (!productCmpt.containsFormulaTest()){
            // only build toc entry if at least one formula is specified
            return null;
        }
        
        // generate the object id, the objectId for this element will be the package root name
        // concatenated with the qualified name
        String packageRootName = productCmpt.getIpsPackageFragment().getRoot().getName();
        String objectId = packageRootName + "." + productCmpt.getQualifiedName(); //$NON-NLS-1$
        objectId = objectId.replace('.', '/') + "." + IpsObjectType.PRODUCT_CMPT.getFileExtension(); //$NON-NLS-1$
        
        String formulaTestCaseName = formulaTestBuilder.getQualifiedClassName(productCmpt);
        TocEntryObject entry = TocEntryObject.createTestCaseTocEntry(objectId, productCmpt.getQualifiedName(), "",
                formulaTestCaseName);

        return entry;
    }
    
    /*
     * Removes the toc file and deletes the runtime content of the formula test.
     * Do nothing if the given scr file doesn't contains a formula test case.
     */
    private void removeFormulaTestEntry(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (getToc(ipsSrcFile).getTestCaseTocEntryByQName(ipsObject.getQualifiedName()) != null){
            getToc(ipsSrcFile).removeEntry(ipsObject.getQualifiedName());
            formulaTestBuilder.delete(ipsSrcFile);
        }
    }
    
    public TocEntryObject createTocEntry(ITableContents tableContents) throws CoreException {
        ITableStructure tableStructure = tableContents.findTableStructure(getIpsProject());
        if (tableStructure == null) {
            return null;
        }
        if (tableStructure.isModelEnumType()){
            // table defines an enum are not created in the toc
            return null;
        }
        String packageInternal = getBuilderSet().getPackage(DefaultBuilderSet.KIND_TABLE_TOCENTRY,
            tableContents.getIpsSrcFile());
        String tableStructureName = tableImplClassBuilder.getQualifiedClassName(tableStructure
                .getIpsSrcFile());
        String xmlResourceName = packageInternal.replace('.', '/') + '/' + tableContents.getName()
                + ".xml"; //$NON-NLS-1$
        TocEntryObject entry = TocEntryObject.createTableTocEntry(tableContents.getQualifiedName(), tableContents.getQualifiedName(),
            xmlResourceName, tableStructureName);
        return entry;
    }

    /**
     * Creates a toc entry for the given test case.
     */
    public TocEntryObject createTocEntry(ITestCase testCase) throws CoreException {
        ITestCaseType type = testCase.findTestCaseType(getIpsProject());
        if (type == null) {
            return null;
        }
        // generate the object id, the objectId for this element will be the package root name concatenated with the qualified name 
        String packageRootName = testCase.getIpsPackageFragment().getRoot().getName();
        String objectId = packageRootName + "." + testCase.getQualifiedName(); //$NON-NLS-1$
        objectId = objectId.replace('.', '/') + "." + IpsObjectType.TEST_CASE.getFileExtension(); //$NON-NLS-1$
        
        String testCaseTypeName = testCaseTypeClassBuilder.getQualifiedClassName(type);
        String xmlResourceName = testCaseBuilder.getXmlResourcePath(testCase);
        TocEntryObject entry = TocEntryObject.createTestCaseTocEntry(objectId, testCase.getQualifiedName(),
            xmlResourceName, testCaseTypeName);
        return entry;
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        MutableClRuntimeRepositoryToc toc = getToc(ipsSrcFile.getIpsPackageFragment().getRoot());
        toc.removeEntry(ipsSrcFile.getQualifiedNameType().getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean buildsDerivedArtefacts() {
        return true;
    }
}
