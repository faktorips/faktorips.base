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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
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
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArchive;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.actions.Messages;

/**
 * 
 * @author Daniel Hohenberger
 * @author Cornelius Dirmeier
 */
public class OpenIpsObjectSelectionDialog extends FilteredItemsSelectionDialog {

    private static final String DIALOG_SETTINGS = "org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog"; //$NON-NLS-1$

    private IpsObjectTypeFilter filter;

    private final boolean onlyProductDefinitionOptions;

    /**
     * Creates a list selection dialog.
     * 
     * @param parent the parent widget.
     * @param onlyProductDefinitionOptions
     */
    public OpenIpsObjectSelectionDialog(Shell parent, boolean onlyProductDefinitionOptions) {
        super(parent);
        this.onlyProductDefinitionOptions = onlyProductDefinitionOptions;
        setListLabelProvider(new OpenIpsObjectLabelProvider());
        setDetailsLabelProvider(new PackageFragmentLabelProvider());
        setTitle(Messages.OpenIpsObjectAction_dialogTitle);
        setMessage(Messages.OpenIpsObjectAction_dialogMessage);
        setSelectionHistory(new IpsObjectSelectionHistory());
    }

    private static String getPackageSrcLabel(IIpsPackageFragment frgmt) {
        String packageSource = frgmt.getName();
        packageSource += " - " + frgmt.getIpsProject().getName() + "/" + frgmt.getRoot().getName(); //$NON-NLS-1$ //$NON-NLS-2$
        return packageSource;
    }

    public IIpsElement getSelectedObject() {
        return (IIpsElement)getFirstResult();
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        // TODO Add additional filter component
        return null;
    }

    @Override
    protected ItemsFilter createFilter() {
        filter = new IpsObjectTypeFilter();
        return filter;
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider,
            ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException {
        if (progressMonitor == null) {
            progressMonitor = new NullProgressMonitor();
        }
        List<IIpsSrcFile> list = new ArrayList<IIpsSrcFile>();
        IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
        progressMonitor.beginTask(Messages.OpenIpsObjectSelectionDialog_processName, projects.length + 1);
        for (int i = 0; i < projects.length; i++) {
            IIpsProject project = projects[i];
            project.findAllIpsSrcFiles(list);
            progressMonitor.worked(1);
        }
        for (Iterator<IIpsSrcFile> iter = list.iterator(); iter.hasNext();) {
            IIpsSrcFile object = iter.next();
            contentProvider.add(object, itemsFilter);
        }
        progressMonitor.worked(1);
        progressMonitor.done();
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
        if (settings == null) {
            settings = IpsPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }
        return settings;
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

        return new Comparator<IIpsElement>() {
            public int compare(IIpsElement o1, IIpsElement o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };

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

    private class IpsObjectTypeFilter extends ItemsFilter {

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
                if (!onlyProductDefinitionOptions || srcFile.getIpsObjectType().isProductDefinitionType()) {
                    return matches(srcFile.getName());
                }
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
            setIspSourceFile2IpsObjectMapping(true);
        }

        @Override
        public void dispose() {
            super.dispose();
            if (fBoldFont != null) {
                fBoldFont.dispose();
                fBoldFont = null;
            }
        }

        /**
         * {@inheritDoc}
         */
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

        public Image decorateImage(Image image, Object element) {
            return image;
        }

        public String decorateText(String text, Object element) {
            return getText(element);
        }

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
         * @since 3.5
         */
        private Font getBoldFont() {
            if (fBoldFont == null) {
                Font font = getDialogArea().getFont();
                FontData[] data = font.getFontData();
                for (int i = 0; i < data.length; i++) {
                    data[i].setStyle(SWT.BOLD);
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

    private static class IpsObjectSelectionHistory extends SelectionHistory {

        private static final String TAG_PATH = "path"; //$NON-NLS-1$

        private static final String TAG_NAMETYPE = "nameType"; //$NON-NLS-1$

        @Override
        protected Object restoreItemFromMemento(IMemento memento) {
            String fileName = memento.getString(TAG_PATH);
            if (fileName == null) {
                return null;
            }

            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IResource resource = root.getFile(new Path(fileName));
            IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
            IIpsElement ipsElement = ipsModel.getIpsElement(resource);
            if (ipsElement == null) {
                String nameType = memento.getString(TAG_NAMETYPE);
                if (nameType != null) {
                    IProject project = resource.getProject();
                    IIpsProject ipsProject = ipsModel.getIpsProject(project);
                    IpsArchive ipsArchive = new IpsArchive(ipsProject, resource.getFullPath());
                    try {
                        return ipsArchive.getRoot().findIpsSrcFile(QualifiedNameType.newQualifedNameType(nameType));
                    } catch (Exception e) {
                        IpsPlugin.log(e);
                    }
                }
                return null;
            } else {
                return ipsElement;
            }
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
