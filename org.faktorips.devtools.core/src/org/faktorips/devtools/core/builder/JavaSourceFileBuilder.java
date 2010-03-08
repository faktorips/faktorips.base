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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
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
    public final static String[] ANNOTATION_GENERATED = new String[] { "generated" }; // $NON-NLSO-1$

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

    public final static String JAVA_EXTENSION = ".java"; //$NON-NLS-1$

    private final static String BEGIN_FAKTORIPS_GENERATOR_INFORMATION_SECTION = "BEGIN FAKTORIPS GENERATOR INFORMATION SECTION"; //$NON-NLS-1$

    private final static String END_FAKTORIPS_GENERATOR_INFORMATION_SECTION = "END FAKTORIPS GENERATOR INFORMATION SECTION"; //$NON-NLS-1$

    private final static Pattern FAKTORIPS_GENERATOR_INFORMATION_SECTION_PATTERN = createFeatureSectionPattern();

    private String versionSection;

    private boolean mergeEnabled;

    private String kindId;

    private IIpsObject ipsObject;

    private IIpsSrcFile ipsSrcFile;

    private LocalizedStringsSet localizedStringsSet;

    private boolean generationCanceled;

    private MultiStatus buildStatus;

    private JControlModel model;

    private Integer javaOptionsSplitLength;

    private Integer javaOptionsTabSize;

    /**
     * Creates a new JavaSourceFileBuilder.
     * 
     * @param builderSet the package information for the generated java source file and for other
     *            generated java classes within this package structure. Cannot be null.
     * @param kindId cannot be null
     * @param localizedStringsSet provides locale specific texts. It can be null. If the
     *            getLocalizedText() methods are called and the localizedStringsSet is not set an
     *            exception is thrown
     */
    public JavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet);
        ArgumentCheck.notNull(kindId, this);
        this.kindId = kindId;
        this.localizedStringsSet = localizedStringsSet;
        initJavaOptions();
    }

    // TODO duplicate code in LocalizedTextHelper
    private void initJavaOptions() {
        try {
            javaOptionsSplitLength = Integer.valueOf(JavaCore
                    .getOption(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT));
            javaOptionsTabSize = Integer.valueOf(JavaCore.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE));
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Unable to apply the java formatter options.", e)); //$NON-NLS-1$
        }
    }

    public Integer getJavaOptionsSplitLength() {
        return javaOptionsSplitLength;
    }

    public Integer getJavaOptionsTabSize() {
        return javaOptionsTabSize;
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
     * Returns the language in that variables, methods are named and and Java docs are written in.
     */
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return getBuilderSet().getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the Java naming convention to be used.
     */
    public JavaNamingConvention getJavaNamingConvention() {
        return JavaGeneratiorHelper.getJavaNamingConvention();
    }

    /**
     * Implementations of this class must override this method to provide the content of the java
     * source file.
     * 
     * @param monitor implementations can report the progress of the generation process to this
     *            monitor
     * @return the source file content
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
     * Returns the ips project, the builder is currently building for.
     */
    @Override
    public IIpsProject getIpsProject() {
        return getBuilderSet().getIpsProject();
    }

    /**
     * Convenience method that delegates the call to the package structure and returns the package
     * name for the java class that is build by this builder.
     * 
     * @param ipsSrcFile The source file to get the package from
     * @throws CoreException is delegated from calls to other methods
     */
    public String getPackage(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getPackageStructure().getPackage(getKindId(), ipsSrcFile);
    }

    /**
     * Calls getPackage(IpsObject). It is only allowed to call this method during the build cycle of
     * this builder.
     * 
     * @return the package string
     * @throws CoreException is delegated from calls to other methods
     */
    public String getPackage() throws CoreException {
        return getPackageStructure().getPackage(getKindId(), getIpsSrcFile());
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the ips object
     * stored in the given ips source file.
     * 
     * @param ipsSrcFile the ips source file.
     * @return the qualified class name
     * @throws CoreException is delegated from calls to other methods
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        StringBuffer buf = new StringBuffer();
        String packageName = getPackageStructure().getPackage(getKindId(), ipsSrcFile);
        if (packageName != null) {
            buf.append(packageName);
            buf.append('.');
        }
        buf.append(getUnqualifiedClassName(ipsSrcFile));
        return buf.toString();
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the given ips
     * object.
     * 
     * @param ipsObject the ips object.
     * @return the qualified class name
     * @throws CoreException
     */
    public String getQualifiedClassName(IIpsObject ipsObject) throws CoreException {
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * Calls getQualifiedClassName(IpsObject). It is only allowed to call this method during the
     * build cycle of this builder.
     * 
     * @return the qualified class name
     * @throws CoreException is delegated from calls to other methods
     */
    public String getQualifiedClassName() throws CoreException {
        return getQualifiedClassName(getIpsSrcFile());
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * 
     * @param ipsSrcFile the ips source file
     * @return the qualified class name
     * @throws CoreException is delegated from calls to other methods
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }

    /**
     * Calls getUnqualifiedClassName(IpsObject). It is only allowed to call this method during the
     * build cycle of this builder.
     * 
     * @return the unqualified class name
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
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param element the ips element used to access the ips project where the language to use is
     *            defined.
     * @param key the key that identifies the requested text
     * @return the requested text
     */
    public String getLocalizedText(IIpsElement element, String key) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns a single line comment containing a TO DO, e.g.
     * 
     * <pre>
     * // TODO Implement this rule.
     * </pre>
     * 
     * @param element Any ips element used to access the ips project and determine the langauge for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     */
    public String getLocalizedToDo(IIpsElement element, String keyPrefix, JavaCodeFragmentBuilder builder) {
        return getLocalizedToDo(element, keyPrefix, new Object[0]);
    }

    /**
     * Returns a single line comment containing a TO DO, e.g.
     * 
     * <pre>
     * // TODO Implement the rule xyz.
     * </pre>
     * 
     * @param element Any ips element used to access the ips project and determine the language for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacement An object to replace the wildcard in the message text.
     */
    public String getLocalizedToDo(IIpsElement element, String keyPrefix, Object replacement) {
        return getLocalizedToDo(element, keyPrefix, new Object[] { replacement });
    }

    /**
     * Returns a single line comment containing a TO DO, e.g.
     * 
     * <pre>
     * // TODO Implement the rule xyz.
     * </pre>
     * 
     * @param element Any ips element used to access the ips project and determine the language for
     *            the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacements Any objects to replace wildcards in the message text.
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
     * @param keyPrefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
     * @param element the ips element used to access the ips project where the language to use is
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
     * @param keyPrefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacement Object that replaces the placeholder {0} in the property file
     * @param element the ips element used to access the ips project where the language to use is
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
     * @param keyPrefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacements Objects that replaces the placeholders {0}, {1} etc. in the property file
     * @param element the ips element used to access the ips project where the language to use is
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
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param element the ips element used to access the ips project where the language to use is
     *            defined.
     * @param key the key that identifies the requested text
     * @param replacement an indicated region within the text is replaced by the string
     *            representation of this value
     * @return the requested text
     */
    public String getLocalizedText(IIpsElement element, String key, Object replacement) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode(), replacement);
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param element the ips element used to access the ips project where the language to use is
     *            defined.
     * @param key the key that identifies the requested text
     * @param replacements indicated regions within the text are replaced by the string
     *            representations of these values.
     * @return the requested text
     */
    public String getLocalizedText(IIpsElement element, String key, Object[] replacements) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode(), replacements);
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
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!isBuilderFor(ipsSrcFile)) {
            return;
        }

        IFile javaFile = getJavaFile(ipsSrcFile);
        String content = generate();
        if (content == null || generationCanceled) {
            return;
        }

        boolean newFileCreated = createFileIfNotThere(javaFile);

        if (newFileCreated) {
            content = writeFeatureVersions(content);
            String formattedContent = format(content);
            javaFile.setContents(transform(ipsSrcFile, formattedContent), true, false, null);
            return;
        }

        String charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
        String javaFileContentsStr = getJavaFileContents(javaFile, charset);
        if (isMergeEnabled()) {
            content = merge(javaFile, javaFileContentsStr, content);
        }
        content = writeFeatureVersions(content);
        String formattedContent = format(content);

        /*
         * If merging is not activated and the content of the file is equal compared to the
         * generated and formatted content then the new content is not written to the file.
         */
        if (formattedContent.equals(javaFileContentsStr)) {
            return;
        }
        javaFile.setContents(transform(ipsSrcFile, formattedContent), true, false, null);
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

    /**
     * {@inheritDoc}
     */
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
        return JavaGeneratiorHelper.getJavaDocCommentForOverriddenMethod();
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
        JavaGeneratiorHelper.appendOverrideAnnotation(fragmentBuilder, getIpsProject(), interfaceMethodImplementation);
    }

    /**
     * Appends the list of classNames as a list of generics to the given fragmentBuilder if
     * comliance level is at least Java5. e.g. if your classNames is [Integer, String], the code
     * 
     * <pre>
     * <Integer, String>
     * </pre>
     * 
     * is added to the fragment builder.
     * 
     * @param fragmentBuilder
     * @param ipsProject
     * @param classNames
     */
    public void appendGenerics(JavaCodeFragmentBuilder fragmentBuilder, String... classeNames) {
        JavaGeneratiorHelper.appendGenerics(fragmentBuilder, getIpsProject(), classeNames);
    }

    /**
     * Appends the list of classes as a list of generics to the given fragmentBuilder if comliance
     * level is at least Java5. e.g. if your classes are [Integer.class, String.class], the code
     * 
     * <pre>
     * <Integer, String>
     * </pre>
     * 
     * is added to the fragment builder.
     * 
     * @param fragmentBuilder
     * @param ipsProject
     * @param classes
     */
    public void appendGenerics(JavaCodeFragmentBuilder fragmentBuilder, Class<?>... classes) {
        JavaGeneratiorHelper.appendGenerics(fragmentBuilder, getIpsProject(), classes);
    }

    /**
     * Returns the localized string set of this builder.
     */
    private LocalizedStringsSet getLocalizedStringSet() {
        return localizedStringsSet;
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
        // with parameter null the CodeFormatter is configured with the
        // preferences that are currently set
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
     * Returns the artefact destination. The destination can eiter be the output folder for merable
     * artefacts or the one for derived artefacts.
     */
    protected IFolder getArtefactDestination(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (buildsDerivedArtefacts()) {
            return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true);
        }
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(false);
    }

    /**
     * Returns the IFile for the provided IIpsSrcFile.
     */
    public IFile getJavaFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFolder destinationFolder = getArtefactDestination(ipsSrcFile);

        String name = getQualifiedClassName(ipsSrcFile);
        int index = name.lastIndexOf('.');

        if (index == name.length()) {
            throw new RuntimeException("The qualified class name is not a valid java class name"); //$NON-NLS-1$
        }
        if (index == -1) {
            return destinationFolder.getFile(name + JAVA_EXTENSION);
        }
        String packageName = name.substring(0, index);
        String fileName = name.substring(index + 1, name.length());
        String[] packageFolders = packageName.split("\\."); //$NON-NLS-1$
        IFolder folder = destinationFolder;
        for (int i = 0; i < packageFolders.length; i++) {
            folder = folder.getFolder(packageFolders[i]);
        }
        return folder.getFile(fileName + JAVA_EXTENSION);
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
        IFile mergeFile = project.getJavaProject().getProject().getFile(
                ComplianceCheck.isComplianceLevelAtLeast5(project) ? "merge.java5.xml" : "merge.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        model = new org.eclipse.emf.codegen.merge.java.JControlModel();
        FacadeHelper facadeHelper;
        if (ComplianceCheck.isComplianceLevelAtLeast5(project)) {
            facadeHelper = new ASTFacadeHelper();
        } else {
            facadeHelper = new JDOMFacadeHelper();
        }
        if (mergeFile.exists()) {
            try {
                model.initialize(facadeHelper, mergeFile.getLocation().toPortableString());

            } catch (Exception e) {
                throw new CoreException(new IpsStatus(e));
            }
            return;
        }
        model.initialize(facadeHelper, getJMergeDefaultConfigLocation(project));
    }

    private String getJMergeDefaultConfigLocation(IIpsProject ipsProject) {
        StringBuffer mergeFile = new StringBuffer();
        mergeFile.append('/').append(JavaSourceFileBuilder.class.getPackage().getName().replace('.', '/')).append(
                ComplianceCheck.isComplianceLevelAtLeast5(ipsProject) ? "/merge.java5.xml" : "/merge.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        return Platform.getBundle(IpsPlugin.PLUGIN_ID).getResource(mergeFile.toString()).toExternalForm();
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
     * <tt>IIpsElement</tt>.
     * <p>
     * Returns an empty list if no <tt>IJavaElement</tt>s are generated for the provided
     * <tt>IIpsElement</tt>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to obtain the generated <tt>IJavaElement</tt>s
     *            for.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    public List<IJavaElement> getGeneratedJavaElements(IIpsElement ipsElement) {
        ArgumentCheck.notNull(ipsElement);

        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        if (ipsElement instanceof IIpsObject) {
            try {
                IIpsObject ipsObject = (IIpsObject)ipsElement;
                if (isBuilderFor(ipsObject.getIpsSrcFile())) {
                    IType javaType = getGeneratedJavaType(ipsObject);
                    javaElements.add(javaType);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        getGeneratedJavaElementsThis(javaElements, ipsElement);

        return javaElements;
    }

    /**
     * Returns the Java type that this builder generates for the given <tt>IIpsObject</tt>.
     * 
     * @param ipsObject The <tt>IIpsObject</tt> to obtain the generated Java type for.
     * 
     * @throws NullPointerException If <tt>ipsObject</tt> is <tt>null</tt>.
     */
    public final IType getGeneratedJavaType(IIpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);

        try {
            IIpsProject ipsProject = ipsObject.getIpsProject();
            IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
            IFolder outputFolder;
            String basePackageName;
            if (buildsDerivedArtefacts()) {
                outputFolder = ipsObjectPath.getOutputFolderForDerivedSources();
                basePackageName = ipsObjectPath.getBasePackageNameForDerivedJavaClasses();
            } else {
                outputFolder = ipsObjectPath.getOutputFolderForMergableSources();
                basePackageName = ipsObjectPath.getBasePackageNameForMergableJavaClasses();
            }

            if (ipsObjectPath.isOutputDefinedPerSrcFolder()) {
                IIpsPackageFragmentRoot ipsRoot = ipsObject.getIpsPackageFragment().getRoot();
                for (IIpsSrcFolderEntry entry : ipsObjectPath.getSourceFolderEntries()) {
                    if (entry.getIpsPackageFragmentRoot().equals(ipsRoot)) {
                        if (buildsDerivedArtefacts()) {
                            outputFolder = entry.getOutputFolderForDerivedJavaFiles();
                            basePackageName = entry.getBasePackageNameForDerivedJavaClasses();
                        } else {
                            outputFolder = entry.getOutputFolderForMergableJavaFiles();
                            basePackageName = entry.getBasePackageNameForMergableJavaClasses();
                        }
                        break;
                    }
                }
            }

            IPackageFragmentRoot javaRoot = ipsProject.getJavaProject().getPackageFragmentRoot(outputFolder);
            String internalPackageSeparator = isBuildingPublishedSourceFile() ? "" : ".internal";
            String qualifiedIpsObjectName = ipsObject.getQualifiedName();
            String packageName = "";
            if (qualifiedIpsObjectName.contains(".") && qualifiedIpsObjectName.length() > 1) {
                // FIXME FS#1684 AW: applied toLowerCase as a workaround.
                packageName = "."
                        + qualifiedIpsObjectName.substring(0, qualifiedIpsObjectName.lastIndexOf('.')).toLowerCase();
            }
            IPackageFragment fragment = javaRoot.getPackageFragment(basePackageName + internalPackageSeparator
                    + packageName);

            String typeName = getUnqualifiedClassName(ipsObject.getIpsSrcFile());
            ICompilationUnit compilationUnit = fragment.getCompilationUnit(typeName + JAVA_EXTENSION);
            return compilationUnit.getType(typeName);

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Subclasses must add the <tt>IJavaElement</tt>s they generate for the given
     * <tt>IIpsElement</tt> to the provided list (collecting parameter pattern).
     * 
     * @param javaElements The list to add generated <tt>IJavaElement</tt>s to.
     * @param ipsElement The <tt>IIpsElement</tt> for that the client requested the generated
     *            <tt>IJavaElement</tt>s.
     */
    protected abstract void getGeneratedJavaElementsThis(List<IJavaElement> javaElements, IIpsElement ipsElement);

    /**
     * Must return <tt>true</tt> if the source file generated by this builder is considered
     * published, <tt>false</tt> if not.
     */
    public abstract boolean isBuildingPublishedSourceFile();

}
