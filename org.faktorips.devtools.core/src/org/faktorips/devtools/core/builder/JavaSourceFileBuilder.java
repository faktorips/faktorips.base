/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.codegen.merge.java.JControlModel;
import org.eclipse.emf.codegen.merge.java.JMerger;
import org.eclipse.emf.codegen.merge.java.facade.FacadeHelper;
import org.eclipse.emf.codegen.merge.java.facade.ast.ASTFacadeHelper;
import org.eclipse.emf.codegen.merge.java.facade.jdom.JDOMFacadeHelper;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.builder.organizeimports.IpsRemoveImportsOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * An implementation of <code>IIpsArtefactBuilder</code> that generates a java source file for a
 * specific IpsObject. It provides support for formatting of the java source file content and
 * merging of the content with the content of an already existing java file that has the same file
 * name. A JavaSourceFileBuilder needs a IJavaPackageStructure that provides the package information
 * about the java source file. A kindId has to be specified at instantiation time. The kindId is
 * used within the IjavaPackageStructure implementation to identify the builder. To generate the
 * actual content of the java source file implementations must override the
 * generate(IProgressMonitor) method.
 * 
 * @author Peter Erzberger
 */
public abstract class JavaSourceFileBuilder extends AbstractArtefactBuilder {

    /**
     * This constant is supposed to be used as a Javadoc annotation. If the merging capabilities are
     * activated a class, method or attribute that is marked by this annotation will be regenerated
     * with every build.
     */
    public final static String[] ANNOTATION_GENERATED = new String[] { "generated" }; //$NON-NLS-1$

    /**
     * This constant is supposed to be used as a Javadoc annotation. It becomes relevant if the
     * merging capabilities are activated. It indicates that within a generated piece of code only
     * the parts that are outside the braces defined by the markers <code>//begin-user-code</code>
     * and <code>//end-user-code</code> are regenerated with the next generation.
     */
    public final static String[] ANNOTATION_RESTRAINED_MODIFIABLE = new String[] { "restrainedmodifiable" }; //$NON-NLS-1$

    /**
     * This constant is supposed to be used as a Java 5 annotation. It suppresses warnings for
     * unchecked casts when interacting with legacy code.
     */
    public static final String ANNOTATION_SUPPRESS_WARNINGS_UNCHECKED = "SuppressWarnings(\"unchecked\")"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used as a Java 5 <code>Override</code> annotation.
     */
    public static final String ANNOTATION_OVERRIDE = "Override"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used as a Java 5 annotations. It contains the java annotation
     * for suppressed warnings of unused code.
     */
    public static final String ANNOTATION_SUPPRESS_WARNINGS_UNUSED = "SuppressWarnings(\"unused\")"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used to indicate the beginning of a section within generated
     * code that a user can modify and will not be overridden by the generator at the next
     * generation.
     */
    public final static String MARKER_BEGIN_USER_CODE = "//begin-user-code"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used to indicate the end of a section within generated code
     * that a user can modify and will not be overridden by the generator at the next generation.
     */
    public final static String MARKER_END_USER_CODE = "//end-user-code"; //$NON-NLS-1$

    private final static String BEGIN_FAKTORIPS_GENERATOR_INFORMATION_SECTION = "BEGIN FAKTORIPS GENERATOR INFORMATION SECTION"; //$NON-NLS-1$

    private final static String END_FAKTORIPS_GENERATOR_INFORMATION_SECTION = "END FAKTORIPS GENERATOR INFORMATION SECTION"; //$NON-NLS-1$

    private final static Pattern FAKTORIPS_GENERATOR_INFORMATION_SECTION_PATTERN = createFeatureSectionPattern();

    private String versionSection;

    private boolean mergeEnabled;

    private String kindId;

    private IIpsObject ipsObject;

    private IIpsSrcFile ipsSrcFile;

    private boolean generationCanceled;

    private MultiStatus buildStatus;

    private JControlModel model;

    private FacadeHelper facadeHelper;

    private JavaClassNaming javaClassNaming;

