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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * A dynamic menu contribution that consists of commands that allow the user to directly jump to the
 * Java source generated for the selected {@link IIpsObjectPartContainer}.
 * 
 * @author Alexander Weickmann
 */
public class JumpToSourceCodeDynamicMenuContribution extends CompoundContributionItem implements IWorkbenchContribution {

    private static final String COMMAND_ID_OPEN_ELEMENT_IN_EDITOR = "org.eclipse.jdt.ui.commands.openElementInEditor";

    private static final String COMMAND_ID_NO_SOURCE_CODE_FOUND = "org.faktorips.devtools.stdbuilder.ui.commands.NoSourceCodeFound";

    private static final String PARAMETER_ID_ELEMENT_REF = "elementRef";

    private IServiceLocator serviceLocator;

    @Override
    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        IIpsElement selectedItem = getSelectedIpsElement();
        if (selectedItem instanceof IIpsSrcFile) {
            try {
                selectedItem = ((IIpsSrcFile)selectedItem).getIpsObject();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        if (!(selectedItem instanceof IIpsObjectPartContainer)) {
            List<IContributionItem> contributionItems = new ArrayList<IContributionItem>(1);
            addNoSourceCodeFoundCommand(contributionItems);
            return contributionItems.toArray(new IContributionItem[1]);
        }
        return getContributionItemsForIpsObjectPartContainer((IIpsObjectPartContainer)selectedItem);
    }

    private IContributionItem[] getContributionItemsForIpsObjectPartContainer(IIpsObjectPartContainer ipsObjectPartContainer) {
        /*
         * Obtain the Java types and their members which are generated for the IPS Object Part
         * Container.
         */
        Map<IType, Set<IMember>> javaTypesToJavaElements = getJavaTypesToJavaElementsMap(ipsObjectPartContainer);

        /*
         * Go over all types (that are either generated or parent of a generated member) and add an
         * "Open in Java Editor" command contribution item for each type itself as well as its
         * members.
         */
        List<IContributionItem> contributionItems = new ArrayList<IContributionItem>(javaTypesToJavaElements.size() * 3);
        List<IType> sortedTypes = sortTypes(javaTypesToJavaElements.keySet());
        for (int i = 0; i < sortedTypes.size(); i++) {
            IType type = sortedTypes.get(i);
            if (!(type.exists())) {
                continue;
            }
            addOpenInJavaEditorCommand(contributionItems, type);
            for (IMember member : javaTypesToJavaElements.get(type)) {
                if (member.exists()) {
                    addOpenInJavaEditorCommand(contributionItems, member);
                }
            }
            /*
             * Add a separator after each type's members but do not add a separator at the very
             * bottom of the menu.
             */
            if (i < sortedTypes.size() - 1) {
                addSeparator(contributionItems);
            }
        }

        if (contributionItems.isEmpty()) {
            addNoSourceCodeFoundCommand(contributionItems);
        }

        return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
    }

    private Map<IType, Set<IMember>> getJavaTypesToJavaElementsMap(IIpsObjectPartContainer ipsObjectPartContainer) {
        Map<IType, Set<IMember>> javaTypesToJavaElements = new HashMap<IType, Set<IMember>>(2);
        for (IJavaElement javaElement : getGeneratedJavaElements(ipsObjectPartContainer)) {
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

    private List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        StandardBuilderSet builderSet = (StandardBuilderSet)ipsObjectPartContainer.getIpsProject()
                .getIpsArtefactBuilderSet();
        return builderSet.getGeneratedJavaElements(ipsObjectPartContainer);
    }

    /**
     * Takes a set of {@link IType}s as input and creates / returns a sorted list according to the
     * fact whether or not a type is an interface.
     * <p>
     * Classes and enums are considered "less than" interfaces.
     */
    private List<IType> sortTypes(Set<IType> types) {
        List<IType> sortedTypes = new ArrayList<IType>(types.size());
        addToSortedTypes(sortedTypes, types, false);
        addToSortedTypes(sortedTypes, types, true);
        return sortedTypes;
    }

    private void addToSortedTypes(List<IType> sortedTypes, Set<IType> types, boolean interfacesIfTrueClassesOtherwise) {
        for (IType type : types) {
            if (type.exists()) {
                try {
                    boolean addCondition = interfacesIfTrueClassesOtherwise ? type.isInterface()
                            : !(type.isInterface());
                    if (addCondition) {
                        sortedTypes.add(type);
                    }
                } catch (JavaModelException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addTypeIfNotPresent(Map<IType, Set<IMember>> javaTypesToJavaElements, IType type) {
        if (!(javaTypesToJavaElements.containsKey(type))) {
            javaTypesToJavaElements.put(type, new HashSet<IMember>());
        }
    }

    private IIpsElement getSelectedIpsElement() {
        IEvaluationService evaluationService = (IEvaluationService)serviceLocator.getService(IEvaluationService.class);
        IStructuredSelection selection = (IStructuredSelection)evaluationService.getCurrentState().getVariable(
                ISources.ACTIVE_MENU_SELECTION_NAME);
        TypedSelection<IIpsElement> typedSelection = TypedSelection.create(IIpsElement.class, selection);
        return typedSelection.getElement();
    }

    private void addSeparator(List<IContributionItem> contributionItems) {
        contributionItems.add(new Separator());
    }

    private void addOpenInJavaEditorCommand(List<IContributionItem> contributionItems, IJavaElement javaElement) {
        Map<String, Object> arguments = new HashMap<String, Object>(1);
        arguments.put(PARAMETER_ID_ELEMENT_REF, javaElement);

        addCommand(contributionItems, COMMAND_ID_OPEN_ELEMENT_IN_EDITOR, arguments, getJavaElementIcon(javaElement),
                getJavaElementLabel(javaElement));
    }

    private void addNoSourceCodeFoundCommand(List<IContributionItem> contributionItems) {
        addCommand(contributionItems, COMMAND_ID_NO_SOURCE_CODE_FOUND, null, null, null);
    }

    private void addCommand(List<IContributionItem> contributionItems,
            String commandId,
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

        contributionItems.add(new CommandContributionItem(itemParameter));
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
