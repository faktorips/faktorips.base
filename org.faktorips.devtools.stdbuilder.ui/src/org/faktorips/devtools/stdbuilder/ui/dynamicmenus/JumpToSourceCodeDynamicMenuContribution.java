/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.ui.dynamicmenus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.ui.StdBuilderUICommandId;

/**
 * A dynamic menu contribution that consists of commands that allow the user to directly jump to the
 * Java source generated for the selected {@link IIpsObjectPartContainer}.
 * 
 * @author Alexander Weickmann
 */
public class JumpToSourceCodeDynamicMenuContribution extends CompoundContributionItem implements IWorkbenchContribution {

    // Unfortunately JDT does not expose this ID via an enum or interface.
    private static final String JDT_COMMAND_ID_OPEN_ELEMENT_IN_JAVA_EDITOR = "org.eclipse.jdt.ui.commands.openElementInEditor";

    // Unfortunately JDT does not expose this ID via an enum or interface.
    private static final String JDT_PARAMETER_ID_ELEMENT_REF = "elementRef";

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
            try {
                selectedItem = ((IIpsSrcFile)selectedItem).getIpsObject();
            } catch (CoreException e) {
                /*
                 * Recover from exception: If the IPS Object cannot be extracted from the source
                 * file we log the exception and show the error to the user. Then, the situation is
                 * treated as if there was no source code found.
                 */
                IpsPlugin.logAndShowErrorDialog(e);
                return getContributionItemsForNoSourceCodeFound();
            }
        }

        if (!(selectedItem instanceof IIpsObjectPartContainer)) {
            return getContributionItemsForNoSourceCodeFound();
        }

        selectedIpsObjectPartContainer = (IIpsObjectPartContainer)selectedItem;
        builderSet = (StandardBuilderSet)selectedIpsObjectPartContainer.getIpsProject().getIpsArtefactBuilderSet();

        return getContributionItemsForIpsObjectPartContainer();
    }

    private IContributionItem[] getContributionItemsForNoSourceCodeFound() {
        List<IContributionItem> contributionItems = new ArrayList<IContributionItem>(1);
        IContributionItem noSourceCodeFoundCommand = createNoSourceCodeFoundCommand();
        contributionItems.add(noSourceCodeFoundCommand);
        return contributionItems.toArray(new IContributionItem[1]);
    }

    private IContributionItem[] getContributionItemsForIpsObjectPartContainer() {
        // Obtain the Java types and their members which are generated for the IPS Object Part
        Map<IType, Set<IMember>> javaTypesToJavaElements = getJavaTypesToJavaElementsMap();

        /*
         * Go over all types (that are either generated or parent of a generated member) and add an
         * "Open in Java Editor" command contribution item for each type itself as well as its
         * members.
         */
        List<IContributionItem> contributionItems = new ArrayList<IContributionItem>(javaTypesToJavaElements.size() * 3);
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
            IMenuManager typeMenu = createTypeMenu(type, members);
            contributionItems.add(typeMenu);
        }

        if (contributionItems.isEmpty()) {
            return getContributionItemsForNoSourceCodeFound();
        }

        return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
    }

    private Map<IType, Set<IMember>> getJavaTypesToJavaElementsMap() {
        Map<IType, Set<IMember>> javaTypesToJavaElements = new LinkedHashMap<IType, Set<IMember>>(2);
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
                throw new RuntimeException("Unknown Java type.");
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
     * The sorting algorithm ensures that first stands an interface and immediately thereafter the
     * associated implementation (if there is one). Thereby it takes the used
     * {@link JavaNamingConvention} into account. Here is an example:
     * <ol>
     * <li>IPolicy
     * <li>Policy
     * <li>IProduct
     * <li>Product
     * </ol>
     * <p>
     * No sorting is done if the selected {@link IIpsObjectPartContainer} is an {@link IEnumType} or
     * {@link IEnumAttribute}.
     */
    private List<IType> sortTypes(Set<IType> javaTypes) {
        if (selectedIpsObjectPartContainer instanceof IEnumType
                || selectedIpsObjectPartContainer instanceof IEnumAttribute) {
            return Arrays.asList(javaTypes.toArray(new IType[javaTypes.size()]));
        }
        List<IType> sortedTypes = new ArrayList<IType>(javaTypes.size());
        for (IType type : javaTypes) {
            if (isInterfaceType(type)) {
                sortedTypes.add(type);
                IType implementation = getImplementationForInterface(javaTypes, type);
                if (implementation != null) {
                    sortedTypes.add(implementation);
                }
            }
        }
        return sortedTypes;
    }

    /**
     * Checks whether the given Java type is an interface type.
     * <p>
     * In contrast to {@link IType#exists()} this method uses the {@link JavaNamingConvention} and
     * the type's name for the check. This way the type does not need to be accessed which should
     * slightly increase performance and avoid certain exceptions.
     */
    private boolean isInterfaceType(IType javaType) {
        return getJavaNamingConvention().isPublishedInterfaceName(javaType.getElementName());
    }

    private IType getImplementationForInterface(Set<IType> types, IType interfaceType) {
        String searchedTypeName = getJavaNamingConvention().getImplementationClassNameForPublishedInterfaceName(
                interfaceType.getElementName());
        for (IType type : types) {
            if (type.getElementName().equals(searchedTypeName)) {
                return type;
            }
        }
        return null;
    }

    private JavaNamingConvention getJavaNamingConvention() {
        return builderSet.getJavaNamingConvention();
    }

    private IIpsElement getSelectedIpsElement() {
        // First try to use the evaluation service.
        IIpsElement selectedIpsElement = getSelectedIpsElementFromEvaluationService();
        if (selectedIpsElement != null) {
            return selectedIpsElement;
        }

        /*
         * If the evaluation service doesn't provide a selection the user activated the menu via
         * editor.
         */
        return getSelectedIpsElementFromEditor();
    }

    private IIpsElement getSelectedIpsElementFromEvaluationService() {
        IEvaluationService evaluationService = (IEvaluationService)serviceLocator.getService(IEvaluationService.class);
        Object selectedObject = evaluationService.getCurrentState().getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
        if (selectedObject instanceof ISelection) {
            TypedSelection<IIpsElement> typedSelection = TypedSelection.create(IIpsElement.class,
                    (ISelection)selectedObject);
            if (typedSelection.isValid()) {
                return typedSelection.getElement();
            }
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
        if (!typedSelection.isValid()) {
            return null;
        }

        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
        try {
            return ipsSrcFile.getIpsObject();
        } catch (CoreException e) {
            /*
             * Recover from exception: If the IPS Object cannot be accessed inform the user about
             * the error and return null as selected IPS element.
             */
            IpsPlugin.logAndShowErrorDialog(e);
            return null;
        }
    }

    private TypedSelection<IAdaptable> getSelectionFromEditor(IWorkbenchPart part) {
        IEditorInput input = ((IEditorPart)part).getEditorInput();
        if (input instanceof IFileEditorInput) {
            return new TypedSelection<IAdaptable>(IAdaptable.class, new StructuredSelection(
                    ((IFileEditorInput)input).getFile()));
        }
        return null;
    }

    /**
     * Creates a menu which represents the given {@link IType} and lists the set of it's
     * {@link IMember}.
     * <p>
     * Each member is represented by a command that allows the user to open that member in a Java
     * editor.
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

    private IContributionItem createOpenInJavaEditorCommand(IJavaElement javaElement) {
        Map<String, Object> arguments = new HashMap<String, Object>(1);
        arguments.put(JDT_PARAMETER_ID_ELEMENT_REF, javaElement);

        return createCommand(JDT_COMMAND_ID_OPEN_ELEMENT_IN_JAVA_EDITOR, arguments, getJavaElementIcon(javaElement),
                getJavaElementLabel(javaElement));
    }

    private IContributionItem createNoSourceCodeFoundCommand() {
        return createCommand(StdBuilderUICommandId.COMMAND_NO_SOURCE_CODE_FOUND.getId(), null, null, null);
    }

    private IContributionItem createCommand(String commandId,
            Map<String, Object> arguments,
            ImageDescriptor icon,
            String label) {

        // @formatter:off
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(
                serviceLocator,                               // serviceLocator
                null,                                         // id
                commandId,                                    // commandId
                arguments,                                    // arguments
                icon,                                         // icon
                null,                                         // disabledIcon
                null,                                         // hoverIcon
                label,                                        // label
                null,                                         // mnemoic
                null,                                         // tooltip
                CommandContributionItem.STYLE_PUSH,           // style
                null,                                         // helpContextId
                false                                         // visibleEnabled
        );
        // @formatter:on

        return new CommandContributionItem(itemParameter);
    }

    private String getJavaElementLabel(IJavaElement javaElement) {
        IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter)javaElement.getAdapter(IWorkbenchAdapter.class);
        return workbenchAdapter != null ? workbenchAdapter.getLabel(javaElement) : null;
    }

    private ImageDescriptor getJavaElementIcon(IJavaElement javaElement) {
        IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter)javaElement.getAdapter(IWorkbenchAdapter.class);
        return workbenchAdapter != null ? workbenchAdapter.getImageDescriptor(javaElement) : null;
    }

}
