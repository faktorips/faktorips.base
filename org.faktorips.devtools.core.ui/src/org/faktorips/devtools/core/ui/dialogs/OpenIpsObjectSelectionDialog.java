/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Dialog showing a list of IpsObjects to select a single or multiple objects. This object is used
 * for the open ips object shortcut as well as for selecting an object for any reference. The filter
 * supports camel case shortcuts. The dialog also supports a history function showing last opened
 * objects on top of the list. The history is implemented global for plugin in IpsUIPlugin to allow
 * other actions like open an editor to add objects to the history. The content of the dialog is
 * configured by an {@link ISelectIpsObjectContext}.
 * <p>
 * NOTE: In eclipse 3.5 there are some new features for {@link FilteredItemsSelectionDialog}. The
 * new feature highlighting filter entries in the list is implemented in this dialog but not
 * activated until eclipse 3.5 support.
 * 
 * @author Daniel Hohenberger
 * @author Cornelius Dirmeier
 */
public class OpenIpsObjectSelectionDialog extends FilteredItemsSelectionDialog {

    public static final String DIALOG_SETTINGS = "org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog"; //$NON-NLS-1$

    private IpsSrcFileFilter filter;

    private final ISelectIpsObjectContext context;

    /**
     * Creates a list selection dialog.
     * 
     * @param parent the parent widget.
     */
    public OpenIpsObjectSelectionDialog(Shell parent, String title, ISelectIpsObjectContext context) {
        this(parent, title, context, false);
    }

    /**
     * Creates a list selection dialog and allowed the multi select of IpsObjects.
     * 
     * @param parent the parent widget.
     * @param multi allowed the multi select in list.
     */
    public OpenIpsObjectSelectionDialog(Shell parent, String title, ISelectIpsObjectContext context, boolean multi) {
        super(parent, multi);
        this.context = context;
        setListLabelProvider(new OpenIpsObjectLabelProvider());
        setDetailsLabelProvider(new PackageFragmentLabelProvider());
        setTitle(title);
        setMessage(Messages.OpenIpsObjectAction_dialogMessage);
        if (context != null && context.getContextFilter() != null) {
            addListFilter(context.getContextFilter());
        }
    }

    private static String getPackageSrcLabel(IIpsPackageFragment frgmt) {
        String packageSource = frgmt.getName();
        packageSource += " - " + frgmt.getIpsProject().getName() + "/" + frgmt.getRoot().getName(); //$NON-NLS-1$ //$NON-NLS-2$
        return packageSource;
    }

    public IIpsElement getSelectedObject() {
        return (IIpsElement)getFirstResult();
    }

    public ArrayList<IIpsElement> getSelectedObjects() {
        ArrayList<IIpsElement> selectedElements = new ArrayList<>();

        for (Object selectedElement : getResult()) {
            selectedElements.add((IIpsElement)selectedElement);
        }
        return selectedElements;
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }

    @Override
    protected ItemsFilter createFilter() {
        filter = new IpsSrcFileFilter();
        return filter;
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider,
            ItemsFilter itemsFilter,
            IProgressMonitor progressMonitorParam) throws CoreRuntimeException {

        IProgressMonitor progressMonitor = progressMonitorParam;
        if (progressMonitorParam == null) {
            progressMonitor = new NullProgressMonitor();
        }
        progressMonitor.beginTask(Messages.OpenIpsObjectSelectionDialog_processName, 100);
        @SuppressWarnings("deprecation")
        org.eclipse.core.runtime.SubProgressMonitor subMonitor = new org.eclipse.core.runtime.SubProgressMonitor(
                progressMonitor, 90);
        List<IIpsSrcFile> srcFiles = context.getIpsSrcFiles(subMonitor);
        for (IIpsSrcFile srcFile : srcFiles) {
            contentProvider.add(srcFile, itemsFilter);
        }
        progressMonitor.worked(10);
        progressMonitor.done();
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return getStaticDialogSettings();
    }

    public static IDialogSettings getStaticDialogSettings() {
        IDialogSettings settings = IpsUIPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
        if (settings == null) {
            settings = IpsUIPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }
        return settings;
    }

