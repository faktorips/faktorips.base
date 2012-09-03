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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.xtend.expression.ResourceManager;
import org.faktorips.devtools.core.builder.AbstractBuilderSet;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportHandler;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportStatement;

/**
 * This class holds all the context information needed to generate the java code with our XPAND
 * builder framework. Context information are for example the java class naming or the builder
 * configuration.
 * <p>
 * The import handler for a single file build is also stored in this context and need to be reseted
 * for every new file. In fact this is not the optimum but ok for the moment. To be thread safe the
 * import handler is stored as {@link ThreadLocal} variable.
 * 
 * 
 * @author widmaier
 */
public class GeneratorModelContext {

    private final JavaClassNaming javaClassNaming;

    /**
     * The import handler holds the import statements for a single file. However this context is the
     * same for all file generations. Because every file is generated sequentially in one thread we
     * could reuse a {@link ThreadLocal} variable in this model context. Every new file have to
     * clear its {@link ImportHandler} before starting generation.
     */
    private final ThreadLocal<ImportHandler> importHandlerThreadLocal = new ThreadLocal<ImportHandler>();

    private final IIpsArtefactBuilderSetConfig config;

    private final Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap;

    private final ResourceManager resourceManager = new OptimizedResourceManager();

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

    public ResourceManager getResourceManager() {
        return resourceManager;
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

    public String getValidationMessageBundleBaseName(IIpsSrcFolderEntry entry) {
        String baseName = getResourceBundlePackage(entry) + "." + entry.getValidationMessagesBundle();
        return baseName;
    }

    protected String getResourceBundlePackage(IIpsSrcFolderEntry entry) {
        String basePack = entry.getBasePackageNameForDerivedJavaClasses();
        return JavaPackageStructure.getInternalPackage(basePack, StringUtils.EMPTY);
    }

    public boolean isGenerateChangeSupport() {
        return config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER)
                .booleanValue();
    }

    /**
     * Returns whether to generate camel case constant names with underscore separator or without.
     * For example if this property is true, the constant for the property
     * checkAnythingAndDoSomething would be generated as CHECK_ANYTHING_AND_DO_SOMETHING, if the
     * property is false the constant name would be CHECKANYTHINGANDDOSOMETHING.
     * 
     * @see StandardBuilderSet#CONFIG_PROPERTY_CAMELCASE_SEPARATED
     */
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

    public boolean isGenerateToXmlSupport() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_TO_XML_SUPPORT);
    }

    public boolean isGeneratingPublishedInterfaces() {
        // TODO FIPS-1059
        return true;
    }

}
