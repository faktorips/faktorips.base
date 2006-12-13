/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * A JavaSourceFileBuilder  that keeps existing imports exactly as found in the source file
 * and adds new imports at the end of the import section. This keeps source files from being
 * modified by the builder by changing the import order when the rest of the file remains the same.
 * This is faster and more important when using the team functionality, the user sees only those
 * sourcefiles that he has really modified as changed files.
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultJavaSourceFileBuilder extends JavaSourceFileBuilder {

	/**
	 * @param builderSet
	 * @param kindId
	 * @param localizedStringsSet
	 */
	public DefaultJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet,
			String kindId, LocalizedStringsSet localizedStringsSet) {
		super(builderSet, kindId, localizedStringsSet);
	}

    /**
     * Overridden.
     * 
     * Calls the generateCodeForJavatype() method and adds the package and import declarations to the
     * content.
     */
    public final String generate() throws CoreException {
    	IImportContainer importContainer = getImportContainer();
        StringBuffer content = new StringBuffer();
        String pack = getPackage();
        content.append("package " + pack + ";"); //$NON-NLS-1$ //$NON-NLS-2$
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        JavaCodeFragment code = generateCodeForJavatype();
        if (importContainer!=null && importContainer.exists()) {
            content.append(importContainer.getSource());
            ImportDeclaration newImports = getNewImports(importContainer, code.getImportDeclaration(pack));
            content.append(newImports);
        } else {
        	content.append(code.getImportDeclaration(pack));
        }
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(code.getSourcecode());
        return content.toString();
    }
    
    /**
     * Generates the sourcecode of the generated Java class or interface.
     */
    protected abstract JavaCodeFragment generateCodeForJavatype() throws CoreException;
    
    private ImportDeclaration getNewImports(IImportContainer container, ImportDeclaration decl) throws JavaModelException {
    	if (decl.getNoOfImports()==0) {
    		return decl;
    	}
    	ImportDeclaration existingImports = new ImportDeclaration();
    	IJavaElement[] imports = container.getChildren();
    	for (int i = 0; i < imports.length; i++) {
    		String imp = ((IImportDeclaration)imports[i]).getSource(); // example for imp: import java.util.Date;
    		existingImports.add(imp.substring(7, imp.length()-1));
		}
    	return existingImports.getUncoveredImports(decl);
    }
    
    private IImportContainer getImportContainer() throws CoreException {
        IFile file = getJavaFile(getIpsSrcFile());
    	ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
    	if (cu==null || !cu.exists()) {
    		return null;
    	}
    	return cu.getImportContainer();
    }
    
}
