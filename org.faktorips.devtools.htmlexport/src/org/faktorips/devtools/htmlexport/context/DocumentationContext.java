/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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

    private boolean showInheritedObjectPartsInTable = true;

    private Locale documentationLocale;

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
            try {
                documentedSrcFiles = ipsProject.findAllIpsSrcFiles(getDocumentedIpsObjectTypes());
            } catch (CoreRuntimeException e) {
                addStatus(new IpsStatus(
                        IStatus.ERROR,
                        "Error finding IpsSrcFiles of types " + StringUtils.join(getDocumentedIpsObjectTypes(), ';'), e.getCause())); //$NON-NLS-1$
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
        try {
            List<IIpsSrcFile> result = ipsProject.findAllIpsSrcFiles(ipsObjectTypes);
            result.retainAll(getDocumentedSourceFiles());

            return result;
        } catch (CoreRuntimeException e) {
            addStatus(new IpsStatus(IStatus.ERROR,
                    "Error finding IpsSrcFiles of types " + StringUtils.join(ipsObjectTypes, ';'), e.getCause())); //$NON-NLS-1$
            return new ArrayList<IIpsSrcFile>();
        }
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

    public boolean showInheritedObjectPartsInTable() {
        return showInheritedObjectPartsInTable;
    }

    public void setShowInheritedObjectPartsInTable(boolean showInheritedObjectPartsInJavaDocStyle) {
        this.showInheritedObjectPartsInTable = showInheritedObjectPartsInJavaDocStyle;
    }

    public void setShowValidationErrors(boolean outputMessages) {
        this.showValidationErrors = outputMessages;
    }

    public void setDocumentationLocale(Locale descriptionLocale) {
        if (this.documentationLocale == descriptionLocale) {
            return;
        }
        this.documentationLocale = descriptionLocale;
        multiLanguageSupport = new MultiLanguageSupport(descriptionLocale);
        messagesManager = new MessagesManager(this);
    }

    public Locale getDocumentationLocale() {
        return documentationLocale;
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
        if (!(element instanceof ILabeledElement)) {
            return element.getName();
        }

        ILabeledElement labeledElement = (ILabeledElement)element;
        if (singular) {
            return getMultiLanguageSupport().getLocalizedLabel(labeledElement);
        }
        return getMultiLanguageSupport().getLocalizedPluralLabel(labeledElement);
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
}
