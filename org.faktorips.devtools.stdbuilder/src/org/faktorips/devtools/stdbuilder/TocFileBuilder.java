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
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.ReadonlyTableOfContents;
import org.faktorips.runtime.TocEntryGeneration;
import org.faktorips.runtime.TocEntryObject;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.util.XmlUtil;
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
    private PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder;
    private ProductCmptImplClassBuilder productCmptTypeImplClassBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;
    private ProductCmptBuilder productCmptBuilder;
    private TableImplBuilder tableImplClassBuilder;
    
    public TocFileBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
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

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "TocFileBuilder";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())
                || IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType());
    }
    
    /**
     * The foc file builder has to remember the modifcation stamp for each toc before
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
            long modStamp = getToc(root).getModificationStamp();
            packFrgmtRootTocModStamps.put(root, new Long(modStamp));
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
                saveProductCmptRegistryToc(root);
            }
        }
    }
    
	/**
	 * Saves the product component registry's table of contents to a file. The table of contents file is needed
	 * by the FaktorIPS runtime's ClassloaderProductCmptRegistry to load the product component's.
	 * 
	 * @throws CoreException if an error occurs while writing the toc to the file.
	 */
	private void saveProductCmptRegistryToc(IIpsPackageFragmentRoot root) throws CoreException {
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
            throw new CoreException(new IpsStatus("Error transforming product component registry's table of contents to xml.", e));
        }
        InputStream is;
        try {
            is = new ByteArrayInputStream(xml.getBytes(encoding));
        } catch (UnsupportedEncodingException e1) {
            throw new CoreException(new IpsStatus(e1));
        }
        if (tocFile.exists()) {
            tocFile.setContents(is, true, true, null);
        } else {
            if (!tocFile.getParent().exists()) {
                createFolder((IFolder)tocFile.getParent());
            }
            tocFile.create(is, true, null);
        }
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
		IIpsArtefactBuilderSet builderSet = root.getIpsProject().getCurrentArtefactBuilderSet();
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
					throw new CoreException(new IpsStatus(
							"Error parsing toc contents.", e));
				}
				Element tocEl = doc.getDocumentElement();
				try {
					toc.initFromXml(tocEl);
				} catch (Exception e) {
					throw new CoreException(new IpsStatus(
							"Error initializing toc from xml!", e));
				}
			}
			tocFileMap.put(tocFile, toc);
		}
		return toc;
	}

    private void createFolder(IFolder folder) throws CoreException {
        while (!folder.getParent().exists()) {
            createFolder((IFolder)folder.getParent());
        }
        folder.create(true, true, null);
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
                entry = createTocEntry((IProductCmpt)object);
            } else if (object.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
                entry = createTocEntry((ITableContents)object);
            } else {
                throw new RuntimeException("Unknown ips object type " + object.getIpsObjectType());
            }
            getToc(ipsSrcFile).addOrReplaceTocEntry(entry);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Unable to update the runtime repository toc file with the entry for: "
                            + object.getQualifiedName(), e));
        }
    }

    TocEntryObject createTocEntry(IProductCmpt productCmpt) throws CoreException {
        if (productCmpt.getNumOfGenerations() == 0) {
            return null;
        }
        IPolicyCmptType pcType = productCmpt.findPolicyCmptType();
        if (pcType == null) {
            return null;
        }
        
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_PRODUCT_CMPT_TOCENTRY, productCmpt.getIpsSrcFile()).replace('.', '/');
        String xmlResourceName = packageString + '/' + productCmpt.getName() + ".xml";
        TocEntryObject entry = TocEntryObject.createProductCmptTocEntry(productCmpt.getQualifiedName(),
            xmlResourceName, 
            productCmptTypeImplClassBuilder.getQualifiedClassName(pcType.getIpsSrcFile()), 
            policyCmptTypeInterfaceBuilder.getQualifiedClassName(pcType.getIpsSrcFile()));
        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
        TocEntryGeneration[] genEntries = new TocEntryGeneration[generations.length];
        for (int i = 0; i < generations.length; i++) {
            DateTime validFrom = DateTime.createDateOnly(generations[i].getValidFrom());
            IProductCmptGeneration gen = (IProductCmptGeneration)generations[i];
            String generationClassName;
            if (gen.getProductCmpt().containsFormula()) {
                generationClassName = productCmptBuilder.getQualifiedClassName((IProductCmptGeneration)generations[i]);
            } else {
                generationClassName = productCmptGenImplClassBuilder.getQualifiedClassName(gen.getProductCmpt().findProductCmptType());
            }
            genEntries[i] = new TocEntryGeneration(entry, validFrom, generationClassName, xmlResourceName); 
        }
        entry.setGenerationEntries(genEntries);
        return entry;
    }

    TocEntryObject createTocEntry(ITableContents tableContents) throws CoreException {
        ITableStructure tableStructure = tableContents.findTableStructure();
        if (tableStructure == null) {
            return null;
        }
        String packageInternal = getBuilderSet().getPackage(DefaultBuilderSet.KIND_TABLE_TOCENTRY,
            tableContents.getIpsSrcFile());
        String tableStructureName = tableImplClassBuilder.getQualifiedClassName(tableStructure
                .getIpsSrcFile());
        String xmlResourceName = packageInternal.replace('.', '/') + '/' + tableContents.getName()
                + ".xml";
        TocEntryObject entry = TocEntryObject.createTableTocEntry(tableContents.getQualifiedName(),
            xmlResourceName, tableStructureName);
        return entry;
    }

    /**
     * {@inheritDoc}
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        MutableClRuntimeRepositoryToc toc = getToc(ipsSrcFile.getIpsPackageFragment().getRoot());
        toc.removeEntry(ipsSrcFile.getQualifiedNameType().getName());
    }
    
}
