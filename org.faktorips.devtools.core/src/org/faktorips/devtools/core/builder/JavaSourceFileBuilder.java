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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.codegen.jmerge.JControlModel;
import org.eclipse.emf.codegen.jmerge.JMerger;
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
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * An implementation of <code>IpsArtefactBuilder</code> that generates a java
 * source file for a specific IpsObject. It provides support for formatting of
 * the java source file content and merging of the content with the content of
 * an already existing java file that has the same file name. A
 * JavaSourceFileBuilder needs a IJavaPackageStructure that provides the package
 * information about the java source file. A kindId has to be specified at
 * instantiation time. The kindId is used within the IjavaPackageStructure
 * implementation to identify the builder. To generate the actual content of the
 * java source file implementations must override the generate(IProgressMonitor)
 * method.
 * 
 * @author Peter Erzberger
 */
public abstract class JavaSourceFileBuilder extends AbstractArtefactBuilder {

	/**
	 * This constant is supposed to be used as a javadoc annotation. If the
	 * merging capabilities are activated a class, method or attribute that is
	 * marked by this annotation will be regenerated with every build.
	 */
	public final static String[] ANNOTATION_GENERATED = new String[] { "generated" }; //$NON-NLS-1$

	/**
	 * This constant is supposed to be used as a javadoc annotation. It becomes
	 * relevant if the merging capabilities are activated. It indicates that a
	 * piece of code was generated in the first place and will not be overridden
	 * by the generator by further builds.
	 */
	public final static String[] ANNOTATION_MODIFIABLE = new String[] { "modifiable" }; //$NON-NLS-1$

	/**
	 * This constant is supposed to be used as a javadoc annotation. It becomes
	 * relevant if the merging capabilities are activated. It indicates that within a
	 * generated piece of code only the parts that are outside the braces defined
	 * by the markers <code>//begin-user-code</code> and <code>//end-user-code</code>
	 * are regenerated with the next generation. 
	 */
	public final static String[] ANNOTATION_RESTRAINED_MODIFIABLE = new String[]{ "restrainedmodifiable" }; //$NON-NLS-1$
	
	/**
	 * This constant is supposed to be used to indicate the beginning of a section within 
	 * generated code that a user can modify and will not be overridden by the generator 
	 * at the next generation.
	 */
	public final static String MARKER_BEGIN_USER_CODE = "//begin-user-code"; //$NON-NLS-1$

	/**
	 * This constant is supposed to be used to indicate the end of a section within 
	 * generated code that a user can modify and will not be overridden by the generator 
	 * at the next generation.
	 */
	public final static String MARKER_END_USER_CODE = "//end-user-code"; //$NON-NLS-1$

