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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * A dynamic menu contribution that consists of commands that allow the user to directly jump to the
 * Java source generated for the selected {@link IIpsObjectPartContainer}.
 * 
 * @author Alexander Weickmann
 */
public class JumpToSourceCodeDynamicMenuContribution extends CompoundContributionItem implements IWorkbenchContribution {

    private static final String COMMAND_ID_OPEN_ELEMENT_IN_EDITOR = "org.eclipse.jdt.ui.commands.openElementInEditor";

    private static final String PARAMETER_ID_ELEMENT_REF = "elementRef";

    private IServiceLocator serviceLocator;

    @Override
    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        IEvaluationService evaluationService = (IEvaluationService)serviceLocator.getService(IEvaluationService.class);
        IStructuredSelection selection = (IStructuredSelection)evaluationService.getCurrentState().getVariable(
                ISources.ACTIVE_MENU_SELECTION_NAME);
        Object selectedItem = selection.getFirstElement();
        if (selectedItem instanceof IIpsSrcFile) {
            try {
                selectedItem = ((IIpsSrcFile)selectedItem).getIpsObject();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        if (!(selectedItem instanceof IIpsObjectPartContainer)) {
            return new IContributionItem[0];
        }

        // Obtain the Java elements generated for the IPS object part container
        IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)selectedItem;
        StandardBuilderSet builderSet = (StandardBuilderSet)ipsObjectPartContainer.getIpsProject()
                .getIpsArtefactBuilderSet();
        List<IJavaElement> generatedJavaElements = builderSet.getGeneratedJavaElements(ipsObjectPartContainer);

        // Add an "Open in Java Editor" command contribution for each generated Java element
        IContributionItem[] contributionItems = new IContributionItem[generatedJavaElements.size()];
        for (int i = 0; i < contributionItems.length; i++) {
            IJavaElement javaElement = generatedJavaElements.get(i);
            contributionItems[i] = createOpenInJavaEditorCommandContributionItem(javaElement);
        }

        return contributionItems;
    }

    private CommandContributionItem createOpenInJavaEditorCommandContributionItem(IJavaElement javaElement) {
        Map<String, Object> arguments = new HashMap<String, Object>(1);
        arguments.put(PARAMETER_ID_ELEMENT_REF, javaElement);
        // @formatter:off
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(
                serviceLocator,                                         // serviceLocator
                null,                                                   // id
                COMMAND_ID_OPEN_ELEMENT_IN_EDITOR,                      // commandId
                arguments,                                              // arguments
                null,                                                   // icon
                null,                                                   // disabledIcon
                null,                                                   // hoverIcon
                javaElement.getElementName(),                           // label
                null,                                                   // mnemoic
                null,                                                   // tooltip
                CommandContributionItem.STYLE_PUSH,                     // style
                null,                                                   // helpContextId
                false                                                   // visibleEnabled
        );
        // @formatter:on
        return new CommandContributionItem(itemParameter);
    }

}
