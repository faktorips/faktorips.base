/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The <tt>ValidationDecorator</tt> is responsible for drawing markers to UI composites. This
 * mechanism is used to show little images to UI controls when the IPS object being edited by an
 * editor or a dialog does not pass the validation because of invalid input.
 * <p>
 * The used pattern is a GoF pattern called DecoratorPattern. In this structural pattern a so called
 * decorator object extends an object that is to decorate (in our case a composite) so it has the
 * same interface. If then a method from the composite is called by the client the decorator may
 * have overwritten this method and in this way changes what happens. In this way it is possible to
 * change the implementation of methods of the object to decorate without the need to change the
 * client (by calling another method or something like that).
 */
// FIXME aw: This class seems not to be used at all.
public class ValidationDecorator extends Composite {

    /** The size of the marker to decorate the UI composite with. */
    protected final static Point IMAGE_SIZE = new Point(8, 8);

    /** The UI label that is used to show the validation image. */
    private Label imageLabel;

    /**
     * Creates an instance of the <tt>ValidationDecorator</tt>.
     * 
     * @param parent The parent ui composite.
     */
    public ValidationDecorator(Composite parent) {
        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 2;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        Label imageLabel = new Label(this, SWT.CENTER);
        imageLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        imageLabel.setBackground(color);
    }

    public void setMessageList(MessageList list) {
        String imageName;
        switch (list.getSeverity()) {
            case Message.ERROR: {
                imageName = "size8/Error.gif"; //$NON-NLS-1$
                break;
            }
            case Message.WARNING: {
                imageName = "size8/Warning.gif"; //$NON-NLS-1$
                break;
            }
            case Message.INFO: {
                imageName = "info.gif"; //$NON-NLS-1$
                break;
            }
            default:
                imageName = "empty.gif"; //$NON-NLS-1$
        }
        imageLabel.setImage(IpsPlugin.getDefault().getImage(imageName));
    }

}
