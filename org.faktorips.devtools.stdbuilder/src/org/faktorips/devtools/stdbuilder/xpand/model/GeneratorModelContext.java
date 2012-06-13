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

import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.builder.AbstractBuilderSet;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * @author widmaier
 */
public class GeneratorModelContext {

    private final JavaClassNaming implClassNaming;

    private final JavaClassNaming interfaceNaming;

    private ImportHandler importHandler = new ImportHandler();

    private final IIpsArtefactBuilderSetConfig config;

    // Model Service
    // ImportHandler

    public GeneratorModelContext(IIpsArtefactBuilderSetConfig config) {
        this.config = config;
        this.implClassNaming = new JavaClassNaming(false, false, true);
        this.interfaceNaming = new JavaClassNaming(true, true, true);
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
        return importHandler.getImports();
    }

    /**
     * Adds a new import. The import statement should be the full qualified name of a class.
     * 
     * @param importStatement The full qualified name of a class that should be imported.
     * @return true if the import was added and not already part of the set.
     */
    public boolean addImport(String importStatement) {
        return importHandler.add(importStatement);
    }

    public boolean removeImport(String importStatement) {
        return importHandler.remove(importStatement);
    }

    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = getConfig().getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_GENERATOR_LOCALE);
        if (localeString == null) {
            return Locale.ENGLISH;
        }
        return AbstractBuilderSet.getLocale(localeString);
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

    public JavaClassNaming getImplClassNaming() {
        return implClassNaming;
    }

    public JavaClassNaming getInterfaceNaming() {
        return interfaceNaming;
    }

}
