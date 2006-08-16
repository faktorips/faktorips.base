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

package org.faktorips.devtools.core.builder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.runtime.ClassloaderRuntimeRepository;

/**
 * A default implementation that extends the AbstractBuilderSet and implements the IJavaPackageStructure
 * interface. The getPackage() method provides package names for the kind constants defined in this
 * DefaultBuilderSet. This implementation uses the base package name for generated java classes as
 * the root of the package structure. The base package name can be configure for an ips project
 * within the ipsproject.xml file. On top of the base package name it adds the ips package fragment
 * name of the IpsSrcFile in question. Internal packages are distinguished from packages that
 * contain published interfaces and classes. It depends on the kind constant if an internal or
 * published package name is returned.
 * 
 * @author Peter Erzberger
 */
public abstract class DefaultBuilderSet extends AbstractBuilderSet {

    // kind constants. These constants are not supposed to be used within JavaSourceFileBuilder
    // implementations. Since the JavaSourceFileBuilder implementations might get used in other
    // artefact builder sets using these constants would introduce a dependency to this builder set.
    // however the constants are public for use in test cases
    public final static String KIND_PRODUCT_CMPT_INTERFACE = "productcmptinterface"; //$NON-NLS-1$
    public final static String KIND_PRODUCT_CMPT_IMPL = "productcmptimplementation"; //$NON-NLS-1$
    public final static String KIND_PRODUCT_CMPT_GENERATION_INTERFACE = "productCmptGenerationInterface"; //$NON-NLS-1$
    public final static String KIND_PRODUCT_CMPT_GENERATION_IMPL = "productCmptGenerationImpl"; //$NON-NLS-1$
    public final static String KIND_PRODUCT_CMPT_CONTENT = "productcmptcontent"; //$NON-NLS-1$
    public final static String KIND_POLICY_CMPT_INTERFACE = "policycmptinterface"; //$NON-NLS-1$
    public final static String KIND_POLICY_CMPT_IMPL = "policycmptimpl"; //$NON-NLS-1$
    public final static String KIND_TABLE_IMPL = "tableimpl"; //$NON-NLS-1$
    public final static String KIND_TABLE_CONTENT = "tablecontent"; //$NON-NLS-1$
    public final static String KIND_TABLE_ROW = "tablerow"; //$NON-NLS-1$
    public final static String KIND_TEST_CASE_TYPE_CLASS = "testcasetypeclass"; //$NON-NLS-1$
    public final static String KIND_TEST_CASE_XML = "testcasexml"; //$NON-NLS-1$
    
    public final static String KIND_TABLE_TOCENTRY = "tabletocentry"; //$NON-NLS-1$
    public final static String KIND_PRODUCT_CMPT_TOCENTRY = "productcmpttocentry"; //$NON-NLS-1$
    
    private final static String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$
    
