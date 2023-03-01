/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementInDocumentedSourceFileFilter;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * Utility for {@link IPageElement}s
 * 
 * @author dicker
 * 
 */
public class PageElementUtils {

    private static final String ANCHOR_SEPARATOR = "."; //$NON-NLS-1$
    private final DocumentationContext context;

    public PageElementUtils(DocumentationContext context) {
        this.context = context;
    }

    /**
     * creates {@link IPageElement}s from the given {@link String}s with {@link Style}s and
     * {@link TextType}s
     * 
     * @return array of {@link IPageElement}s. To enable storing of other types of PageElements like
     *             LinkPageElement, the return type is not TextPageElement[]
     */
    public IPageElement[] createTextPageElements(List<String> texts, Set<Style> styles, TextType type) {
        IPageElement[] textPageElements = new IPageElement[texts.size()];

        for (int i = 0; i < textPageElements.length; i++) {
            textPageElements[i] = new TextPageElement(texts.get(i), styles, type, getContext());
        }

        return textPageElements;
    }

    /**
     * creates {@link IPageElement}s from the given {@link String}s
     * 
     * @return array of {@link IPageElement}s
     */
    public IPageElement[] createTextPageElements(List<String> texts) {
        return createTextPageElements(texts, null, TextType.WITHOUT_TYPE);
    }

    /**
     * creates a {@link List} with link to the given {@link IIpsObject}s with the given target and
     * {@link Style}s
     * 
     * @return {@link List} of {@link LinkPageElement}s
     */
    public List<IPageElement> createLinkPageElements(List<? extends IIpsSrcFile> srcFiles,
            TargetType target,
            Set<Style> styles,
            DocumentationContext context) {
        List<IPageElement> liste = new ArrayList<>();

        for (IIpsSrcFile srcFile : srcFiles) {
            IPageElement linkPageElement = createLinkPageElement(context, srcFile, target, srcFile.getIpsObjectName(),
                    true, styles.toArray(new Style[styles.size()]));
            linkPageElement.addStyles(styles.toArray(new Style[styles.size()]));
            liste.add(linkPageElement);
        }

        return liste;
    }

    /**
     * creates a LinkPageElement for an IpsElement
     */
    public IPageElement createLinkPageElement(DocumentationContext context,
            IIpsElement to,
            TargetType target,
            String text,
            boolean useImage,
            Style... styles) {
        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        IPageElement element = createIpsElementRepresentation(context, to, text, useImage);

        if (filter.accept(to)) {
            return createLinkPageElementToIpsElement(to, target, element).addStyles(styles);
        }
        return element.addStyles(Style.DEAD_LINK);
    }

    /**
     * creates a LinkPageElement for an IpsElement
     */
    public IPageElement createLinkPageElement(DocumentationContext context,
            IIpsObjectPartContainer to,
            TargetType target,
            Style... styles) {
        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        String text = getIpsObjectPartContainerText(context, to);

        IPageElement element = createIpsElementRepresentation(context, to, text, false);

        if (filter.accept(to.getIpsSrcFile())) {
            LinkPageElement linkToIpsObjectPart = createLinkPageElementToIpsElement(to.getIpsSrcFile(),
                    createAnchorId(to), target, element);
            linkToIpsObjectPart.addStyles(styles);

            return linkToIpsObjectPart;
        }
        return element.addStyles(Style.DEAD_LINK);
    }

    private String getIpsObjectPartContainerText(DocumentationContext context, IIpsObjectPartContainer to) {
        if (to instanceof IBaseMethod) {
            return ((IBaseMethod)to).getSignatureString();
        }
        return to.getName() + " (" + context.getLabel(to) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public IPageElement createIpsElementRepresentation(DocumentationContext context,
            IIpsElement ipsElement,
            String text,
            boolean useImage) {
        if (useImage) {
            try {
                return new WrapperPageElement(WrapperType.NONE, context).addPageElements(
                        new IpsElementImagePageElement(ipsElement, getContext())).addPageElements(
                                new TextPageElement('\u00A0' + text, context));
            } catch (IpsException e) {
                context.addStatus(
                        new IpsStatus(IStatus.WARNING, "Could not find image for " + ipsElement.getName(), e)); //$NON-NLS-1$
            }
        }
        return new TextPageElement(text, context);
    }

    /**
     * creates a Link to the given {@link IIpsElement}
     */
    public IPageElement createLinkPageElement(DocumentationContext context,
            IIpsElement to,
            TargetType target,
            String text,
            boolean useImage) {
        return createLinkPageElement(context, to, target, text, useImage, new Style[0]);
    }

    private LinkPageElement createLinkPageElementToIpsElement(IIpsElement to, TargetType target, IPageElement element) {
        return createLinkPageElementToIpsElement(to, null, target, element);
    }

    public LinkPageElement createLinkPageElementToIpsElement(IIpsElement to,
            String linkAnchor,
            TargetType target,
            IPageElement element) {
        String path = HtmlPathFactory.createPathUtil(to).getPathFromRoot(
                LinkedFileType.getLinkedFileTypeByIpsElement(to));
        LinkPageElement linkPageElement = new LinkPageElement(path, target, getContext(), element);
        linkPageElement.setLinkAnchor(linkAnchor);
        return linkPageElement;
    }

    public String createAnchorId(IIpsElement element) {
        if (element instanceof IIpsObjectPart part) {

            String objectPartContainerName = part.getIpsSrcFile().getQualifiedNameType().getName();
            String objectPartClassName = part.getClass().getSimpleName();

            return objectPartContainerName + ANCHOR_SEPARATOR + objectPartClassName + ANCHOR_SEPARATOR
                    + element.getName();
        }

        return element.getName();
    }

    public DocumentationContext getContext() {
        return context;
    }
}
