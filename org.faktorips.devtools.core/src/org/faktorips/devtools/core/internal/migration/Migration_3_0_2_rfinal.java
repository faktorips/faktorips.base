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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
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
 * generator language. A new description is added to every {@link IDescribedElement} that does not
 * have a description yet (in case there should be any at all).
 * 
 * @author Alexander Weickmann
 */
public class Migration_3_0_2_rfinal extends DefaultMigration {

    private Locale generatorLocale;

    public Migration_3_0_2_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        IIpsObject ipsObject = srcFile.getIpsObject();
        if (ipsObject instanceof ILabeledElement) {
            addLabelForGeneratorLocale((ILabeledElement)ipsObject);
        }
        associateDescriptionToGeneratorLocale(ipsObject);
        migrateChildren(ipsObject);
    }

    private void migrateChildren(IIpsObjectPartContainer container) throws CoreException {
        for (IIpsElement child : container.getChildren()) {
            if (child instanceof IIpsObjectPartContainer) {
                IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)child;
                migrateChildren(ipsObjectPartContainer);
            }
            if (child instanceof IDescribedElement) {
                associateDescriptionToGeneratorLocale((IDescribedElement)child);
            } else {
                if (child instanceof IIpsObjectPartContainer) {
                    deleteObsoleteDescriptions((IIpsObjectPartContainer)child);
                }
            }
            if (child instanceof ILabeledElement) {
                addLabelForGeneratorLocale((ILabeledElement)child);
            }
        }
    }

    /**
     * This is necessary as there are some containers that had descriptions in the past where it
     * does not make sense at all, e.g. attribute values.
     */
    private void deleteObsoleteDescriptions(IIpsObjectPartContainer container) {
        List<IDescription> obsoleteDescriptions = ((IpsObjectPartContainer)container).getDescriptions();
        IDescription[] descriptionArray = obsoleteDescriptions.toArray(new IDescription[obsoleteDescriptions.size()]);
        for (IDescription description : descriptionArray) {
            if (description.getText().length() > 0) {
                MessageDialog
                        .openInformation(Display.getDefault().getActiveShell(),
                                "Migration Information", //$NON-NLS-1$
                                "The description '" + description.getText() + "' of the element '" + container.getName() //$NON-NLS-1$ //$NON-NLS-2$
                                        + "' in the file '" + container.getIpsSrcFile() + "' cannot be migrated and will be deleted."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            description.delete();
        }
    }

    private void associateDescriptionToGeneratorLocale(IDescribedElement describedElement) {
        /*
         * There is exactly one description or none, it has already been loaded by the XML
         * initialization process.
         */
        List<IDescription> descriptionList = describedElement.getDescriptions();
        IDescription description;
        if (descriptionList.size() > 0) {
            description = descriptionList.get(0);
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
        generatorLocale = getIpsProject().getIpsArtefactBuilderSet().getLanguageUsedInGeneratedSourceCode();
        IIpsProjectProperties properties = getIpsProject().getProperties();
        properties.addSupportedLanguage(generatorLocale);
        properties.setDefaultLanguage(generatorLocale);
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
