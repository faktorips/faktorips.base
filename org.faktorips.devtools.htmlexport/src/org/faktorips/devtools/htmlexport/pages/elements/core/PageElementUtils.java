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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementInDocumentedSourceFileFilter;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;

/**
 * Utility for {@link PageElement}s
 * 
 * @author dicker
 * 
 */
public class PageElementUtils {

    private static final String ANCHOR_SEPARATOR = "."; //$NON-NLS-1$

    /**
     * creates {@link PageElement}s from the given {@link String}s with {@link Style}s and
     * {@link TextType}s
     * 
     * @return array of {@link PageElement}s. To enable storing of other types of PageElements like
     *         LinkPageElement, the return type is not TextPageElement[]
     */
    public PageElement[] createTextPageElements(List<String> texts, Set<Style> styles, TextType type) {
        PageElement[] textPageElements = new PageElement[texts.size()];

        for (int i = 0; i < textPageElements.length; i++) {
            textPageElements[i] = new TextPageElement(texts.get(i), styles, type);
        }

        return textPageElements;
    }

    /**
     * creates {@link PageElement}s from the given {@link String}s
     * 
     * @return array of {@link PageElement}s
     */
    public PageElement[] createTextPageElements(List<String> texts) {
        return createTextPageElements(texts, null, TextType.WITHOUT_TYPE);
    }

    /**
     * creates a {@link List} with link to the given {@link IIpsObject}s with the given target and
     * {@link Style}s
     * 
     * @return {@link List} of {@link LinkPageElement}s
     */
    public List<PageElement> createLinkPageElements(List<? extends IIpsSrcFile> srcFiles,
            String target,
            Set<Style> styles,
            DocumentationContext context) {
        List<PageElement> liste = new ArrayList<PageElement>();

        for (IIpsSrcFile srcFile : srcFiles) {
            PageElement linkPageElement = createLinkPageElement(context, srcFile, target, srcFile.getIpsObjectName(),
                    true, styles.toArray(new Style[styles.size()]));
            linkPageElement.addStyles(styles.toArray(new Style[styles.size()]));
            liste.add(linkPageElement);
        }

        return liste;
    }

    /**
     * creates a LinkPageElement for an IpsElement
     */
    public PageElement createLinkPageElement(DocumentationContext context,
            IIpsElement to,
            String target,
            String text,
            boolean useImage,
            Style... styles) {
        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        PageElement element = createIpsElementRepresentation(to, context, text, useImage);

        if (filter.accept(to)) {
            return createLinkPageElementToIpsElement(to, target, element).addStyles(styles);
        }
        return element.addStyles(Style.DEAD_LINK);
    }

    /**
     * creates a LinkPageElement for an IpsElement
     */
    public PageElement createLinkPageElement(DocumentationContext context,
            IIpsObjectPartContainer to,
            String target,
            Style... styles) {
        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        String text = getIpsObjectPartContainerText(context, to);

        PageElement element = createIpsElementRepresentation(to, context, text, false);

        if (filter.accept(to.getIpsSrcFile())) {
            LinkPageElement linkToIpsObjectPart = createLinkPageElementToIpsElement(to.getIpsSrcFile(),
                    createAnchorId(to), target, element);
            linkToIpsObjectPart.addStyles(styles);

            return linkToIpsObjectPart;
        }
        return element.addStyles(Style.DEAD_LINK);
    }

    private String getIpsObjectPartContainerText(DocumentationContext context, IIpsObjectPartContainer to) {
        if (to instanceof ILabeledElement) {
            return to.getName() + " (" + context.getLabel((ILabeledElement)to) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (to instanceof IMethod) {
            return ((IMethod)to).getSignatureString();
        }
        return to.getName();
    }

    public PageElement createIpsElementRepresentation(IIpsElement ipsElement,
            DocumentationContext context,
            String text,
            boolean useImage) {
        if (useImage) {
            try {
                return new WrapperPageElement(WrapperType.NONE).addPageElements(
                        new IpsElementImagePageElement(ipsElement)).addPageElements(
                        new TextPageElement('\u00A0' + text));
            } catch (CoreException e) {
                context.addStatus(new IpsStatus(IStatus.WARNING, "Could not find image for " + ipsElement.getName(), e)); //$NON-NLS-1$
            }
        }
        return new TextPageElement(text);
    }

    /**
     * creates a Link to the given {@link IIpsElement}
     */
    public PageElement createLinkPageElement(DocumentationContext context,
            IIpsElement to,
            String target,
            String text,
            boolean useImage) {
        return createLinkPageElement(context, to, target, text, useImage, new Style[0]);
    }

    private LinkPageElement createLinkPageElementToIpsElement(IIpsElement to, String target, PageElement element) {
        return createLinkPageElementToIpsElement(to, null, target, element);
    }

    public LinkPageElement createLinkPageElementToIpsElement(IIpsElement to,
            String linkAnchor,
            String target,
            PageElement element) {
        String path = PathUtilFactory.createPathUtil(to).getPathFromRoot(
                LinkedFileType.getLinkedFileTypeByIpsElement(to));
        LinkPageElement linkPageElement = new LinkPageElement(path, target, element);
        linkPageElement.setLinkAnchor(linkAnchor);
        return linkPageElement;
    }

    public String createAnchorId(IIpsElement element) {
        if (element instanceof IIpsObjectPart) {

            IIpsObjectPart part = (IIpsObjectPart)element;

            String objectPartContainerName = part.getIpsSrcFile().getQualifiedNameType().getName();
            String objectPartClassName = part.getClass().getSimpleName();

            return objectPartContainerName + ANCHOR_SEPARATOR + objectPartClassName + ANCHOR_SEPARATOR
                    + element.getName();
        }

        return element.getName();
    }
}
