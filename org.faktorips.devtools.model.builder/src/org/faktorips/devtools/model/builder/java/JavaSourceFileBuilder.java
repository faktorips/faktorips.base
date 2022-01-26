/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.codegen.merge.java.JControlModel;
import org.eclipse.emf.codegen.merge.java.JMerger;
import org.eclipse.emf.codegen.merge.java.facade.FacadeHelper;
import org.eclipse.emf.codegen.merge.java.facade.ast.ASTFacadeHelper;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.builder.IJavaPackageStructure;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.builder.naming.JavaClassNaming;
import org.faktorips.devtools.model.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.model.builder.organizeimports.IpsRemoveImportsOperation;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.Preferences;

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
    public static final String ANNOTATION_GENERATED = "generated"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used as a Javadoc annotation. It becomes relevant if the
     * merging capabilities are activated. It indicates that within a generated piece of code only
     * the parts that are outside the braces defined by the markers <code>//begin-user-code</code>
     * and <code>//end-user-code</code> are regenerated with the next generation.
     */
    public static final String ANNOTATION_RESTRAINED_MODIFIABLE = "restrainedmodifiable"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used as a Java 5 <code>Override</code> annotation.
     */
    public static final String ANNOTATION_OVERRIDE = "Override"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used to indicate the beginning of a section within generated
     * code that a user can modify and will not be overridden by the generator at the next
     * generation.
     */
    public static final String MARKER_BEGIN_USER_CODE = "//begin-user-code"; //$NON-NLS-1$

    /**
     * This constant is supposed to be used to indicate the end of a section within generated code
     * that a user can modify and will not be overridden by the generator at the next generation.
     */
    public static final String MARKER_END_USER_CODE = "//end-user-code"; //$NON-NLS-1$

    /**
     * The default java doc comment for overridden methods.
     */
    public static final String INHERIT_DOC = "{@inheritDoc}"; //$NON-NLS-1$

    public static final String PLUGIN_ID = "org.faktorips.devtools.model.builder"; //$NON-NLS-1$

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
    public void afterBuildProcess(IIpsProject project, ABuildKind buildKind) throws CoreRuntimeException {
        model = null;
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, ABuildKind buildKind) throws CoreRuntimeException {
        initJControlModel(project);
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
     * 
     * @deprecated since 3.22. Access to builder settings is builder set specific.
     */
    @Deprecated
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
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
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
                description = IIpsModel.get().getMultiLanguageSupport().getDefaultDescription(describedElement);
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
     * @throws CoreRuntimeException implementations can wrap rising checked exceptions into a
     *             CoreRuntimeException. If an exception is thrown by this method the current build
     *             of this builder is interrupted. Alternatively the exception can be reported to
     *             the buildStatus to avoid interrupting the build process of this builder.
     */
    protected abstract String generate() throws CoreRuntimeException;

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
     * @throws CoreRuntimeException is delegated from calls to other methods
     */
    public final String getPackage(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
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
     * @throws CoreRuntimeException is delegated from calls to other methods
     */
    public final String getPackage() throws CoreRuntimeException {
        return getPackage(getIpsSrcFile());
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the IPS object
     * stored in the given IPS source file.
     * 
     * @param ipsSrcFile the IPS source file.
     * 
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return getJavaClassNaming().getQualifiedClassName(ipsSrcFile, BuilderAspect.getValue(generatesInterface()),
                getJavaClassNameProvider());
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the given IPS
     * object.
     * 
     * @param ipsObject the IPS object.
     */
    public String getQualifiedClassName(IIpsObject ipsObject) {
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * Calls getQualifiedClassName(IpsObject). It is only allowed to call this method during the
     * build cycle of this builder.
     * 
     * @return the qualified class name
     * 
     */
    public String getQualifiedClassName() {
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
     * @throws CoreRuntimeException is delegated from calls to other methods
     */
    public final String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        return getJavaClassNaming().getUnqualifiedClassName(ipsSrcFile, BuilderAspect.getValue(generatesInterface()),
                getJavaClassNameProvider());
    }

    /**
     * Calls getUnqualifiedClassName(IpsObject). It is only allowed to call this method during the
     * build cycle of this builder.
     * 
     * @throws CoreRuntimeException is delegated from calls to other methods
     */
    public String getUnqualifiedClassName() throws CoreRuntimeException {
        return getUnqualifiedClassName(getIpsSrcFile());
    }

    /**
     * This method has been overridden for convenience. Subclasses might need to implement this
     * method to clean up the state of the builder that was created during the generation.
     * 
     * @see org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder#afterBuild(org.faktorips.devtools.model.ipsobject.IIpsSrcFile)
     */
    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
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
     * @see org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder#beforeBuild(org.faktorips.devtools.model.ipsobject.IIpsSrcFile,
     *      org.eclipse.core.runtime.MultiStatus)
     */
    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreRuntimeException {
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
        return "// TODO " + getLocalizedText(keyPrefix + "_TODO", replacements); //$NON-NLS-1$ //$NON-NLS-2$
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
     * @param element The IPS element for which this java doc is created. It is used for getting
     *            additional information like the since version. If the generated code part is not
     *            for a specific model element this element could be <code>null</code>.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            IIpsObjectPartContainer element,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, modelDescription, element, builder, new Object[] {});
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, IIpsObjectPartContainer, String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            IIpsObjectPartContainer element,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, element, (String)null, builder);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, IIpsObjectPartContainer, String, JavaCodeFragmentBuilder)}
     * without a description and without element.
     */
    public void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, null, (String)null, builder);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, IIpsObjectPartContainer, String, JavaCodeFragmentBuilder)}
     * without an element.
     */
    public void appendLocalizedJavaDoc(String keyPrefix, String modelDescription, JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, null, modelDescription, builder);
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
     * @param element The IPS element for which this java doc is created. It is used for getting
     *            additional information like the since version. If the generated code part is not
     *            for a specific model element this element could be <code>null</code>.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            String modelDescription,
            IIpsObjectPartContainer element,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, modelDescription, element, builder, replacement);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object, String, IIpsObjectPartContainer, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            IIpsObjectPartContainer element,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, replacement, null, element, builder);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, String, IIpsObjectPartContainer, JavaCodeFragmentBuilder, Object[])}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            IIpsObjectPartContainer element,
            JavaCodeFragmentBuilder builder,
            Object... replacements) {
        appendLocalizedJavaDoc(keyPrefix, (String)null, element, builder, replacements);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, String, IIpsObjectPartContainer, JavaCodeFragmentBuilder, Object[])}
     * without a description and without element.
     */
    public void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder, Object... replacements) {
        appendLocalizedJavaDoc(keyPrefix, null, null, builder, replacements);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, String, IIpsObjectPartContainer, JavaCodeFragmentBuilder, Object[])}
     * without a the element.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            String modelDescription,
            JavaCodeFragmentBuilder builder,
            Object... replacements) {
        appendLocalizedJavaDoc(keyPrefix, modelDescription, null, builder, replacements);
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
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param element The IPS element for which this java doc is created. It is used for getting
     *            additional information like the since version. If the generated code part is not
     *            for a specific model element this element could be <code>null</code>.
     * @param builder the builder the Javadoc is appended to.
     * @param replacements Objects that replaces the place holders {0}, {1} etc. in the property
     *            file
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            String modelDescription,
            IIpsObjectPartContainer element,
            JavaCodeFragmentBuilder builder,
            Object... replacements) {

        String text = getLocalizedText(keyPrefix + "_JAVADOC", replacements); //$NON-NLS-1$
        List<String> annotations = getJavaDocTags(element, keyPrefix, builder);
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        if (modelDescription != null) {
            sb.append(System.lineSeparator()).append(modelDescription);
        }
        builder.javaDoc(sb.toString(), annotations.toArray(new String[annotations.size()]));
    }

    /**
     * Create generic java doc tags. The default implementation searches for localized annotations
     * with the key <code>{keyPrefix}_ANNOTATION</code>. You could overwrite this class to provide
     * further generic java doc tags that should be appended by
     * {@link #appendLocalizedJavaDoc(String, IIpsObjectPartContainer, JavaCodeFragmentBuilder)}
     * 
     * @param element The {@link IIpsElement} for which the java doc is generated
     * @param keyPrefix The keyPrefix for messages should be found by adding _ANNOTATION
     * @param builder The {@link JavaCodeFragmentBuilder} that is currently used. Normally you do
     *            not need to add anything but maybe you want to provide some import statements.
     * @return The tags that should be added to the java doc (also known as annotations)
     */
    protected List<String> getJavaDocTags(IIpsObjectPartContainer element,
            String keyPrefix,
            JavaCodeFragmentBuilder builder) {
        String localizedAnnotationText = getLocalizedText(keyPrefix + "_ANNOTATION"); //$NON-NLS-1$
        return Arrays.asList(localizedAnnotationText);
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
    public void build(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        if (!isBuilderFor(ipsSrcFile)) {
            return;
        }
        if (!getBuilderSet().isGeneratePublishedInterfaces() && generatesInterface()) {
            return;
        }
        AFile javaFile = getJavaFile(ipsSrcFile);
        String content = generate();
        if (content == null || generationCanceled) {
            return;
        }

        boolean newFileCreated = createFileIfNotThere(javaFile);

        String oldJavaFileContentsStr = null;
        if (!newFileCreated) {
            Charset charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
            oldJavaFileContentsStr = getJavaFileContents(javaFile, charset);
        }
        if (isMergeEnabled()) {
            content = merge(javaFile, oldJavaFileContentsStr, content);
        }

        content = removeUnusedImports(content);
        content = format(content, newFileCreated);

        /*
         * If merging is not activated and the old content of the file is equal compared to the new
         * content, then the new content is not written to the file.
         */
        if (content.equals(oldJavaFileContentsStr)) {
            return;
        } else {
            writeToFile(ipsSrcFile, javaFile, content);
        }
    }

    /* private */ void writeToFile(IIpsSrcFile ipsSrcFile, AFile javaFile, String content) throws CoreRuntimeException {
        ByteArrayInputStream inputStream = transform(ipsSrcFile, content);
        writeToFile(javaFile, inputStream, true, false);
    }

    /* private */ protected String getJavaFileContents(AFile javaFile, Charset charset) throws CoreRuntimeException {
        InputStream javaFileContents = null;
        try {
            javaFileContents = javaFile.getContents();
            return StringUtil.readFromInputStream(javaFileContents, charset);
        } catch (IOException e) {
            throw new CoreRuntimeException(
                    new IpsStatus("An exception ocurred while trying to read the contents of the java file " //$NON-NLS-1$
                            + javaFile, e));
        } finally {
            closeStream(javaFileContents);
        }
    }

    private void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        AFile file = getJavaFile(ipsSrcFile);
        AContainer parent = file.getParent();
        AResource destination = getArtefactDestination(ipsSrcFile).getResource();
        if (file.exists()) {
            file.delete(null);
            if (!parent.equals(destination) && parent instanceof AFolder) {
                AFolder parentFolder = (AFolder)parent;
                if (parentFolder.getMembers().size() == 0) {
                    parentFolder.delete(null);
                }
            }
        }
    }

    public String getJavaDocCommentForOverriddenMethod() {
        return JavaSourceFileBuilder.INHERIT_DOC;
    }

    private String removeUnusedImports(String content) {
        return new IpsRemoveImportsOperation().removeUnusedImports(content);
    }

    /**
     * Returns the preferred line separator, as defined in the eclipse settings. If the preference
     * is not set, the system line separator will be returned.
     */
    protected String getLineSeparatorPreference() {
        Preferences preferences = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE)
                .node(getIpsProject().getName());
        return preferences.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR,
                System.lineSeparator());
    }

    private String format(String content, boolean newFileCreated) {
        if (content == null) {
            return content;
        }
        AJavaProject javaProject = getIpsProject().getJavaProject();
        CodeFormatter formatter;
        if (javaProject != null) {
            formatter = ToolFactory.createCodeFormatter(javaProject.getOptions());
        } else {
            formatter = ToolFactory.createCodeFormatter(null);
        }

        Document doc = new Document(content);

        String separator = null;
        if (newFileCreated) {
            separator = getLineSeparatorPreference();
        } else {
            separator = doc.getDefaultLineDelimiter();
            if (separator == null) {
                separator = getLineSeparatorPreference();
            }
        }

        /*
         * With parameter null the CodeFormatter is configured with the preferences that are
         * currently set.
         */
        TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, content,
                0, content.length(), 0, separator);

        if (edit == null) {
            return content;
        }

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
     * Returns the AFile for the provided IIpsSrcFile.
     */
    /**
     * Returns the AFile for the provided IIpsSrcFile.
     */
    public AFile getJavaFile(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        APackageFragmentRoot destinationFolder = getArtefactDestination(ipsSrcFile);

        Path javaFile = getRelativeJavaFile(ipsSrcFile);

        return ((AContainer)destinationFolder.getResource()).getFile(javaFile);
    }

    /**
     * Return the path to the java file relative to the destination folder.
     * <p>
     * If the java class name is org.example.MyExample this method would return an {@link IPath} of
     * <em>/org/example/MyExample.java</em>
     */
    public Path getRelativeJavaFile(IIpsSrcFile ipsSrcFile) {
        return getJavaClassNaming().getRelativeJavaFile(ipsSrcFile, BuilderAspect.getValue(generatesInterface()),
                getJavaClassNameProvider());
    }

    private JControlModel getJControlModel() {
        if (model == null) {
            throw new IllegalStateException("The jmerge control model has not been set, " //$NON-NLS-1$
                    + "while merging is activated. Possible reason for that might be that " //$NON-NLS-1$
                    + "the builder initialization method beforeBuildProcess(IIpsProject, int) " //$NON-NLS-1$
                    + "this class: " + JavaSourceFileBuilder.class + " has been overridden and " //$NON-NLS-1$ //$NON-NLS-2$
                    + "a call to the super class method has been forgotten."); //$NON-NLS-1$
        }
        return model;
    }

    private String merge(AFile javaFile, String oldContent, String newContent) throws CoreRuntimeException {
        // CSOFF: IllegalCatch
        JMerger merger;
        try {
            merger = new JMerger(getJControlModel());
            List<String> additionalImports = getBuilderSet().getAdditionalImports();
            List<String> additionalAnnotations = getBuilderSet().getAdditionalAnnotations();
            merger.setAdditionalAnnotations(additionalImports, additionalAnnotations);
            List<String> retainedAnnotations = getBuilderSet().getRetainedAnnotations();
            merger.setRetainedAnnotations(retainedAnnotations);
        } catch (Exception e) {
            throw new CoreRuntimeException(new IpsStatus("An error occurred while initializing JMerger.", e)); //$NON-NLS-1$
        }
        try {
            merger.setSourceCompilationUnit(merger.createCompilationUnitForContents(newContent));
        } catch (Exception e) {
            throw new CoreRuntimeException(new IpsStatus(
                    "Can't create JDT Compilation Unit for the new generated Java source: " + javaFile, e)); //$NON-NLS-1$
        }
        try {
            merger.setTargetCompilationUnit(merger.createCompilationUnitForContents(oldContent));
        } catch (Exception e) {
            throw new CoreRuntimeException(new IpsStatus(
                    "Can't create JDT Compilation Unit for the Java source existing Java Source. Probably the code does not compile. " //$NON-NLS-1$
                            + javaFile,
                    e));
        }
        try {
            merger.merge();
            return merger.getTargetCompilationUnitContents();
        } catch (Exception e) {
            throw new CoreRuntimeException(new IpsStatus("An error occurred while trying to merge " //$NON-NLS-1$
                    + "the generated content with the old content of the file: " + javaFile, e)); //$NON-NLS-1$
        }
        // CSON: IllegalCatch
    }

    private void initJControlModel(IIpsProject project) throws CoreRuntimeException {
        // CSOFF: IllegalCatch
        model = new JControlModel();
        ASTFacadeHelper astFacadeHelper = new ASTFacadeHelper();
        configureDefaults(astFacadeHelper.getJavaCoreOptions(), project);
        facadeHelper = astFacadeHelper;
        try {
            model.initialize(facadeHelper, getJMergeConfigLocation(project));
        } catch (Exception e) {
            throw new CoreRuntimeException(new IpsStatus(e));
        }
        // CSON: IllegalCatch
    }

    private void configureDefaults(Map<String, String> javaCoreOptions, IIpsProject project) {
        AJavaProject javaProject = project.getJavaProject();
        javaCoreOptions.putAll(javaProject.getOptions());
    }

    private String getJMergeConfigLocation(IIpsProject ipsProject) {
        AFile mergeFile = ipsProject.getJavaProject().getProject().getFile("merge.java5.xml"); //$NON-NLS-1$
        if (mergeFile.exists()) {
            return mergeFile.getLocation().toString();
        }
        StringBuilder mergeFileDefault = new StringBuilder();
        mergeFileDefault.append('/').append(JavaSourceFileBuilder.class.getPackage().getName().replace('.', '/'))
                .append("/merge.java5.xml"); //$NON-NLS-1$
        Bundle bundle = Platform.getBundle(PLUGIN_ID);
        return getFileNameFromBundle(bundle, mergeFileDefault.toString());
    }

    private String getFileNameFromBundle(Bundle bundle, String mergeFileName) {
        if (bundle != null) {
            URL resource = bundle.getResource(mergeFileName);
            if (resource != null) {
                return resource.toExternalForm();
            } else {
                throw new IllegalArgumentException("Cannot find jmerge configuration " + mergeFileName); //$NON-NLS-1$
            }
        } else {
            throw new IllegalStateException("Cannot accedd Ips Plugin."); //$NON-NLS-1$
        }
    }

    private ByteArrayInputStream transform(IIpsSrcFile ipsSrcFile, String content) throws CoreRuntimeException {
        Charset charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
        return new ByteArrayInputStream(content.getBytes(charset));
    }

    /**
     * Returns a list containing all <code>IJavaElement</code>s this builder generates for the given
     * <code>IIpsObjectPartContainer</code>.
     * <p>
     * Returns an empty list if no <code>IJavaElement</code>s are generated for the provided
     * <code>IIpsObjectPartContainer</code>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsObjectPartContainer The <code>IIpsObjectPartContainer</code> to obtain the
     *            generated <code>IJavaElement</code>s for.
     * 
     * @throws NullPointerException If the parameter is null
     */
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);
        List<IJavaElement> javaElements = new ArrayList<>();
        if (ipsObjectPartContainer instanceof IIpsObject) {
            try {
                if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
                    javaElements.addAll(getGeneratedJavaTypes((IIpsObject)ipsObjectPartContainer));
                }
            } catch (CoreRuntimeException e) {
                return new ArrayList<>();
            }
        }

        getGeneratedJavaElementsThis(javaElements, ipsObjectPartContainer);
        return javaElements;
    }

    /**
     * Returns the Java types that this builder generates for the given <code>IIpsObject</code>.
     * 
     * @param ipsObject The <code>IIpsObject</code> to obtain the generated Java types for.
     * 
     * @throws NullPointerException If <code>ipsObject</code> is <code>null</code>.
     */
    public final List<IType> getGeneratedJavaTypes(IIpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);

        try {
            IPackageFragmentRoot javaRoot = ipsObject.getIpsPackageFragment().getRoot()
                    .getArtefactDestination(buildsDerivedArtefacts()).unwrap();
            String packageName = getPackage(ipsObject.getIpsSrcFile());
            IPackageFragment fragment = javaRoot.getPackageFragment(packageName);
            List<IType> javaTypes = new ArrayList<>(1);
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
        IType type = getJavaType(fragment, typeName);
        javaTypes.add(type);
    }

    protected IType getJavaType(IPackageFragment fragment, String typeName) throws JavaModelException {
        IType type = fragment.getJavaProject().findType(fragment.getElementName() + '.' + typeName);
        if (type == null || !type.exists()) {
            // maybe type is not compiled yet, we could still try to get the source
            ICompilationUnit compilationUnit = fragment.getCompilationUnit(typeName + JavaClassNaming.JAVA_EXTENSION);
            type = compilationUnit.getType(typeName);
        }
        return type;
    }

    /**
     * Subclasses must add the <code>IJavaElement</code>s they generate for the given
     * <code>IIpsObjectPartContainer</code> to the provided list (collecting parameter pattern).
     * 
     * @param javaElements The list to add generated <code>IJavaElement</code>s to.
     * @param ipsObjectPartContainer The <code>IIpsObjectPartContainer</code> for that the client
     *            requested the generated <code>IJavaElement</code>s.
     */
    protected abstract void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Must return <code>true</code> if the source file generated by this builder is considered
     * published, <code>false</code> if not.
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
     * Returns <code>true</code> if the generated artifact is not published and we do generate
     * published interfaces. If we do not generate published interfaces, there are no internal
     * artifacts except for the generated artifact of EnumXmlAdapterBuilder.
     * 
     * @see #isBuildingPublishedSourceFile()
     */
    @Override
    public boolean isBuildingInternalArtifacts() {
        return !isBuildingPublishedSourceFile() && getBuilderSet().isGeneratePublishedInterfaces();
    }

    /**
     * Returns true if an interface is generated, false if a class is generated.
     */
    protected abstract boolean generatesInterface();

}
