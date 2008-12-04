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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.bf.BFElementType;

public class PaletteBuilder {

    public PaletteBuilder(){
    }
    
	private List<PaletteContainer> createCategories(PaletteRoot root) {
		List<PaletteContainer> categories = new ArrayList<PaletteContainer>();
		categories.add(createControlGroup(root));
		categories.add(createComponentsDrawer());
		return categories;
	}

	private PaletteContainer createComponentsDrawer() {

		List<ToolEntry> entries = new ArrayList<ToolEntry>();

		ImageDescriptor image = BFElementType.ACTION_METHODCALL.getImageDescriptor();
		CombinedTemplateCreationEntry actionEntry = new CombinedTemplateCreationEntry(
		        BFElementType.ACTION_METHODCALL.getName(), "Method Call Action Description",
		        new BFElementFactory(BFElementType.ACTION_METHODCALL), image, image);
		entries.add(actionEntry);

		image = BFElementType.ACTION_BUSINESSFUNCTIONCALL.getImageDescriptor();
		CombinedTemplateCreationEntry bfCallEntry = new CombinedTemplateCreationEntry(
		        BFElementType.ACTION_BUSINESSFUNCTIONCALL.getName(),
		        "Business Function Call Action Description", new BFElementFactory(BFElementType.ACTION_BUSINESSFUNCTIONCALL), image, image);
		entries.add(bfCallEntry);

		image = BFElementType.ACTION_INLINE.getImageDescriptor();
		CombinedTemplateCreationEntry inlineActionEntry = new CombinedTemplateCreationEntry(
				BFElementType.ACTION_INLINE.getName(), "Inline Action Description",
				new BFElementFactory(BFElementType.ACTION_INLINE), image, image);
		entries.add(inlineActionEntry);

		image = BFElementType.DECISION.getImageDescriptor();
		CombinedTemplateCreationEntry decisionEntry = new CombinedTemplateCreationEntry(
				"Decision", "Decision Description", new BFElementFactory(BFElementType.DECISION), image, image);
		entries.add(decisionEntry);

		image = BFElementType.START.getImageDescriptor();
		CombinedTemplateCreationEntry startEntry = new CombinedTemplateCreationEntry(
				"Start", "Start Description",
				new BFElementFactory(BFElementType.START), image, image);
		entries.add(startEntry);

		image = BFElementType.END.getImageDescriptor();
		CombinedTemplateCreationEntry endEntry = new CombinedTemplateCreationEntry(
				"End", "End Description", new BFElementFactory(BFElementType.END),
				image, image);
		entries.add(endEntry);

		image = BFElementType.MERGE.getImageDescriptor();
		CombinedTemplateCreationEntry mergeEntry = new CombinedTemplateCreationEntry(
				"Merge", "Merge Description",
				new BFElementFactory(BFElementType.MERGE), image, image);
		entries.add(mergeEntry);

		//UMLElementTypes.ActivityParameterNode_3052

		image = IpsPlugin.getDefault().getImageDescriptor("/obj16/" + "ControlFlow.gif");
		ToolEntry controlflowEntry = new ConnectionCreationToolEntry(
				"Control Flow",
				"Control Flow Description",
				null, image,
				image);
		entries.add(controlflowEntry);

		PaletteDrawer drawer = new PaletteDrawer("", null);
		drawer.addAll(entries);
		return drawer;
	}

	static private PaletteContainer createControlGroup(PaletteRoot root) {
		PaletteGroup controlGroup = new PaletteGroup("Control Group");
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		ToolEntry tool = new PanningSelectionToolEntry();
		entries.add(tool);
		root.setDefaultEntry(tool);

		PaletteStack marqueeStack = new PaletteStack("Marquee Tools", "", null); //$NON-NLS-1$
		marqueeStack.add(new MarqueeToolEntry());
		MarqueeToolEntry marquee = new MarqueeToolEntry();
		marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
				new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED));
		marqueeStack.add(marquee);
		marquee = new MarqueeToolEntry();
		marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
				new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED
						| MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED));
		marqueeStack.add(marquee);
		marqueeStack
				.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(marqueeStack);

		controlGroup.addAll(entries);
		return controlGroup;
	}

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

		public Object getNewObject() {
		    return type;
		}

		public Object getObjectType() {
			return type;
		}

	}
}
