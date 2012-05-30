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
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xpand2.XpandFacade;
import org.eclipse.xpand2.output.JavaBeautifier;
import org.eclipse.xpand2.output.Output;
import org.eclipse.xpand2.output.OutputImpl;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.Type;
import org.faktorips.util.LocalizedStringsSet;

public abstract class XpandBuilder extends JavaSourceFileBuilder {

    private final Set<Type> imports = new LinkedHashSet<Type>();

    public XpandBuilder(StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    protected String generate() throws CoreException {
        Output out = new OutputImpl();

        String charset = getIpsProject().getProject().getDefaultCharset();
        StringOutlet outlet = new StringOutlet(charset, null);
        outlet.addPostprocessor(new JavaBeautifier());
        out.addOutlet(outlet);

        XpandExecutionContextImpl execCtx = new XpandExecutionContextImpl(out, null);

        JavaBeansMetaModel mm = new JavaBeansMetaModel();
        execCtx.registerMetaModel(mm);

        XpandFacade facade = XpandFacade.create(execCtx);
        facade.evaluate(getTemplate(), getGeneratorModel());

        return outlet.getContent(getRelativeJavaFile(getIpsSrcFile()));
    }

    public abstract String getTemplate();

    public abstract Object getGeneratorModel();

    /**
     * @return Returns the imports.
     */
    public Set<Type> getImports() {
        return imports;
    }

    public boolean addImport(String importStatement) {
        return imports.add(new Type(importStatement));
    }

    public boolean removeImport(String importStatement) {
        return imports.remove(new Type(importStatement));
    }

}
