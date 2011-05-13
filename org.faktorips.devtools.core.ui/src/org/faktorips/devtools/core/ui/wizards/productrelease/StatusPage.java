/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productrelease;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class StatusPage extends WizardPage implements Observer {

    private final MessageContentProvider messageContentProvider = new MessageContentProvider();

    private TableViewer viewer;

    private static class CopyAction extends Action {
        private MessageContentProvider contentProvider;
        private Clipboard clipboard;

        public CopyAction(Clipboard clipboard, MessageContentProvider contentProvider) {
            super(Messages.StatusPage_copyToClipboardActionText);
            this.clipboard = clipboard;
            this.contentProvider = contentProvider;
        }

        @Override
        public void run() {
            Object[] elements = contentProvider.getElements(null);
            StringBuffer sb = new StringBuffer();
            for (Object msg : elements) {
                sb.append(msg).append(SystemUtils.LINE_SEPARATOR);
            }
            clipboard.setContents(new String[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
        }
    }

    protected StatusPage() {
        super(Messages.ReleaserBuilderWizardSelectionPage_title, Messages.ReleaserBuilderWizardSelectionPage_title,
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/DeploymentWizard.png"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, true));
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        pageControl.setLayoutData(data);

        viewer = new TableViewer(pageControl);
        viewer.setContentProvider(messageContentProvider);
        viewer.setLabelProvider(new MessageLabelProvider());
        GridData data2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewer.getControl().setLayoutData(data2);
        viewer.setInput(new MessageList());

        initPopupMenu(viewer);

        setControl(pageControl);
    }

    private void initPopupMenu(TableViewer tableViewer) {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(false);
        Control control = tableViewer.getControl();
        menuMgr.add(new CopyAction(new Clipboard(control.getDisplay()), messageContentProvider));
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
    }

    public void setEnable(boolean state) {
        viewer.getControl().setEnabled(state);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Message) {
            final Message msg = (Message)arg;
            messageContentProvider.addMessage(msg);
            getShell().getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    viewer.getControl().setEnabled(true);
                    viewer.refresh();
                    Table table = viewer.getTable();
                    TableItem item = table.getItem(table.getItemCount() - 1);
                    table.showItem(item);
                }
            });
        }
    }

    private static class MessageContentProvider implements IStructuredContentProvider {

        private MessageList messageList = new MessageList();

        @Override
        public Object[] getElements(Object inputElement) {
            if (messageList != null) {
                Object[] result = new Object[messageList.size()];
                int i = 0;
                for (Iterator<Message> iterator = messageList.iterator(); iterator.hasNext();) {
                    Message msg = iterator.next();
                    result[i] = msg;
                    i++;
                }
                return result;
            }
            return new Object[0];
        }

        public void addMessage(Message message) {
            messageList.add(message);
        }

        @Override
        public void dispose() {
            // empty default implementation
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof MessageList) {
                messageList = (MessageList)newInput;
            } else {
                messageList = null;
            }
        }
    }

    private static class MessageLabelProvider extends DefaultLabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element instanceof Message) {
                Message msg = (Message)element;
                switch (msg.getSeverity()) {
                    case Message.ERROR:
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    case Message.WARNING:
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                    case Message.INFO:
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                    default:
                }
            }
            return super.getImage(element);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof Message) {
                Message msg = (Message)element;
                return msg.getText();
            }
            return super.getText(element);
        }

    }

}
