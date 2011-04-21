/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.formulatest.FormulaTestBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.EnumXmlAdapterTocEntry;
import org.faktorips.runtime.internal.toc.FormulaTestTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.PolicyCmptTypeTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTypeTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TocFileBuilder extends AbstractArtefactBuilder {

    // a map that contains the table of contents objects (value) for each table of contents file.
    private Map<IFile, TableOfContent> tocFileMap = new HashMap<IFile, TableOfContent>();

    // required builders
    private ProductCmptImplClassBuilder productCmptTypeImplClassBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;
    private ProductCmptBuilder productCmptBuilder;
    private TableImplBuilder tableImplClassBuilder;
    private TestCaseTypeClassBuilder testCaseTypeClassBuilder;
    private TestCaseBuilder testCaseBuilder;
    private EnumTypeBuilder enumTypeBuilder;
    private FormulaTestBuilder formulaTestBuilder;
    private ModelTypeXmlBuilder policyModelTypeXmlBuilder;
    private ModelTypeXmlBuilder productModelTypeXmlBuilder;
    private PolicyCmptImplClassBuilder policyCmptImplClassBuilder;
    private EnumXmlAdapterBuilder enumXmlAdapterBuilder;

    private boolean generateEntriesForModelTypes;

    public TocFileBuilder(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    public boolean isGenerateEntriesForModelTypes() {
        return generateEntriesForModelTypes;
    }

    public void setGenerateEntriesForModelTypes(boolean generateEntriesForModelTypes) {
        this.generateEntriesForModelTypes = generateEntriesForModelTypes;
    }

    public void setEnumXmlAdapterBuilder(EnumXmlAdapterBuilder enumXmlAdapterBuilder) {
        this.enumXmlAdapterBuilder = enumXmlAdapterBuilder;
    }

    public void setProductCmptTypeImplClassBuilder(ProductCmptImplClassBuilder builder) {
        productCmptTypeImplClassBuilder = builder;
    }

    public void setProductCmptGenImplClassBuilder(ProductCmptGenImplClassBuilder builder) {
        productCmptGenImplClassBuilder = builder;
    }

    public void setProductCmptBuilder(ProductCmptBuilder builder) {
        productCmptBuilder = builder;
    }

    public void setTableImplBuilder(TableImplBuilder builder) {
        tableImplClassBuilder = builder;
    }

    public void setTestCaseTypeClassBuilder(TestCaseTypeClassBuilder testCaseTypeClassBuilder) {
        this.testCaseTypeClassBuilder = testCaseTypeClassBuilder;
    }

    public void setTestCaseBuilder(TestCaseBuilder testCaseBuilder) {
        this.testCaseBuilder = testCaseBuilder;
    }

    public void setEnumTypeBuilder(EnumTypeBuilder enumTypeBuilder) {
        this.enumTypeBuilder = enumTypeBuilder;
    }

    public void setFormulaTestBuilder(FormulaTestBuilder formulaTestBuilder) {
        this.formulaTestBuilder = formulaTestBuilder;
    }

    public void setPolicyModelTypeXmlBuilder(ModelTypeXmlBuilder policyModelTypeXmlBuilder) {
        this.policyModelTypeXmlBuilder = policyModelTypeXmlBuilder;
    }

    public void setProductModelTypeXmlBuilder(ModelTypeXmlBuilder productModelTypeXmlBuilder) {
        this.productModelTypeXmlBuilder = productModelTypeXmlBuilder;
    }

    public void setPolicyCmptImplClassBuilder(PolicyCmptImplClassBuilder policyCmptImplClassBuilder) {
        this.policyCmptImplClassBuilder = policyCmptImplClassBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "TocFileBuilder"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        IpsObjectType type = ipsSrcFile.getIpsObjectType();
        return IpsObjectType.PRODUCT_CMPT.equals(type) || IpsObjectType.TABLE_CONTENTS.equals(type)
                || IpsObjectType.TEST_CASE.equals(type) || IpsObjectType.ENUM_CONTENT.equals(type)
                || IpsObjectType.ENUM_TYPE.equals(type) || (generateEntriesForModelTypes && type.isEntityType());
    }

    /**
     * The toc file builder has to remember the modifcation stamp for each toc before the build
     * process starts.
     * 
     * {@inheritDoc}
     */
    @Override
    public void beforeBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        if (buildKind == IncrementalProjectBuilder.FULL_BUILD) {
            tocFileMap.clear();
        }
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoot;
            if (buildKind == IncrementalProjectBuilder.FULL_BUILD) {
                getToc(root).clear();
            }
            // next lines are a workaround for a bug in PDE
            // if we create the folder in afterBuildProcess, it is marked in the MANIFEST section
            // for exported packages as not existing (but it's there).
            IFile tocFile = getBuilderSet().getRuntimeRepositoryTocFile(root);
            if (tocFile == null) {
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
    @Override
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoot;
            if (getToc(root).isModified()) {
                saveToc(root);
            }
        }
    }

    /**
     * Saves the repository's table of contents to a file. The table of contents file is needed by
     * the FaktorIPS runtime to load the product components and table data.
     * 
     * @throws CoreException if an error occurs while writing the toc to the file.
     */
    private void saveToc(IIpsPackageFragmentRoot root) throws CoreException {
        IFile tocFile = getBuilderSet().getRuntimeRepositoryTocFile(root);
        if (tocFile == null) {
            return;
        }
        String encoding = root.getIpsProject().getXmlFileCharset();
        if (encoding == null) {
            return;
        }
        String xml = null;
        try {
            Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
            String version = getIpsProject().getProperties().getVersion();
            if (StringUtils.isEmpty(version)) {
                version = "" + new Date().getTime();
            }
            Element tocElement = getToc(root).toXml(version, doc);
            doc.appendChild(tocElement);
            xml = XmlUtil.nodeToString(doc, encoding);
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus(
                    "Error transforming product component registry's table of contents to xml.", e)); //$NON-NLS-1$
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

    private void replaceTocFileIfContentHasChanged(IIpsProject ipsProject, IFile tocFile, String newContents)
            throws CoreException {
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

    private TableOfContent getToc(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsPackageFragmentRoot root = ipsSrcFile.getIpsObject().getIpsPackageFragment().getRoot();
        return getToc(root);
    }

    /**
     * Returns the product component registry's table of contents for the indicated ips package
     * fragment root.
     * 
     * @throws CoreException if an error occurs while accessing the toc file.
     */
    public TableOfContent getToc(IIpsPackageFragmentRoot root) throws CoreException {
        IIpsArtefactBuilderSet builderSet = root.getIpsProject().getIpsArtefactBuilderSet();
        IFile tocFile = builderSet.getRuntimeRepositoryTocFile(root);
        TableOfContent toc = tocFileMap.get(tocFile);
        if (toc == null) {
            toc = new TableOfContent();
            if (tocFile != null && tocFile.exists()) {
                InputStream is = tocFile.getContents(true);
                Document doc;
                try {
                    DocumentBuilder builder = IpsPlugin.getDefault().getDocumentBuilder();
                    doc = builder.parse(is);
                } catch (Exception e) {
                    // can happen if the file is deleted in the filesystem, but the workspace has
                    // not been synchronized
                    // nothing seriuos, we just write the file again
                    doc = null;
                    tocFile.refreshLocal(1, null);
                }
                if (doc != null) {
                    Element tocEl = doc.getDocumentElement();
                    try {
                        toc.initFromXml(tocEl);
                    } catch (Exception e) {
                        throw new CoreException(new IpsStatus("Error initializing toc from xml!", e)); //$NON-NLS-1$
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
    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsObject object = null;
        try {
            TocEntryObject entry;
            object = ipsSrcFile.getIpsObject();
            IpsObjectType type = object.getIpsObjectType();
            if (type.equals(IpsObjectType.PRODUCT_CMPT)) {
                // add entry for formula test, the formula test depends on the product cmpt
                // source file, thus it will be implicit created or deleted here
                entry = createFormulaTestTocEntry((IProductCmpt)object);
                if (entry != null) {
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
            } else if (type.equals(IpsObjectType.TABLE_CONTENTS)) {
                entry = createTocEntry((ITableContents)object);
            } else if (generateEntriesForModelTypes && type.isEntityType()) {
                entry = createTocEntry((IType)object);
            } else if (type.equals(IpsObjectType.TEST_CASE)) {
                entry = createTocEntry((ITestCase)object);
            } else if (type.equals(IpsObjectType.ENUM_CONTENT)) {
                entry = createTocEntry((IEnumContent)object);
            } else if (type.equals(IpsObjectType.ENUM_TYPE)) {
                entry = createTocEntry((IEnumType)object);
            } else {
                throw new RuntimeException("Unknown ips object type " + object.getIpsObjectType()); //$NON-NLS-1$
            }
            if (entry != null) {
                getToc(ipsSrcFile).addOrReplaceTocEntry(entry);
            } else {
                // no toc entry has been newly created, remove the previous toc entry
                getToc(ipsSrcFile).removeEntry(object.getQualifiedNameType());
            }
        } catch (Exception e) {
            IStatus status;
            if (object == null) {
                status = new IpsStatus("Unable to update the runtime repository toc file, ips object is null", e); //$NON-NLS-1$;
            } else {
                status = new IpsStatus("Unable to update the runtime repository toc file with the entry for: " //$NON-NLS-1$
                        + object.getQualifiedName(), e);
            }
            throw new CoreException(status);
        }
    }

    public ProductCmptTocEntry createTocEntry(IProductCmpt productCmpt) throws CoreException {
        if (productCmpt.getNumOfGenerations() == 0) {
            return null;
        }
        IProductCmptType pcType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        if (pcType == null) {
            return null;
        }
        IProductCmptKind kind = productCmpt.findProductCmptKind();
        if (kind == null) {
            return null;
        }
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_PRODUCT_CMPT_TOCENTRY,
                productCmpt.getIpsSrcFile()).replace('.', '/');
        String xmlResourceName = packageString + '/' + productCmpt.getName() + ".xml"; //$NON-NLS-1$
        String ipsObjectId = productCmpt.getRuntimeId();
        String ipsObjectQName = productCmpt.getQualifiedName();
        String implementationClass = productCmptTypeImplClassBuilder.getQualifiedClassName(pcType.getIpsSrcFile());
        String generationImplClass = productCmptGenImplClassBuilder.getQualifiedClassName(pcType.getIpsSrcFile());
        String kindId = productCmpt.findProductCmptKind().getRuntimeId();
        String versionId = productCmpt.getVersionId();
        DateTime validTo = DateTime.createDateOnly(productCmpt.getValidTo());

        ProductCmptTocEntry entry = new ProductCmptTocEntry(ipsObjectId, ipsObjectQName, kindId, versionId,
                xmlResourceName, implementationClass, generationImplClass, validTo);
        IIpsObjectGeneration[] generations = productCmpt.getGenerationsOrderedByValidDate();
        List<GenerationTocEntry> genEntries = new ArrayList<GenerationTocEntry>(generations.length);
        for (IIpsObjectGeneration generation : generations) {
            DateTime validFrom = DateTime.createDateOnly(generation.getValidFrom());
            IProductCmptGeneration gen = (IProductCmptGeneration)generation;
            String generationClassName;
            if (gen.getProductCmpt().containsFormula() && getBuilderSet().getFormulaCompiling().compileToSubclass()) {
                generationClassName = productCmptBuilder.getQualifiedClassName((IProductCmptGeneration)generation);
            } else {
                generationClassName = productCmptGenImplClassBuilder.getQualifiedClassName(pcType);
            }
            genEntries.add(new GenerationTocEntry(entry, validFrom, generationClassName, xmlResourceName));
        }
        entry.setGenerationEntries(genEntries);
        return entry;
    }

    /**
     * Creates a toc entry for the given formula test.
     */
    private TocEntryObject createFormulaTestTocEntry(IProductCmpt productCmpt) throws CoreException {
        if (!productCmpt.containsFormulaTest()) {
            // only build toc entry if at least one formula is specified
            return null;
        }

        // generate the object id, the objectId for this element will be the package root name
        // concatenated with the qualified name
        String packageRootName = productCmpt.getIpsPackageFragment().getRoot().getName();
        String objectId = packageRootName + "." + productCmpt.getQualifiedName(); //$NON-NLS-1$
        objectId = objectId.replace('.', '/') + "." + IpsObjectType.PRODUCT_CMPT.getFileExtension(); //$NON-NLS-1$

        String formulaTestCaseName = formulaTestBuilder.getQualifiedClassName(productCmpt);

        String kindId = productCmpt.findProductCmptKind().getName();
        TocEntryObject entry = new FormulaTestTocEntry(objectId, productCmpt.getQualifiedName(), kindId,
                productCmpt.getVersionId(), formulaTestCaseName);

        return entry;
    }

    /*
     * Removes the toc file and deletes the runtime content of the formula test. Do nothing if the
     * given scr file doesn't contains a formula test case.
     */
    private void removeFormulaTestEntry(IIpsSrcFile ipsSrcFile) throws CoreException {
        getToc(ipsSrcFile).removeEntry(new QualifiedNameType(ipsSrcFile.getIpsObjectName(), IpsObjectType.TEST_CASE));
        if (formulaTestBuilder != null) {
            formulaTestBuilder.delete(ipsSrcFile);
        }
    }

    public TocEntryObject createTocEntry(ITableContents tableContents) throws CoreException {
        ITableStructure tableStructure = tableContents.findTableStructure(getIpsProject());
        if (tableStructure == null) {
            return null;
        }
        String packageInternal = getBuilderSet().getPackage(DefaultBuilderSet.KIND_TABLE_TOCENTRY,
                tableContents.getIpsSrcFile());
        String tableStructureName = tableImplClassBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile());
        String xmlResourceName = packageInternal.replace('.', '/') + '/' + tableContents.getName() + ".xml"; //$NON-NLS-1$
        TocEntryObject entry = new TableContentTocEntry(tableContents.getQualifiedName(),
                tableContents.getQualifiedName(), xmlResourceName, tableStructureName);
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
        /*
         * generate the object id: the objectId for this element will be the package root name
         * concatenated with the qualified name
         */
        String packageRootName = testCase.getIpsPackageFragment().getRoot().getName();
        String objectId = packageRootName + "." + testCase.getQualifiedName(); //$NON-NLS-1$
        objectId = objectId.replace('.', '/') + "." + IpsObjectType.TEST_CASE.getFileExtension(); //$NON-NLS-1$

        String xmlResourceName = testCaseBuilder.getXmlResourcePath(testCase);
        String testCaseTypeName = testCaseTypeClassBuilder.getQualifiedClassName(type);
        TocEntryObject entry = new TestCaseTocEntry(objectId, testCase.getQualifiedName(), xmlResourceName,
                testCaseTypeName);
        return entry;
    }

    /** Creates a toc entry for the given enum content. */
    public TocEntryObject createTocEntry(IEnumContent enumContent) throws CoreException {
        IEnumType enumType = enumContent.findEnumType(enumContent.getIpsProject());
        if (enumType == null) {
            return null;
        }

        /*
         * generate the object id: the objectId for this element will be the package root name
         * concatenated with the qualified name
         */
        String packageRootName = enumContent.getIpsPackageFragment().getRoot().getName();
        String objectId = packageRootName + "." + enumContent.getQualifiedName(); //$NON-NLS-1$
        objectId = objectId.replace('.', '/') + "." + IpsObjectType.ENUM_CONTENT.getFileExtension(); //$NON-NLS-1$

        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_ENUM_CONTENT_TOCENTRY,
                enumContent.getIpsSrcFile()).replace('.', '/');
        String xmlResourceName = packageString + '/' + enumContent.getName() + ".xml"; //$NON-NLS-1$
        String enumTypeName = enumTypeBuilder.getQualifiedClassName(enumType);
        TocEntryObject entry = new EnumContentTocEntry(objectId, enumContent.getQualifiedName(), xmlResourceName,
                enumTypeName);
        return entry;
    }

    public TocEntryObject createTocEntry(IEnumType enumType) throws CoreException {
        if (!getBuilderSet().isGenerateJaxbSupport() || !ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())) {
            return null;
        }
        if (enumType.isContainingValues() || enumType.isAbstract()) {
            return null;
        }
        TocEntryObject entry = new EnumXmlAdapterTocEntry(enumType.getQualifiedName(), enumType.getQualifiedName(),
                enumXmlAdapterBuilder.getQualifiedClassName(enumType));
        return entry;
    }

    /**
     * Creates a toc entry for the given model type.
     */
    public TocEntryObject createTocEntry(IType type) throws CoreException {
        String javaImplClass;
        String xmlResourceName;
        String id = type.getQualifiedName(); // for model types, the qualified name is also the id.
        if (type instanceof IPolicyCmptType) {
            javaImplClass = policyCmptImplClassBuilder.getQualifiedClassName(type);
            xmlResourceName = policyModelTypeXmlBuilder.getXmlResourcePath(type);
            return new PolicyCmptTypeTocEntry(id, type.getQualifiedName(), xmlResourceName, javaImplClass);
        } else if (type instanceof IProductCmptType) {
            javaImplClass = productCmptTypeImplClassBuilder.getQualifiedClassName(type);
            xmlResourceName = productModelTypeXmlBuilder.getXmlResourcePath(type);
            return new ProductCmptTypeTocEntry(id, type.getQualifiedName(), xmlResourceName, javaImplClass);
        } else {
            throw new CoreException(new IpsStatus("Unkown subclass " + type.getClass()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        TableOfContent toc = getToc(ipsSrcFile.getIpsPackageFragment().getRoot());
        toc.removeEntry(ipsSrcFile.getQualifiedNameType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }
}
