/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;

/**
 * Default implementation of {@link IContentProposal}, since Eclipse only provides a default
 * implementation in 3.6 and we use 3.5
 * 
 * FIXME in Eclipse 3.6: use org.eclipse.jface.fieldassist.ContentProposal
 * 
 * @author Daniel Schwering, Faktor Zehn AG
 */
public class ContentProposal implements IContentProposal {
    private final String content;
    private final String label;
    private final String description;
    private final String prefix;

    /**
     * Creates a {@link IContentProposal} with the given content, label and description and the
     * cursor position at {@code content.length()}.
     */
    public ContentProposal(final String content, final String label, final String description) {
        this(content, label, description, StringUtils.EMPTY);
    }

    public ContentProposal(final String content, final String label, final String description, final String prefix) {
        this.content = content;
        this.label = label;
        this.description = description;
        this.prefix = prefix;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getCursorPosition() {
        return content.length();
    }

    @Override
    public String getContent() {
        return content;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPrefixLength() {
        return StringUtils.length(prefix);
    }

    @Override
    public String toString() {
        return "ContentProposal [content=" + content + ", label=" + label + ", description=" + description //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ", prefix=" + prefix + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}