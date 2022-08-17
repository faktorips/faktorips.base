/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.util.Locale;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;

/**
 * This is an Ant task that can only be used with the Eclipse Ant runner with an Eclipse
 * installation that contains the Faktor-IPS plugin. Creates documentation for an IPS project and
 * exports it as HTML by calling the {@link HtmlExportOperation} configured with the parameters
 * provided to this Ant task.
 */
public class ExportHtmlTask extends AbstractIpsTask {

    private static final String DEFAULT_LANGUAGE = "de";
    private DocumentationContext context;
    private String ipsProjectName;
    private String locale;
    private String destination;
    private boolean showValidationErrors;
    private boolean showInheritedObjectPartsInTable;
    private String ipsObjectTypes;

    public ExportHtmlTask() {
        super("ExportHtmlTask");
    }

    /**
     * Starts the {@link HtmlExportOperation} configured with the parameters provided to this Ant
     * task.
     */
    @Override
    protected void executeInternal() throws Exception {
        initContext();
        System.out.println("exporting HTML for project: " + ipsProjectName);
        HtmlExportOperation operation = new HtmlExportOperation(context);
        operation.run(new NullProgressMonitor());
    }

    private void initContext() {
        context = new DocumentationContext();
        context.setPath(destination);
        context.setShowValidationErrors(showValidationErrors);
        context.setShowInheritedObjectPartsInTable(showInheritedObjectPartsInTable);
        IIpsProject ipsProject = IIpsModel.get().getIpsProject(ipsProjectName);
        if (ipsProject == null || !ipsProject.exists()) {
            throw new BuildException("No IpsProject found for the specified name: " + ipsProjectName);
        }
        context.setIpsProject(ipsProject);
        context.setDocumentationLocale(getValidLanguage(ipsProject));
        context.setLayouter(new HtmlLayouter(context, ".resource")); //$NON-NLS-1$
        context.addDocumentorScript(new StandardDocumentorScript());
        context.setDocumentedIpsObjectTypes(getIpsObjectTypes());
    }

    private IpsObjectType[] getIpsObjectTypes() {
        return IpsObjectTypesParser.getIpsObjectTypes(ipsObjectTypes,
                IIpsModel.get().getIpsObjectTypes());
    }

    private Locale getValidLanguage(IIpsProject ipsProject) {
        if (locale.isEmpty()) {
            return new Locale(DEFAULT_LANGUAGE);
        }

        for (ISupportedLanguage supportedLanguage : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
            if (supportedLanguage.getLocale().getLanguage().equalsIgnoreCase(locale)) {
                return supportedLanguage.getLocale();
            }
        }
        throw new BuildException(
                String.format("Locale %s is not supported by the project %s.", locale, ipsProjectName));
    }

    public String getIpsProjectName() {
        return ipsProjectName;
    }

    public void setIpsProjectName(String ipsProjectName) {
        this.ipsProjectName = ipsProjectName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean getShowValidationErrors() {
        return showValidationErrors;
    }

    public void setShowValidationErrors(boolean showValidationErrors) {
        this.showValidationErrors = showValidationErrors;
    }

    public boolean getShowInheritedObjectPartsInTable() {
        return showInheritedObjectPartsInTable;
    }

    public void setShowInheritedObjectPartsInTable(boolean showInheritedObjectPartsInTable) {
        this.showInheritedObjectPartsInTable = showInheritedObjectPartsInTable;
    }

    public void setIpsObjectTypes(String ipsObjectTypes) {
        this.ipsObjectTypes = ipsObjectTypes;
    }
}
