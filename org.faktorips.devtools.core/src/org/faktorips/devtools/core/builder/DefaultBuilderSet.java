/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
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
 * configure for an IPS project within the ipsproject.xml file. On top of the base package name it
 * adds the IPS package fragment name of the IpsSrcFile in question. Internal packages are
 * distinguished from packages that contain published interfaces and classes. It depends on the kind
 * constant if an internal or published package name is returned.
 * 
 * @author Peter Erzberger
 */
public abstract class DefaultBuilderSet extends AbstractBuilderSet implements IJavaPackageStructure {

    private final static String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$

    /**
     * Returns the name of the (Java) package that contains the artefacts specified by the
     * parameters generated for the given ips source file.
     * 
     * @param publishedArtefact <code>true</code> if the artefacts are published (usable by
     *            clients), <code>false</code> if they are internal.
     * @param mergableArtefact <code>true</code> if the generated artefact is mergable (at the
     *            moment this applies to Java Source files only). <code>false</code) if the artefact
     *            is 100% generated and can't be modified by the user.
     */
    public String getPackageNameForGeneratedArtefacts(IIpsSrcFile ipsSrcFile,
            boolean publishedArtefact,
            boolean mergableArtefact) throws CoreException {

        String basePackName = mergableArtefact ? ipsSrcFile.getBasePackageNameForMergableArtefacts() : ipsSrcFile
                .getBasePackageNameForDerivedArtefacts();
        String packageFragName = ipsSrcFile.getIpsPackageFragment().getName().toLowerCase();
        if (!publishedArtefact) {
            return getInternalPackage(basePackName, packageFragName);
        } else {
            return QNameUtil.concat(basePackName, packageFragName);
        }
    }

    @Override
    public String getInternalPackage(final String basePackName, final String subPackageFragment) {
        String internalBasePack = QNameUtil.concat(basePackName, INTERNAL_PACKAGE);
        return QNameUtil.concat(internalBasePack, subPackageFragment);
    }

    /**
     * Returns the name of the (Java) package name that contains the published artefacts that are
     * generated for the given IPS source file that (the artefacts) are also mergable.
     */
    public String getPackageNameForMergablePublishedArtefacts(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, true, true);
    }

    /**
     * Returns the name of the (Java) package name that contains the internal artefacts that are
     * generated for the given IPS source file that (the artefacts) are also mergable.
     */
    public String getPackageNameForMergableInternalArtefacts(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, false, true);
    }

    @Override
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackName = entry.getBasePackageNameForDerivedJavaClasses();
        return getInternalPackage(basePackName, StringUtils.EMPTY);
    }

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
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

    private IFolder getTocFileLocation(IIpsPackageFragmentRoot root) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getOutputFolderForDerivedJavaFiles();
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) {
        IFile tocFile = getRuntimeRepositoryTocFile(root);
        if (tocFile == null) {
            return null;
        }
        IFolder tocFileLocation = getTocFileLocation(root);
        return tocFile.getFullPath().removeFirstSegments(tocFileLocation.getFullPath().segmentCount()).toString();
    }

    @Override
    public String getPackage(IIpsArtefactBuilder builder, IIpsSrcFile ipsSrcFile) throws CoreException {
        if (builder instanceof JavaSourceFileBuilder) {

            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builder;
            return getPackageNameForGeneratedArtefacts(ipsSrcFile, javaBuilder.isBuildingPublishedSourceFile(),
                    builder.buildsDerivedArtefacts());
        }
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, false, builder.buildsDerivedArtefacts());
    }

    @Override
    public boolean isSupportTableAccess() {
        return false;
    }

    /**
     * Empty implementation. Might be overridden by subclasses that support the formula language.
     */
    @Override
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {

        return null;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IExpression formula, ExprCompiler exprCompiler)
            throws CoreException {
        return null;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IExpression formula, ExprCompiler exprCompiler)
            throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>null</code>. This method is supposed to be overridden by subclasses.
     */
    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns an empty string. This method is supposed to be overridden by subclasses.
     */
    @Override
    public String getVersion() {
        return ""; //$NON-NLS-1$
    }

}