	protected final static String JAVA_EXTENSION = ".java"; //$NON-NLS-1$
    
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
	 * @param builderSet
	 *            the package information for the generated java source file and
	 *            for other generated java classes within this package
	 *            structure. Cannot be null.
	 * @param kindId
	 *            cannot be null
	 * @param localizedStringsSet
	 *            provides locale specific texts. It can be null. If the
	 *            getLocalizedText() methods are called and the
	 *            localizedStringsSet is not set an exception is thrown
	 */
	public JavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet,
			String kindId, LocalizedStringsSet localizedStringsSet) {
		super(builderSet);
		ArgumentCheck.notNull(kindId, this);
		this.kindId = kindId;
		this.localizedStringsSet = localizedStringsSet;
        initJavaOptions();
	}

    private void initJavaOptions(){
        try {
            javaOptionsSplitLength = Integer.valueOf(JavaCore.getOption(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT));
            javaOptionsTabSize = Integer.valueOf(JavaCore.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE));
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Unable to apply the java formatter options.", e)); //$NON-NLS-1$
        }
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
		model = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
		initJControlModel(project);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return StringUtil.unqualifiedName(getClass().getName());
	}
	
	/**
	 * Returns the naming convention for product changes over time. 
	 */
	public IChangesOverTimeNamingConvention getChangesInTimeNamingConvention(IIpsElement element) {
		return element.getIpsProject().getChangesInTimeNamingConventionForGeneratedCode();
	}

    /**
	 * Returns the language in that variables, methods are named and and Java docs are 
	 * written in.
	 */
    public Locale getLanguageUsedInGeneratedSourceCode(IIpsElement element) {
    	return element.getIpsProject().getGeneratedJavaSourcecodeDocumentationLanguage();
    }
	
    /**
	 * Returns the Java naming convention to be used.
	 */
	public JavaNamingConvention getJavaNamingConvention() {
		return JavaNamingConvention.ECLIPSE_STANDARD;
	}
	
	/**
	 * Implementations of this class must override this method to provide the
	 * content of the java source file.
	 * 
	 * @param monitor
	 *            implementations can report the progress of the generation
	 *            process to this monitor
	 * @return the source file content
	 * @throws CoreException
	 *             implementations can wrap rising checked exceptions into a
	 *             CoreException. If an exception is thrown by this method the
	 *             current build of this builder is interrupted. Alternatively
	 *             the exception can be reported to the buildStatus to avoid
	 *             interrupting the build process of this builder.
	 * 
	 */
	protected abstract String generate() throws CoreException;

	/**
	 * Returns the IpsObject provided to this builder. It returns the IpsObject
	 * only during the generating phase otherwise null is returned.
	 */
	public IIpsObject getIpsObject() {
		return ipsObject;
	}

	/**
	 * Returns the IpsSrcFile provided to this builder. It returns the
	 * IpsSrcFile only during the generating phase otherwise null is returned.
	 */
	public IIpsSrcFile getIpsSrcFile() {
		return ipsSrcFile;
	}

	/**
	 * Convenience method that delegates the call to the package structure and
	 * returns the package name for the java class that is build by this
	 * builder.
	 * 
	 * @param the
	 *            package string
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getPackage(IIpsSrcFile ipsSrcFile) throws CoreException {
		return getPackageStructure().getPackage(getKindId(), ipsSrcFile);
	}

	/**
	 * Calls getPackage(IpsObject). It is only allowed to call this method
	 * during the build cycle of this builder.
	 * 
	 * @return the package string
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getPackage() throws CoreException {
		return getPackageStructure().getPackage(getKindId(), getIpsSrcFile());
	}

	/**
	 * Returns the qualified name of the Java class generated by this builder
	 * for the ips object stored in the given ips source file.
	 * 
	 * @param ipsSrcFile the ips source file.
	 * @return the qualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getQualifiedClassName(IIpsSrcFile ipsSrcFile)
			throws CoreException {
		StringBuffer buf = new StringBuffer();
		String packageName = getPackageStructure().getPackage(getKindId(),
				ipsSrcFile);
		if (packageName != null) {
			buf.append(packageName);
			buf.append('.');
		}
		buf.append(getUnqualifiedClassName(ipsSrcFile));
		return buf.toString();
	}
	
	/**
	 * Returns the qualified name of the Java class generated by this builder
	 * for the given ips object.
	 * 
	 * @param ipsObject the ips object.
	 * @return the qualified class name
	 * @throws CoreException 
	 */
	public String getQualifiedClassName(IIpsObject ipsObject) throws CoreException {
		return getQualifiedClassName(ipsObject.getIpsSrcFile());
	}

	/**
	 * Calls getQualifiedClassName(IpsObject). It is only allowed to call this
	 * method during the build cycle of this builder.
	 * 
	 * @return the qualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getQualifiedClassName() throws CoreException {
		return getQualifiedClassName(getIpsSrcFile());
	}

	/**
	 * Returns the unqualified name for Java class generated by this builder for
	 * the given ips source file.
	 * 
	 * @param ipsSrcFile 
	 *            the ips source file
	 * @return the qualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile)
			throws CoreException {
		return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
	}

	/**
	 * Calls getUnqualifiedClassName(IpsObject). It is only allowed to call this
	 * method during the build cycle of this builder.
	 * 
	 * @return the unqualified class name
	 * @throws CoreException
	 *             is delegated from calls to other methods
	 */
	public String getUnqualifiedClassName() throws CoreException {
		return getUnqualifiedClassName(getIpsSrcFile());
	}

	/**
	 * This method has been overriden for convinence. Subclasses might need to
	 * implement this method to clean up the state of the builder that was
	 * created during the generation.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#afterBuild(org.faktorips.devtools.core.model.IIpsSrcFile)
	 */
	public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
		ipsSrcFile = null;
		ipsObject = null;
		buildStatus = null;
		generationCanceled = false;
	}

	/**
	 * This method has been overriden for convinence. Subclasses might need to
	 * implement this method to set up a defined state before the generation
	 * starts.
	 * 
	 * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#beforeBuild(org.faktorips.devtools.core.model.IIpsSrcFile,
	 *      org.eclipse.core.runtime.MultiStatus)
	 */
	public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status)
			throws CoreException {
		this.ipsSrcFile = ipsSrcFile;
		this.buildStatus = status;
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
	 * Can be used by subclasses within the implementation of the generate()
	 * method to prevent this builder from creating a java source file.
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
	 * Returns the id that identifies which kind of java classes this builder
	 * creates.
	 */
	public String getKindId() {
		return kindId;
	}

	/**
	 * Logs a CoreException to the build status of this builder. This method can
	 * only be called during the build cycle.
	 */
	protected void addToBuildStatus(CoreException e) {
		buildStatus.add(new IpsStatus(e));
	}

	/**
	 * Logs the provided IStatus to the build status of this builder. This
	 * method can only be called during the build cycle.
	 */
	protected void addToBuildStatus(IStatus status) {
		buildStatus.add(status);
	}

	/**
	 * Returns the localized text for the provided key. Calling this method is
	 * only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 *
	 * @param element
	 *            the ips element used to access the ips project where the 
	 *            language to use is defined.
	 * @param key
	 *            the key that identifies the requested text
	 * @return the requested text
	 */
	public String getLocalizedText(IIpsElement element, String key) {

		if (localizedStringsSet == null) {
			throw new RuntimeException(
					"A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
		}
		return getLocalizedStringSet().getString(
				key,
				getLanguageUsedInGeneratedSourceCode(element));
	}

	/**
	 * Inserts the localized Javadoc inclusing the annotations into the given 
	 * JavaCodeFragmentBuikder. 
	 * <p>
	 * Calling this method is only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 *
	 * @param key prefix
	 *            the key prefix that identifies the requested javadoc and annotation.
	 *            The javadoc is looked up in the localized text by adding _JAVADOC to the prefic.
	 *            The annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
	 * @param element
	 *            the ips element used to access the ips project where the 
	 *            language to use is defined.
	 * @param builder
	 *             the builder the Javadoc is appended to.
	 * @return the requested text
	 */
	public void appendLocalizedJavaDoc(String keyPrefix, IIpsElement element, JavaCodeFragmentBuilder builder) {
		String text = getLocalizedText(element, keyPrefix + "_JAVADOC"); //$NON-NLS-1$
		String[] annotations = new String[]{getLocalizedText(element, keyPrefix + "_ANNOTATION")}; //$NON-NLS-1$
		builder.javaDoc(wrapText(text), annotations);
	}

	/**
	 * Inserts the localized Javadoc inclusing the annotations into the given 
	 * JavaCodeFragmentBuikder. 
	 * <p>
	 * Calling this method is only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 *
	 * @param key prefix
	 *            the key prefix that identifies the requested javadoc and annotation.
	 *            The javadoc is looked up in the localized text by adding _JAVADOC to the prefic.
	 *            The annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
	 * @param replacement 
	 *            Object that replaces the placeholder {0} in the property file
	 * @param element
	 *            the ips element used to access the ips project where the 
	 *            language to use is defined.
	 * @param builder
	 *             the builder the Javadoc is appended to.
	 * @return the requested text
	 */
	public void appendLocalizedJavaDoc(String keyPrefix, Object replacement, IIpsElement element, JavaCodeFragmentBuilder builder) {
		String text = getLocalizedText(element, keyPrefix + "_JAVADOC", replacement); //$NON-NLS-1$
		String[] annotations = new String[]{getLocalizedText(element, keyPrefix + "_ANNOTATION")}; //$NON-NLS-1$
		builder.javaDoc(wrapText(text), annotations);
	}

	/**
	 * Inserts the localized Javadoc including the annotations into the given 
	 * JavaCodeFragmentBuilder. 
	 * <p>
	 * Calling this method is only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 *
	 * @param key prefix
	 *            the key prefix that identifies the requested javadoc and annotation.
	 *            The javadoc is looked up in the localized text by adding _JAVADOC to the prefic.
	 *            The annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
	 * @param replacements 
	 *            Objects that replaces the placeholdersw {0}, {1} etc.  in the property file
	 * @param element
	 *            the ips element used to access the ips project where the 
	 *            language to use is defined.
	 * @param builder
	 *             the builder the Javadoc is appended to.
	 * @return the requested text
	 */
	public void appendLocalizedJavaDoc(String keyPrefix, Object[] replacements, IIpsElement element, JavaCodeFragmentBuilder builder) {
		String text = getLocalizedText(element, keyPrefix + "_JAVADOC", replacements); //$NON-NLS-1$
		String[] annotations = new String[]{getLocalizedText(element, keyPrefix + "_ANNOTATION")}; //$NON-NLS-1$
		builder.javaDoc(wrapText(text), annotations);
	}

    private String wrapText(String text){
        
        if(StringUtils.isEmpty(text) || javaOptionsSplitLength == null || javaOptionsTabSize == null){
            return text;
        }
        int maxLengthInt = javaOptionsSplitLength.intValue();
        int tabSizeInt = javaOptionsTabSize.intValue();
        int length = maxLengthInt - tabSizeInt - 3;
        String[] lines = StringUtils.split(text, SystemUtils.LINE_SEPARATOR);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            String lineText = lines[i];
            while(lineText.length() > length){
                int index = lineText.indexOf(' ', length);
                if(index != -1){
                    buf.append(lineText.substring(0, index));
                    buf.append(SystemUtils.LINE_SEPARATOR);
                    if(lineText.length() > index + 1){
                        lineText = lineText.substring(index + 1, lineText.length() - 1);
                    }
                    else{
                        break;
                    }
                }
                else{
                    break;
                }
            }
            buf.append(lineText);
            buf.append(SystemUtils.LINE_SEPARATOR);
        }
        return buf.toString();
    }
    
	/**
	 * Returns the localized text for the provided key. Calling this method is
	 * only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 * 
	 * @param element
	 *            the ips element used to access the ips project where the 
	 *            language to use is defined.
	 * @param key
	 *            the key that identifies the requested text
	 * @param replacement
	 *            an indicated region within the text is replaced by the string
	 *            representation of this value
	 * @return the requested text
	 */
	public String getLocalizedText(IIpsElement element, String key, Object replacement) {
		if (localizedStringsSet == null) {
			throw new RuntimeException(
					"A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
		}
		return getLocalizedStringSet().getString(
				key,
				getLanguageUsedInGeneratedSourceCode(element),
				replacement);
	}
	
	/**
	 * Returns the localized text for the provided key. Calling this method is
	 * only allowed during the build cycle. If it is called outside the build
	 * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet
	 * has been set to this builder a RuntimeException is thrown.
	 * 
	 * @param element
	 *            the ips element used to access the ips project where the 
	 *            language to use is defined.
	 * @param key
	 *            the key that identifies the requested text
	 * @param replacements
	 *            indicated regions within the text are replaced by the string
	 *            representations of these values.
	 * @return the requested text
	 */
	public String getLocalizedText(IIpsElement element, String key, Object[] replacements) {
		if (localizedStringsSet == null) {
			throw new RuntimeException(
					"A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
		}
		return getLocalizedStringSet().getString(
				key,
				getLanguageUsedInGeneratedSourceCode(element),
				replacements);
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
        
        String formattedContent = format(content);
		boolean newFileCreated = createFileIfNotThere(javaFile);

		if (!newFileCreated) {

			String charset = ipsSrcFile.getIpsProject().getProject()
					.getDefaultCharset();
			String javaFileContentsStr = getJavaFileContents(javaFile, charset);
			if (isMergeEnabled()) {
				merge(javaFile, javaFileContentsStr, formattedContent, charset);
				return;
			}

			// if merging is not activated and the content of the file is
			// equal compared to the generated and formatted
			// content then the new content is not written to the file
			if (formattedContent.equals(javaFileContentsStr)) {
				return;
			}
		}

		javaFile.setContents(transform(ipsSrcFile, formattedContent), true,
				false, null);
	}

    private String getJavaFileContents(IFile javaFile, String charset) throws CoreException{
        InputStream javaFileContents = null;
        try {
            javaFileContents = javaFile.getContents(true);
            return StringUtil.readFromInputStream(javaFileContents, charset);
        } catch(IOException e){
            throw new CoreException(new IpsStatus("An exception ocurred while trying to read the contents of the java file " + //$NON-NLS-1$
                    javaFile, e));
        }
        finally {
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
		IFolder destination = ipsSrcFile.getIpsPackageFragment().getRoot()
				.getArtefactDestination();
		if (file.exists()) {
			file.delete(true, null);
			if (!parent.equals(destination) && parent instanceof IFolder) {
				IFolder parentFolder = (IFolder) parent;
				if (parentFolder.members().length == 0) {
					parentFolder.delete(true, null);
				}
			}
		}
	}
	
	protected String getJavaDocCommentForOverriddenMethod() {
		return "{@inheritDoc}"; //$NON-NLS-1$
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
		// with parameter null the CodeFormatter is configured with the
		// preferences that are
		// currently set
		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
		TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
				content, 0, content.length(), 0, StringUtil
						.getSystemLineSeparator());

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

	// TODO check for qualified class name validity
	public IFile getJavaFile(IIpsSrcFile ipsSrcFile) throws CoreException {
		IFolder destinationFolder = ipsSrcFile.getIpsPackageFragment()
				.getRoot().getArtefactDestination();

		String name = getQualifiedClassName(ipsSrcFile);
		int index = name.lastIndexOf('.');

		if (index == name.length()) {
			throw new RuntimeException(
					"The qualified class name is not a valid java class name"); //$NON-NLS-1$
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

	private void initJControlModel(IIpsProject project) throws CoreException{
		IFile mergeFile = project.getJavaProject().getProject().getFile("merge.xml"); //$NON-NLS-1$
		if(mergeFile.exists()){
			try {
				model = new JControlModel(XmlUtil.getDocument(mergeFile.getContents()).getDocumentElement());
			} catch (Exception e) {
				throw new CoreException(new IpsStatus(e));
			}
			return;
		}
		model = new JControlModel(getJMergeDefaultConfigDocument().getDocumentElement());
	}

	private org.w3c.dom.Document getJMergeDefaultConfigDocument() throws CoreException{
		InputStream is = null;
		try {
			StringBuffer mergeFile = new StringBuffer();
			mergeFile.append('/').append(
					JavaSourceFileBuilder.class.getPackage().getName().replace(
							'.', '/')).append("/merge.xml"); //$NON-NLS-1$
			is = (InputStream) Platform.getBundle(IpsPlugin.PLUGIN_ID)
					.getResource(mergeFile.toString()).getContent();
			return XmlUtil.getDocument(is);
		} catch (Exception e) {
			throw new CoreException(new IpsStatus(e));
		} finally {
			closeStream(is);
		}
		
	}
	
	private JControlModel getJControlModel(){
	
		if(model == null){
			throw new IllegalStateException("The jmerge control model has not been set, " + //$NON-NLS-1$
					"while merging is activated. Possible reason for that might be that " + //$NON-NLS-1$
					"the builder initialization method beforeBuildProcess(IIpsProject, int) " + //$NON-NLS-1$
					"this class: " + JavaSourceFileBuilder.class + " has been overridden and " + //$NON-NLS-1$ //$NON-NLS-2$
							"a call to the super class method has been forgotten."); //$NON-NLS-1$
		}
		return model;
	}
	
	private void merge(IFile javaFile, String oldContent,
			String newContent, String charset) throws CoreException {

	    try {
            JMerger merger = new JMerger();
            merger.setControlModel(getJControlModel());
            merger.setSourceCompilationUnit(merger.createCompilationUnitForContents(newContent));
            merger.setTargetCompilationUnit(merger.createCompilationUnitForContents(oldContent));
            String targetContentsBeforeMerge = merger.getTargetCompilationUnitContents();
            merger.merge();
            String targetContents = merger.getTargetCompilationUnitContents();

            if (targetContents == null || targetContents.equals(targetContentsBeforeMerge)) {
                return;
            }
            javaFile.setContents(new ByteArrayInputStream(targetContents.getBytes(charset)), true, false, null);
        } catch (Exception e){
            throw new CoreException(new IpsStatus("An error occurred while trying to merge " + //$NON-NLS-1$
                    "the generated content with the old content of the file: " + javaFile, e)); //$NON-NLS-1$
        }
	}

	private ByteArrayInputStream transform(IIpsSrcFile ipsSrcFile,
			String content) throws CoreException {
		String charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
		try {
			return new ByteArrayInputStream(content.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					"org.faktorips.std.builder", IStatus.OK, //$NON-NLS-1$
					"The charset is not supported: " + charset, e)); //$NON-NLS-1$
		}
	}

	private boolean createFileIfNotThere(IFile file) throws CoreException {

		if (!file.exists()) {
			IContainer parent = file.getParent();

			if (parent instanceof IFolder) {
				createFolder((IFolder) parent);
			}
			file.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
			return true;
		}

		return false;
	}

	private void createFolder(IFolder folder) throws CoreException {

		if (folder == null) {
			return;
		}
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent);
			}
			folder.create(true, true, null);
		}
	}
}