    private final IJavaClassNameProvider javaClassNameProvider;

    /**
     * Creates a new JavaSourceFileBuilder.
     * 
     * @param builderSet the package information for the generated java source file and for other
     *            generated java classes within this package structure. Cannot be null.
     * @param localizedStringsSet provides locale specific texts. It can be null. If the
     *            getLocalizedText() methods are called and the localizedStringsSet is not set an
     *            exception is thrown
     */
    public JavaSourceFileBuilder(DefaultBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
        setJavaClassNaming(new JavaClassNaming(builderSet, !buildsDerivedArtefacts()));
        javaClassNameProvider = createJavaClassNameProvider(builderSet.isGeneratePublishedInterfaces());
    }

    @Override
    public DefaultBuilderSet getBuilderSet() {
        return (DefaultBuilderSet)super.getBuilderSet();
    }

    @Override
    public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        model = null;
        versionSection = null;
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        initJControlModel(project);
        createVersionSection();
    }

    protected JavaClassNaming getJavaClassNaming() {
        return javaClassNaming;
    }

    protected void setJavaClassNaming(JavaClassNaming javaClassNaming) {
        this.javaClassNaming = javaClassNaming;
    }

    @Override
    public String getName() {
        return StringUtil.unqualifiedName(getClass().getName());
    }

    /**
     * Returns the naming convention for product changes over time.
     */
    public static IChangesOverTimeNamingConvention getChangesInTimeNamingConvention(IIpsElement element) {
        return element.getIpsProject().getChangesInTimeNamingConventionForGeneratedCode();
    }

    /**
     * Returns the description of the given {@link IIpsObjectPartContainer} in the language of the
     * code generator.
     * <p>
     * If there is no description in that locale, the description of the default language will be
     * returned.
     * <p>
     * Returns an empty string if there is no default description as well or the given
     * {@link IIpsObjectPartContainer} does not support descriptions.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the description
     *            of.
     * 
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     */
    protected final String getDescriptionInGeneratorLanguage(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);
        String description = ""; //$NON-NLS-1$
        if (ipsObjectPartContainer instanceof IDescribedElement) {
            IDescribedElement describedElement = (IDescribedElement)ipsObjectPartContainer;
            IDescription generatorDescription = describedElement.getDescription(getLanguageUsedInGeneratedSourceCode());
            if (generatorDescription != null) {
                description = generatorDescription.getText();
            } else {
                description = IpsPlugin.getMultiLanguageSupport().getDefaultDescription(describedElement);
            }
        }
        return description;
    }

    /**
     * Returns the Java naming convention to be used.
     */
    public IJavaNamingConvention getJavaNamingConvention() {
        return getIpsProject().getJavaNamingConvention();
    }

    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    private IJavaClassNameProvider createJavaClassNameProvider(boolean isGeneratePublishedInterface) {
        // Interfaces are always published artifacts. But some implementations are also published
        return new DefaultJavaClassNameProvider(isGeneratePublishedInterface) {

            @Override
            public boolean isImplClassInternalArtifact() {
                return isBuildingInternalArtifacts();
            }

        };
    }

    /**
     * Implementations of this class must override this method to provide the content of the java
     * source file.
     * 
     * @throws CoreException implementations can wrap rising checked exceptions into a
     *             CoreException. If an exception is thrown by this method the current build of this
     *             builder is interrupted. Alternatively the exception can be reported to the
     *             buildStatus to avoid interrupting the build process of this builder.
     */
    protected abstract String generate() throws CoreException;

    /**
     * Returns the IpsObject provided to this builder. It returns the IpsObject only during the
     * generating phase otherwise null is returned.
     */
    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    /**
     * Returns the IpsSrcFile provided to this builder. It returns the IpsSrcFile only during the
     * generating phase otherwise null is returned.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    /**
     * Returns the IPS project, the builder is currently building for.
     */
    @Override
    public IIpsProject getIpsProject() {
        return getBuilderSet().getIpsProject();
    }

    /**
     * Convenience method that delegates the call to the package structure and returns the package
     * name for the java class that is build by this builder.
     * <p>
     * It is not allowed to overwrite this method because other methods in {@link JavaClassNaming}
     * uses the {@link JavaPackageStructure} object directly and this may impact an inconsistent
     * package name.
     * 
     * @param ipsSrcFile The source file to get the package from
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    public final String getPackage(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getPackageStructure().getPackageName(ipsSrcFile, isBuildingInternalArtifacts(),
                !buildsDerivedArtefacts());
    }

    /**
     * Calls getPackage(IpsObject). It is only allowed to call this method during the build cycle of
     * this builder.
     * <p>
     * It is not allowed to overwrite this method because other methods in {@link JavaClassNaming}
     * uses the {@link JavaPackageStructure} object directly and this may impact an inconsistent
     * package name.
     * 
     * @return the package string
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    public final String getPackage() throws CoreException {
        return getPackage(getIpsSrcFile());
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the IPS object
     * stored in the given IPS source file.
     * 
     * @param ipsSrcFile the IPS source file.
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaClassNaming().getQualifiedClassName(ipsSrcFile, BuilderAspect.getValue(generatesInterface()),
                getJavaClassNameProvider());
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the given IPS
     * object.
     * 
     * @param ipsObject the IPS object.
     */
    public String getQualifiedClassName(IIpsObject ipsObject) throws CoreException {
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * Calls getQualifiedClassName(IpsObject). It is only allowed to call this method during the
     * build cycle of this builder.
     * 
     * @return the qualified class name
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    public String getQualifiedClassName() throws CoreException {
        return getQualifiedClassName(getIpsSrcFile());
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * <p>
     * It is not allowed to overwrite this class because other methods in the
     * {@link JavaClassNaming} does also reference the unqualified name. If you want to change the
     * unqualified name you have to set your own {@link IJavaClassNameProvider} to the
     * {@link JavaClassNaming}.
     * 
     * @param ipsSrcFile the IPS source file
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    public final String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaClassNaming().getUnqualifiedClassName(ipsSrcFile, BuilderAspect.getValue(generatesInterface()),
                getJavaClassNameProvider());
    }

    /**
     * Calls getUnqualifiedClassName(IpsObject). It is only allowed to call this method during the
     * build cycle of this builder.
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    public String getUnqualifiedClassName() throws CoreException {
        return getUnqualifiedClassName(getIpsSrcFile());
    }

    /**
     * This method has been overridden for convenience. Subclasses might need to implement this
     * method to clean up the state of the builder that was created during the generation.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder#afterBuild(org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile)
     */
    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        facadeHelper.reset();
        this.ipsSrcFile = null;
        ipsObject = null;
        buildStatus = null;
        generationCanceled = false;
    }

    /**
     * This method has been overridden for convenience. Subclasses might need to implement this
     * method to set up a defined state before the generation starts.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder#beforeBuild(org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile,
     *      org.eclipse.core.runtime.MultiStatus)
     */
    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        this.ipsSrcFile = ipsSrcFile;
        buildStatus = status;
        if (ipsSrcFile.isContentParsable()) {
            ipsObject = ipsSrcFile.getIpsObject();
        } else {
            ipsObject = null;
        }
        generationCanceled = false;
    }

    /**
     * Returns the java package structure available for this builder.
     */
    public IJavaPackageStructure getPackageStructure() {
        return getBuilderSet();
    }

    /**
     * Can be used by subclasses within the implementation of the generate() method to prevent this
     * builder from creating a java source file.
     */
    public final void cancelGeneration() {
        generationCanceled = true;
    }

    /**
     * Sets if merging is enabled or not.
     */
    public void setMergeEnabled(boolean enabled) {
        mergeEnabled = enabled;
    }

    /**
     * Returns if merging is enabled or not.
     */
    public boolean isMergeEnabled() {
        return mergeEnabled;
    }

    /**
     * Returns the id that identifies which kind of java classes this builder creates.
     */
    public String getKindId() {
        return kindId;
    }

    /**
     * Logs a CoreException to the build status of this builder. This method can only be called
     * during the build cycle.
     */
    protected void addToBuildStatus(CoreException e) {
        buildStatus.add(new IpsStatus(e));
    }

    /**
     * Logs the provided IStatus to the build status of this builder. This method can only be called
     * during the build cycle.
     */
    protected void addToBuildStatus(IStatus status) {
        buildStatus.add(status);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param element Any IPS element used to access the IPS project and determine the language for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * 
     * @deprecated Use {@link #getLocalizedToDo(IIpsElement, String)} instead as the builder is of
     *             no relevance.
     */
    @Deprecated
    // Deprecated since 3.0
    @SuppressWarnings("unused")
    // Suppressing warning that the parameter builder is not used since the method is deprecated
    // because of that
    public String getLocalizedToDo(IIpsElement element, String keyPrefix, JavaCodeFragmentBuilder builder) {
        return getLocalizedToDo(element, keyPrefix);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param element Any IPS element used to access the IPS project and determine the language for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     */
    public String getLocalizedToDo(IIpsElement element, String keyPrefix) {
        return getLocalizedToDo(element, keyPrefix, new Object[0]);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param element Any IPS element used to access the IPS project and determine the language for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacement An object to replace the wild card in the message text.
     */
    public String getLocalizedToDo(IIpsElement element, String keyPrefix, Object replacement) {
        return getLocalizedToDo(element, keyPrefix, new Object[] { replacement });
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param element Any IPS element used to access the IPS project and determine the language for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacements Any objects to replace wild cards in the message text.
     */
    public String getLocalizedToDo(IIpsElement element, String keyPrefix, Object[] replacements) {
        return "// TODO " + getLocalizedText(element, keyPrefix + "_TODO", replacements); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuikder.
     * <p>
     * Calling this method is only allowed during the build cycle. If it is called outside the build
     * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet has been set to this
     * builder a RuntimeException is thrown.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param element the IPS element used to access the IPS project where the language to use is
     *            defined.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            IIpsElement element,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        String text = getLocalizedText(element, keyPrefix + "_JAVADOC"); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(element, keyPrefix + "_ANNOTATION") }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(buf.toString(), annotations);
    }

    /**
     * Like {@link #appendLocalizedJavaDoc(String, IIpsElement, String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix, IIpsElement element, JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, element, (String)null, builder);
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuikder.
     * <p>
     * Calling this method is only allowed during the build cycle. If it is called outside the build
     * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet has been set to this
     * builder a RuntimeException is thrown.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacement Object that replaces the place holder {0} in the property file
     * @param element the IPS element used to access the IPS project where the language to use is
     *            defined.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            String modelDescription,
            IIpsElement element,
            JavaCodeFragmentBuilder builder) {

        String text = getLocalizedText(element, keyPrefix + "_JAVADOC", replacement); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(element, keyPrefix + "_ANNOTATION") }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(buf.toString(), annotations);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object, String, IIpsElement, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            IIpsElement element,
            JavaCodeFragmentBuilder builder) {

        appendLocalizedJavaDoc(keyPrefix, replacement, null, element, builder);
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuilder.
     * <p>
     * Calling this method is only allowed during the build cycle. If it is called outside the build
     * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet has been set to this
     * builder a RuntimeException is thrown.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacements Objects that replaces the place holders {0}, {1} etc. in the property
     *            file
     * @param element the IPS element used to access the IPS project where the language to use is
     *            defined.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            String modelDescription,
            IIpsElement element,
            JavaCodeFragmentBuilder builder) {

        String text = getLocalizedText(element, keyPrefix + "_JAVADOC", replacements); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(element, keyPrefix + "_ANNOTATION") }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(buf.toString(), annotations);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object[], String, IIpsElement, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            IIpsElement element,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, replacements, null, element, builder);
    }

    /**
     * Returns the modifier used to defined a method in an interface.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getModifierForInterfaceMethod() {
        return Modifier.PUBLIC + Modifier.ABSTRACT;
    }

    /**
     * Implementation of the build procedure of this builder.
     */
    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!isBuilderFor(ipsSrcFile)) {
            return;
        }
        if (!getBuilderSet().isGeneratePublishedInterfaces() && generatesInterface()) {
            return;
        }
        IFile javaFile = getJavaFile(ipsSrcFile);
        String content = generate();
        if (content == null || generationCanceled) {
            return;
        }

        boolean newFileCreated = createFileIfNotThere(javaFile);

        String oldJavaFileContentsStr = null;
        if (!newFileCreated) {
            String charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
            oldJavaFileContentsStr = getJavaFileContents(javaFile, charset);
            if (isMergeEnabled()) {
                content = merge(javaFile, oldJavaFileContentsStr, content);
            }
        }

        content = writeFeatureVersions(content);
        content = removeUnusedImports(content);
        content = format(content);

        /*
         * If merging is not activated and the old content of the file is equal compared to the new
         * content, then the new content is not written to the file.
         */
        if (content.equals(oldJavaFileContentsStr)) {
            return;
        } else {
            ByteArrayInputStream inputStream = transform(ipsSrcFile, content);
            writeToFile(javaFile, inputStream, true, false);
        }
    }

    private String getJavaFileContents(IFile javaFile, String charset) throws CoreException {
        InputStream javaFileContents = null;
        try {
            javaFileContents = javaFile.getContents(true);
            return StringUtil.readFromInputStream(javaFileContents, charset);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(
                    "An exception ocurred while trying to read the contents of the java file " + //$NON-NLS-1$
                            javaFile, e));
        } finally {
            closeStream(javaFileContents);
        }
    }

    private void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = getJavaFile(ipsSrcFile);
        IContainer parent = file.getParent();
        IFolder destination = getArtefactDestination(ipsSrcFile);
        if (file.exists()) {
            file.delete(true, null);
            if (!parent.equals(destination) && parent instanceof IFolder) {
                IFolder parentFolder = (IFolder)parent;
                if (parentFolder.members().length == 0) {
                    parentFolder.delete(true, null);
                }
            }
        }
    }

    public String getJavaDocCommentForOverriddenMethod() {
        return JavaGeneratorHelper.getJavaDocCommentForOverriddenMethod();
    }

    /**
     * Adds an <code>Override</code> annotation to the java code fragment if the java compliance
     * level is greater than 1.5. It takes into account the fine differences regarding the
     * <code>Override</code> annotation for compliance level 1.5 and higher.
     * 
     * @param fragmentBuilder the annotation is added to this {@link JavaCodeFragmentBuilder}
     * @param interfaceMethodImplementation to be able to decide if an Override annotation needs to
     *            be generated it must be known if the the generated method is an implementation of
     *            an interface method or an override of a super class method.
     */
    public void appendOverrideAnnotation(JavaCodeFragmentBuilder fragmentBuilder, boolean interfaceMethodImplementation) {
        JavaGeneratorHelper.appendOverrideAnnotation(fragmentBuilder, getIpsProject(), interfaceMethodImplementation);
    }

    /**
     * Appends the list of classNames as a list of generics to the given fragmentBuilder if
     * compliance level is at least Java5. e.g. if your classNames is [Integer, String], the code
     * 
     * <pre>
     * <Integer, String>
     * </pre>
     * 
     * is added to the fragment builder.
     */
    public void appendGenerics(JavaCodeFragmentBuilder fragmentBuilder, String... classeNames) {
        JavaGeneratorHelper.appendGenerics(fragmentBuilder, getIpsProject(), classeNames);
    }

    /**
     * Appends the list of classes as a list of generics to the given fragmentBuilder if compliance
     * level is at least Java5. e.g. if your classes are [Integer.class, String.class], the code
     * 
     * <pre>
     * <Integer, String>
     * </pre>
     * 
     * is added to the fragment builder.
     */
    public void appendGenerics(JavaCodeFragmentBuilder fragmentBuilder, Class<?>... classes) {
        JavaGeneratorHelper.appendGenerics(fragmentBuilder, getIpsProject(), classes);
    }

    private String removeUnusedImports(String content) {
        return new IpsRemoveImportsOperation().removeUnusedImports(content);
    }

    private String format(String content) {
        if (content == null) {
            return content;
        }
        IJavaProject javaProject = getIpsProject().getJavaProject();
        CodeFormatter formatter;
        if (javaProject != null) {
            formatter = ToolFactory.createCodeFormatter(javaProject.getOptions(true));
        } else {
            formatter = ToolFactory.createCodeFormatter(null);
        }
        /*
         * With parameter null the CodeFormatter is configured with the preferences that are
         * currently set.
         */
        TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, content,
                0, content.length(), 0, StringUtil.getSystemLineSeparator());

        if (edit == null) {
            return content;
        }
        Document doc = new Document(content);
        try {
            edit.apply(doc);
        } catch (MalformedTreeException e) {
            throw new RuntimeException(e);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        return doc.get();
    }

    /**
     * Returns the IFile for the provided IIpsSrcFile.
     */
    /**
     * Returns the IFile for the provided IIpsSrcFile.
     */
    public IFile getJavaFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFolder destinationFolder = getArtefactDestination(ipsSrcFile);

        IPath javaFile = getRelativeJavaFile(ipsSrcFile);

        return destinationFolder.getFile(javaFile);
    }

    /**
     * Return the path to the java file relative to the destination folder.
     * <p>
     * If the java class name is org.example.MyExample this method would return an {@link IPath} of
     * <em>/org/example/MyExample.java</em>
     */
    public IPath getRelativeJavaFile(IIpsSrcFile ipsSrcFile) {
        return getJavaClassNaming().getRelativeJavaFile(ipsSrcFile, BuilderAspect.getValue(generatesInterface()),
                getJavaClassNameProvider());
    }

    private JControlModel getJControlModel() {
        if (model == null) {
            throw new IllegalStateException("The jmerge control model has not been set, " + //$NON-NLS-1$
                    "while merging is activated. Possible reason for that might be that " + //$NON-NLS-1$
                    "the builder initialization method beforeBuildProcess(IIpsProject, int) " + //$NON-NLS-1$
                    "this class: " + JavaSourceFileBuilder.class + " has been overridden and " + //$NON-NLS-1$ //$NON-NLS-2$
                    "a call to the super class method has been forgotten."); //$NON-NLS-1$
        }
        return model;
    }

    private String merge(IFile javaFile, String oldContent, String newContent) throws CoreException {
        JMerger merger;
        try {
            merger = new JMerger(getJControlModel());
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("An error occurred while initializing JMerger.", e)); //$NON-NLS-1$
        }
        try {
            merger.setSourceCompilationUnit(merger.createCompilationUnitForContents(newContent));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Can't create JDT Compilation Unit for the new generated Java source: " + javaFile, e)); //$NON-NLS-1$
        }
        try {
            merger.setTargetCompilationUnit(merger.createCompilationUnitForContents(oldContent));
        } catch (Exception e) {
            throw new CoreException(
                    new IpsStatus(
                            "Can't create JDT Compilation Unit for the Java source existing Java Source. Probably the code does not compile. " + javaFile, e)); //$NON-NLS-1$
        }
        try {
            merger.merge();
            return merger.getTargetCompilationUnitContents();
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("An error occurred while trying to merge " + //$NON-NLS-1$
                    "the generated content with the old content of the file: " + javaFile, e)); //$NON-NLS-1$
        }
    }

    private void initJControlModel(IIpsProject project) throws CoreException {
        model = new JControlModel();
        if (ComplianceCheck.isComplianceLevelAtLeast5(project)) {
            ASTFacadeHelper astFacadeHelper = new ASTFacadeHelper();
            configureDefaults(astFacadeHelper.getJavaCoreOptions(), project);
            facadeHelper = astFacadeHelper;
        } else {
            facadeHelper = new JDOMFacadeHelper();
        }
        try {
            model.initialize(facadeHelper, getJMergeConfigLocation(project));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    @SuppressWarnings("unchecked")
    private void configureDefaults(Map<?, ?> javaCoreOptions, IIpsProject project) {
        IJavaProject javaProject = project.getJavaProject();
        javaCoreOptions.putAll(javaProject.getOptions(true));
    }

    private String getJMergeConfigLocation(IIpsProject ipsProject) {
        IFile mergeFile = ipsProject.getJavaProject().getProject()
                .getFile(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject) ? "merge.java5.xml" : "merge.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        if (mergeFile.exists()) {
            return mergeFile.getLocation().toPortableString();
        }
        StringBuffer mergeFileDefault = new StringBuffer();
        mergeFileDefault.append('/').append(JavaSourceFileBuilder.class.getPackage().getName().replace('.', '/'))
                .append(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject) ? "/merge.java5.xml" : "/merge.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        return Platform.getBundle(IpsPlugin.PLUGIN_ID).getResource(mergeFileDefault.toString()).toExternalForm();
    }

    private final static Pattern createFeatureSectionPattern() {
        StringBuffer buf = new StringBuffer();
        buf.append("/\\*.*"); //$NON-NLS-1$
        buf.append(BEGIN_FAKTORIPS_GENERATOR_INFORMATION_SECTION);
        buf.append(".*"); //$NON-NLS-1$
        buf.append(END_FAKTORIPS_GENERATOR_INFORMATION_SECTION);
        buf.append("[\\s\\*]*\\*/"); //$NON-NLS-1$
        return Pattern.compile(buf.toString(), Pattern.DOTALL);
    }

    private void createVersionSection() {
        StringBuffer versionSecionBuf = new StringBuffer();
        versionSecionBuf.append("/* "); //$NON-NLS-1$
        versionSecionBuf.append(BEGIN_FAKTORIPS_GENERATOR_INFORMATION_SECTION);
        versionSecionBuf.append(SystemUtils.LINE_SEPARATOR);
        versionSecionBuf.append(" * "); //$NON-NLS-1$
        versionSecionBuf.append(SystemUtils.LINE_SEPARATOR);

        versionSecionBuf.append(" * builder set: "); //$NON-NLS-1$
        versionSecionBuf.append(getBuilderSet().getId());
        versionSecionBuf.append(", Version: "); //$NON-NLS-1$
        versionSecionBuf.append(getBuilderSet().getVersion());

        versionSecionBuf.append(SystemUtils.LINE_SEPARATOR);
        versionSecionBuf.append(" * "); //$NON-NLS-1$
        versionSecionBuf.append(SystemUtils.LINE_SEPARATOR);
        versionSecionBuf.append(" * "); //$NON-NLS-1$
        versionSecionBuf.append(END_FAKTORIPS_GENERATOR_INFORMATION_SECTION);
        versionSecionBuf.append(SystemUtils.LINE_SEPARATOR);
        versionSecionBuf.append(" */"); //$NON-NLS-1$
        versionSection = versionSecionBuf.toString();
    }

    private String writeFeatureVersions(String source) {
        if (source == null) {
            return source;
        }
        Matcher m = FAKTORIPS_GENERATOR_INFORMATION_SECTION_PATTERN.matcher(source);
        StringBuffer newSource = new StringBuffer();
        if (m.find()) {
            if (m.start() > 0) {
                newSource.append(source.substring(0, m.start()));
            }
            newSource.append(versionSection);
            if (source.length() > m.end()) {
                newSource.append(source.substring(m.end(), source.length()));
            }
        } else {
            newSource.append(versionSection);
            newSource.append(SystemUtils.LINE_SEPARATOR);
            newSource.append(source);
        }
        return newSource.toString();
    }

    private ByteArrayInputStream transform(IIpsSrcFile ipsSrcFile, String content) throws CoreException {
        String charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
        try {
            return new ByteArrayInputStream(content.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new Status(IStatus.ERROR, "org.faktorips.std.builder", IStatus.OK, //$NON-NLS-1$
                    "The charset is not supported: " + charset, e)); //$NON-NLS-1$
        }
    }

    /**
     * Returns a list containing all <tt>IJavaElement</tt>s this builder generates for the given
     * <tt>IIpsObjectPartContainer</tt>.
     * <p>
     * Returns an empty list if no <tt>IJavaElement</tt>s are generated for the provided
     * <tt>IIpsObjectPartContainer</tt>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsObjectPartContainer The <tt>IIpsObjectPartContainer</tt> to obtain the generated
     *            <tt>IJavaElement</tt>s for.
     * 
     * @throws NullPointerException If the parameter is null
     */
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        if (ipsObjectPartContainer instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsObjectPartContainer;
            try {
                if (isBuilderFor(ipsObject.getIpsSrcFile())) {
                    javaElements.addAll(getGeneratedJavaTypes(ipsObject));
                }
            } catch (CoreException e) {
                return new ArrayList<IJavaElement>();
            }
        }

        getGeneratedJavaElementsThis(javaElements, ipsObjectPartContainer);

        return javaElements;
    }

    /**
     * Returns the Java types that this builder generates for the given <tt>IIpsObject</tt>.
     * 
     * @param ipsObject The <tt>IIpsObject</tt> to obtain the generated Java types for.
     * 
     * @throws NullPointerException If <tt>ipsObject</tt> is <tt>null</tt>.
     */
    public final List<IType> getGeneratedJavaTypes(IIpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);

        try {
            IFolder outputFolder = ipsObject.getIpsPackageFragment().getRoot()
                    .getArtefactDestination(buildsDerivedArtefacts());
            IPackageFragmentRoot javaRoot = ipsObject.getIpsProject().getJavaProject()
                    .getPackageFragmentRoot(outputFolder);
            String packageName = getPackage(ipsObject.getIpsSrcFile());
            IPackageFragment fragment = javaRoot.getPackageFragment(packageName);
            List<IType> javaTypes = new ArrayList<IType>(1);
            getGeneratedJavaTypesThis(ipsObject, fragment, javaTypes);
            return javaTypes;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the Java types generated to the given list. This method may be overridden by subclasses.
     */
    protected void getGeneratedJavaTypesThis(IIpsObject ipsObject, IPackageFragment fragment, List<IType> javaTypes)
            throws CoreException {

        String typeName = getUnqualifiedClassName(ipsObject.getIpsSrcFile());
        ICompilationUnit compilationUnit = fragment.getCompilationUnit(typeName + JavaClassNaming.JAVA_EXTENSION);
        javaTypes.add(compilationUnit.getType(typeName));
    }

    /**
     * Subclasses must add the <tt>IJavaElement</tt>s they generate for the given
     * <tt>IIpsObjectPartContainer</tt> to the provided list (collecting parameter pattern).
     * 
     * @param javaElements The list to add generated <tt>IJavaElement</tt>s to.
     * @param ipsObjectPartContainer The <tt>IIpsObjectPartContainer</tt> for that the client
     *            requested the generated <tt>IJavaElement</tt>s.
     */
    protected abstract void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Must return <tt>true</tt> if the source file generated by this builder is considered
     * published, <tt>false</tt> if not.
     * <p>
     * This does not consider whether we generate published interfaces or not. Hence if this method
     * returns false the generated artifacts are not always considered to be internal!
     * 
     * @see #isBuildingInternalArtifacts()
     */
    protected abstract boolean isBuildingPublishedSourceFile();

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code> if this builder generates internal artifacts. This is the case if
     * the generated artifact is not published and we do generate published interfaces. If we do not
     * generate published interfaces, there are not internal artifacts at all.
     * 
     * @see #isBuildingPublishedSourceFile()
     */
    @Override
    public final boolean isBuildingInternalArtifacts() {
        return !isBuildingPublishedSourceFile() && getBuilderSet().isGeneratePublishedInterfaces();
    }

    /**
     * Returns true if an interface is generated, false if a class is generated.
     */
    protected abstract boolean generatesInterface();

}
