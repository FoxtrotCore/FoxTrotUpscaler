/**
 * Copyright 2018 Christian Devile
 * 
 * This file is part of FoxTrotUpscaler.
 * 
 * FoxTrotUpscaler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FoxTrotUpscaler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FoxTrotUpscaler.  If not, see <http://www.gnu.org/licenses/>.
 */
package graphical;

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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import main.DRI;
import main.Settings;

/**
 * @author Christian
 *
 */
public class ReporterQueryDialog extends JDialog
{

	/**
	 * Version 1.0.0
	 */
	private static final long serialVersionUID = -6490356300225705552L;
	private JTextField clientField;
	private JTextField hostNameField;
	private JSpinner spinnerPort;
	private JPasswordField passwordField;
	private JProgressBar bar;

	private String enteredClientID;
	private String enteredHostName;
	private int enteredPort;
	private char[] enteredPassword;
	private boolean verifiedValues;

	private JOptionPane optionPane;

	private JButton btn1 = new JButton("Verify");
	private JButton btn2 = new JButton("Cancel");

	/**
	 * Returns null if the typed string was invalid; otherwise, returns the
	 * string as the user entered it.
	 */
	public String getClientID()
	{
		return enteredClientID;
	}

	public String getHostname()
	{
		return enteredHostName;
	}

	public int getPort()
	{
		return enteredPort;
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
	public ReporterQueryDialog(JFrame window, Settings intermediateSettings)
	{
		super(window, true);
		verifiedValues = false;
		if (intermediateSettings.getClientID() != null)
		{
			enteredClientID = intermediateSettings.getClientID();
		}
		else
		{
			enteredClientID = new String("JoeBlow's_Computer");
		}
		if (intermediateSettings.getHostName() != null)
		{
			enteredHostName = intermediateSettings.getHostName();
		}
		else
		{
			enteredHostName = new String("localhost");
		}
		if (intermediateSettings.getPort() != -1)
		{
			enteredPort = intermediateSettings.getPort();
		}
		else
		{
			enteredPort = 9626;
		}
		setTitle("FTU Reporter Connection Establisher");

		clientField = new JTextField();
		clientField.setText(enteredClientID);
		clientField.addFocusListener(new FocusAdapter()
		{

			@Override
			public void focusLost(FocusEvent e)
			{
				String s = clientField.getText().trim().replaceAll("\\s", "_");
				clientField.setText(s.substring(0, Math.min(s.length(), 50)));
			}
		});
		hostNameField = new JTextField();
		hostNameField.addFocusListener(new FocusAdapter()
		{

			@Override
			public void focusLost(FocusEvent e)
			{
				hostNameField.setText(hostNameField.getText().replaceAll("\\s", "%20"));
			}
		});
		hostNameField.setText(enteredHostName);
		spinnerPort = new JSpinner();
		SpinnerNumberModel model = new SpinnerNumberModel(enteredPort, 1024, 49151, 1); // step
		spinnerPort.setModel(model);
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinnerPort, "#");
		spinnerPort.setEditor(editor);
		passwordField = new JPasswordField();
		bar = new JProgressBar();
		bar.setString("Connecting...");
		bar.setForeground(Color.YELLOW);
		// Create an array of the text and components to be displayed.
		String msgString1 = "What unique Client Name do you want for this instance of FTU";
		String msgString2 = "What is the Hostname or IP Address of the FTU Reporter?";
		String msgString3 = "What is the Port of the Server that FTU is using? (1024 < x < 49151)";
		String msgString4 = "What is the Encryption Password of the Server?";
		Object[] array =
		{ msgString1, clientField, msgString2, hostNameField, msgString3, spinnerPort, msgString4, passwordField, bar };

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
						enteredClientID = clientField.getText().substring(0, Math.min(20, clientField.getText().length()));
						enteredHostName = hostNameField.getText();
						enteredPort = (int) spinnerPort.getValue();
						enteredPassword = passwordField.getPassword();
						String result = intermediateSettings.verifyReporting(enteredClientID, enteredHostName, enteredPort, enteredPassword);
						EventQueue.invokeLater(new Runnable()
						{

							@Override
							public void run()
							{
								if (result.equals("0"))
								{
									verifiedValues = true;
									setVisible(false);
								}
								else if (result.equals("1"))
								{
									clientField.selectAll();
									bar.setStringPainted(false);
									JOptionPane.showMessageDialog(ReporterQueryDialog.this,
											"Server has no avaliable slots\nA Client must first Disconnect",
											"Full Server", JOptionPane.ERROR_MESSAGE);
									clientField.requestFocusInWindow();
									btn1.setEnabled(true);
									btn2.setEnabled(true);
								}
								else if (result.equals("2"))
								{
									clientField.selectAll();
									bar.setStringPainted(false);
									JOptionPane.showMessageDialog(ReporterQueryDialog.this,
											"Client is already logged into Server under\n" + clientField.getText() + "\nPick Another Name",
											"Client Conflict", JOptionPane.ERROR_MESSAGE);
									clientField.requestFocusInWindow();
									btn1.setEnabled(true);
									btn2.setEnabled(true);
								}
								else if (result.equals("3"))
								{
									hostNameField.selectAll();
									bar.setStringPainted(false);
									JOptionPane.showMessageDialog(ReporterQueryDialog.this,
											"Connection Failed\nVerify entered Values or\nVerify that Server is Running", "Connection Failed",
											JOptionPane.ERROR_MESSAGE);
									clientField.requestFocusInWindow();
									btn1.setEnabled(true);
									btn2.setEnabled(true);
								}
								else if (result.equals("4"))
								{
									passwordField.selectAll();
									bar.setStringPainted(false);
									JOptionPane.showMessageDialog(null, "Reenter the Server Password", "Decryption Failed", JOptionPane.ERROR_MESSAGE);
									clientField.requestFocusInWindow();
									btn1.setEnabled(true);
									btn2.setEnabled(true);
								}
								else if (result.equals("5"))
								{
									passwordField.selectAll();
									bar.setStringPainted(false);
									JOptionPane.showMessageDialog(null, "Decryption Password is not functioning\n Reenter the Server Password", "Bad Password", JOptionPane.ERROR_MESSAGE);
									clientField.requestFocusInWindow();
									btn1.setEnabled(true);
									btn2.setEnabled(true);
								}
								else
								{
									hostNameField.selectAll();
									bar.setStringPainted(false);
									JOptionPane.showMessageDialog(null,
											"Server is requiring Version " + result + "\nYou are running Client Version " + DRI.version + "\nCan not Connect to Server\nThere may be an update to either Server or Client",
											"Version Conflict", JOptionPane.ERROR_MESSAGE);
									clientField.requestFocusInWindow();
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
		Object[] options =
		{ btn1, btn2 };

		// Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);

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
				hostNameField.requestFocusInWindow();
			}
		});

	}
}