/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.context.messages.MessagesManager;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.IpsObjectTypeComparator;

/**
 * Context for the Documentation
 * 
 * 
 * This class contains options for the HtmlExport as well as a List of all data to be documented
 * 
 * 
 * @author dicker
 * 
 */
public class DocumentationContext {

    /**
     * related {@link IpsObjectType}s: Just {@link IIpsObject} of these types are documented
     */
    protected IpsObjectType[] documentedIpsObjectTypes;

    private List<IStatus> exportStatus = new ArrayList<IStatus>();

    private final IPluginResourceFacade pluginResources;

    /**
     * Path for output
     */
    protected String path;

    private boolean showValidationErrors = true;

    private Locale descriptionLocale;

    private MessagesManager messagesManager;
    private MultiLanguageSupport multiLanguageSupport;

    /**
     * All scripts within this documentation
     */
    private List<IDocumentorScript> scripts = new ArrayList<IDocumentorScript>();

    /**
     * {@link IIpsProject}, which will be documented
     */
    private IIpsProject ipsProject;

    /**
     * {@link ILayouter} contains the layout for the documentation
     */
    private ILayouter layouter;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$

    /**
     * All {@link IIpsSrcFile}s, which should be documented within the Export
     */
    private List<IIpsSrcFile> documentedSrcFiles;

    public DocumentationContext() {
        this(new PluginResourceFacade());
    }

    public DocumentationContext(IPluginResourceFacade pluginResources) {
        this.pluginResources = pluginResources;
        setDocumentedIpsObjectTypes(pluginResources.getDefaultIpsObjectTypes());
    }

    public void setDocumentedIpsObjectTypes(IpsObjectType... ipsObjectTypes) {
        if (ArrayUtils.isEmpty(ipsObjectTypes)) {
            throw new IllegalArgumentException("ipsObjectTypes must not be empty"); //$NON-NLS-1$
        }
        Arrays.sort(ipsObjectTypes, new IpsObjectTypeComparator());
        documentedIpsObjectTypes = ipsObjectTypes;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IpsObjectType[] getDocumentedIpsObjectTypes() {
        return documentedIpsObjectTypes;
    }

    public List<IIpsSrcFile> getDocumentedSourceFiles() {
        if (documentedSrcFiles == null) {
            documentedSrcFiles = new ArrayList<IIpsSrcFile>();
            try {
                ipsProject.findAllIpsSrcFiles(documentedSrcFiles, getDocumentedIpsObjectTypes());
            } catch (CoreException e) {
                addStatus(new IpsStatus(IStatus.ERROR,
                        "Error finding IpsSrcFiles of types " + getDocumentedIpsObjectTypes(), e)); //$NON-NLS-1$
            }
        }

        return documentedSrcFiles;

    }

    /**
     * returns all {@link IIpsSrcFile} within the {@link IpsProject}, which type is within the given
     * array.
     * 
     * @param ipsObjectTypes Array with all relevant {@link IpsObjectType}s
     * @return List<IIpsSrcFile>
     */
    public List<IIpsSrcFile> getDocumentedSourceFiles(IpsObjectType... ipsObjectTypes) {

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        try {
            ipsProject.findAllIpsSrcFiles(result, ipsObjectTypes);
        } catch (CoreException e) {
            addStatus(new IpsStatus(IStatus.ERROR, "Error finding IpsSrcFiles of types " + ipsObjectTypes, e)); //$NON-NLS-1$
        }

        result.retainAll(getDocumentedSourceFiles());

        return result;
    }

    public Collection<IIpsPackageFragment> getLinkedPackageFragments() {

        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<IIpsPackageFragment>();
        List<IIpsSrcFile> linkedObjects = getDocumentedSourceFiles(getDocumentedIpsObjectTypes());
        for (IIpsSrcFile ipsObject : linkedObjects) {
            relatedPackageFragments.add(ipsObject.getIpsPackageFragment());
        }

        return relatedPackageFragments;
    }

    public void addDocumentorScript(IDocumentorScript script) {
        scripts.add(script);
    }

    public List<IDocumentorScript> getScripts() {
        return scripts;
    }

    public ILayouter getLayouter() {
        return layouter;
    }

    public void setLayouter(ILayouter layouter) {
        this.layouter = layouter;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return dateFormat;
    }

    public boolean showsValidationErrors() {
        return showValidationErrors;
    }

    public void setShowValidationErrors(boolean outputMessages) {
        this.showValidationErrors = outputMessages;
    }

    public void setDescriptionLocale(Locale descriptionLocale) {
        if (this.descriptionLocale == descriptionLocale) {
            return;
        }
        this.descriptionLocale = descriptionLocale;
        multiLanguageSupport = new MultiLanguageSupport(descriptionLocale);
        messagesManager = new MessagesManager(this);
    }

    public Locale getDescriptionLocale() {
        return descriptionLocale;
    }

    public String getDescription(IDescribedElement describedElement) {
        return getMultiLanguageSupport().getLocalizedDescription(describedElement);
    }

    private MultiLanguageSupport getMultiLanguageSupport() {
        return multiLanguageSupport;
    }

    public String getLabel(IIpsElement element) {
        return getLabel(element, true);
    }

    public String getLabel(IIpsElement element, boolean singular) {
        if (element instanceof ILabeledElement) {
            ILabeledElement labeledElement = (ILabeledElement)element;
            if (singular) {
                return getMultiLanguageSupport().getLocalizedLabel(labeledElement);
            }
            return getMultiLanguageSupport().getLocalizedPluralLabel(labeledElement);
        }

        return element.getName();
    }

    public String getCaption(IIpsObjectPartContainer ipsObjectPartContainer) {
        return getCaption(ipsObjectPartContainer, true);
    }

    public String getCaption(IIpsObjectPartContainer ipsObjectPartContainer, boolean singular) {
        if (singular) {
            return getMultiLanguageSupport().getLocalizedCaption(ipsObjectPartContainer);
        }
        return getMultiLanguageSupport().getLocalizedPluralCaption(ipsObjectPartContainer);
    }

    public IStatus getExportStatus() {
        if (exportStatus.size() == 0) {
            return new IpsStatus(IStatus.OK, "No problems"); //$NON-NLS-1$
        }
        if (exportStatus.size() == 1) {
            return exportStatus.get(0);
        }
        return new MultiStatus(pluginResources.getIpsPluginPluginId(), 0, exportStatus.toArray(new IStatus[exportStatus
                .size()]), Messages.DocumentationContext_multipleErrorsMessage, null);
    }

    public void addStatus(IStatus status) {
        pluginResources.log(status);
        exportStatus.add(status);
    }

    public String getMessage(String messageId) {
        return messagesManager.getMessage(messageId);
    }

    public String getMessage(Object object) {
        return messagesManager.getMessage(object);
    }

    public UIDatatypeFormatter getDatatypeFormatter() {
        return pluginResources.getDatatypeFormatter();
    }

    public Properties getMessageProperties(String resourceName) {
        try {
            return pluginResources.getMessageProperties(resourceName);
        } catch (CoreException e) {
            addStatus(e.getStatus());
            return new Properties();
        }
    }
}
