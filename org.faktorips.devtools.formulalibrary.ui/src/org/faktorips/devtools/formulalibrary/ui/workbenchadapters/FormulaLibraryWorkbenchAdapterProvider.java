/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui.workbenchadapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.workbenchadapters.DefaultIpsObjectWorkbenchAdapter;
import org.faktorips.devtools.core.ui.workbenchadapters.IWorkbenchAdapterProvider;
import org.faktorips.devtools.core.ui.workbenchadapters.IpsElementWorkbenchAdapter;
import org.faktorips.devtools.core.ui.workbenchadapters.MethodWorkbenchAdapter;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaFunction;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibrary;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaMethod;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

/**
 * WorkbenchAdapterProvider for the {@link IFormulaLibrary}
 * 
 * @author frank
 */
public class FormulaLibraryWorkbenchAdapterProvider implements IWorkbenchAdapterProvider {

	private static final String PLUGIN_ID = "org.faktorips.devtools.formulalibrary.ui"; //$NON-NLS-1$
	private static final String FORMULA_LIBRARY_GIF = "FormulaLibrary.gif"; //$NON-NLS-1$
    private static final String FORMULA_FUNCTION_GIF = "Function.gif"; //$NON-NLS-1$
    private static final String ICONS_FOLDER = "icons/"; //$NON-NLS-1$

    private Map<Class<? extends IpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public FormulaLibraryWorkbenchAdapterProvider() {
        workbenchAdapterMap = new HashMap<Class<? extends IpsElement>, IpsElementWorkbenchAdapter>();
        registerAdapters();
    }

    @Override
    public Map<Class<? extends IpsElement>, IpsElementWorkbenchAdapter> getAdapterMap() {
        return workbenchAdapterMap;
    }

    private void registerAdapters() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_FOLDER
                + FORMULA_LIBRARY_GIF);
        register(FormulaLibrary.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_FOLDER
                + FORMULA_FUNCTION_GIF);
        register(FormulaFunction.class, new FormulaFunctionIpsObjectPartWorkbenchAdapter(imageDescriptor));
        register(FormulaMethod.class, new MethodWorkbenchAdapter());
    }

    private void register(Class<? extends IpsElement> adaptableClass, IpsElementWorkbenchAdapter adapter) {
        workbenchAdapterMap.put(adaptableClass, adapter);
    }
}
