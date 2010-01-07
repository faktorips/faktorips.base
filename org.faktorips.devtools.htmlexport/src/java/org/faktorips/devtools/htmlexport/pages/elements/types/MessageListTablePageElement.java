package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class MessageListTablePageElement extends AbstractSpecificTablePageElement {
	protected MessageList messageList;

	public MessageListTablePageElement(MessageList messageList) {
		super();
		this.messageList = messageList;
	}

	@SuppressWarnings("unchecked")
	protected void addDataRows() {
		for (Iterator iterator = messageList.iterator(); iterator.hasNext();) {
			addMessageRow((Message) iterator.next());
		}
	}

	protected void addMessageRow(Message msg) {
		int severity = msg.getSeverity();
		subElements.add(new TableRowPageElement(new PageElement[] {
				new TextPageElement(msg.getCode()),
				new TextPageElement(msg.getText()),
				new TextPageElement(severity == Message.ERROR ? "ERROR" : severity == Message.WARNING ? "WARNING"
						: severity == Message.INFO ? "INFO" : "Severity " + severity),
				new TextPageElement(Arrays.toString(msg.getInvalidObjectProperties())) }));
	}

	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		headline.add("code");
		headline.add("message");
		headline.add("severity");
		headline.add("properties");
		
		return headline;
	}

}
