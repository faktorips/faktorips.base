/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * This class uses a {@link ControlDecoration} to paint a marker next to a {@link Control} to
 * indicate a problem. The problem is triggered by setting a {@link MessageList}.
 * 
 * @author dirmeier
 * @since 3.6
 */
public class MessageDecoration {

    private ControlDecoration controlDecoration;

    /**
     * Installing a {@link ControlDecoration} to the given control to the specified position.
     * 
     * @param control The control used to paint the message decoration to.
     * @param position The position of the decoration
     * @param composite The composite on which the decoration should be painted on. May be null but
     *            should normally be the client area. This is important if the control is painted in
     *            a expandable area like sections to avoid painting in collapsed state
     * 
     * @see ControlDecoration
     */
    public MessageDecoration(Control control, int position, Composite composite) {
        controlDecoration = new ControlDecoration(control, position, composite);
    }

    /**
     * Setting a message list for this decoration. The first message with the highest severity is
     * displayed as marker decoration with an hover tooltip. The message list may be empty or null
     * to hide the decoration.
     * 
     * @param list The message list to decorate the control or null to hide the decoration
     */
    public void setMessageList(MessageList list) {
        if (list != null) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    mapSeverityToFieldDecoration(list.getSeverity()));
            if (decoration != null) {
                controlDecoration.setImage(decoration.getImage());
                Message messageWithHighestSeverity = list.getMessageWithHighestSeverity();
                controlDecoration.setDescriptionText(messageWithHighestSeverity != null ? messageWithHighestSeverity
                        .getText() : decoration.getDescription());
                controlDecoration.show();
                return;
            }
        }
        controlDecoration.hide();
    }

    /**
     * Mapping the severity of {@link Message} to the {@link FieldDecorationRegistry} id.
     * 
     * @param severity The {@link Message} severity we want to map
     * 
     * @return the id of the {@link FieldDecoration}
     */
    private String mapSeverityToFieldDecoration(Severity severity) {
        switch (severity) {
            case ERROR:
                return FieldDecorationRegistry.DEC_ERROR;
            case WARNING:
                return FieldDecorationRegistry.DEC_WARNING;
            case INFO:
                return FieldDecorationRegistry.DEC_INFORMATION;
            default:
                return null;
        }
    }

}
