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

package org.faktorips.devtools.core.model.ipsproject;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * Interface for the extension point <tt>org.faktorips.plugin.artefactbuilderset</tt>, provided by
 * this plug-in.
 * <p>
 * Only one implementation of this interface can be registered to this extension point. If more than
 * one extension is declared only the first one will be registered and the others will be ignored.
 * <p>
 * An <tt>IIpsArtefactBuilderSet</tt> collects a list of <tt>IIpsArtefactBuilders</tt> and makes
 * them available to the build system.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSet {

    /** The XML element name. */
    public final static String XML_ELEMENT = "IpsArtefactBuilderSet"; //$NON-NLS-1$

    /**
     * Returns the <tt>IIpsArtefactBuilderSetConfig</tt> that configures this builder set. The
     * configuration is available after the initialize method has been called.
     * 
     * @since 2.1
     */
    public IIpsArtefactBuilderSetConfig getConfig();

    /**
     * Returns all <tt>IIpsArtefactBuilders</tt> of this set.
     * <p>
     * An empty array has to be returned, if the set is empty.
     */
    public IIpsArtefactBuilder[] getArtefactBuilders();

    /** Returns <tt>true</tt> if the builder set supports table access functions, otherwise false. */
    public boolean isSupportTableAccess();

    /** Returns <tt>true</tt> if the builder set supports a formula language identifier resolver. */
    public boolean isSupportFlIdentifierResolver();

    /**
     * Returns <tt>true</tt> if this builder set requires role names in plural form, even for
     * relations with a max cardinality of 1.
     */
    public boolean isRoleNamePluralRequiredForTo1Relations();

    /**
     * Returns <code>true</code> if this artefact builder set requires that master-to-detail
     * compositions contain a reference to an inverse detail-to-master relation.
     * <p>
     * The standard Faktor-IPS generator doesn't need this link. See the artikel on modeling
     * relations for further details.
     */
    public boolean isInverseRelationLinkRequiredFor2WayCompositions();

    /**
     * @return <code>true</code> if the validations for the Table as Enum-Datatype is required (as
     *         by the standard Faktor-IPS generator, <code>false</code> otherwise.
     */
    public boolean isTableBasedEnumValidationRequired();

    /**
     * Returns <code>true</code> if the persistent provider supports converter. If no persistent
     * provider is specified returns <code>false</code>.
     */
    public boolean isPersistentProviderSupportConverter();

    /**
     * Returns <code>true</code> if the persistent provider supports orphan removal. If no
     * persistent provider is specified returns <code>false</code>.
     */
    public boolean isPersistentProviderSupportOrphanRemoval();

    /**
     * Returns a compilation result that gives access to a table via the indicated function. Returns
     * <code>null</code> if this builder set does not support table access.
     * 
     * @param tableContents for table structures that allow multiple contents the table contents is
     *            needed to identify for which table contents of a table structure a table access
     *            function is called. Can be null for single content table structures
     * 
     * @param fct The table access function code should be generated for.
     * @param argResults Compilation Results for the function's arguments.
     * 
     * @throws CoreException if an error occurs while generating the code.
     */
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException;

    /**
     * Creates an<code>IdentifierResolver</code> used to resolve identifiers in the given formula.
     * Returns <code>null</code> if this builder set doesn't support an formula language identifier
     * resolver.
     * 
     * @param exprCompiler can be used by the {@link IdentifierResolver} to ask for properties or
     *            services that are necessary to be able to resolve an identifier properly
     */
    public IdentifierResolver createFlIdentifierResolver(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException;

    /**
     * Creates an<code>IdentifierResolver</code> used to resolve identifiers in the given formula.
     * The returned identifier resolver has an special handling of type attribute (e.g. an policy
     * cmpt type attribute), instead of using the getter method of the attribute a parameter will be
     * used.
     * <p>
     * Returns <code>null</code> if this builder set doesn't support a formula language identifier
     * resolver.
     * 
     * @param exprCompiler can be used by the {@link IdentifierResolver} to ask for properties or
     *            services that are necessary to be able to resolve an identifier properly
     */
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException;

    /**
     * Returns the data type helper for the provided {@link IEnumType}. <code>IEnumType</code>
     * implements the {@link Datatype} interface and this method provides the datatype helper for
     * it. The data type helper of an <code>IEnumType</code> depends on the
     * {@link IIpsArtefactBuilderSet} since it determines the full qualified class name of the
     * generated data type class.
     */
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter);

    /**
     * Returns the file that contain the runtime repository toc file. Note that the file might not
     * exists.
     */
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException;

    /**
     * Returns the name of the resource containing the root's table of contents at runtime. E.g.
     * "org.faktorips.sample.internal.sample-toc.xml". This returned path can be used to create a
     * ClassloaderRuntimeRepository. Returns <code>null</code> if this builder does not generate
     * TOCs or this root is not a root based on a source folder. Returns <code>null</code> if the
     * root is <code>null</code>.
     * 
     * @see org.faktorips.runtime.ClassloaderRuntimeRepository#create(String)
     */
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) throws CoreException;

    /**
     * Returns the package name of the generated TOC file.<br>
     * Returns <code>null</code> if the builder doesn't create a TOC file.
     * 
     * @deprecated use getRuntimeRepositoryTocResourceName(root)
     */
    @Deprecated
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) throws CoreException;

    /**
     * Returns the locale of the language that is used by the generator to generate source code and
     * documentation.
     */
    public Locale getLanguageUsedInGeneratedSourceCode();

    /**
     * When the builder set is loaded by the Faktor-IPS plug-in the extension id is set by means of
     * this method. This method is called before initialization.
     * 
     * @param id the extension id
     */
    public void setId(String id);

    /**
     * When the builder set is loaded by the Faktor-IPS plug-in the extension description label is
     * set by means of this method. This method is called before initialization.
     * 
     * @param label the extension description label
     */
    public void setLabel(String label);

    /**
     * Returns the extension id declared in the plug-in descriptor.
     */
    public String getId();

    /**
     * Returns the extension description label declared in the plug-in descriptor.
     */
    public String getLabel();

    /**
     * This version indicates the version of the generated code. That means changes to the generator
     * that do not change the generated code a not reflected in this version identifier.
     */
    public String getVersion();

    /**
     * Initializes this set. Creation of IpsArtefactBuilders has to go here instead of the
     * constructor of the set implementation.
     * 
     * @param config the configuration for this builder set instance. The configuration for a
     *            builder set instance is defined in the .ipsproject file of an IPS project.
     * 
     * @throws NullPointerException if <tt>config</tt> is <code>null</code>.
     * 
     * @see IIpsArtefactBuilderSetConfig class description
     */
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException;

    /**
     * Subclasses should re-implement this method if an aggregate root builder is contained
     * 
     * @return <code>true</code> if this builder set contains an builder which requires to be called
     *         for the aggregate root object if any child (regardless of the relations depth) has
     *         been modified. Only composite-relations are allowed for this dependency scan.
     *         <code>false</code> if only builder are contained which need only a dependency scan of
     *         depth one.
     */
    public boolean containsAggregateRootBuilder();

    /**
     * The IpsProject for which this builder set is registered.
     */
    public void setIpsProject(IIpsProject ipsProject);

    /**
     * Returns the IpsProject for which this builder set is registered.
     */
    public IIpsProject getIpsProject();

    /**
     * This method is called when the build process starts for this builder set. This method is
     * called before the <code>beforeBuildProcess(IIpsProject, int)</code> method of the registered
     * IpsArtefactBuilders will be called.
     * 
     * @param buildKind One of the build kinds defined in
     *            <code>org.eclipse.core.resources.IncrementalProjectBuilder</code>
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or RuntimeException will interrupt the build cycle
     */
    public void beforeBuildProcess(int buildKind) throws CoreException;

    /**
     * This method is called when the build process is finished for this builder set. It is called
     * after the <tt>afterBuildProcess(IIpsProject, int)</tt> methods on the registered
     * <tt>IIpsArtefactBuilders</tt> were called.
     * 
     * @param buildKind One of the build kinds defined in
     *            <code>org.eclipse.core.resources.IncrementalProjectBuilder</code>
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions.
     */
    public void afterBuildProcess(int buildKind) throws CoreException;

    /**
     * Returns an array of builders which are sub types of or from the same type as the provided
     * builder class.
     * 
     * @param builderClass The class of the builders you are searching for.
     * 
     */
    public <T extends IIpsArtefactBuilder> List<T> getBuildersByClass(Class<T> builderClass);

}
