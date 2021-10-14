package com.foxtrotfanatics.ftu.graphical;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class Help extends JPanel
{

	private static Logger logger = LogManager.getLogger();
	private ArrayList<URL> files = new ArrayList<URL>();
	private String[] titles = new String[]
	{ "1. Welcome", "2. File System", "3. Config.txt", "4. Upscale Setup", "5. QuickScripts", "6. Preferences", "7. FTUServerBot",
			"8. WebDAV Uploading" };
	private String[] fileNames = new String[]
	{ "Welcome", "FileSystem", "Config", "UpscaleSetup", "QuickScripts", "Preferences", "ServerBot", "WebDAV" };
	private JEditorPane editorPane = new JEditorPane();

	/**
	 * Create the panel.
	 */
	public Help()
	{
		setLayout(new MigLayout("", "[][grow]", "[][grow]"));
		addResources("/help/");
		editorPane.setEditable(false);
		try
		{
			editorPane.setPage(files.get(0));
		}
		catch (IOException e)
		{
			logger.error("Attempted to read a bad URL when loading Help: {}", files.get(0));
			System.exit(20);
		}
		editorPane.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					try
					{
						java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
					}
					catch (IOException | URISyntaxException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		JLabel lblDocument = new JLabel("Document:");
		add(lblDocument, "cell 0 0,alignx leading");

		JComboBox<String> messageBox = new JComboBox<String>();
		messageBox.setModel(new DefaultComboBoxModel<String>(titles));
		messageBox.addActionListener(e -> {
			try
			{
				editorPane.setPage(files.get(messageBox.getSelectedIndex()));
			}
			catch (IOException e1)
			{
				logger.error("Attempted to read a bad URL when loading Help: {}", files.get(0));
				System.exit(20);
			} ;
		});
		add(messageBox, "cell 1 0,growx");

		//Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setMinimumSize(new Dimension(200, 200));

		add(editorScrollPane, "cell 0 1 2 1,grow");
	}

	private void addResources(String parentPath)
	{
		for (String s : fileNames)
		{
			files.add(Help.class.getResource(parentPath + s + ".html"));
		}
	}
}