    /**
     * Returns the base package name. It is the <b>base package name for generated java classes</b>
     * from the IpsSrcFolderEntry that is assigned to the package root that contains the provided
     * IpsSrcFile. The base package name can be configure for an ips project within the
     * ipsproject.xml file.
     */
    private String getBasePackageName(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)ipsSrcFile.getIpsPackageFragment().getRoot()
                .getIpsObjectPathEntry();
        return entry.getBasePackageNameForGeneratedJavaClasses();
    }

    /**
     * Returns the addition of the name of the ips package fragment that contains the provided
     * IpsSrcFile and the base package name. This method is used within the getPackage() method
     * implementation.
     */
    protected String getPackageName(IIpsSrcFile ipsSrcFile) throws CoreException {
        StringBuffer buf = new StringBuffer();
        String basePackeName = getBasePackageName(ipsSrcFile);
        if (!StringUtils.isEmpty(basePackeName)) {
            buf.append(basePackeName);
        }
        String packageFragName = ipsSrcFile.getIpsPackageFragment().getName();
        if (!StringUtils.isEmpty(packageFragName)) {
            buf.append('.').append(packageFragName);
        }
        return buf.toString().toLowerCase();
    }

    /**
     * Returns ips package fragment + ".internal." + base package name. This method is used within
     * the getPackage() method implementation.
     */
    protected String getInternalPackageName(IIpsSrcFile ipsSrcFile) throws CoreException {
        StringBuffer buf = new StringBuffer();
        String basePackeName = getBasePackageName(ipsSrcFile);
        if (!StringUtils.isEmpty(basePackeName)) {
            buf.append(basePackeName).append('.');
        }
        buf.append(INTERNAL_PACKAGE);
        String packageFragName = ipsSrcFile.getIpsPackageFragment().getName();
        if (!StringUtils.isEmpty(packageFragName)) {
            buf.append('.').append(packageFragName);
        }

        return buf.toString().toLowerCase();
    }
    
    /**
     * Returns ips package fragment + ".internal." from the given package fragment root.
     * TODO Joerg: auch ins interface?, bzw. hier richtig oder evtl. anders loesen
     */
    public String getInternalBasePackageName(IIpsPackageFragmentRoot root) throws CoreException {
        StringBuffer buf = new StringBuffer();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackeName = entry.getBasePackageNameForGeneratedJavaClasses();
        if (!StringUtils.isEmpty(basePackeName)) {
            buf.append(basePackeName).append('.');
        }
        buf.append(INTERNAL_PACKAGE);
        return buf.toString().toLowerCase();
    }
    
    /**
	 * {@inheritDoc}
	 */
	public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException {
		IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry(); 
		String basePack = entry.getBasePackageNameForGeneratedJavaClasses();
        if (StringUtils.isEmpty(basePack)) {
        	basePack = INTERNAL_PACKAGE;
        } else {
        	basePack = basePack + '.' + INTERNAL_PACKAGE;
        	
        }
		IFolder folder = entry.getOutputFolderForGeneratedJavaFiles();
		IFolder tocFolder = folder.getFolder(basePack.replace('.', IPath.SEPARATOR));
		return tocFolder.getFile(ClassloaderRuntimeRepository.TABLE_OF_CONTENTS_FILE);
	}

	/**
     * Implements the org.faktorips.plugin.builder.IJavaPackageStructure interface.
     */
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {

        if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())) {
            return getPackageName(ipsSrcFile);
        }

        if (IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_PRODUCT_CMPT_INTERFACE.equals(kind)) {
                return getPackageName(ipsSrcFile);
            }

            if (KIND_POLICY_CMPT_INTERFACE.equals(kind)) {
                return getPackageName(ipsSrcFile);
            }

            if (KIND_POLICY_CMPT_IMPL.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }

            if (KIND_PRODUCT_CMPT_IMPL.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
            if (KIND_PRODUCT_CMPT_GENERATION_INTERFACE.equals(kind)) {
                return getPackageName(ipsSrcFile);
            }
            if (KIND_PRODUCT_CMPT_GENERATION_IMPL.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }

        if (IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_PRODUCT_CMPT_CONTENT.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
            if (KIND_PRODUCT_CMPT_TOCENTRY.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
            if (KIND_PRODUCT_CMPT_GENERATION_IMPL.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }

        if (IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_TABLE_CONTENT.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
            if (KIND_TABLE_TOCENTRY.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }

        if (IpsObjectType.TEST_CASE_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_TEST_CASE_TYPE_CLASS.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }
        
        if (IpsObjectType.TEST_CASE.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_TEST_CASE_XML.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }

        throw new IllegalArgumentException("No package has been defined for the provided kind: " //$NON-NLS-1$
                + kind + " and IpsSrcFile: " + ipsSrcFile); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public boolean isSupportTableAccess() {
        return false;
    }

    /**
     * Empty implementation. Might be overriden by subclasses that support the formula language.
     */
    public CompilationResult getTableAccessCode(ITableContents tableContents, ITableAccessFunction fct, CompilationResult[] argResults) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IParameterIdentifierResolver getFlParameterIdentifierResolver() {
        return null;
    }
    
}
