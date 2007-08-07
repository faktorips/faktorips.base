package org.faktorips.devtools.core.ui.views.modeldescription;

public class DescriptionItem {

	private String description;
	private String name;
	
	public DescriptionItem() {
		
	}

	public DescriptionItem(String name, String description) {
		this.description = description;
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
