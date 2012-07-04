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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.builder.AbstractBuilderSet;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * @author widmaier
 */
public class GeneratorModelContext {

    private final JavaClassNaming javaClassNaming;

    private final ThreadLocal<ImportHandler> importHandlerThreadLocal = new ThreadLocal<ImportHandler>();

    private final IIpsArtefactBuilderSetConfig config;

    private final Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap;

    // Model Service
    // ImportHandler

    public GeneratorModelContext(IIpsArtefactBuilderSetConfig config,
            Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap) {
        this.config = config;
        this.annotationGeneratorMap = annotationGeneratorMap;
        // TODO FIPS-1059
        this.javaClassNaming = new JavaClassNaming(true);
    }

    IIpsArtefactBuilderSetConfig getConfig() {
        return config;
    }

    /**
     * Gets the thread local import handler. The import handler stores all import statements needed
     * in the generated class file.
     * <p>
     * The import handler is stored as {@link ThreadLocal} variable to have the ability to generate
     * different files in different threads
     * 
     * @return The thread local import handler
     */
    public ImportHandler getImportHandler() {
        return importHandlerThreadLocal.get();
    }

    /**
     * Sets the thread local import handler. The import handler stores all import statements needed
     * in the generated class file.
     * <p>
     * The import handler is stored as {@link ThreadLocal} variable to have the ability to generate
     * different files in different threads
     * 
     * @param importHandler The thread local import handler
     */
    public void setImportHandler(ImportHandler importHandler) {
        this.importHandlerThreadLocal.set(importHandler);
    }

    /**
     * Getting the set of collected import statements.
     * 
     * @return Returns the imports.
     */
    public Set<ImportStatement> getImports() {
        return getImportHandler().getImports();
    }

    /**
     * Adds a new import. The import statement should be the full qualified name of a class.
     * 
     * @param importStatement The full qualified name of a class that should be imported.
     * @return the unqualified name of the import statement
     */
    public ImportStatement addImport(String importStatement) {
        return getImportHandler().add(importStatement);
    }

    public boolean removeImport(String importStatement) {
        return getImportHandler().remove(importStatement);
    }

    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = getConfig().getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_GENERATOR_LOCALE);
        if (localeString == null) {
            return Locale.ENGLISH;
        }
        return AbstractBuilderSet.getLocale(localeString);
    }

    /**
     * Returns the list of annotation generators for the given type. This method never returns null.
     * If there is no annotation generator for the specified type an empty list will be returned.
     * 
     * @param type The {@link AnnotatedJavaElementType} you want to get the generators for
     * @return the list of {@link IAnnotationGenerator annotation generators} or an empty list if
     *         there is none
     */
    public List<IAnnotationGenerator> getAnnotationGenerator(AnnotatedJavaElementType type) {
        List<IAnnotationGenerator> result = annotationGeneratorMap.get(type);
        if (result == null) {
            result = new ArrayList<IAnnotationGenerator>();
        }
        return result;
    }

    public JavaClassNaming getJavaClassNaming() {
        return javaClassNaming;
    }

    public boolean isGenerateChangeSupport() {
        return config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER)
                .booleanValue();
    }

    public boolean isGenerateSeparatedCamelCase() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_CAMELCASE_SEPARATED);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean.booleanValue();
    }

    public boolean isGenerateDeltaSupport() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT);
    }

    public boolean isGenerateCopySupport() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_COPY_SUPPORT);
    }

    public boolean isGenerateVisitorSupport() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT);
    }

    public boolean isGeneratingPublishedInterfaces() {
        // TODO FIPS-1059
        return true;
    }

}