    @Override
    protected void restoreDialog(IDialogSettings settings) {
        super.restoreDialog(settings);
        setSelectionHistory(IpsUIPlugin.getDefault().getOpenIpsObjectHistory());
    }

    @Override
    protected void storeDialog(IDialogSettings settings) {
        super.storeDialog(settings);
        IpsUIPlugin.getDefault().saveOpenIpsObjectHistory((IpsObjectSelectionHistory)getSelectionHistory());
    }

    @Override
    public String getElementName(Object item) {
        if (item instanceof IIpsElement) {
            IIpsElement element = (IIpsElement)item;
            return element.getName();
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    protected Comparator<IIpsElement> getItemsComparator() {
        return Comparator.comparing(IIpsElement::getName);

    }

    @Override
    protected IStatus validateItem(Object item) {
        return new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, null);
    }

    public void setFilter(String unqualifiedName) {
        setInitialPattern(unqualifiedName);
    }

    private static IIpsPackageFragment getPackageFragment(Object element) {
        if (element instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)element;
            return ipsObject.getIpsPackageFragment();
        } else if (element instanceof IIpsSrcFile) {
            IIpsSrcFile srcFile = (IIpsSrcFile)element;
            return srcFile.getIpsPackageFragment();
        }
        return null;
    }

    private class IpsSrcFileFilter extends ItemsFilter {

        private static final String ALL_PATTERN = "?"; //$NON-NLS-1$

        public IpsSrcFileFilter() {
            super();
            // empty string should match all columns
            if (StringUtils.isEmpty(patternMatcher.getPattern())) {
                patternMatcher.setPattern(ALL_PATTERN);
            }
        }

