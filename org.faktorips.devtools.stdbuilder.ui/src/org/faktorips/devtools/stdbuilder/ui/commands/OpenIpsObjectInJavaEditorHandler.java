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

package org.faktorips.devtools.stdbuilder.ui.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.commands.ICommandService;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.commands.IpsAbstractHandler;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.ui.StdBuilderUICommandId;

/**
 * @see StdBuilderUICommandId#COMMAND_OPEN_IPS_OBJECT_IN_JAVA_EDITOR
 * 
 * @author Alexander Weickmann
 */
public class OpenIpsObjectInJavaEditorHandler extends IpsAbstractHandler {

    // Unfortunately JDT does not expose these IDs via an enum or interface.
    private static final String JDT_COMMAND_ID_OPEN_ELEMENT_IN_JAVA_EDITOR = "org.eclipse.jdt.ui.commands.openElementInEditor";

    // Unfortunately JDT does not expose these IDs via an enum or interface.
    private static final String JDT_PARAMETER_ID_ELEMENT_REF = "elementRef";

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {

        ICommandService commandService = (ICommandService)activePage.getActivePart().getSite()
                .getService(ICommandService.class);
        Command openInJavaEditorCommand = commandService.getCommand(JDT_COMMAND_ID_OPEN_ELEMENT_IN_JAVA_EDITOR);

        try {
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            StandardBuilderSet builderSet = (StandardBuilderSet)ipsObject.getIpsProject().getIpsArtefactBuilderSet();
            IType generatedJavaImplementationType = builderSet.getGeneratedJavaImplementationType(ipsObject);

            /*
             * We need to copy the execution event because the event object does not allow to modify
             * it's command parameters (returns an immutable map for the parameters). Furthermore we
             * have to switch the event's command with the 'Open in Java Editor' command.
             */
            Map<String, String> jdtParameters = new HashMap<String, String>(1);
            ParameterType parameterType = openInJavaEditorCommand.getParameterType(JDT_PARAMETER_ID_ELEMENT_REF);
            String stringParameterValue = parameterType.getValueConverter().convertToString(
                    generatedJavaImplementationType);
            jdtParameters.put(JDT_PARAMETER_ID_ELEMENT_REF, stringParameterValue);
            ExecutionEvent copyEvent = new ExecutionEvent(openInJavaEditorCommand, jdtParameters, event.getTrigger(),
                    event.getApplicationContext());

            openInJavaEditorCommand.executeWithChecks(copyEvent);

        } catch (CoreException e) {
            throw new RuntimeException(e);
        } catch (NotDefinedException e) {
            throw new RuntimeException(e);
        } catch (NotEnabledException e) {
            throw new RuntimeException(e);
        } catch (NotHandledException e) {
            throw new RuntimeException(e);
        } catch (ParameterValueConversionException e) {
            throw new RuntimeException(e);
        }
    }

}
