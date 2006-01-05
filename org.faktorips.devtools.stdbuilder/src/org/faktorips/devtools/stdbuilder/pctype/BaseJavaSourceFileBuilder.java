package org.faktorips.devtools.stdbuilder.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for all JavaSourceFileBuilder of this package.
 * 
 * @author Peter Erzberger
 */
public abstract class BaseJavaSourceFileBuilder extends SimpleJavaSourceFileBuilder {

    protected final static String[] ANNOTATION_GENERATED = new String[]{"generated"};
    protected final static String[] ANNOTATION_MODIFIABLE = new String[]{"modifiable"};

    public BaseJavaSourceFileBuilder(IJavaPackageStructure packageStructure, String kindId,
            LocalizedStringsSet stringsSet) {
        super(packageStructure, kindId, stringsSet);
    }

    public void addToBuildStatus(IStatus status) {
        super.addToBuildStatus(status);
    }

    public JavaCodeFragmentBuilder getJavaCodeFragementBuilder(){
        return super.getJavaCodeFragementBuilder();
    }
    
    protected final void buildAttributes(IAttribute[] attributes) throws CoreException {
        for (int i = attributes.length - 1; i >= 0; i--) {
            if (!attributes[i].validate().containsErrorMsg()) {
                try {
                    buildAttribute(attributes[i]);
                } catch (Exception e) {
                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building attribute " + attributes[i].getName() + " of "
                                    + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
                }
            }
        }
    }
    
    protected abstract void buildAttribute(IAttribute a) throws CoreException;
}