        @Override
        public boolean isConsistentItem(Object object) {
            if (object instanceof IIpsSrcFile) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)object;
                return ipsSrcFile.exists();
            }
            return true;
        }

        @Override
        public boolean matchItem(Object object) {
            if (object instanceof IIpsSrcFile) {
                IIpsSrcFile srcFile = (IIpsSrcFile)object;
                return matches(srcFile.getName());
            }
            return false;
        }

    }

    private class OpenIpsObjectLabelProvider extends DefaultLabelProvider implements ILabelDecorator,
            IStyledLabelProvider {

        private static final String PACKAGE_CONCAT = " - "; //$NON-NLS-1$

        private Font fBoldFont;
        private Styler fBoldStyler;

        public OpenIpsObjectLabelProvider() {
            fBoldStyler = createBoldStyler();
        }

        @Override
        public void dispose() {
            super.dispose();
            if (fBoldFont != null) {
                fBoldFont.dispose();
                fBoldFont = null;
            }
        }

        @Override
        public String getText(Object element) {
            String label = super.getText(element);
            if (isDuplicateElement(element)) {
                label += PACKAGE_CONCAT + getAdditionalLabel(element);
            }
            return label;
        }

        private String getAdditionalLabel(Object element) {
            IIpsPackageFragment fragment = getPackageFragment(element);
            if (fragment != null) {
                return getPackageSrcLabel(fragment);
            } else {
                return ""; //$NON-NLS-1$
            }
        }

        @Override
        public Image decorateImage(Image image, Object element) {
            return image;
        }

        @Override
        public String decorateText(String text, Object element) {
            return getText(element);
        }

        @Override
        public StyledString getStyledText(Object element) {
            String text = getText(element);
            StyledString string = new StyledString(text);

            int index = text.indexOf(PACKAGE_CONCAT);

            if (filter == null) {
                return string;
            }

            String namePattern = filter.getPattern();
            if (namePattern != null && !"*".equals(namePattern)) { //$NON-NLS-1$
                String typeName = index == -1 ? text : text.substring(0, index);
                int[] matchingRegions = SearchPattern.getMatchingRegions(namePattern, typeName, filter.getMatchRule());
                createAndAddStyler(string, matchingRegions, fBoldStyler);
            }

            if (index != -1) {
                string.setStyle(index, text.length() - index, StyledString.QUALIFIER_STYLER);
            }
            return string;
        }

        private void createAndAddStyler(StyledString string, int[] matchingRegions, Styler styler) {
            if (matchingRegions != null) {
                int offset = -1;
                int length = 0;
                for (int i = 0; i + 1 < matchingRegions.length; i = i + 2) {
                    if (offset == -1) {
                        offset = matchingRegions[i];
                    }
                    if (i + 2 < matchingRegions.length
                            && matchingRegions[i] + matchingRegions[i + 1] == matchingRegions[i + 2]) {
                        length = length + matchingRegions[i + 1];
                    } else {
                        string.setStyle(offset, length + matchingRegions[i + 1], styler);
                        offset = -1;
                        length = 0;
                    }
                }
            }
        }

        /**
         * Create the bold variant of the currently used font.
         * 
         * @return the bold font
         */
        private Font getBoldFont() {
            if (fBoldFont == null) {
                Font font = getDialogArea().getFont();
                FontData[] data = font.getFontData();
                for (FontData element : data) {
                    element.setStyle(SWT.BOLD);
                }
                fBoldFont = new Font(font.getDevice(), data);
            }
            return fBoldFont;
        }

        private Styler createBoldStyler() {
            return new Styler() {
                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.font = getBoldFont();
                }
            };
        }

    }

    /**
     * A <code>LabelProvider</code> for the label showing type details.
     */
    private static class PackageFragmentLabelProvider extends DefaultLabelProvider {

        @Override
        public Image getImage(Object element) {
            IIpsPackageFragment packageFragment = getPackageFragment(element);
            if (packageFragment != null) {
                return super.getImage(packageFragment);
            } else {
                return super.getImage(element);
            }
        }

        @Override
        public String getText(Object element) {
            IIpsPackageFragment packageFragment = getPackageFragment(element);
            if (packageFragment != null) {
                return getPackageSrcLabel(packageFragment);
            } else {
                return super.getText(element);
            }
        }

    }

    public static class IpsObjectSelectionHistory extends SelectionHistory {

        /** In memento the path to the IpsSrcFile is stored with this tag */
        static final String TAG_PATH = "path"; //$NON-NLS-1$

        /**
         * If the resource is an archive, the IpsSrcFile is stored in an Archive Store the qualified
         * name type of the IpsSrcFile within the archive with this tag
         */
        static final String TAG_NAMETYPE = "nameType"; //$NON-NLS-1$

        @Override
        protected Object restoreItemFromMemento(IMemento memento) {
            String fileName = memento.getString(TAG_PATH);
            if (fileName == null) {
                return null;
            }

            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            Path path = new Path(fileName);
            IResource resource;
            try {
                resource = root.getFile(path);
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                // If the resource is invalid we return null, the object will be removed
                return null;
            }
            IIpsModel ipsModel = IIpsModel.get();
            IIpsElement ipsElement = ipsModel.getIpsElement(resource);
            if (ipsElement instanceof IIpsSrcFile) {
                return ipsElement;
            } else {
                return getIpsSrcFileFromArchive(memento, resource);
            }
        }

        protected IIpsSrcFile getIpsSrcFileFromArchive(IMemento memento, IResource resource) {
            IIpsModel ipsModel = IIpsModel.get();
            String nameType = memento.getString(TAG_NAMETYPE);
            if (nameType != null) {
                IProject project = resource.getProject();
                IIpsProject ipsProject = ipsModel.getIpsProject(project);
                String resourceName = resource.getName();
                IIpsPackageFragmentRoot packageFragmentRoot = ipsProject.getIpsPackageFragmentRoot(resourceName);
                if (packageFragmentRoot != null) {
                    try {
                        return packageFragmentRoot.findIpsSrcFile(QualifiedNameType.newQualifedNameType(nameType));
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void storeItemToMemento(Object object, IMemento memento) {
            if (object instanceof IIpsSrcFile) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)object;
                IResource resource = ipsSrcFile.getEnclosingResource();
                memento.putString(TAG_PATH, resource.getFullPath().toString());
                if (ipsSrcFile.isContainedInArchive()) {
                    memento.putString(TAG_NAMETYPE, ipsSrcFile.getQualifiedNameType().toPath().toString());
                }
            }
        }
    }

}
