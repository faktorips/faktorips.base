/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migration;

import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;

/**
 * Migration to version 3.1.0.ms1.
 * <p>
 * This migration ensures that the .ipsproject files are rewritten. This is necessary because a new
 * XML element <tt>&lt;SupportedLanguages&gt;</tt> has been added.
 * <p>
 * Furthermore, the natural language used when generating source code is added as
 * {@link ISupportedLanguage} to each IPS project. An {@link ILabel} is added for that language in
 * each {@link ILabeledElement}.
 * <p>
 * The existing descriptions for each {@link IDescribedElement} are associated to the locale of the
 * generator language.
 * 
 * @author Alexander Weickmann
 */
public class Migration_3_0_0_rfinal extends DefaultMigration {

    private final Locale generatorLocale;

    public Migration_3_0_0_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
        generatorLocale = getIpsProject().getIpsArtefactBuilderSet().getLanguageUsedInGeneratedSourceCode();
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        IIpsObject ipsObject = srcFile.getIpsObject();
        if (ipsObject.hasLabelSupport()) {
            addLabelForGeneratorLocale(ipsObject);
        }
        if (ipsObject.hasDescriptionSupport()) {
            associateDescriptionToGeneratorLocale(ipsObject);
        }
        migrateChildren(ipsObject);
    }

    private void migrateChildren(IIpsObjectPartContainer container) throws CoreException {
        for (IIpsElement child : container.getChildren()) {
            if (child instanceof IIpsObjectPartContainer) {
                IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)child;
                migrateChildren(ipsObjectPartContainer);
                if (ipsObjectPartContainer.hasDescriptionSupport()) {
                    associateDescriptionToGeneratorLocale(ipsObjectPartContainer);
                }
                if (ipsObjectPartContainer.hasLabelSupport()) {
                    addLabelForGeneratorLocale(ipsObjectPartContainer);
                }
                continue;
            }
            if (child instanceof ILabeledElement) {
                addLabelForGeneratorLocale((ILabeledElement)child);
            }
        }
    }

    private void associateDescriptionToGeneratorLocale(IDescribedElement describedElement) {
        /*
         * There is exactly one description or none, it has already been loaded by the XML
         * initialization process.
         */
        Set<IDescription> descriptionSet = describedElement.getDescriptions();
        IDescription description;
        if (descriptionSet.size() > 0) {
            description = descriptionSet.toArray(new IDescription[1])[0];
        } else {
            description = describedElement.newDescription();
        }
        description.setLocale(generatorLocale);
    }

    private void addLabelForGeneratorLocale(ILabeledElement labeledElement) {
        ILabel label = labeledElement.newLabel();
        label.setLocale(generatorLocale);
    }

    @Override
    protected void beforeFileMigration() throws CoreException {
        IIpsProjectProperties properties = getIpsProject().getProperties();
        properties.addSupportedLanguage(generatorLocale);
        properties.setDefaultLanguage(properties.getSupportedLanguage(generatorLocale));
        getIpsProject().setProperties(properties);
    }

    @Override
    public String getDescription() {
        return "For the new Faktor-IPS multi-language support feature a new XML " + //$NON-NLS-1$
                "element called <SupportedLanguages> has been added to the .ipsproject file." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + SystemUtils.LINE_SEPARATOR
                + "The language that the code generator uses at the moment of the " //$NON-NLS-1$
                + " migration is added to the supported languages of the IPS project." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR + SystemUtils.LINE_SEPARATOR
                + "In addition, it is now possible to attach labels to several model " //$NON-NLS-1$
                + "elements (one for each supported language)." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR + "A new label is added to each model element that supports labels." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR + SystemUtils.LINE_SEPARATOR + "Descriptions can now be written " //$NON-NLS-1$
                + "for each supported language as well. Every existing description will be associated with the " //$NON-NLS-1$
                + "language of the code generator during this migration."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.1.0.ms1"; //$NON-NLS-1$
    }

}
