package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * This specialization of <code>JavaSourceFileBuilder</code> uses a
 * <code>org.faktorips.codegen.JavaCodeFragmentBuilder</code> to create the source file content.
 * Subclasses don't need to instantiate the fragment builder the can just use it. Instead of
 * implementing the generate(IpsObject) method the template method buildInternal() is to implement.
 * Package and import declarations are handled by this class. Implementations don't need to care
 * about these.
 * 
 * @author Peter Erzberger
 */
public abstract class SimpleJavaSourceFileBuilder extends JavaSourceFileBuilder {

    private JavaCodeFragmentBuilder codeFragBuilder;

    /**
     * The actual generation of the source file content is supposed to be implemented here.
     * 
     * @throws CoreException checked exceptions can be wrapped in a CoreException
     */
    protected abstract void generateInternal() throws CoreException;

    /**
     * A template method that is called from beforeBuild method before it finishes.
     * 
     * @param ipsSrcFile see super class method beforeBuild
     * @throws CoreException see super class method beforeBuild
     */
    protected void beforeBuildInternal(IIpsSrcFile ipsSrcFile)
            throws CoreException{
        
    }

    /**
     * A template method that is called from afterBuild method before it finishes.
     * 
     * @param ipsSrcFile see super class method afterBuild
     * @throws CoreException see super class method afterBuild
     */
    protected void afterBuildInternal(IIpsSrcFile ipsSrcFile)
            throws CoreException{
        
    }

    /**
     * Creates a new SimpleJavaSourceFileBuilder
     */
    public SimpleJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet stringsSet) {
        super(builderSet, kindId, stringsSet);
    }

    /**
     * Returns the code fragement builder for this java source file builder.
     */
    protected JavaCodeFragmentBuilder getJavaCodeFragementBuilder() {
        return codeFragBuilder;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#beforeBuild(org.faktorips.devtools.core.model.IIpsSrcFile, org.eclipse.core.runtime.MultiStatus)
     */
    public final void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        codeFragBuilder = new JavaCodeFragmentBuilder();
        beforeBuildInternal(ipsSrcFile);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#afterBuild(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public final void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        super.afterBuild(ipsSrcFile);
        codeFragBuilder = null;
        afterBuildInternal(ipsSrcFile);
    }

    /**
     * Calls the generateInternal() method and addes the package and import declarations to the
     * content.
     *
     * @see org.faktorips.devtools.core.builder.JavaSourceFileBuilder#generate()
     */
    public final String generate() throws CoreException {
        generateInternal();
        StringBuffer content = new StringBuffer();
        content.append("package ");
        content.append(getPackage(getIpsSrcFile()));
        content.append(';');
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(codeFragBuilder.getFragment().getImportDeclaration().toString());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(codeFragBuilder.getFragment().getSourcecode());
        return content.toString();
    }
}
