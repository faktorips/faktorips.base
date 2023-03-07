/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractStandardTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsObjectMessageListTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * <p>
 * The AbstractObjectContentPageElement represents a complete Page for an {@link IIpsObject}. Use
 * the {@link ContentPageUtil} to choose the right subclass.
 * </p>
 * 
 * @author dicker
 * 
 */
public abstract class AbstractIpsObjectContentPageElement<T extends IIpsObject> extends AbstractRootPageElement {

    private T documentedIpsObject;

    /**
     * creates a page, which represents the given documentedIpsObject according to the given context
     * 
     */
    protected AbstractIpsObjectContentPageElement(T documentedIpsObject, DocumentationContext context) {
        super(context);
        this.documentedIpsObject = documentedIpsObject;
        setTitle(context.getLabel(documentedIpsObject));
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(), new LinkPageElement(
                "index", TargetType.OVERALL, //$NON-NLS-1$
                getContext().getMessage(HtmlExportMessages.AbstractObjectContentPageElement_overviewProject)
                        + " " + getContext().getIpsProject().getName(), //$NON-NLS-1$
                getContext())));

        IIpsPackageFragment ipsPackageFragment = getDocumentedIpsObject().getIpsPackageFragment();
        addPageElements(new PageElementUtils(getContext()).createLinkPageElement(getContext(), ipsPackageFragment,
                TargetType.CLASSES,
                IIpsDecorators.get(ipsPackageFragment.getClass()).getLabel(ipsPackageFragment),
                true));
        addPageElements(new TextPageElement(getIpsObjectTypeDisplayName() + " " //$NON-NLS-1$
                + getContext().getLabel(getDocumentedIpsObject()), TextType.HEADING_1, getContext()));

        addTypeHierarchy();

        addPageElements(new TextPageElement(getContext().getLabel(getDocumentedIpsObject()), TextType.HEADING_2,
                getContext()));

        if (getDocumentedIpsObject() instanceof IVersionControlledElement
                && ((IVersionControlledElement)getDocumentedIpsObject()).isDeprecated()) {
            TextPageElement deprecationElement = new TextPageElement(
                    ((IVersionControlledElement)getDocumentedIpsObject()).getDeprecation().toString(),
                    TextType.HEADING_3,
                    getContext());
            deprecationElement.addStyles(Style.ITALIC);
            addPageElements(deprecationElement);
        }

        addStructureData();

        if (!getDocumentedIpsObject().getIpsProject().equals(getContext().getIpsProject())) {
            addPageElements(TextPageElement.createParagraph(
                    getContext().getMessage(HtmlExportMessages.AbstractObjectContentPageElement_project) + ": " //$NON-NLS-1$
                            + getDocumentedIpsObject().getIpsProject().getName(),
                    getContext()));
        }
        addPageElements(TextPageElement.createParagraph(
                getContext().getMessage(HtmlExportMessages.AbstractObjectContentPageElement_projectFolder) + ": " //$NON-NLS-1$
                        + getDocumentedIpsObject().getIpsSrcFile().getIpsPackageFragment(),
                getContext()));

        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractObjectContentPageElement_description), TextType.HEADING_2, getContext()));
        addPageElements(new TextPageElement(
                IpsStringUtils.isBlank(getContext().getDescription(getDocumentedIpsObject())) ? getContext().getMessage(
                        HtmlExportMessages.AbstractObjectContentPageElement_noDescription)
                        : getContext()
                                .getDescription(getDocumentedIpsObject()),
                TextType.BLOCK, getContext()));
        addVersionPageElement();

        if (getContext().showsValidationErrors()) {
            addValidationErrorsTable();
        }

        addExtensionPropertiesTable();
    }

    private void addVersionPageElement() {
        if (getDocumentedIpsObject() instanceof IVersionControlledElement) {
            IVersionControlledElement versionControlledElement = (IVersionControlledElement)getDocumentedIpsObject();
            IVersion<?> sinceVersion = versionControlledElement.getSinceVersion();
            if (sinceVersion != null) {
                addPageElements(new TextPageElement(getContext().getMessage(
                        HtmlExportMessages.TablePageElement_headlineSince), TextType.HEADING_2, getContext()));
                String content = getContext().getMessage(HtmlExportMessages.TablePageElement_version)
                        + sinceVersion.asString();
                addPageElements(new TextPageElement(content, TextType.BLOCK, getContext()));
            }
        }
    }

    /**
     * Fetches the displayName of the IpsObjectType
     * <p>
     * The method getDisplayName is not useful, because it depends on the platform language and not
     * the chosen one.
     * <p>
     * If the IpsObjectType of the documented IpsObject is a subclass of IpsObjectType the method
     * getDisplayName is used with the possibility of using a wrong language
     * 
     */
    private String getIpsObjectTypeDisplayName() {
        String id = getDocumentedIpsObject().getIpsObjectType().getId();
        String messageId = "IpsObjectType_name" + id; //$NON-NLS-1$
        String message = getContext().getMessage(messageId);
        if (IpsStringUtils.isNotBlank(message) && !messageId.equals(message)) {
            return message;
        }
        return getDocumentedIpsObject().getIpsObjectType().getDisplayName();
    }

    /**
     * adds a table with all validation messages of the {@link IIpsObject}. Nothing will be shown,
     * if there are no messages.
     */
    private void addValidationErrorsTable() {

        MessageList messageList = new MessageList();

        try {
            messageList = getDocumentedIpsObject().validate(getDocumentedIpsObject().getIpsProject());
        } catch (IpsException e) {
            getContext().addStatus(new IpsStatus(IStatus.ERROR, "Error validating " //$NON-NLS-1$
                    + getDocumentedIpsObject().getQualifiedName(), e));
        }

        if (messageList.isEmpty()) {
            return;
        }

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractObjectContentPageElement_validationErrors), TextType.HEADING_2,
                getContext()));

        TablePageElement tablePageElement = new IpsObjectMessageListTablePageElement(messageList, getContext());

        wrapper.addPageElements(tablePageElement);

        addPageElements(wrapper);

    }

    /**
     * adds {@link IPageElement}s for structural data like fitting ProductCmpt for a PolicyCmptType
     */
    protected void addStructureData() {
        // could be overridden
    }

    /**
     * adds {@link IPageElement}s for hierarchical data like super- and subclasses
     */
    protected void addTypeHierarchy() {
        // could be overridden
    }

    @Override
    public String getPathToRoot() {
        return HtmlPathFactory.createPathUtil(getDocumentedIpsObject()).getPathToRoot();
    }

    /**
     * returns the given table or the given alternative text, if the table is empty
     * 
     */
    IPageElement getTableOrAlternativeText(AbstractStandardTablePageElement tablePageElement, String alternativeText) {
        if (tablePageElement == null || tablePageElement.isEmpty()) {
            return new TextPageElement(alternativeText, getContext());
        }
        return tablePageElement;
    }

    /**
     * returns the documentedIpsObject
     * 
     */
    protected T getDocumentedIpsObject() {
        return documentedIpsObject;
    }

    @Override
    protected void createId() {
        setId(documentedIpsObject.getQualifiedName());
    }

    protected void addExtensionPropertiesTable() {
        Collection<IExtensionPropertyDefinition> properties = getDocumentedIpsObject()
                .getExtensionPropertyDefinitions();

        if (properties.isEmpty()) {
            return;
        }

        KeyValueTablePageElement extensionPropertiesTable = new KeyValueTablePageElement(getContext(), getContext()
                .getMessage(HtmlExportMessages.AbstractIpsObjectContentPageElement_extensionPropertyKeyHeadline),
                getContext().getMessage(
                        HtmlExportMessages.AbstractIpsObjectContentPageElement_extensionPropertyValueHeadline));

        for (IExtensionPropertyDefinition iExtensionPropertyDefinition : properties) {
            Object extPropertyValue = getDocumentedIpsObject().getExtPropertyValue(
                    iExtensionPropertyDefinition.getPropertyId());
            extensionPropertiesTable.addKeyValueRow(iExtensionPropertyDefinition.getName(),
                    extPropertyValue == null ? null : extPropertyValue.toString());
        }

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractIpsObjectContentPageElement_extensionProperties), TextType.HEADING_2,
                getContext()));

        wrapper.addPageElements(extensionPropertiesTable);

        addPageElements(wrapper);

    }

    @Override
    public boolean isContentUnit() {
        return true;
    }
}
