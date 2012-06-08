/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.internal.xpand2.model.XpandDefinition;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModel;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportStatement;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutlet;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutput;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * An abstract implementation to use XPAND templates for code generation. The implementation only
 * needs to provide the name of the template and the corresponding generator model.
 * 
 * @author dirmeier
 */
public abstract class XpandBuilder extends JavaSourceFileBuilder {

    /**
     * You can set tracing switch in the run configuration to reload the template on every builder's
     * run
     */
    private static final boolean DEBUG = Boolean.valueOf(Platform
            .getDebugOption("org.faktorips.devtools.stdbuilder/debug/xpand")); //$NON-NLS-1$;

    private final Set<ImportStatement> imports = new LinkedHashSet<ImportStatement>();
    private XpandDefinition templateDefinition;
    private XpandExecutionContextImpl xpandContext;
    private StringOutput out;

    /**
     * The XPAND builder is associated to a builder set and need the {@link LocalizedStringsSet} for
     * translating for example java doc.
     * 
     * @param builderSet The builder set used with this builder
     * @param localizedStringsSet the {@link LocalizedStringsSet} for translations
     */
    public XpandBuilder(StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
        initTemplate();
    }

    private void initTemplate() {
        out = new StringOutput();
        xpandContext = new XpandExecutionContextImpl(out, null);
        JavaBeansMetaModel mm = new JavaBeansMetaModel();
        xpandContext.registerMetaModel(mm);

        final org.eclipse.xtend.typesystem.Type targetType = xpandContext.getTypeForName(getGeneratorModelNodeClass()
                .getName().replaceAll("\\.", SyntaxConstants.NS_DELIM));
        ArgumentCheck.notNull(targetType);
        final org.eclipse.xtend.typesystem.Type[] paramTypes = new org.eclipse.xtend.typesystem.Type[0];
        templateDefinition = xpandContext.findDefinition(getTemplate(), targetType, paramTypes);
        ArgumentCheck.notNull(templateDefinition);

        StringOutlet outlet = new StringOutlet("UTF-8", null);
        out.addOutlet(outlet);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        if (DEBUG) {
            initTemplate();
        }
        String charset = project.getProject().getDefaultCharset();
        StringOutlet outlet = (StringOutlet)out.getOutlet(null);
        if (outlet == null || !outlet.getFileEncoding().equals(charset)) {
            outlet = new StringOutlet(charset, null);
            out.addOutlet(outlet);
        }
    }

    /**
     * Calls the template evaluation and provides the generated code.
     * 
     * @return the generated java source code
     */
    @Override
    protected String generate() throws CoreException {
        StringOutlet outlet = (StringOutlet)out.getOutlet(null);
        templateDefinition.evaluate((XpandExecutionContext)xpandContext.cloneWithoutVariables(), getGeneratorModel());
        return outlet.getContent(getRelativeJavaFile(getIpsSrcFile()));
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return super.getUnqualifiedClassName(ipsSrcFile) + "_X";
    }

    /**
     * Getting the name of the template. The template name should be a static information. This
     * method is called when initializing the template in the constructor!
     * <p>
     * The templates name needs to be in the XPAND qualifier syntax for example:
     * org::faktorips::devtools::stdbuilder::xpand::policycmpt::template::PolicyCmpt::main
     * 
     * @return The name qualified of the template
     */
    protected abstract String getTemplate();

    protected abstract GeneratorModel getGeneratorModel();

    protected abstract Class<?> getGeneratorModelNodeClass();

    /**
     * Getting the set of collected import statements.
     * 
     * @return Returns the imports.
     */
    public Set<ImportStatement> getImports() {
        return imports;
    }

    /**
     * Adding a new import. The import statement should be the full qualified name of a class.
     * 
     * @param importStatement The full qualified name of a class that should be imported.
     * @return true if the import was added and not already part of the set.
     */
    public boolean addImport(String importStatement) {
        return imports.add(new ImportStatement(importStatement));
    }

    public boolean removeImport(String importStatement) {
        return imports.remove(new ImportStatement(importStatement));
    }

}
