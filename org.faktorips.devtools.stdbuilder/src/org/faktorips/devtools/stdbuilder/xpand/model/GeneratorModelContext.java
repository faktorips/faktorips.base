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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.builder.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * @author widmaier
 */
public class GeneratorModelContext {

    private final JavaClassNaming classNaming;

    // TODO move to ImportHandler
    private final Set<ImportStatement> imports = new LinkedHashSet<ImportStatement>();

    private final IIpsArtefactBuilderSetConfig config;

    // Model Service
    // ImportHandler

    public GeneratorModelContext(IIpsArtefactBuilderSetConfig config, JavaClassNaming classNaming) {
        this.config = config;
        this.classNaming = classNaming;
    }

    public IIpsArtefactBuilderSetConfig getConfig() {
        return config;
    }

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

    public Locale getLanguageUsedInGeneratedSourceCode() {
        // TODO Auto-generated method stub
        return null;
    }

    public IPath getRelativeJavaFile(IIpsSrcFile ipsSrcFile) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUnqualifiedClassName(IType type) {
        return classNaming.getUnqualifiedClassName(type.getIpsSrcFile());
    }

    public String getPackage(IType type) {
        return classNaming.getPackageName(type.getIpsSrcFile());
    }

    public String getQualifiedClassName(IType type) {
        return classNaming.getQualifiedClassName(type.getIpsSrcFile());
    }

    public String getQualifiedClassNameForInterface(IType type) {
        return classNaming.getUnqualifiedClassName(type.getIpsSrcFile());
    }

    public boolean isGeneratePropertyChange() {
        return config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER)
                .booleanValue();
    }

    public boolean isGenerateSeparatedCamelCase() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_CAMELCASE_SEPARATED);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean.booleanValue();
    }

}
