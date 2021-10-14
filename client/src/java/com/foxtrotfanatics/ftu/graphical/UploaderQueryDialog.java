package com.foxtrotfanatics.ftu.graphical;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import com.foxtrotfanatics.ftu.Settings;

/**
 * @author Christian
 *
 */
public class UploaderQueryDialog extends JDialog
{
	/**
	 * Version 1.0.0
	 */
	private static final long serialVersionUID = -6490356300225705552L;
	private JTextField usernameField;
	private JTextField filepathField;
	private JPasswordField passwordField;
	private JProgressBar bar;

	private String enteredUsername;
	private String enteredFilepath;
	private char[] enteredPassword;
	private boolean verifiedValues;

	private JOptionPane optionPane;

	private JButton btn1 = new JButton("Verify");
	private JButton btn2 = new JButton("Cancel");

	/**
	 * Returns null if the typed string was invalid; otherwise, returns the
	 * string as the user entered it.
	 */
	public String getUsername()
	{
		return enteredUsername;
	}

	public String getFilePath()
	{
		return enteredFilepath;
	}

	public char[] getPassword()
	{
		return enteredPassword;
	}

	public boolean wasVerified()
	{
		return verifiedValues;
	}

	/** Creates the reusable dialog. */
	public UploaderQueryDialog(JFrame window, Settings intermediateSettings)
	{
		super(window, true);
		verifiedValues = false;
		if (intermediateSettings.getClientID() != null)
		{
			enteredUsername = intermediateSettings.getUserName();
		}
		else
		{
			enteredUsername = new String("admin");
		}
		if (intermediateSettings.getHostName() != null)
		{
			enteredFilepath = intermediateSettings.getFilePath();
		}
		else
		{
			enteredFilepath = new String("https://www.google.com/");
		}
		if (intermediateSettings.getPort() != -1)
		{
			enteredPassword = intermediateSettings.getPassword();
		}
		else
		{
			enteredPassword = new char[] {};
		}
		setTitle("FTU Reporter Connection Establisher");

		usernameField = new JTextField();
		usernameField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				String name = usernameField.getText().trim().replaceAll("\\s", "_");
				if(name.startsWith("-"))
					name = name.substring(1, name.length());
				usernameField.setText(name.substring(0, Math.min(20, name.length())));
			}
		});
		usernameField.setText(enteredUsername);
		filepathField = new JTextField();
		filepathField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				filepathField.setText(filepathField.getText().replaceAll("\\s", "%20"));
			}
		});
		filepathField.setText(enteredFilepath);
		passwordField = new JPasswordField();
		bar = new JProgressBar();
		bar.setString("Connecting...");
		bar.setForeground(Color.YELLOW);
		// Create an array of the text and components to be displayed.
		String msgString1 = "What the network address of the folder you wish to upload to";
		String msgString2 = "What is the Username used to Connect?";
		String msgString3 = "What is the Password used to Connect?";
		Object[] array = { msgString1, filepathField, msgString2, usernameField, msgString3, passwordField, bar };

		btn1.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				bar.setValue(0);
				bar.setStringPainted(true);
				bar.setIndeterminate(true);
				btn1.setEnabled(false);
				btn2.setEnabled(false);
				Thread work = new Thread()
				{
					@Override
					public void run()
					{
						enteredUsername = usernameField.getText();
						enteredFilepath = filepathField.getText();
						enteredPassword = passwordField.getPassword();
						boolean result = intermediateSettings.verifyUploading(enteredFilepath, enteredUsername,
								enteredPassword);
						EventQueue.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{

								if (result)
								{
									verifiedValues = true;
									setVisible(false);
								}
								else
								{
									usernameField.selectAll();
									JOptionPane.showMessageDialog(UploaderQueryDialog.this,
											"Connection Failed\nVerify entered Values or\nVerify that Server is Running",
											"Connection Failed", JOptionPane.ERROR_MESSAGE);
									usernameField.requestFocusInWindow();
									bar.setStringPainted(false);
									btn1.setEnabled(true);
									btn2.setEnabled(true);
								}
								bar.setIndeterminate(false);

							}
						});
					}

				};
				work.start();
			}

		});
		;

		btn2.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}

		});
		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { btn1, btn2 };

		// Create the JOptionPane
		optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options,
				options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				setVisible(false);
			}
		});

		// Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter()
		{
			public void componentShown(ComponentEvent ce)
			{
				filepathField.requestFocusInWindow();
			}
		});
	}
}