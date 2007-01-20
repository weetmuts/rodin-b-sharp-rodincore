package org.eventb.internal.ui.prover;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eventb.eventBKeyboard.EventBStyledTextModifyListener;
import org.eventb.eventBKeyboard.preferences.PreferenceConstants;

public class EventBPredicateText implements IPropertyChangeListener {

	StyledText text;

	ScrolledForm parent;

	final Cursor handCursor;

	final Cursor arrowCursor;

	private Collection<Point> boxes;

	Point[] links;

	Runnable[] commands;

	Point current;

	private int currSize;

	Collection<Point> dirtyStates;

	final Color BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);

	final Color YELLOW = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);

	int currentLinkIndex;

	public EventBPredicateText(FormToolkit toolkit, final ScrolledForm parent) {

		this.parent = parent;
		dirtyStates = new ArrayList<Point>();
		text = new StyledText(parent.getBody(), SWT.MULTI | SWT.FULL_SELECTION);
		Font font = JFaceResources
				.getFont(PreferenceConstants.EVENTB_MATH_FONT);
		text.setFont(font);
		handCursor = new Cursor(text.getDisplay(), SWT.CURSOR_HAND);
		arrowCursor = new Cursor(text.getDisplay(), SWT.CURSOR_ARROW);
		JFaceResources.getFontRegistry().addListener(this);
		text.addListener(SWT.MouseDown, new MouseDownListener());
		text.addListener(SWT.MouseMove, new MouseMoveListener());
		text.addListener(SWT.MouseHover, new MouseHoverListener());
		text.addListener(SWT.MouseExit, new MouseExitListener());
		text.addListener(SWT.MouseEnter, new MouseEnterListener());
	}

	// This must be called after initialisation
	public void setText(String string, Collection<Point> boxes, Point[] links,
			Runnable[] commands) {
		this.boxes = boxes;
		this.links = links;
		this.commands = commands;
		currentLinkIndex = -1;
		text.setText(string);
		text.pack();
		text.addModifyListener(new EventBStyledTextModifyListener());

		text.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				drawBoxes(event);
			}

		});

		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (current == null)
					return;
				dirtyStates.add(current);
				updateIndexes();
			}

		});

		text.addVerifyListener(new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				checkModifiable(e);
			}

		});
		setStyle();
	}

	protected void checkModifiable(VerifyEvent e) {
		e.doit = false;
		Point pt = new Point(e.start, e.end);

		// Make sure the selection is from left to right
		if (pt.x > pt.y) {
			pt = new Point(pt.y, pt.x);
		}

		// It is only modify-able if it is within one subrange and the
		for (Point index : boxes) {

			// if (ProverUIUtils.DEBUG)
			ProverUIUtils.debug("Event " + e);
			ProverUIUtils.debug("index.x " + index.x + ", index.y " + index.y);
			ProverUIUtils.debug("pt.x " + pt.x + ", pt.y " + pt.y);
			if (index.x > pt.x)
				continue;
			if (index.y < pt.y)
				continue;

			if (e.text.equals("")) { // deletion
				if (pt.x == pt.y - 1 && index.y == pt.x) // SWT.DEL at the
					// end
					continue;
				else if (pt.x == pt.y + 1 && index.x == pt.y) // SWT.BS at the
					// beginning
					continue;
			}

			e.doit = true;
			current = index;
			currSize = text.getText().length();
			break;
		}
	}

	protected void updateIndexes() {
		int offset = text.getText().length() - currSize;

		for (Point box : boxes) {
			if (box.x > current.y) {
				box.x = box.x + offset;
				box.y = box.y + offset;
			}
		}

		for (Point link : links) {
			if (link.x > current.y) {
				link.x = link.x + offset;
				link.y = link.y + offset;
			}
		}

		current.y = current.y + offset;

		setStyle();
		text.pack();
		parent.reflow(true);
	}

	void drawBoxes(Event event) {
		// ProverUIUtils.debugProverUI("Draw boxes");
		if (boxes == null)
			return;
		String contents = text.getText();
		int lineHeight = text.getLineHeight();
		final Color RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		event.gc.setForeground(RED);
		for (Point index : boxes) {
			String str = contents.substring(index.x, index.y);
			int stringWidth = event.gc.stringExtent(str).x;
			Point topLeft = text.getLocationAtOffset(index.x);
			event.gc.drawRectangle(topLeft.x - 1, topLeft.y, stringWidth + 1,
					lineHeight - 1);
		}
	}

	private void setStyle() {
		if (boxes == null)
			return;
		for (Point index : boxes) {
			StyleRange style = new StyleRange();
			style.start = index.x;
			style.length = index.y - index.x;
			if (dirtyStates.contains(index))
				style.background = YELLOW;
			style.fontStyle = SWT.ITALIC;
			text.setStyleRange(style);
		}

		for (Point link : links) {
			StyleRange style = new StyleRange();
			style.start = link.x;
			style.length = link.y - link.x;
			style.foreground = BLUE;
			text.setStyleRange(style);
		}
	}

	public StyledText getMainTextWidget() {
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.EVENTB_MATH_FONT)) {
			Font font = JFaceResources
					.getFont(PreferenceConstants.EVENTB_MATH_FONT);
			text.setFont(font);
			text.pack();
		}
	}

	public void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
		text.dispose();
	}

	public String[] getResults() {
		if (boxes == null)
			return new String[0];
		String[] results = new String[boxes.size()];
		int i = 0;
		for (Point index : boxes) {
			String str = text.getText().substring(index.x, index.y);
			results[i] = str;
			i++;
		}

		return results;
	}

	class MouseDownListener implements Listener {

		public void handleEvent(Event e) {
			if (currentLinkIndex != -1) {
				Runnable op = commands[currentLinkIndex];
				op.run();
			}
			return;
		}

	}

	class MouseMoveListener implements Listener {

		public void handleEvent(Event e) {
			Point location = new Point(e.x, e.y);
			try {
				int offset = getCharacterOffset(location);
				int index = getLinkIndex(offset);
				if (index != -1) {
					if (currentLinkIndex != index) {
						if (currentLinkIndex != -1) {
							disableCurrentLink();
						}
						enableLink(index);
						currentLinkIndex = index;
						text.setCursor(handCursor);
					}
				} else {
					if (currentLinkIndex != -1) {
						disableCurrentLink();
						text.setCursor(arrowCursor);
						currentLinkIndex = -1;
					}
				}
				// if (ProverUIUtils.DEBUG)
				// ProverUIUtils.debug("Move Offset " + offset);
			} catch (IllegalArgumentException exception) {
				// if (ProverUIUtils.DEBUG)
				// ProverUIUtils.debug("Invalid");
			}
			return;
		}
	}

	class MouseEnterListener implements Listener {

		public void handleEvent(Event e) {
			if (ProverUIUtils.DEBUG)
				ProverUIUtils.debug("Enter ");
			Point location = new Point(e.x, e.y);
			try {
				int offset = getCharacterOffset(location);
				int index = getLinkIndex(offset);
				if (index != -1) {
					if (currentLinkIndex != index) {
						if (currentLinkIndex != -1) {
							disableCurrentLink();
						}
						enableLink(index);
						currentLinkIndex = index;
						text.setCursor(handCursor);
					}
				} else {
					if (currentLinkIndex != -1) {
						disableCurrentLink();
						text.setCursor(arrowCursor);
						currentLinkIndex = -1;
					}
				}
			} catch (IllegalArgumentException exception) {
				// if (ProverUIUtils.DEBUG)
				// ProverUIUtils.debug("Invalid");
			}
			return;
		}
	}

	class MouseHoverListener implements Listener {

		public void handleEvent(Event e) {
			Point location = new Point(e.x, e.y);
			try {
				int offset = getCharacterOffset(location);

				if (offset == 0 && commands.length != 0) {

					// IHyperlinkListener listener =
					// listeners.iterator().next();
					// listener.linkActivated(new HyperlinkEvent(text, text,
					// text
					// .toString(), 0));
				}
				// if (ProverUIUtils.DEBUG)
				ProverUIUtils.debug("Hover Offset " + offset);
			} catch (IllegalArgumentException exception) {
				// Do nothing
			}
			return;
		}
	}

	class MouseExitListener implements Listener {

		public void handleEvent(Event event) {
			if (ProverUIUtils.DEBUG)
				ProverUIUtils.debug("Exit ");
			if (currentLinkIndex != -1) {
				disableCurrentLink();
				currentLinkIndex = -1;
			}

		}

	}

	int getCharacterOffset(Point pt) {
		int offset = text.getOffsetAtLocation(pt);
		Point location = text.getLocationAtOffset(offset);

		// From the caret offset to the character offset.
		if (pt.x < location.x)
			offset = offset - 1;
		return offset;
	}

	public void enableLink(int index) {
		StyleRange style = new StyleRange();
		style.start = links[index].x;
		style.length = links[index].y - links[index].x;
		style.foreground = BLUE;
		style.underline = true;
		text.setStyleRange(style);
	}

	public void disableCurrentLink() {
		assert currentLinkIndex != -1;
		Point currentLink = links[currentLinkIndex];
		StyleRange style = new StyleRange();
		style.start = currentLink.x;
		style.length = currentLink.y - currentLink.x;
		style.foreground = BLUE;
		style.underline = false;
		text.setStyleRange(style);
	}

	public int getLinkIndex(int offset) {
		for (int i = 0; i < links.length; ++i) {
			if (links[i].x <= offset && offset < links[i].y)
				return i;
		}
		return -1;
	}
}
