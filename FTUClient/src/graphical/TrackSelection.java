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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.miginfocom.swing.MigLayout;
import structures.Timing;

@SuppressWarnings("serial")
public class TrackSelection extends JPanel
{
	private static Logger logger = LogManager.getLogger();
	private JTextField txtNa_1;
	private JTextField txtNa_2;
	private JTextField txtNa_3;
	private JTextField txtNa_4;
	private JTextField txtNa_5;
	private JTextField txtNa_6;
	private JTextField txtNa_7;
	private JTextField txtNa_8;
	private JTextField txtNa_9;
	private JTextField txtNa_10;
	private JCheckBox chckbxLang_1;
	private JCheckBox chckbxLang_2;
	private JCheckBox chckbxLang_3;
	private JCheckBox chckbxLang_4;
	private JCheckBox chckbxLang_5;
	private JComboBox<String> lBox1;
	private JComboBox<String> lBox2;
	private JComboBox<String> lBox3;
	private JComboBox<String> lBox4;
	private JComboBox<String> lBox5;
	private JComboBox<String> lBox6;
	private JComboBox<String> lBox7;
	private JComboBox<String> lBox8;
	private JComboBox<String> lBox9;
	private JComboBox<String> lBox10;
	private JComboBox<String> hBox1;
	private JComboBox<String> hBox2;
	private JComboBox<String> hBox3;
	private JComboBox<String> hBox4;
	private JComboBox<String> hBox5;
	private JComboBox<String> hBox6;
	private JComboBox<String> hBox7;
	private JComboBox<String> hBox8;
	private JComboBox<String> hBox9;
	private JComboBox<String> hBox10;
	private JCheckBox chckbxSubs_1;
	private JCheckBox chckbxSubs_2;
	private JCheckBox chckbxSubs_3;
	private JCheckBox chckbxSubs_4;
	private JCheckBox chckbxSubs_5;
	private static final String[] lHandles = new String[]
	{ "Offical", "FanDub" };
	private static final String[] sHandles = new String[]
	{ "Transcription", "Translated" };
	public static final String[] videoExtensions = new String[]
	{ "mkv", "avi", "mp4", "wmv", "mov", "flv" };
	public static final String[] audioExtensions = new String[]
	{ "aac", "mp3", "flac", "wav", "ogg" , "ac3"};
	public static final String[] subtitleExtensions = new String[]
	{ "ass", "srt", "smi", "ssa" };
	private JLabel lblMetaTitle;
	private JTextField txtTitle;
	private JLabel lblMetaDescription;
	private JTextField txtDescription;
	private JCheckBox checkDuration;
	private JTextField durationField;
	private JCheckBox checkStartTime;
	private JTextField startTimeField;
	private static final String defaultOffset = new String("00:00:00.000");
	private Border defaultBorder;
	private JComboBox<String> comboBoxShowName;
	private JSpinner episodeSpinner;
	private JLabel lblEpisode;
	private JLabel lblShowName;
	private JSeparator separator;
	private JSeparator separator_1;
	private String dir;
	private JLabel lblExportContainer;
	private JComboBox<String> extensionBox;

