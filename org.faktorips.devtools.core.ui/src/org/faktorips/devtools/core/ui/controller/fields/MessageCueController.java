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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The internal controller for cues and error messages on {@link Text} and {@link Combo} widgets.
 * <p>
 * An instance of the class is created per shell.
 */
// TODO CD 19.05.2011 This code seems to be copied from eclipse. The ControlDecoration seems to do
// nearly the same, is very easy to use and available since eclipse 3.3.
public class MessageCueController {

    /**
     * Installs or de-installs a visual cue indicating messages related to the control's content. If
     * the user moves the mouse over the cue image or the control the messages are displayed in a
     * hover box above the control.
     * 
     * @param control the control on which to install or uninstall the cue
     * @param list the message list or <code>null</code> to uninstall the cue
     * 
     * @throws NullPointerException if control is null
     */
    public static void setMessageCue(Control control, MessageList list) {
        Shell shell = control.getShell();
        MessageCueController controller = (MessageCueController)shell.getData(MESSAGE_CUE_CONTROLLER);
        if (controller == null) {
            controller = new MessageCueController(shell);
            shell.setData(MESSAGE_CUE_CONTROLLER, controller);
        }
        controller.getFieldController(control, true).setMessageList(list);
    }

    private static final String MESSAGE_CUE_CONTROLLER = MessageCueController.class.getName();
    private static final String FIELD_CONTROLLER = MESSAGE_CUE_CONTROLLER + ".FieldController"; //$NON-NLS-1$
    private static final String ANNOTATION_HANDLER = MESSAGE_CUE_CONTROLLER + ".annotationHandler"; //$NON-NLS-1$

    private static String fgPlatform = SWT.getPlatform();
    private static boolean fgCarbon = "carbon".equals(fgPlatform); //$NON-NLS-1$

    /** the shell the controller is attached to */
    private Shell shell;

