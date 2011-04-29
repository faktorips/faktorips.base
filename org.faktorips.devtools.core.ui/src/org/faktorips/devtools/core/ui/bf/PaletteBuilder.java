/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Builds the palette that is needed by the business function edtior.
 * 
 * @author Peter Erzberger
 */
public class PaletteBuilder {

    private List<PaletteContainer> createCategories(PaletteRoot root) {
        List<PaletteContainer> categories = new ArrayList<PaletteContainer>();
        categories.add(createControlGroup(root));
        categories.add(createComponentsDrawer());
        return categories;
    }

    private PaletteContainer createComponentsDrawer() {
        List<ToolEntry> entries = new ArrayList<ToolEntry>();

        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                "obj16/CallOperationAction.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry actionEntry = new CombinedTemplateCreationEntry(
                BFElementType.ACTION_METHODCALL.getName(), Messages.PaletteBuilder_methodCallActionDesc,
                new BFElementFactory(BFElementType.ACTION_METHODCALL), imageDescriptor, imageDescriptor);
        entries.add(actionEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/CallBehaviorAction.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry bfCallEntry = new CombinedTemplateCreationEntry(
                BFElementType.ACTION_BUSINESSFUNCTIONCALL.getName(), Messages.PaletteBuilder_bfCallActionDesc,
                new BFElementFactory(BFElementType.ACTION_BUSINESSFUNCTIONCALL), imageDescriptor, imageDescriptor);
        entries.add(bfCallEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/OpaqueAction.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry inlineActionEntry = new CombinedTemplateCreationEntry(
                BFElementType.ACTION_INLINE.getName(), Messages.PaletteBuilder_inlineActionDesc, new BFElementFactory(
                        BFElementType.ACTION_INLINE), imageDescriptor, imageDescriptor);
        entries.add(inlineActionEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/DecisionNode.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry decisionEntry = new CombinedTemplateCreationEntry(
                BFElementType.DECISION.getName(), Messages.PaletteBuilder_decisionDesc, new BFElementFactory(
                        BFElementType.DECISION), imageDescriptor, imageDescriptor);
        entries.add(decisionEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/DecisionNode.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry methodCallDecisionEntry = new CombinedTemplateCreationEntry(
                BFElementType.DECISION_METHODCALL.getName(), Messages.PaletteBuilder_methodCallDecisionDesc,
                new BFElementFactory(BFElementType.DECISION_METHODCALL), imageDescriptor, imageDescriptor);
        entries.add(methodCallDecisionEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/InitialNode.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry startEntry = new CombinedTemplateCreationEntry(BFElementType.START.getName(),
                Messages.PaletteBuilder_startDec, new BFElementFactory(BFElementType.START), imageDescriptor,
                imageDescriptor);
        entries.add(startEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/ActivityFinalNode.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry endEntry = new CombinedTemplateCreationEntry(BFElementType.END.getName(),
                Messages.PaletteBuilder_endDesc, new BFElementFactory(BFElementType.END), imageDescriptor,
                imageDescriptor);
        entries.add(endEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/MergeNode.gif"); //$NON-NLS-1$
        CombinedTemplateCreationEntry mergeEntry = new CombinedTemplateCreationEntry(BFElementType.MERGE.getName(),
                Messages.PaletteBuilder_mergeDesc, new BFElementFactory(BFElementType.MERGE), imageDescriptor,
                imageDescriptor);
        entries.add(mergeEntry);

        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/ControlFlow.gif"); //$NON-NLS-1$
        ToolEntry controlflowEntry = new ConnectionCreationToolEntry(Messages.PaletteBuilder_controlflow,
                Messages.PaletteBuilder_controlFlowDesc, null, imageDescriptor, imageDescriptor);
        entries.add(controlflowEntry);

        PaletteDrawer drawer = new PaletteDrawer("", null); //$NON-NLS-1$
        drawer.addAll(entries);
        return drawer;
    }

    static private PaletteContainer createControlGroup(PaletteRoot root) {
        PaletteGroup controlGroup = new PaletteGroup("Control Group"); //$NON-NLS-1$
        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

        ToolEntry tool = new PanningSelectionToolEntry();
        entries.add(tool);
        root.setDefaultEntry(tool);

        PaletteStack marqueeStack = new PaletteStack("Marquee Tools", "", null); //$NON-NLS-1$ //$NON-NLS-2$
        marqueeStack.add(new MarqueeToolEntry());
        MarqueeToolEntry marquee = new MarqueeToolEntry();
        marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR, new Integer(
                MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED));
        marqueeStack.add(marquee);
        marquee = new MarqueeToolEntry();
        marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR, new Integer(
                MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED | MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED));
        marqueeStack.add(marquee);
        marqueeStack.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
        entries.add(marqueeStack);

        controlGroup.addAll(entries);
        return controlGroup;
    }

    /**
     * Builds and returns the palette.
     */
    public PaletteRoot buildPalette() {
        PaletteRoot logicPalette = new PaletteRoot();
        logicPalette.addAll(createCategories(logicPalette));

        return logicPalette;
    }

    private class BFElementFactory implements CreationFactory {

        private BFElementType type;

        public BFElementFactory(BFElementType type) {
            this.type = type;
        }

        @Override
        public Object getNewObject() {
            return type;
        }

        @Override
        public Object getObjectType() {
            return type;
        }

    }
}
