/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productrelease;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class StatusPage extends WizardPage implements PropertyChangeListener {

    private final MessageContentProvider messageContentProvider = new MessageContentProvider();

    private TableViewer viewer;

    protected StatusPage() {
        super(Messages.ReleaserBuilderWizardSelectionPage_title, Messages.ReleaserBuilderWizardSelectionPage_title,
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/DeploymentWizard.png")); //$NON-NLS-1$
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
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof Message) {
            final Message msg = (Message)evt.getNewValue();
            messageContentProvider.addMessage(msg);
            getShell().getDisplay().syncExec(() -> {
                viewer.getControl().setEnabled(true);
                viewer.refresh();
                Table table = viewer.getTable();
                TableItem item = table.getItem(table.getItemCount() - 1);
                table.showItem(item);
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
                for (Message msg : messageList) {
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
            if (element instanceof Message msg) {
                switch (msg.getSeverity()) {
                    case ERROR:
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    case WARNING:
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                    case INFO:
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                    default:
                }
            }
            return super.getImage(element);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof Message msg) {
                return msg.getText();
            }
            return super.getText(element);
        }

    }

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
            StringBuilder sb = new StringBuilder();
            for (Object msg : elements) {
                sb.append(msg).append(System.lineSeparator());
            }
            clipboard.setContents(new String[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
        }
    }
}