	/**
	 * Create the panel.
	 */
	public TrackSelection(String dir)
	{
		this.dir = dir;
		setLayout(new MigLayout("",
				"[center][center][center][][growprio 3,grow][::1px,center][center][center][center][center][growprio 1,grow][growprio 1,grow][growprio 1,grow]",
				"[][][][][][][][][][][]"));
		FocusAdapter focusLostAction = new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				JTextField textField = (JTextField) e.getSource();
				if (checkOffset(textField.getText()))
				{
					textField.setBorder(defaultBorder);
				}
				else
				{
					textField.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
				}
			}
		};
		chckbxLang_1 = new JCheckBox("Lang 1");
		chckbxLang_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxLang_1.isSelected())
				{
					lBox1.setEnabled(true);
					hBox1.setEnabled(true);
					txtNa_1.setEnabled(true);
				}
				else
				{
					lBox1.setEnabled(false);
					hBox1.setEnabled(false);
					txtNa_1.setText(defaultOffset);
					txtNa_1.setBorder(defaultBorder);
					txtNa_1.setEnabled(false);
				}
			}
		});

		lblShowName = new JLabel("Show Name:");
		add(lblShowName, "cell 0 0,alignx right");

		comboBoxShowName = new JComboBox<String>();
		comboBoxShowName.setToolTipText("Show name used in File Path");
		comboBoxShowName.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String item = (String) comboBoxShowName.getSelectedItem();
				item = item.replaceAll(" ", "_");
				comboBoxShowName.setSelectedItem(item);
				for (int x = 0; x < comboBoxShowName.getModel().getSize(); x++)
				{
					if (comboBoxShowName.getModel().getElementAt(x).equals(comboBoxShowName.getSelectedItem()))
					{
						return;
					}
				}
				comboBoxShowName.addItem((String) comboBoxShowName.getSelectedItem());
			}
		});
		comboBoxShowName.setModel(new DefaultComboBoxModel<String>(updateShowList()));
		comboBoxShowName.setSelectedIndex(0);
		comboBoxShowName.setEditable(true);
		add(comboBoxShowName, "cell 1 0 4 1,grow");

		lblEpisode = new JLabel("Episode:");
		lblEpisode.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblEpisode, "cell 6 0,alignx right");

		episodeSpinner = new JSpinner();
		episodeSpinner.setModel(new SpinnerNumberModel(0, 0, 999, 1));
		add(episodeSpinner, "cell 7 0 6 1,grow");

		separator = new JSeparator();
		add(separator, "cell 0 1 13 1,growx");
		add(chckbxLang_1, "cell 0 2,alignx center,aligny center");

		lBox1 = new JComboBox<String>();
		lBox1.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox1, "cell 1 2,grow");

		hBox1 = new JComboBox<String>();
		hBox1.setEditable(true);
		hBox1.setEnabled(false);
		hBox1.setModel(new DefaultComboBoxModel<String>(lHandles));
		add(hBox1, "cell 2 2,growx");

		JLabel lblOffset_1 = new JLabel("Offset:");
		add(lblOffset_1, "cell 3 2,alignx right,aligny center");

		txtNa_1 = new JTextField();
		txtNa_1.setHorizontalAlignment(SwingConstants.CENTER);
		defaultBorder = txtNa_1.getBorder();
		txtNa_1.addFocusListener(focusLostAction);
		txtNa_1.setText(defaultOffset);
		add(txtNa_1, "cell 4 2,grow");
		txtNa_1.setColumns(10);

		JSeparator separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		add(separator_3, "cell 5 2 1 5,alignx center,growy");

		chckbxSubs_1 = new JCheckBox("Subs 1");
		chckbxSubs_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxSubs_1.isSelected())
				{
					lBox6.setEnabled(true);
					hBox6.setEnabled(true);
					txtNa_6.setEnabled(true);
				}
				else
				{
					lBox6.setEnabled(false);
					hBox6.setEnabled(false);
					txtNa_6.setText(defaultOffset);
					txtNa_6.setBorder(defaultBorder);
					txtNa_6.setEnabled(false);
				}
			}
		});
		add(chckbxSubs_1, "cell 6 2,alignx center,aligny center");

		lBox6 = new JComboBox<String>();
		lBox6.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox6, "cell 7 2,grow");

		hBox6 = new JComboBox<String>();
		hBox6.setEditable(true);
		hBox6.setEnabled(false);
		hBox6.setModel(new DefaultComboBoxModel<String>(sHandles));
		add(hBox6, "cell 8 2,growx");

		JLabel lblOffset_6 = new JLabel("Offset:");
		add(lblOffset_6, "cell 9 2,alignx right,aligny center");

		txtNa_6 = new JTextField();
		txtNa_6.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_6.addFocusListener(focusLostAction);
		txtNa_6.setText(defaultOffset);
		add(txtNa_6, "cell 10 2 3 1,grow");
		txtNa_6.setColumns(10);

		chckbxLang_2 = new JCheckBox("Lang 2");
		chckbxLang_2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxLang_2.isSelected())
				{
					lBox2.setEnabled(true);
					hBox2.setEnabled(true);
					txtNa_2.setEnabled(true);
				}
				else
				{
					lBox2.setEnabled(false);
					hBox2.setEnabled(false);
					txtNa_2.setText(defaultOffset);
					txtNa_2.setBorder(defaultBorder);
					txtNa_2.setEnabled(false);
				}

			}
		});
		add(chckbxLang_2, "cell 0 3,alignx center,aligny center");

		lBox2 = new JComboBox<String>();
		lBox2.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox2, "cell 1 3,grow");

		hBox2 = new JComboBox<String>();
		hBox2.setEditable(true);
		hBox2.setEnabled(false);
		hBox2.setModel(new DefaultComboBoxModel<String>(lHandles));
		add(hBox2, "cell 2 3,growx");

		JLabel lblOffset_2 = new JLabel("Offset:");
		add(lblOffset_2, "cell 3 3,alignx right,aligny center");

		txtNa_2 = new JTextField();
		txtNa_2.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_2.addFocusListener(focusLostAction);
		txtNa_2.setText(defaultOffset);
		add(txtNa_2, "cell 4 3,grow");
		txtNa_2.setColumns(10);

		chckbxSubs_2 = new JCheckBox("Subs 2");
		chckbxSubs_2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				if (chckbxSubs_2.isSelected())
				{
					lBox7.setEnabled(true);
					hBox7.setEnabled(true);
					txtNa_7.setEnabled(true);
				}
				else
				{
					lBox7.setEnabled(false);
					hBox7.setEnabled(false);
					txtNa_7.setText(defaultOffset);
					txtNa_7.setBorder(defaultBorder);
					txtNa_7.setEnabled(false);
				}
			}
		});
		add(chckbxSubs_2, "cell 6 3,alignx center,aligny center");

		lBox7 = new JComboBox<String>();
		lBox7.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox7, "cell 7 3,grow");

		hBox7 = new JComboBox<String>();
		hBox7.setEditable(true);
		hBox7.setEnabled(false);
		hBox7.setModel(new DefaultComboBoxModel<String>(sHandles));
		add(hBox7, "cell 8 3,growx");

		JLabel lblOffset_7 = new JLabel("Offset:");
		add(lblOffset_7, "cell 9 3,alignx right,aligny center");

		txtNa_7 = new JTextField();
		txtNa_7.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_7.addFocusListener(focusLostAction);
		txtNa_7.setText(defaultOffset);
		add(txtNa_7, "cell 10 3 3 1,grow");
		txtNa_7.setColumns(10);

		chckbxLang_3 = new JCheckBox("Lang 3");
		chckbxLang_3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxLang_3.isSelected())
				{
					lBox3.setEnabled(true);
					hBox3.setEnabled(true);
					txtNa_3.setEnabled(true);
				}
				else
				{
					lBox3.setEnabled(false);
					hBox3.setEnabled(false);
					txtNa_3.setText(defaultOffset);
					txtNa_3.setBorder(defaultBorder);
					txtNa_3.setEnabled(false);
				}

			}
		});
		add(chckbxLang_3, "cell 0 4,alignx center,aligny center");

		lBox3 = new JComboBox<String>();
		lBox3.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox3, "cell 1 4,grow");

		hBox3 = new JComboBox<String>();
		hBox3.setEditable(true);
		hBox3.setEnabled(false);
		hBox3.setModel(new DefaultComboBoxModel<String>(lHandles));
		add(hBox3, "cell 2 4,growx");

		JLabel lblOffset_3 = new JLabel("Offset:");
		add(lblOffset_3, "cell 3 4,alignx right,aligny center");

		txtNa_3 = new JTextField();
		txtNa_3.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_3.addFocusListener(focusLostAction);
		txtNa_3.setText(defaultOffset);
		add(txtNa_3, "cell 4 4,grow");
		txtNa_3.setColumns(10);

		chckbxSubs_3 = new JCheckBox("Subs 3");
		chckbxSubs_3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxSubs_3.isSelected())
				{
					lBox8.setEnabled(true);
					hBox8.setEnabled(true);
					txtNa_8.setEnabled(true);
				}
				else
				{
					lBox8.setEnabled(false);
					hBox8.setEnabled(false);
					txtNa_8.setText(defaultOffset);
					txtNa_8.setBorder(defaultBorder);
					txtNa_8.setEnabled(false);
				}
			}
		});
		add(chckbxSubs_3, "cell 6 4,alignx center,aligny center");

		lBox8 = new JComboBox<String>();
		lBox8.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox8, "cell 7 4,grow");

		hBox8 = new JComboBox<String>();
		hBox8.setEditable(true);
		hBox8.setEnabled(false);
		hBox8.setModel(new DefaultComboBoxModel<String>(sHandles));
		add(hBox8, "cell 8 4,growx");

		JLabel lblOffset_8 = new JLabel("Offset:");
		add(lblOffset_8, "cell 9 4,alignx right,aligny center");

		txtNa_8 = new JTextField();
		txtNa_8.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_8.addFocusListener(focusLostAction);
		txtNa_8.setText(defaultOffset);
		add(txtNa_8, "cell 10 4 3 1,grow");
		txtNa_8.setColumns(10);

		chckbxLang_4 = new JCheckBox("Lang 4");
		chckbxLang_4.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxLang_4.isSelected())
				{
					lBox4.setEnabled(true);
					hBox4.setEnabled(true);
					txtNa_4.setEnabled(true);
				}
				else
				{
					lBox4.setEnabled(false);
					hBox4.setEnabled(false);
					txtNa_4.setText(defaultOffset);
					txtNa_4.setBorder(defaultBorder);
					txtNa_4.setEnabled(false);
				}

			}
		});
		add(chckbxLang_4, "cell 0 5,alignx center,aligny center");

		lBox4 = new JComboBox<String>();
		lBox4.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox4, "cell 1 5,grow");

		hBox4 = new JComboBox<String>();
		hBox4.setEditable(true);
		hBox4.setEnabled(false);
		hBox4.setModel(new DefaultComboBoxModel<String>(lHandles));
		add(hBox4, "cell 2 5,growx");

		JLabel lblOffset_4 = new JLabel("Offset:");
		add(lblOffset_4, "cell 3 5,alignx right,aligny center");

		txtNa_4 = new JTextField();
		txtNa_4.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_4.addFocusListener(focusLostAction);
		txtNa_4.setText(defaultOffset);
		add(txtNa_4, "cell 4 5,grow");
		txtNa_4.setColumns(10);

		chckbxSubs_4 = new JCheckBox("Subs 4");
		chckbxSubs_4.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxSubs_4.isSelected())
				{
					lBox9.setEnabled(true);
					hBox9.setEnabled(true);
					txtNa_9.setEnabled(true);
				}
				else
				{
					lBox9.setEnabled(false);
					hBox9.setEnabled(false);
					txtNa_9.setText(defaultOffset);
					txtNa_9.setBorder(defaultBorder);
					txtNa_9.setEnabled(false);
				}
			}
		});
		add(chckbxSubs_4, "cell 6 5,alignx center,aligny center");

		lBox9 = new JComboBox<String>();
		lBox9.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox9, "cell 7 5,grow");

		hBox9 = new JComboBox<String>();
		hBox9.setEditable(true);
		hBox9.setEnabled(false);
		hBox9.setModel(new DefaultComboBoxModel<String>(sHandles));
		add(hBox9, "cell 8 5,growx");

		JLabel lblOffset_9 = new JLabel("Offset:");
		add(lblOffset_9, "cell 9 5,alignx right,aligny center");

		txtNa_9 = new JTextField();
		txtNa_9.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_9.addFocusListener(focusLostAction);
		txtNa_9.setText(defaultOffset);
		add(txtNa_9, "cell 10 5 3 1,grow");
		txtNa_9.setColumns(10);

		chckbxLang_5 = new JCheckBox("Lang 5");
		chckbxLang_5.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxLang_5.isSelected())
				{
					lBox5.setEnabled(true);
					hBox5.setEnabled(true);
					txtNa_5.setEnabled(true);
				}
				else
				{
					lBox5.setEnabled(false);
					hBox5.setEnabled(false);
					txtNa_5.setText(defaultOffset);
					txtNa_5.setBorder(defaultBorder);
					txtNa_5.setEnabled(false);
				}

			}
		});
		add(chckbxLang_5, "cell 0 6,alignx center,aligny center");

		lBox5 = new JComboBox<String>();
		lBox5.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox5, "cell 1 6,grow");

		hBox5 = new JComboBox<String>();
		hBox5.setEditable(true);
		hBox5.setEnabled(false);
		hBox5.setModel(new DefaultComboBoxModel<String>(lHandles));
		add(hBox5, "cell 2 6,growx");

		JLabel lblOffset_5 = new JLabel("Offset:");
		add(lblOffset_5, "cell 3 6,alignx right,aligny center");

		txtNa_5 = new JTextField();
		txtNa_5.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_5.addFocusListener(focusLostAction);
		txtNa_5.setText(defaultOffset);
		add(txtNa_5, "cell 4 6,grow");
		txtNa_5.setColumns(10);

		chckbxSubs_5 = new JCheckBox("Subs 5");
		chckbxSubs_5.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxSubs_5.isSelected())
				{
					lBox10.setEnabled(true);
					hBox10.setEnabled(true);
					txtNa_10.setEnabled(true);
				}
				else
				{
					lBox10.setEnabled(false);
					hBox10.setEnabled(false);
					txtNa_10.setText(defaultOffset);
					txtNa_10.setEnabled(false);
				}
			}
		});
		add(chckbxSubs_5, "cell 6 6,alignx center,aligny center");

		lBox10 = new JComboBox<String>();
		lBox10.setModel(new DefaultComboBoxModel<String>(Timing.getLangs()));
		add(lBox10, "cell 7 6,grow");

		hBox10 = new JComboBox<String>();
		hBox10.setEditable(true);
		hBox10.setEnabled(false);
		hBox10.setModel(new DefaultComboBoxModel<String>(sHandles));
		add(hBox10, "cell 8 6,growx");

		JLabel lblOffset_10 = new JLabel("Offset:");
		add(lblOffset_10, "cell 9 6,alignx right,aligny center");

		txtNa_10 = new JTextField();
		txtNa_10.setHorizontalAlignment(SwingConstants.CENTER);
		txtNa_10.addFocusListener(focusLostAction);
		txtNa_10.setText(defaultOffset);
		add(txtNa_10, "cell 10 6 3 1,grow");
		txtNa_10.setColumns(10);

		txtNa_1.setEnabled(false);
		txtNa_2.setEnabled(false);
		txtNa_3.setEnabled(false);
		txtNa_4.setEnabled(false);
		txtNa_5.setEnabled(false);
		txtNa_6.setEnabled(false);
		txtNa_7.setEnabled(false);
		txtNa_8.setEnabled(false);
		txtNa_9.setEnabled(false);
		txtNa_10.setEnabled(false);
		lBox1.setEnabled(false);
		lBox2.setEnabled(false);
		lBox3.setEnabled(false);
		lBox4.setEnabled(false);
		lBox5.setEnabled(false);
		lBox6.setEnabled(false);
		lBox7.setEnabled(false);
		lBox8.setEnabled(false);
		lBox9.setEnabled(false);
		lBox10.setEnabled(false);

		separator_1 = new JSeparator();
		add(separator_1, "cell 0 7 13 1,growx");

		lblMetaTitle = new JLabel("Meta Title:");
		add(lblMetaTitle, "cell 0 8 2 1,alignx right");

		txtTitle = new JTextField();
		add(txtTitle, "cell 2 8 9 1,grow");
		txtTitle.setColumns(10);

		lblExportContainer = new JLabel("Export Container:");
		add(lblExportContainer, "cell 11 8,alignx right");

		extensionBox = new JComboBox<String>();
		extensionBox.setModel(new DefaultComboBoxModel<String>(videoExtensions));
		extensionBox.setSelectedIndex(0);
		add(extensionBox, "cell 12 8,growx");

		lblMetaDescription = new JLabel("Meta Description:");
		add(lblMetaDescription, "cell 0 9 2 1,alignx right");

		txtDescription = new JTextField();
		add(txtDescription, "cell 2 9 11 1,grow");
		txtDescription.setColumns(10);

		checkDuration = new JCheckBox("Duration:");
		checkDuration.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if (checkDuration.isSelected())
				{
					logger.debug("Duration Field Unlocked");
					durationField.setEnabled(true);
					checkStartTime.setEnabled(true);
				}
				else
				{
					logger.debug("Duration Field Locked to 00:00:00.000");
					durationField.setText(defaultOffset);
					durationField.setBorder(defaultBorder);
					durationField.setEnabled(false);
				}
			}
		});
		add(checkDuration, "cell 0 10 2 1,alignx right");

		durationField = new JTextField();
		durationField.setEnabled(false);
		durationField.setText(defaultOffset);
		durationField.setHorizontalAlignment(SwingConstants.CENTER);
		add(durationField, "cell 2 10 3 1,grow");
		durationField.setColumns(10);

		checkStartTime = new JCheckBox("Master Offset:");
		checkStartTime.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (checkStartTime.isSelected())
				{
					logger.debug("Start Time Field Toggled");
					logger.debug("Start Time Field Unlocked");
					startTimeField.setEnabled(true);
					JOptionPane.showMessageDialog(TrackSelection.this,
							"Using the Master Offset will force FFMPEG to scrub through the whole video until it reaches this value\nThe GUI will not have any progress to track until it reaches the start time.\nPlease be patient, and know the longer the Master Offset, the longer the delay until FFMPEG appears active",
							"Be Patient", JOptionPane.WARNING_MESSAGE);
				}
				else
				{
					logger.debug("Start Time Field Locked to " + defaultOffset);
					startTimeField.setEnabled(false);
					startTimeField.setText(defaultOffset);
				}
			}
		});
		add(checkStartTime, "cell 6 10 2 1,alignx right");

		startTimeField = new JTextField();
		startTimeField.setHorizontalAlignment(SwingConstants.CENTER);
		startTimeField.setEnabled(false);
		startTimeField.addFocusListener(focusLostAction);
		startTimeField.setText(defaultOffset);
		add(startTimeField, "cell 8 10 5 1,grow");
		startTimeField.setColumns(10);
	}

	/**
	 * Checks if File is Compatible with FFMPEg, has a Special Condition for "flac" files, since that extension is 4 chars
	 * long
	 * @param fileName String of the file Name, could be any length, only cares about end
	 * @param extensions Which set of Extensions to look for, (Video,Audio,Subtitle)
	 * @return if FFMPEG Compatible
	 */
	public static boolean isCompatible(String fileName, String[] extensions)
	{
		for (String i : extensions)
		{
			if (i.equals(fileName.substring(fileName.length() - 3)))
			{
				return true;
			}
		}
		return "flac".equals(fileName.substring(fileName.length() - 4));
	}

	/**
	 * Global method to filter down to the correct extension intended for a file, only for Video File
	 * @param c The JComponent to display the JOptionpane over if necessary
	 * @param fileName The FULL Path to the file minus the extension
	 * @return The extension, with the "." or Null if not found or selected
	 */
	public static String findExtension(Component c, String fileName)
	{
		ArrayList<String> options = new ArrayList<String>();
		for (String i : videoExtensions)
		{
			if (new File(fileName + "." + i).exists())
			{
				options.add(i);
			}
		}
		if (options.isEmpty())
		{
			return null;
		}
		else if (options.size() == 1)
		{
			return "." + options.get(0);
		}
		else
		{
			String option = (String) JOptionPane.showInputDialog(c, "Multiple Video Files found\nPlease Select which to Use", "Select File",
					JOptionPane.QUESTION_MESSAGE, null, options.toArray(new String[]
					{}), options.get(0));
			if (option.equals(""))
				return null;
			return "." + option;
		}
	}

	/**
	 * Global method to filter down to the correct extension intended for a file, only for Video File
	 * @param c The JComponent to display the JOptionpane over if necessary
	 * @param fileName The FULL Path to the file minus the extension
	 * @param extensions Array of Extensions acceptable, different for Videos, Audio, and Subtitles
	 * @param stream The A/S Stream slot number for the output file 0-4 for Audio, 5-9 for Subtitles
	 * @return The extension, with the "." or Null if not found or selected
	 */
	public static String findExtension(Component c, String fileName, String[] extensions, int stream)
	{
		ArrayList<String> options = new ArrayList<String>();
		for (String i : extensions)
		{
			if (new File(fileName + "." + i).exists())
			{
				options.add(i);
			}
		}
		if (options.isEmpty())
		{
			return null;
		}
		else if (options.size() == 1)
		{
			return "." + options.get(0);
		}
		else
		{
			String as = "Audio";
			if (extensions == subtitleExtensions)
			{
				as = "Subtitle";
				stream -= 5;
			}
			String option = (String) JOptionPane.showInputDialog(c,
					"Multiple " + as + " Files found\nPlease Select which to Use for " + as + " " + stream, "Select File",
					JOptionPane.QUESTION_MESSAGE, null, options.toArray(new String[]
					{}), options.get(0));
			if (option.equals(""))
				return null;
			return "." + option;
		}
	}

	public String[] updateInterlacedList()
	{
		ArrayList<String> shows = new ArrayList<String>();
		String[] list = new File(dir + File.separator + "ImportVideo").list();
		for (int x = 0; x < list.length; x++)
		{
			// Interlaced_<EP ###>_<SHOWNAME>.<EXTENSION>
			try
			{
				if (isCompatible(list[x], videoExtensions) && list[x].substring(0, 10).equals("Interlaced")
						&& !shows.contains(list[x].substring(15, list[x].length() - 4)))
				{
					shows.add(list[x].substring(15, list[x].length() - 4));
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				logger.trace("{} Out of Interlaced Video String Expected Bounds", list[x]);
			}
		}
		if (shows.isEmpty())
		{
			shows.add("NoShowsFound");
		}
		return shows.toArray(new String[]
		{ "" });
	}

	public String[] updateShowList()
	{
		ArrayList<String> shows = new ArrayList<String>();
		String[] list = new File(dir + File.separator + "ImportVideo").list();
		for (int x = 0; x < list.length; x++)
		{
			try
			{
				// Input_<EP ###>_<SHOWNAME>.<EXTENSION>
				if (isCompatible(list[x], videoExtensions) && list[x].substring(0, 5).equals("Input")
						&& !shows.contains(list[x].substring(10, list[x].length() - 4)))
				{
					shows.add(list[x].substring(10, list[x].length() - 4));
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				logger.trace("{} Out of Interlaced Video String Expected Bounds", list[x]);
			}
		}
		list = new File(dir + File.separator + "ImportAudio").list();
		for (int x = 0; x < list.length; x++)
		{
			try
			{
				// <language @@@>_<EP ###>_<SHOWNAME>.<EXTENSION>
				if (isCompatible(list[x], audioExtensions) && !shows.contains(list[x].substring(8, list[x].length() - 4)))
				{
					shows.add(list[x].substring(8, list[x].length() - 4));
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				logger.trace("{} Out of Interlaced Video String Expected Bounds", list[x]);
			}
		}
		list = new File(dir + File.separator + "ImportSubtitle").list();
		for (int x = 0; x < list.length; x++)
		{
			try
			{
				// <language @@@>_<EP ###>_<SHOWNAME>.<EXTENSION>
				if (isCompatible(list[x], subtitleExtensions) && !shows.contains(list[x].substring(8, list[x].length() - 4)))
				{
					shows.add(list[x].substring(8, list[x].length() - 4));
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				logger.trace("{} Out of Interlaced Video String Expected Bounds", list[x]);
			}
		}
		list = new File(dir + File.separator + "Export").list();
		for (int x = 0; x < list.length; x++)
		{
			try
			{
				// Upscaled_eng_000_Code_Lyoko.mkv
				// Remuxed_eng_000_Code_Lyoko.mkv
				String temp;
				if (list[x].startsWith("Upscaled"))
				{
					temp = (list[x].substring(17, list[x].length() - 4));
				}
				else if (list[x].startsWith("Remuxed"))
				{
					temp = (list[x].substring(16, list[x].length() - 4));
				}
				else
				{
					temp = null;
				}
				if (temp != null && !shows.contains(temp))
				{
					shows.add(temp);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				logger.trace("{} Out of Interlaced Video String Expected Bounds", list[x]);
			}
		}
		if (shows.isEmpty())
		{
			shows.add("NoShowsFound");
		}
		return shows.toArray(new String[]
		{ "" });
	}

	public void disablePanel()
	{
		txtNa_1.setEnabled(false);
		txtNa_2.setEnabled(false);
		txtNa_3.setEnabled(false);
		txtNa_4.setEnabled(false);
		txtNa_5.setEnabled(false);
		txtNa_6.setEnabled(false);
		txtNa_7.setEnabled(false);
		txtNa_8.setEnabled(false);
		txtNa_9.setEnabled(false);
		txtNa_10.setEnabled(false);
		chckbxLang_1.setEnabled(false);
		chckbxLang_2.setEnabled(false);
		chckbxLang_3.setEnabled(false);
		chckbxLang_4.setEnabled(false);
		chckbxLang_5.setEnabled(false);
		chckbxSubs_1.setEnabled(false);
		chckbxSubs_2.setEnabled(false);
		chckbxSubs_3.setEnabled(false);
		chckbxSubs_4.setEnabled(false);
		chckbxSubs_5.setEnabled(false);
		lBox1.setEnabled(false);
		lBox2.setEnabled(false);
		lBox3.setEnabled(false);
		lBox4.setEnabled(false);
		lBox5.setEnabled(false);
		lBox6.setEnabled(false);
		lBox7.setEnabled(false);
		lBox8.setEnabled(false);
		lBox9.setEnabled(false);
		lBox10.setEnabled(false);
		hBox1.setEnabled(false);
		hBox2.setEnabled(false);
		hBox3.setEnabled(false);
		hBox4.setEnabled(false);
		hBox5.setEnabled(false);
		hBox6.setEnabled(false);
		hBox7.setEnabled(false);
		hBox8.setEnabled(false);
		hBox9.setEnabled(false);
		hBox10.setEnabled(false);
		txtTitle.setEnabled(false);
		txtDescription.setEnabled(false);
		checkDuration.setEnabled(false);
		durationField.setEnabled(false);
		checkStartTime.setEnabled(false);
		startTimeField.setEnabled(false);
		comboBoxShowName.setEnabled(false);
		episodeSpinner.setEnabled(false);
	}

	public void enablePanel()
	{
		chckbxLang_1.setEnabled(true);
		chckbxLang_2.setEnabled(true);
		chckbxLang_3.setEnabled(true);
		chckbxLang_4.setEnabled(true);
		chckbxLang_5.setEnabled(true);
		chckbxSubs_1.setEnabled(true);
		chckbxSubs_2.setEnabled(true);
		chckbxSubs_3.setEnabled(true);
		chckbxSubs_4.setEnabled(true);
		chckbxSubs_5.setEnabled(true);
		if (chckbxLang_1.isSelected())
		{
			txtNa_1.setEnabled(true);
			lBox1.setEnabled(true);
			hBox1.setEnabled(true);
		}
		if (chckbxLang_2.isSelected())
		{
			txtNa_2.setEnabled(true);
			lBox2.setEnabled(true);
			hBox2.setEnabled(true);
		}
		if (chckbxLang_3.isSelected())
		{
			txtNa_3.setEnabled(true);
			lBox3.setEnabled(true);
			hBox3.setEnabled(true);
		}
		if (chckbxLang_4.isSelected())
		{
			txtNa_4.setEnabled(true);
			lBox4.setEnabled(true);
			hBox4.setEnabled(true);
		}
		if (chckbxLang_5.isSelected())
		{
			txtNa_5.setEnabled(true);
			lBox5.setEnabled(true);
			hBox5.setEnabled(true);
		}
		if (chckbxSubs_1.isSelected())
		{
			txtNa_6.setEnabled(true);
			lBox6.setEnabled(true);
			hBox6.setEnabled(true);
		}
		if (chckbxSubs_2.isSelected())
		{
			txtNa_7.setEnabled(true);
			lBox7.setEnabled(true);
			hBox7.setEnabled(true);
		}
		if (chckbxSubs_3.isSelected())
		{
			txtNa_8.setEnabled(true);
			lBox8.setEnabled(true);
			hBox8.setEnabled(true);
		}
		if (chckbxSubs_4.isSelected())
		{
			txtNa_9.setEnabled(true);
			lBox9.setEnabled(true);
			hBox9.setEnabled(true);
		}
		if (chckbxSubs_5.isSelected())
		{
			txtNa_10.setEnabled(true);
			lBox10.setEnabled(true);
			hBox10.setEnabled(true);
		}
		comboBoxShowName.setEnabled(true);
		episodeSpinner.setEnabled(true);
		txtTitle.setEnabled(true);
		txtDescription.setEnabled(true);
		checkDuration.setEnabled(true);
		checkStartTime.setEnabled(true);
		if (checkDuration.isSelected())
		{
			durationField.setEnabled(true);
		}
		if (checkStartTime.isSelected())
		{
			startTimeField.setEnabled(true);
		}
	}

	/**
	 * Analyze String to Update GUI border if needed
	 * 
	 * @param text
	 * The text to Verify
	 * @return if Offset Text is Acceptable
	 */
	private boolean checkOffset(String text)
	{
		if (!text.equals("00:00:00.000"))
		{
			boolean wrong = true;
			if (text.startsWith("-"))
			{
				if (text.length() == 13 && StringUtils.isNumeric(text.substring(1, 3)) && text.substring(3, 4).equals(":")
						&& StringUtils.isNumeric(text.substring(4, 6)) && text.substring(6, 7).equals(":")
						&& StringUtils.isNumeric(text.substring(7, 9)) && text.substring(9, 10).equals(".")
						&& StringUtils.isNumeric(text.substring(10, 13)))
				{
					wrong = false;
				}
			}
			else
			{
				if (text.length() == 12 && StringUtils.isNumeric(text.substring(0, 2)) && text.substring(2, 3).equals(":")
						&& StringUtils.isNumeric(text.substring(3, 5)) && text.substring(5, 6).equals(":")
						&& StringUtils.isNumeric(text.substring(6, 8)) && text.substring(8, 9).equals(".")
						&& StringUtils.isNumeric(text.substring(9, 12)))
				{
					wrong = false;
				}
			}
			return !wrong;
		}
		else
		{
			return true;
		}
	}

	public boolean[] getChosen()
	{
		boolean[] list = new boolean[10];
		list[0] = chckbxLang_1.isSelected();
		list[1] = chckbxLang_2.isSelected();
		list[2] = chckbxLang_3.isSelected();
		list[3] = chckbxLang_4.isSelected();
		list[4] = chckbxLang_5.isSelected();
		list[5] = chckbxSubs_1.isSelected();
		list[6] = chckbxSubs_2.isSelected();
		list[7] = chckbxSubs_3.isSelected();
		list[8] = chckbxSubs_4.isSelected();
		list[9] = chckbxSubs_5.isSelected();
		return list;
	}

	public String[] getLangs()
	{
		String[] list = new String[10];
		list[0] = (String) lBox1.getSelectedItem();
		list[1] = (String) lBox2.getSelectedItem();
		list[2] = (String) lBox3.getSelectedItem();
		list[3] = (String) lBox4.getSelectedItem();
		list[4] = (String) lBox5.getSelectedItem();
		list[5] = (String) lBox6.getSelectedItem();
		list[6] = (String) lBox7.getSelectedItem();
		list[7] = (String) lBox8.getSelectedItem();
		list[8] = (String) lBox9.getSelectedItem();
		list[9] = (String) lBox10.getSelectedItem();
		return list;
	}

	public String[] getHandles()
	{
		String[] list = new String[10];
		list[0] = formatForCMD((String) hBox1.getSelectedItem());
		list[1] = formatForCMD((String) hBox2.getSelectedItem());
		list[2] = formatForCMD((String) hBox3.getSelectedItem());
		list[3] = formatForCMD((String) hBox4.getSelectedItem());
		list[4] = formatForCMD((String) hBox5.getSelectedItem());
		list[5] = formatForCMD((String) hBox6.getSelectedItem());
		list[6] = formatForCMD((String) hBox7.getSelectedItem());
		list[7] = formatForCMD((String) hBox8.getSelectedItem());
		list[8] = formatForCMD((String) hBox9.getSelectedItem());
		list[9] = formatForCMD((String) hBox10.getSelectedItem());
		return list;
	}

	public String[] getOffsets()
	{
		String[] list = new String[10];
		list[0] = txtNa_1.getText();
		list[1] = txtNa_2.getText();
		list[2] = txtNa_3.getText();
		list[3] = txtNa_4.getText();
		list[4] = txtNa_5.getText();
		list[5] = txtNa_6.getText();
		list[6] = txtNa_7.getText();
		list[7] = txtNa_8.getText();
		list[8] = txtNa_9.getText();
		list[9] = txtNa_10.getText();
		return list;
	}

	public String formatForCMD(String text)
	{
		String formatted = new String(text);
		formatted = formatted.replace("\\", "\\\\");
		formatted = formatted.replace("\"", "\\\"");
		formatted = formatted.replace("\'", "\\\'");
		formatted = formatted.replace("/", "\\/");
		return formatted;
	}

	public String getTitle()
	{
		return formatForCMD(txtTitle.getText());
	}

	public String getDescription()
	{
		return formatForCMD(txtDescription.getText());
	}

	public String getDuration()
	{
		return durationField.getText();
	}

	public String getMasterOffset()
	{
		return startTimeField.getText();
	}

	public String getShowName()
	{
		return (String) comboBoxShowName.getSelectedItem();
	}

	public int getEpisode()
	{
		return (int) episodeSpinner.getValue();
	}

	public String getExtension()
	{
		return (String) extensionBox.getSelectedItem();
	}
}