    private MessageCueController(Shell initShell) {
        shell = initShell;
        shell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                Object data = shell.getData(MESSAGE_CUE_CONTROLLER);
                if (data == MessageCueController.this) {
                    shell.setData(MESSAGE_CUE_CONTROLLER, null);
                    shell = null;
                }
            }
        });
    }

    private FieldController getFieldController(Control control, boolean create) {
        FieldController controller = (FieldController)control.getData(FIELD_CONTROLLER);
        if (controller == null && create) {
            controller = new FieldController(control);
            control.setData(FIELD_CONTROLLER, controller);
        }
        return controller;
    }

    /**
     * The field controller handles the message cue for a single control.
     */
    class FieldController implements DisposeListener {

        /** The controlled control. The cue image is placed relative to this control. */
        private Control fControl;

        /** the messages shown in the hover and the list's severity shown as cue. */
        private MessageList messageList = null;

        /** The icon's horizontal screen distance from top-left corner of control (in pixels). */
        private int fDx;

        /** The icon's vertical screen distance from top-left corner of control (in pixels). */
        private int fDy;

        /** The HoverController (only when control has focus). */
        private HoverController fHoverController;

        /** the hover containing the message text. */
        private Hover fHover;

        /** Shell resize/move and deactivation listener */
        private Listener shellListener;

        /**
         * Create a new FieldController
         * 
         * @param control the target control
         */
        FieldController(Control control) {
            ArgumentCheck.notNull(control);
            fControl = control;
        }

        /**
         * Sets the messages shown in the hover and the list's severity shown as cue.
         */
        void setMessageList(MessageList list) {
            messageList = list;
            if (list == null || list.getSeverity() == Message.NONE) {
                if (installed()) {
                    uninstall();
                }
            } else {
                if (!installed()) {
                    install();
                }
            }
            // repaint parents
            Control c = fControl.getParent();
            while (c != null) {
                c.redraw();
                if (c instanceof Shell) {
                    break;
                } else {
                    c = c.getParent();
                }
            }

        }

        private boolean installed() {
            return shellListener != null;
        }

        void install() {
            fControl.addDisposeListener(this);

            // listener that moves the hover, when the control is moved or resized
            // and hides the hover on deactication.
            shellListener = new Listener() {
                @Override
                public void handleEvent(Event event) {
                    switch (event.type) {
                        case SWT.Resize:
                        case SWT.Move:
                            if (fHover != null) {
                                fHover.setLocation();
                            }
                            break;
                        case SWT.Deactivate:
                        case SWT.Iconify:
                            hideHover();
                            break;
                    }
                }
            };
            shell.addListener(SWT.Resize, shellListener);
            shell.addListener(SWT.Move, shellListener);
            shell.addListener(SWT.Deactivate, shellListener);
            shell.addListener(SWT.Iconify, shellListener);

            // install the hover controller. The hover controller shows the hover when
            // the mouse hovers over the control.
            if (fHoverController == null) {
                fHoverController = new HoverController(this);
                fControl.addMouseTrackListener(fHoverController);
            }

            // Install a MessageCuePainter on every parent control to paint the cue image
            // The painter also shows the hover when the mouse hovers over the cue image.
            String dataIdentifier = ANNOTATION_HANDLER + toString();
            Control c = fControl.getParent();
            while (c != null) {
                MessageCuePainter cueHandler = new MessageCuePainter(this);
                c.setData(dataIdentifier, cueHandler);
                c.addPaintListener(cueHandler);
                c.addMouseTrackListener(cueHandler);
                c.redraw();
                if (c instanceof Shell) {
                    break;
                } else {
                    c = c.getParent();
                }
            }
        }

        void uninstall() {
            fControl.removeDisposeListener(this);

            if (fHover != null) {
                fHover.dispose();
                fHover = null;
            }
            if (fHoverController != null) {
                fControl.removeMouseTrackListener(fHoverController);
                fHoverController = null;
            }

            // remove shell listener
            shell = fControl.getShell();
            shell.removeListener(SWT.Resize, shellListener);
            shell.removeListener(SWT.Move, shellListener);
            shell.removeListener(SWT.Deactivate, shellListener);
            shell.removeListener(SWT.Iconify, shellListener);
            shellListener = null;

            // remove the painters from the parent hierarchy
            String dataIdentifier = ANNOTATION_HANDLER + toString();
            Control c = fControl.getParent();
            while (c != null) {
                MessageCuePainter cueHandler = (MessageCuePainter)c.getData(dataIdentifier);
                if (cueHandler != null) {
                    c.setData(dataIdentifier, null);
                    c.removePaintListener(cueHandler);
                    c.removeMouseTrackListener(cueHandler);
                    c.redraw();
                }
                if (c instanceof Shell) {
                    break;
                } else {
                    c = c.getParent();
                }
            }
        }

        @Override
        public void widgetDisposed(DisposeEvent e) {
            uninstall();
        }

        private void hideHover() {
            if (fHover != null) {
                fHover.setVisible(false);
            }
        }

        private void showHover() {
            if (fHover == null) {
                fHover = new Hover(fControl);
            }
            fHover.setText(messageList.getText());
            fHover.setLocation();

            // fix the position if hover couldn't be displayed completly on the display
            Point controlPoint = fControl.toDisplay(0, 0);
            Point extent = fHover.getExtent();
            Rectangle displayBounds = fControl.getDisplay().getBounds();
            int correctedPos = displayBounds.width - controlPoint.x - extent.x;
            if (correctedPos < 0) {
                fHover.setVisible(false);
                fHover.dispose();
                fHover = new Hover(fControl, -1 * correctedPos + fHover.getDefaultOffsetOfArrow());
                fHover.setText(messageList.getText());
                fHover.setLocation();
            }

            fHover.setVisible(true);
        }

        /**
         * Shows the hover if the mouse event has taken place on the cue image, otherwise hides the
         * hower.
         */
        void updateHoverOnCue(MouseEvent e) {
            Image image = getCueImage();
            if (image == null) {
                return;
            }
            Rectangle r = image.getBounds();
            Point global = fControl.toDisplay(fDx, fDy);
            Point local = ((Control)e.widget).toControl(global);
            r.x = local.x;
            r.y = local.y;
            if (r.contains(e.x, e.y)) {
                showHover();
            } else {
                hideHover();
            }
        }

        private boolean isHoverVisible() {
            return fHover != null && fHover.isVisible();
        }

        /**
         * Paints the cue image.
         */
        void paintMessageCueImage(PaintEvent e) {
            if (fControl.isDisposed()) {
                return;
            }
            Image image = getCueImage();
            if (image == null) {
                return;
            }
            fDy = 8;
            fDx = -9; // image size is 8

            Point global = fControl.toDisplay(fDx, fDy);
            Point local = ((Control)e.widget).toControl(global);
            e.gc.drawImage(image, local.x, local.y);
        }

        /**
         * Returns the cue image based on the message list's severity.
         */
        private Image getCueImage() {
            ImageDescriptor imageDescriptor = IpsProblemOverlayIcon.getOverlay(messageList.getSeverity());
            if (imageDescriptor != null) {
                return IpsUIPlugin.getImageHandling().getImage(imageDescriptor, true);
            } else {
                return null;
            }
        }
    }

    /**
     * A single plain HoverController is registered for the content assist control. It handles mouse
     * hover events to show/hide the info hover.
     */
    class HoverController extends MouseTrackAdapter {

        /**
         * The managing FieldController.
         */
        FieldController fieldController;

        /**
         * Create a new HoverController.
         */
        HoverController(FieldController controller) {
            fieldController = controller;
        }

        @Override
        public void mouseHover(MouseEvent e) {
            handleMouseEvent(e);
        }

        @Override
        public void mouseExit(MouseEvent e) {
            if (fieldController.isHoverVisible()) {
                fieldController.hideHover();
            }
        }

        /**
         * @param e The mouse event to handle.
         */
        void handleMouseEvent(MouseEvent e) {
            fieldController.showHover();
        }
    }

    /**
     * One MessageCuePainter is registered per ancestor control of the content assist control. It
     * paints the visual icon representing the messages. It also extends the HoverController as the
     * hover controller listens to mouse events on the target control only. So it does not catch the
     * case, that the user hovers with the mouse over the image. So here all mouse events from all
     * parents are analyzed, if it occurred in the image area. (this is actually done in the
     * updateHoverOnCue() method).
     */
    class MessageCuePainter extends HoverController implements PaintListener {

        /**
         * Create a new MessageCuePainter.
         */
        MessageCuePainter(FieldController controller) {
            super(controller);
        }

        @Override
        public void paintControl(PaintEvent e) {
            fieldController.paintMessageCueImage(e);
        }

        /**
         * Updates the hover.
         * 
         * @param event the mouse event
         */
        @Override
        void handleMouseEvent(MouseEvent event) {
            fieldController.updateHoverOnCue(event);
        }
    }

    /**
     * An info Hover to display a message next to a {@link Control}.
     */
    class Hover {

        /**
         * Distance of info hover arrow from left side.
         */
        private int HD = 10;

        /**
         * Width of info hover arrow.
         */
        private int HW = 8;

        /**
         * Height of info hover arrow.
         */
        private int HH = 10;

        /**
         * Margin around info hover text.
         */
        private int LABEL_MARGIN = 2;

        private int defaultOffsetOfArrow = HD + HW / 2;

        /**
         * This info hover's shell.
         */
        Shell fHoverShell;

        /** the control the hover belongs to. */
        Control control;

        /**
         * The info hover text.
         */
        String fText = ""; //$NON-NLS-1$

        Hover(Control control, int arrowOffset) {
            HD = arrowOffset;
            createHover(control);
        }

        Hover(Control control) {
            createHover(control);
        }

        private void createHover(Control control) {
            this.control = control;
            final Display display = control.getDisplay();
            fHoverShell = new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
            fHoverShell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            fHoverShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            fHoverShell.addPaintListener(new PaintListener() {
                @Override
                public void paintControl(PaintEvent pe) {
                    pe.gc.drawText(fText, LABEL_MARGIN, LABEL_MARGIN);
                    if (!fgCarbon) {
                        pe.gc.drawPolygon(getPolygon(true));
                    }
                }
            });
        }

        int[] getPolygon(boolean border) {
            Point e = getExtent();
            if (border) {
                return new int[] { 0, 0, e.x - 1, 0, e.x - 1, e.y - 1, HD + HW, e.y - 1, HD + HW / 2, e.y + HH - 1, HD,
                        e.y - 1, 0, e.y - 1, 0, 0 };
            } else {
                return new int[] { 0, 0, e.x, 0, e.x, e.y, HD + HW, e.y, HD + HW / 2, e.y + HH, HD, e.y, 0, e.y, 0, 0 };
            }
        }

        void dispose() {
            if (!fHoverShell.isDisposed()) {
                fHoverShell.dispose();
            }
        }

        void setVisible(boolean visible) {
            if (visible) {
                if (!fHoverShell.isVisible()) {
                    fHoverShell.setVisible(true);
                }
            } else {
                if (fHoverShell.isVisible()) {
                    fHoverShell.setVisible(false);
                }
            }
        }

        void setText(String t) {
            if (t == null) {
                t = ""; //$NON-NLS-1$
            }
            if (!t.equals(fText)) {
                Point oldSize = getExtent();
                fText = t;
                fHoverShell.redraw();
                Point newSize = getExtent();
                if (!oldSize.equals(newSize)) {
                    Region region = new Region();
                    region.add(getPolygon(false));
                    fHoverShell.setRegion(region);
                }
            }
        }

        boolean isVisible() {
            return fHoverShell.isVisible();
        }

        void setLocation() {
            if (control != null) {
                int h = getExtent().y;
                Point point = control.toDisplay(-HD + HW / 2, -h - HH + 1);
                fHoverShell.setLocation(point.x, point.y);
            }
        }

        Point getExtent() {
            GC gc = new GC(fHoverShell);
            Point e = gc.textExtent(fText);
            gc.dispose();
            e.x += LABEL_MARGIN * 2;
            e.y += LABEL_MARGIN * 2;
            return e;
        }

        /**
         * Returns the default offset of the arrow.
         */
        public int getDefaultOffsetOfArrow() {
            return defaultOffsetOfArrow;
        }
    }
}
