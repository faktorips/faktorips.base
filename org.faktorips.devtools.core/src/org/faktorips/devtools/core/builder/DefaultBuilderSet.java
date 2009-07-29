/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * A default implementation that extends the AbstractBuilderSet and implements the
 * IJavaPackageStructure interface. The getPackage() method provides package names for the kind
 * constants defined in this DefaultBuilderSet. This implementation uses the base package name for
 * generated java classes as the root of the package structure. The base package name can be
 * configure for an ips project within the ipsproject.xml file. On top of the base package name it
 * adds the ips package fragment name of the IpsSrcFile in question. Internal packages are
 * distinguished from packages that contain published interfaces and classes. It depends on the kind
 * constant if an internal or published package name is returned.
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
    public final static String KIND_MODEL_TYPE = "modeltype"; //$NON-NLS-1$
    public final static String KIND_TABLE_IMPL = "tableimpl"; //$NON-NLS-1$
    public final static String KIND_TABLE_CONTENT = "tablecontent"; //$NON-NLS-1$
    public final static String KIND_TABLE_ROW = "tablerow"; //$NON-NLS-1$
    public final static String KIND_TEST_CASE_TYPE_CLASS = "testcasetypeclass"; //$NON-NLS-1$
    public final static String KIND_TEST_CASE_XML = "testcasexml"; //$NON-NLS-1$
    public final static String KIND_FORMULA_TEST_CASE = "formulatestcase"; //$NON-NLS-1$
    public final static String KIND_ENUM_CONTENT = "enumcontent"; //$NON-NLS-1$

    public final static String KIND_TABLE_TOCENTRY = "tabletocentry"; //$NON-NLS-1$
    public final static String KIND_PRODUCT_CMPT_TOCENTRY = "productcmpttocentry"; //$NON-NLS-1$
    public final static String KIND_ENUM_CONTENT_TOCENTRY = "enumcontenttocentry"; //$NON-NLS-1$

    private final static String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$

    /**
     * Returns the Java naming convention to be used.
     */
    // TODO duplicate method in JavaSourceFileBuilder
    public JavaNamingConvention getJavaNamingConvention() {
        return JavaNamingConvention.ECLIPSE_STANDARD;
    }

    /**
     * Returns the addition of the name of the ips package fragment that contains the provided
     * IpsSrcFile and the base package name. This method is used within the getPackage() method
     * implementation.
     */
    public String getPackageName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String basePackName = ipsSrcFile.getBasePackageNameForMergableArtefacts();
        String packageFragName = ipsSrcFile.getIpsPackageFragment().getName().toLowerCase();
        return QNameUtil.concat(basePackName, packageFragName);
    }

    /**
     * Returns ips package fragment + ".internal." + base package name. This method is used within
     * the getPackage() method implementation.
     */
    public String getInternalPackageName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String basePackName = QNameUtil.concat(ipsSrcFile.getBasePackageNameForMergableArtefacts(), INTERNAL_PACKAGE);
        String packageFragName = ipsSrcFile.getIpsPackageFragment().getName().toLowerCase();
        return QNameUtil.concat(basePackName, packageFragName);
    }

    /**
     * {@inheritDoc}
     */
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackName = entry.getBasePackageNameForDerivedJavaClasses();
        return QNameUtil.concat(basePackName, INTERNAL_PACKAGE);
    }

    /**
     * {@inheritDoc}
     */
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException {
        if (root == null) {
            return null;
        }
        if (!root.isBasedOnSourceFolder()) {
            return null;
        }
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackInternal = getTocFilePackageName(root);
        IPath path = QNameUtil.toPath(basePackInternal);
        path = path.append(entry.getBasePackageRelativeTocPath());
        IFolder tocFileLocation = getTocFileLocation(root);
        if (tocFileLocation == null) {
            return null;
        }
        return tocFileLocation.getFile(path);
    }

    private IFolder getTocFileLocation(IIpsPackageFragmentRoot root) throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getOutputFolderForDerivedJavaFiles();
    }

    /**
     * {@inheritDoc}
     */
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) throws CoreException {
        IFile tocFile = getRuntimeRepositoryTocFile(root);
        if (tocFile == null) {
            return null;
        }
        IFolder tocFileLocation = getTocFileLocation(root);
        return tocFile.getFullPath().removeFirstSegments(tocFileLocation.getFullPath().segmentCount()).toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
        // TODO v2 - das koenner wir effizienter implementieren
        if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_TABLE_IMPL.equals(kind) || KIND_TABLE_ROW.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }

        if (IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_POLICY_CMPT_INTERFACE.equals(kind)) {
                return getPackageName(ipsSrcFile);
            }

            if (KIND_POLICY_CMPT_IMPL.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }

            if (KIND_MODEL_TYPE.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }

        }

        if (IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_PRODUCT_CMPT_INTERFACE.equals(kind)) {
                return getPackageName(ipsSrcFile);
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

            if (KIND_MODEL_TYPE.equals(kind)) {
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
            if (KIND_FORMULA_TEST_CASE.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
        }

        if (IpsObjectType.ENUM_CONTENT.equals(ipsSrcFile.getIpsObjectType())) {
            if (KIND_ENUM_CONTENT.equals(kind)) {
                return getInternalPackageName(ipsSrcFile);
            }
            if (KIND_ENUM_CONTENT_TOCENTRY.equals(kind)) {
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

        return null;
    }

    /**
     * Overridden.
     */
    public boolean isSupportTableAccess() {
        return false;
    }

    /**
     * Empty implementation. Might be overridden by subclasses that support the formula language.
     */
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {
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
    public IdentifierResolver createFlIdentifierResolver(IFormula formula, ExprCompiler exprCompiler) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula, ExprCompiler exprCompiler) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns <code>null</code>. This method is supposed to be overridden by subclasses.
     */
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns an empty string. This method is supposed to be overridden by subclasses.
     */
    public String getVersion() {
        return "";
    }

}
