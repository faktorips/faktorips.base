/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.ui.dynamicmenus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.ui.StdBuilderUICommandId;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A dynamic menu contribution that consists of commands that allow the user to directly jump to the
 * Java source generated for the selected {@link IIpsObjectPartContainer}.
 * 
 * @author Alexander Weickmann
 */
public class JumpToSourceCodeDynamicMenuContribution extends CompoundContributionItem
        implements IWorkbenchContribution {

    private static final String EDITOR_JUMP_TO_SOURCE_CODE_COMMAND = "org.faktorips.devtools.stdbuilder.ui.dynamicmenus.editorJumpToSourceCode"; //$NON-NLS-1$

    // Unfortunately JDT does not expose this ID via an enum or interface.
    private static final String JDT_COMMAND_ID_OPEN_ELEMENT_IN_JAVA_EDITOR = "org.eclipse.jdt.ui.commands.openElementInEditor"; //$NON-NLS-1$

    // Unfortunately JDT does not expose this ID via an enum or interface.
    private static final String JDT_PARAMETER_ID_ELEMENT_REF = "elementRef"; //$NON-NLS-1$

    private IServiceLocator serviceLocator;

    private StandardBuilderSet builderSet;

    private IIpsObjectPartContainer selectedIpsObjectPartContainer;

    @Override
    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public IContributionItem[] getContributionItems() {
        IIpsElement selectedItem = getSelectedIpsElement();
        if (selectedItem == null) {
            return getContributionItemsForNoSourceCodeFound();
        }
        if (selectedItem instanceof IIpsSrcFile) {
            selectedItem = ((IIpsSrcFile)selectedItem).getIpsObject();
        }

        if (!(selectedItem instanceof IIpsObjectPartContainer)) {
            return getContributionItemsForNoSourceCodeFound();
        }

        selectedIpsObjectPartContainer = (IIpsObjectPartContainer)selectedItem;
        builderSet = (StandardBuilderSet)selectedIpsObjectPartContainer.getIpsProject().getIpsArtefactBuilderSet();

        return getContributionItemsForIpsObjectPartContainer();
    }

    private IIpsElement getSelectedIpsElement() {
        if (getParent() != null && !getParent().getItems()[0].getId().equals(EDITOR_JUMP_TO_SOURCE_CODE_COMMAND)) {
            IIpsElement selectedIpsElement = getSelectedIpsElementFromEvaluationService();
            if (selectedIpsElement != null) {
                return selectedIpsElement;
            }
        }
        return getSelectedIpsElementFromEditor();
    }

    private IIpsElement getSelectedIpsElementFromEvaluationService() {
        ISelectionService service = serviceLocator.getService(ISelectionService.class);
        ISelection selectedObject = service.getSelection();
        TypedSelection<IAdaptable> typedSelection = TypedSelection.create(IAdaptable.class, selectedObject);
        if (typedSelection.isValid()) {
            return typedSelection.getElement().getAdapter(IIpsElement.class);
        }
        return null;
    }

    private IIpsElement getSelectedIpsElementFromEditor() {
        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPart part = activeWindow.getPartService().getActivePart();
        if (!(part instanceof IEditorPart)) {
            return null;
        }

        TypedSelection<IAdaptable> typedSelection = getSelectionFromEditor(part);
        if (typedSelection == null || !typedSelection.isValid()) {
            return null;
        }

        IIpsSrcFile ipsSrcFile = typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
        return ipsSrcFile.getIpsObject();
    }

    private TypedSelection<IAdaptable> getSelectionFromEditor(IWorkbenchPart part) {
        IEditorInput input = ((IEditorPart)part).getEditorInput();
        if (input instanceof IFileEditorInput) {
            return new TypedSelection<>(IAdaptable.class,
                    new StructuredSelection(((IFileEditorInput)input).getFile()));
        }
        return null;
    }

    private IContributionItem[] getContributionItemsForNoSourceCodeFound() {
        List<IContributionItem> contributionItems = new ArrayList<>(1);
        IContributionItem noSourceCodeFoundCommand = createNoSourceCodeFoundCommand();
        contributionItems.add(noSourceCodeFoundCommand);
        return contributionItems.toArray(new IContributionItem[1]);
    }

    private IContributionItem createNoSourceCodeFoundCommand() {
        return createCommand(StdBuilderUICommandId.COMMAND_NO_SOURCE_CODE_FOUND.getId(), null, null, null);
    }

    private IContributionItem createCommand(String commandId,
            Map<String, Object> arguments,
            ImageDescriptor icon,
            String label) {

        // CSOFF: TrailingComment
        // @formatter:off
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(serviceLocator, // serviceLocator
                null, // id
                commandId, // commandId
                arguments, // arguments
                icon, // icon
                null, // disabledIcon
                null, // hoverIcon
                label, // label
                null, // mnemoic
                null, // tooltip
                CommandContributionItem.STYLE_PUSH, // style
                null, // helpContextId
                false // visibleEnabled
                );
        // @formatter:on
        // CSON: TrailingComment

        return new CommandContributionItem(itemParameter);
    }

    private IContributionItem[] getContributionItemsForIpsObjectPartContainer() {
        // Obtain the Java types and their members which are generated for the IPS Object Part
        Map<IType, Set<IMember>> javaTypesToJavaElements = getJavaTypesToJavaElementsMap();

        /*
         * Go over all types (that are either generated or parent of a generated member) and add an
         * "Open in Java Editor" command contribution item for each type itself as well as its
         * members.
         */
        List<IContributionItem> contributionItems = new ArrayList<>(
                javaTypesToJavaElements.size() * 3);
        List<IType> sortedJavaTypes = sortTypes(javaTypesToJavaElements.keySet());
        for (IType type : sortedJavaTypes) {
            if (!type.exists()) {
                continue;
            }
            Set<IMember> members = javaTypesToJavaElements.get(type);
            if (members.isEmpty()) {
                IContributionItem openTypeCommand = createOpenInJavaEditorCommand(type);
                contributionItems.add(openTypeCommand);
                continue;
            }
            createContributionItemsForMembers(contributionItems, type, members);
        }

        if (contributionItems.isEmpty()) {
            return getContributionItemsForNoSourceCodeFound();
        }

        return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
    }

    private Map<IType, Set<IMember>> getJavaTypesToJavaElementsMap() {
        Map<IType, Set<IMember>> javaTypesToJavaElements = new LinkedHashMap<>(2);
        for (IJavaElement javaElement : builderSet.getGeneratedJavaElements(selectedIpsObjectPartContainer)) {
            IType type = null;
            if (javaElement instanceof IType) {
                type = (IType)javaElement;
                addTypeIfNotPresent(javaTypesToJavaElements, type);
            } else if (javaElement instanceof IMember) {
                type = (IType)javaElement.getParent();
                addTypeIfNotPresent(javaTypesToJavaElements, type);
                Set<IMember> members = javaTypesToJavaElements.get(type);
                members.add((IMember)javaElement);
            } else {
                throw new RuntimeException("Unknown Java type."); //$NON-NLS-1$
            }
        }
        return javaTypesToJavaElements;
    }

    private void addTypeIfNotPresent(Map<IType, Set<IMember>> javaTypesToJavaElements, IType type) {
        if (!(javaTypesToJavaElements.containsKey(type))) {
            javaTypesToJavaElements.put(type, new LinkedHashSet<IMember>());
        }
    }

    /**
     * Takes a set of {@link IType}s as input and creates / returns a sorted version of it.
     * <p>
     * The sorting algorithm ensures that first stand all implementations and thereafter the
     * interfaces. Thereby it takes the used {@link JavaNamingConvention} into account. Here is an
     * example:
     * <ol>
     * <li>Policy
     * <li>Product
     * <li>IPolicy
     * <li>IProduct
     * </ol>
     */
    private List<IType> sortTypes(Set<IType> javaTypes) {
        List<IType> sortedTypes = new ArrayList<>(javaTypes.size());
        for (IType type : javaTypes) {
            try {
                if (!type.isInterface()) {
                    sortedTypes.add(type);
                    IType implementation = getImplementationForInterface(javaTypes, type);
                    if (implementation != null) {
                        sortedTypes.add(implementation);
                    }
                }
            } catch (JavaModelException e) {
                /*
                 * Continue with the remaining types, the type for which the exception was thrown
                 * will be added to the end of the menu. In addition we log the error.
                 */
                IpsPlugin.log(e);
            }
        }
        // Add types that could not be sorted by interface
        if (sortedTypes.size() != javaTypes.size()) {
            for (IType type : javaTypes) {
                if (!sortedTypes.contains(type)) {
                    sortedTypes.add(type);
                }
            }
        }
        return sortedTypes;
    }

    private IType getImplementationForInterface(Set<IType> types, IType interfaceType) {
        String searchedTypeName = getJavaNamingConvention()
                .getImplementationClassNameForPublishedInterfaceName(interfaceType.getElementName());
        for (IType type : types) {
            if (type.getElementName().equals(searchedTypeName)) {
                return type;
            }
        }
        return null;
    }

    private IJavaNamingConvention getJavaNamingConvention() {
        return selectedIpsObjectPartContainer.getIpsProject().getJavaNamingConvention();
    }

    private IContributionItem createOpenInJavaEditorCommand(IJavaElement javaElement) {
        Map<String, Object> arguments = new HashMap<>(1);
        arguments.put(JDT_PARAMETER_ID_ELEMENT_REF, javaElement);

        return createCommand(JDT_COMMAND_ID_OPEN_ELEMENT_IN_JAVA_EDITOR, arguments, getJavaElementIcon(javaElement),
                getJavaElementLabel(javaElement));
    }

    private String getJavaElementLabel(IJavaElement javaElement) {
        if (isTypeLabelRequired(javaElement)) {
            return getLabelWithTypeLabel(javaElement);
        } else {
            return getLabelOnly(javaElement);
        }
    }

    private boolean isTypeLabelRequired(IJavaElement javaElement) {
        IType type = getType(javaElement);
        return type != null && !isInterface(type) && !isConstructor(javaElement);
    }

    /**
     * Returns the {@link IType} of the given {@link IJavaElement}. If no {@link IType type} can be
     * found, <code>null</code> is returned.
     * 
     * @param javaElement The {@link IJavaElement} that is declared in the {@link IType}.
     */
    private IType getType(IJavaElement javaElement) {
        IType type = null;
        if (javaElement instanceof IMember) {
            type = ((IMember)javaElement).getDeclaringType();
        }
        return type;
    }

    private boolean isInterface(IType type) {
        try {
            return type.isInterface();
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isConstructor(IJavaElement javaElement) {
        String typeName = getTypeName(javaElement);
        if (javaElement instanceof IMember) {
            return ((IMember)javaElement).getElementName().equalsIgnoreCase(typeName);
        }
        return false;
    }

    private String getTypeName(IJavaElement javaElement) {
        IType type = getType(javaElement);
        String typeName = IpsStringUtils.EMPTY;
        if (type != null) {
            typeName = type.getElementName();
        }
        return typeName;
    }

    private String getLabelWithTypeLabel(IJavaElement javaElement) {
        return getLabelOnly(javaElement) + " \t " + getTypeName(javaElement); //$NON-NLS-1$
    }

    private String getLabelOnly(IJavaElement javaElement) {
        IWorkbenchAdapter workbenchAdapter = javaElement.getAdapter(IWorkbenchAdapter.class);
        return workbenchAdapter != null ? workbenchAdapter.getLabel(javaElement) : null;
    }

    private ImageDescriptor getJavaElementIcon(IJavaElement javaElement) {
        IWorkbenchAdapter workbenchAdapter = javaElement.getAdapter(IWorkbenchAdapter.class);
        return workbenchAdapter != null ? workbenchAdapter.getImageDescriptor(javaElement) : null;
    }

    /**
     * Creates {@link IContributionItem}s for each {@link IMember} and adds them to the list of
     * contributionItems.
     * <p>
     * Each member is represented by a command that allows the user to open that member in a Java
     * editor.
     * 
     * @param contributionItems This list holds all {@link IContributionItem}s that are displayed in
     *            the jump to sourcecode context menu.
     * @param type The {@link IMember}s are part of this {@link IType}. The type represents an
     *            interface or implementation class
     * @param members A set of {@link IMember}s that contains java elements like fields,
     *            constructors and methods.
     */
    private void createContributionItemsForMembers(List<IContributionItem> contributionItems,
            IType type,
            Set<IMember> members) {
        if (!isInterface(type)) {
            createItemsWithoutTypeMenu(contributionItems, members);
        } else {
            IMenuManager typeMenu = createTypeMenu(type, members);
            contributionItems.add(typeMenu);
        }
    }

    /**
     * Creates {@link IContributionItem}s for each {@link IMember} and adds them directly (without a
     * sub-menu) to the list of contributionItems.
     * 
     * @param contributionItems This list holds all {@link IContributionItem}s that are displayed in
     *            the jump to sourcecode context menu.
     * @param members A set of {@link IMember}s that contains java elements like fields,
     *            constructors and methods.
     */
    private void createItemsWithoutTypeMenu(List<IContributionItem> contributionItems, Set<IMember> members) {
        for (IMember member : members) {
            if (member.exists()) {
                IContributionItem openInJavaEditorCommand = createOpenInJavaEditorCommand(member);
                contributionItems.add(openInJavaEditorCommand);
            }
        }
        contributionItems.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
    }

    /**
     * Creates a menu which represents the given {@link IType} and adds a menu item for each
     * {@link IMember}.
     */
    private IMenuManager createTypeMenu(IType type, Set<IMember> members) {
        IMenuManager typeMenu = new MenuManager(getJavaElementLabel(type), getJavaElementIcon(type), null);
        for (IMember member : members) {
            if (member.exists()) {
                IContributionItem openInJavaEditorCommand = createOpenInJavaEditorCommand(member);
                typeMenu.add(openInJavaEditorCommand);
            }
        }
        if (typeMenu.isEmpty()) {
            IContributionItem noSourceCodeFoundCommand = createNoSourceCodeFoundCommand();
            typeMenu.add(noSourceCodeFoundCommand);
        }
        return typeMenu;
    }

}
