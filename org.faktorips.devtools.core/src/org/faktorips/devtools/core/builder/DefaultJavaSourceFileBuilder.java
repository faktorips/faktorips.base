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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * A JavaSourceFileBuilder that keeps existing imports exactly as found in the source file and adds
 * new imports at the end of the import section. This keeps source files from being modified by the
 * builder by changing the import order when the rest of the file remains the same. This is faster
 * and more important when using the team functionality, the user sees only those sourcefiles that
 * he has really modified as changed files. <p> In addition this builder provides sections for typical
 * source code fragments. The following sections are distinguished: constant section, java doc
 * section for the main class, attribute section, constructor section for the main class, method
 * section, inner classes section. For inner classes the same subdivision of sections except for the
 * inner classes section is provided. To write code to this sections for the main class that is to
 * generation by an implementation of this builder one has to access the main type section by means
 * of the getMainTypeSection() method. The TypeSection object retrieved by this method offers the
 * methods for the accordant sub sections. Inner classes are created by calling the
 * createInnerClassTypeSection() method. This method returns a TypeSection object for the inner
 * class. For each inner class that is to generate a new TypeSection object needs to be created. The
 * TypeSection objects are typically accessed within implementations of the
 * generateCodeForJavatype() method. <p>This builder also provides a set of methods that generate code
 * fragments for logging statements. These methods generate logging code for the logging framework
 * registered with the <code>org.faktorips.devtools.core.loggingFrameworkConnector</code>
 * extension point. Typical java logging frameworks are log4j or the logging framework that comes
 * with the JDK since 1.4.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public abstract class DefaultJavaSourceFileBuilder extends JavaSourceFileBuilder {

    /**
     * Configuration property that is supposed to be used to read a configuration value from
     * the IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public final static String CONFIG_PROPERTY_GENERATE_LOGGING = "generateLoggingStatements"; //$NON-NLS-1$

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    private boolean loggingGenerationEnabled = false;
    private TypeSection mainSection;
    private List innerClassesSections;
    private boolean loggerInstanceGenerated = false;

    
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
    protected String generate() throws CoreException {
    	IImportContainer importContainer = getImportContainer();
        StringBuffer content = new StringBuffer();
        String pack = getPackage();
        content.append("package " + pack + ";"); //$NON-NLS-1$ //$NON-NLS-2$
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        JavaCodeFragment code = new JavaCodeFragment();
        try{
            loggerInstanceGenerated = false;
            mainSection = new TypeSection();
            innerClassesSections = new ArrayList();
            
            generateCodeForJavatype();
            code = generateClassBody(mainSection, innerClassesSections);
        } 
        finally {
            loggerInstanceGenerated = false;
            mainSection = null;
            innerClassesSections = null;
        }
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
     * Returns the type section of the main class that is generated by this builder.
     */
    public TypeSection getMainTypeSection(){
        return mainSection;
    }
    
    /**
     * Returns a <code>TypeSection</code> object for an inner class. For each inner class that is
     * to generate a new <code>TypeSection</code> object needs to be created by means of this
     * method.
     */
    public TypeSection createInnerClassSection(){
        if(innerClassesSections != null){
            TypeSection section = new TypeSection();
            innerClassesSections.add(section);
            return section;
        }
        throw new RuntimeException(
                "This exception occures when the list for inner class sections has not been properly initialized. " + //$NON-NLS-1$
                "Initialization takes place in the generate() method of the class " +  //$NON-NLS-1$
                DefaultJavaSourceFileBuilder.class);
    }
    
    private JavaCodeFragment generateClassBody(TypeSection section, List innerClassSections) throws CoreException{
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        codeBuilder.append(section.getJavaDocForTypeBuilder().getFragment());
        if (section.isClass()) {
            codeBuilder.classBegin(section.getClassModifier(), section.getUnqualifiedName(), 
                    section.getSuperClass(), section.getExtendedInterfaces());
        } else {
            codeBuilder.interfaceBegin(section.getUnqualifiedName(), section.getExtendedInterfaces());
        }
        codeBuilder.appendln();
        codeBuilder.append(section.getConstantBuilder().getFragment());
        codeBuilder.appendln();
        codeBuilder.append(section.getMemberVarBuilder().getFragment());
        codeBuilder.appendln();
        codeBuilder.append(section.getConstructorBuilder().getFragment());
        codeBuilder.appendln();
        codeBuilder.append(section.getMethodBuilder().getFragment());

        if(innerClassSections != null){
            codeBuilder.appendln();
            codeBuilder.appendln();
            for (Iterator it = innerClassSections.iterator(); it.hasNext();){
                codeBuilder.append(generateClassBody((TypeSection)it.next(), null));
            }
        }
        codeBuilder.classEnd();
        return codeBuilder.getFragment();
        
    }
    
    /**
     * This method is the entry point for implementations of this abstract builder. Within this
     * method the specific code generation has to be implemented. Typically the generated code is
     * written to sub sections of the main <code>TypeSection</code> retrieved by the
     * <code>getMainTypeSection()</code> method or to <code>TypeSection</code> objects retrieved
     * by the <code>createInnerClassTypeSection()</code> method.
     * 
     * @throws CoreException exceptions that occur during the generation process can be wrapped into
     *             a CoreExceptions and are safely handled by the framework
     */
    protected abstract void generateCodeForJavatype() throws CoreException;
    
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
    
    /**
     * Defines if the generation of the logging statement is enabled. Irrespective of the fact that a
     * generator uses the generate logging methods of this builder the actual code generation for
     * logging statements can be enabled and disabled by this method.
     */
    public void setLoggingCodeGenerationEnabled(boolean enabled){
        this.loggingGenerationEnabled = enabled;
    }
    
    private boolean checkLoggingGenerationConditions(){
        return loggingGenerationEnabled && getBuilderSet() != null && getBuilderSet().getIpsLoggingFrameworkConnector() != null;
    }
    
    private void addLoggingConnectorImports(List usedClasses, JavaCodeFragment frag){
        for (Iterator it = usedClasses.iterator(); it.hasNext();) {
            String className = (String)it.next();
            frag.addImport(className);
        }
    }
    
    private void generateLoggerConstantIfNecessary() throws CoreException{
        if(!loggerInstanceGenerated){
            generateLoggerInstance(mainSection.getConstantBuilder());
            loggerInstanceGenerated = true;
        }
    }

    private void generateLoggerInstance(JavaCodeFragmentBuilder builder) throws CoreException{
        if(!checkLoggingGenerationConditions()){
            return;
        }
        ArrayList usedClasses = new ArrayList();
        IIpsLoggingFrameworkConnector connector = getBuilderSet().getIpsLoggingFrameworkConnector();
        JavaCodeFragment value = new JavaCodeFragment();
        value.append(getBuilderSet().getIpsLoggingFrameworkConnector().getLoggerInstanceStmt(getUnqualifiedClassName() + ".class.getName()", usedClasses)); //$NON-NLS-1$
        addLoggingConnectorImports(usedClasses, value);
        builder.varDeclaration(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, 
                connector.getLoggerClassName(), getLoggerInstanceExpession(), value);
    }
    
    /**
     * Returns the constant name of the logger instance. Can be overridden by subclasses.
     */
    protected String getLoggerInstanceExpession() {
        return "LOGGER"; //$NON-NLS-1$
    }

    protected final void generateLoggingStmtForMessageExpression(int level, JavaCodeFragment frag, String messageExp) throws CoreException{
        if(!checkLoggingGenerationConditions()){
            return;
        }
        generateLoggerConstantIfNecessary();
        ArrayList usedClasses = new ArrayList();
        String loggingStmt = getBuilderSet().getIpsLoggingFrameworkConnector().getLogStmtForMessageExp(
                level, messageExp, getLoggerInstanceExpession(), usedClasses);
        frag.append(loggingStmt);
        frag.append(";"); //$NON-NLS-1$
        addLoggingConnectorImports(usedClasses, frag);
    }

    protected final void generateLoggingStmtWithConditionForMessageExpression(int level, JavaCodeFragment frag, String messageExp) throws CoreException{
        if(!checkLoggingGenerationConditions()){
            return;
        }
        generateLoggerConstantIfNecessary();
        ArrayList usedClasses = new ArrayList();
        frag.append("if ("); //$NON-NLS-1$
        frag.append(getBuilderSet().getIpsLoggingFrameworkConnector().getLogConditionExp(
                level, getLoggerInstanceExpession(), usedClasses));
        frag.append(")"); //$NON-NLS-1$
        frag.appendOpenBracket();
        generateLoggingStmtForMessageExpression(level, frag, messageExp);
        frag.appendCloseBracket();
        addLoggingConnectorImports(usedClasses, frag);
    }
    
    protected final void generateLoggingStmt(int level, JavaCodeFragment frag, String message) throws CoreException{
        if(!checkLoggingGenerationConditions()){
            return;
        }
        generateLoggerConstantIfNecessary();
        ArrayList usedClasses = new ArrayList();
        String loggingStmt = getBuilderSet().getIpsLoggingFrameworkConnector().getLogStmtForMessage(
                level, message, getLoggerInstanceExpession(), usedClasses);
        frag.append(loggingStmt);
        frag.append(";"); //$NON-NLS-1$
        addLoggingConnectorImports(usedClasses, frag);
    }
    
    protected final void generateLoggingCondition(int level, JavaCodeFragment frag) throws CoreException{
        if (!checkLoggingGenerationConditions()) {
            return;
        }
        generateLoggerConstantIfNecessary();
        ArrayList usedClasses = new ArrayList();
        frag.append(getBuilderSet().getIpsLoggingFrameworkConnector().getLogConditionExp(
                level, getLoggerInstanceExpession(), usedClasses));
        addLoggingConnectorImports(usedClasses, frag);
    }

    protected final void generateLoggingStmtWithCondition(int level, JavaCodeFragment frag, String message) throws CoreException{
        if(!checkLoggingGenerationConditions()){
            return;
        }
        generateLoggerConstantIfNecessary();
        ArrayList usedClasses = new ArrayList();
        frag.append("if ("); //$NON-NLS-1$
        frag.append(getBuilderSet().getIpsLoggingFrameworkConnector().getLogConditionExp(
                level, getLoggerInstanceExpession(), usedClasses));
        frag.append(")"); //$NON-NLS-1$
        frag.appendOpenBracket();
        generateLoggingStmt(level, frag, message);
        frag.appendCloseBracket();
        addLoggingConnectorImports(usedClasses, frag);
    }

    protected final void generateMethodEnteringLoggingStmt(JavaCodeFragment frag,
            String className,
            String methodName,
            String[] parameters) throws CoreException {
        if (!checkLoggingGenerationConditions()) {
            return;
        }
        StringBuffer message = new StringBuffer();
        message.append("\""); //$NON-NLS-1$
        message.append("Entering method: "); //$NON-NLS-1$
        if (className != null) {
            message.append(className);
            message.append('.');
        }
        message.append(methodName);
        if (parameters != null && parameters.length > 0) {
            message.append(", parameters: "); //$NON-NLS-1$
            for (int i = 0; i < parameters.length; i++) {
                message.append(parameters[i]);
                message.append("="); //$NON-NLS-1$
                message.append("\""); //$NON-NLS-1$
                message.append("+"); //$NON-NLS-1$
                message.append(parameters[i]);
                if (i < parameters.length - 1) {
                    message.append("+"); //$NON-NLS-1$
                    message.append("\""); //$NON-NLS-1$
                    message.append(", "); //$NON-NLS-1$
                }
            }
        } else {
            message.append("\""); //$NON-NLS-1$
        }
        generateLoggingStmtWithConditionForMessageExpression(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, frag, message.toString());
    }
    
    protected final void generateMethodExitingLoggingStmt(JavaCodeFragment frag, String className, String methodName, String returnVariable) throws CoreException{
        if(!checkLoggingGenerationConditions()){
            return;
        }
        StringBuffer message = new StringBuffer();
        message.append("\""); //$NON-NLS-1$
        message.append("Exiting method: "); //$NON-NLS-1$
        if(className != null){
            message.append(className);
            message.append('.');
        }
        message.append(methodName);
        message.append(", return value: "); //$NON-NLS-1$
        message.append("\""); //$NON-NLS-1$
        message.append("+ "); //$NON-NLS-1$
        message.append(returnVariable);
        generateLoggingStmtWithConditionForMessageExpression(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, frag, message.toString());
    }
    
    /**
     * Generates a debug level logging statement.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateDebugLoggingStmt(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmt(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, frag, message);
    }

    /**
     * Generates a debug level boolean expression that checks if debug level logging is enabled.
     * E.g. <code>getLogger("foo").isDebugEnabled()</code>
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     */
    protected final void generateDebugLoggingCondition(JavaCodeFragment frag) throws CoreException{
        generateLoggingCondition(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, frag);
    }

    /**
     * Generates a debug level logging statement wrapped in an if statement that checks the 
     * condition of the debug level.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateDebugLoggingStmtWithCondition(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmtWithCondition(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, frag, message);
    }

    /**
     * Generates an info level logging statement.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateInfoLoggingStmt(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmt(IIpsLoggingFrameworkConnector.LEVEL_INFO, frag, message);
    }

    /**
     * Generates a info level boolean expression that checks if info level logging is enabled.
     * E.g. <code>getLogger("foo").isInfoEnabled()</code>
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     */
    protected final void generateInfoLoggingCondition(JavaCodeFragment frag) throws CoreException {
        generateLoggingCondition(IIpsLoggingFrameworkConnector.LEVEL_INFO, frag);
    }

    /**
     * Generates a info level logging statement wrapped in an if statement that checks the 
     * condition of the info level.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateInfoLoggingStmtWithCondition(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmtWithCondition(IIpsLoggingFrameworkConnector.LEVEL_INFO, frag, message);
    }

    /**
     * Generates a warning level logging statement.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateWarningLoggingStmt(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmt(IIpsLoggingFrameworkConnector.LEVEL_WARNING, frag, message);
    }

    /**
     * Generates a warning level boolean expression that checks if warning level logging is enabled.
     * E.g. <code>getLogger("foo").isWarningEnabled()</code>
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     */
    protected final void generateWarningLoggingCondition(JavaCodeFragment frag) throws CoreException {
        generateLoggingCondition(IIpsLoggingFrameworkConnector.LEVEL_WARNING, frag);
    }

    /**
     * Generates a warning level logging statement wrapped in an if statement that checks the 
     * condition of the warning level.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateWarningLoggingStmtWithCondition(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmtWithCondition(IIpsLoggingFrameworkConnector.LEVEL_WARNING, frag, message);
    }

    /**
     * Generates an error level logging statement.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateErrorLoggingStmt(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmt(IIpsLoggingFrameworkConnector.LEVEL_ERROR, frag, message);
    }

    /**
     * Generates a error level boolean expression that checks if error level logging is enabled.
     * E.g. <code>getLogger("foo").isErrorEnabled()</code>
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     */
    protected final void generateErrorLoggingCondition(JavaCodeFragment frag) throws CoreException {
        generateLoggingCondition(IIpsLoggingFrameworkConnector.LEVEL_ERROR, frag);
    }

    /**
     * Generates a error level logging statement wrapped in an if statement that checks the 
     * condition of the error level.
     * 
     * @param frag the {@link JavaCodeFragment} where the code is written to
     * @param message the message text that will be logged
     */
    protected final void generateErrorLoggingStmtWithCondition(JavaCodeFragment frag, String message) throws CoreException{
        generateLoggingStmtWithCondition(IIpsLoggingFrameworkConnector.LEVEL_ERROR, frag, message);
    }
}
