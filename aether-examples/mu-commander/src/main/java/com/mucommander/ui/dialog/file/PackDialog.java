/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.dialog.file;

import com.mucommander.file.archiver.Archiver;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.job.ArchiveJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.PackAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.text.FilePathField;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * This dialog allows the user to pack marked files to an archive file of a selected format (Zip, TAR, ...)
 * and add an optional comment to the archive (for the formats that support it).
 *
 * @author Maxence Bernard
 */
public class PackDialog extends JobDialog implements ActionListener, ItemListener {

    private JTextField filePathField;
	
    private JComboBox formatsComboBox;
    private int formats[];
	
    private JTextArea commentArea;

    private JButton okButton;
    private JButton cancelButton;

    /** Used to keep track of the last selected archive format. */
    private int oldFormatIndex;

    // Dialog's width has to be at least 240
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

    // Dialog's width has to be at most 320
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	

    /** Last archive format used (Zip initially), selected by default when this dialog is created */
    private static int lastFormat = Archiver.ZIP_FORMAT;


    public PackDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown) {
        super(mainFrame, ActionProperties.getActionLabel(PackAction.Descriptor.ACTION_ID), files);

        // Retrieve available formats for single file or many file archives
        int nbFiles = files.size();
        this.formats = Archiver.getFormats(nbFiles>1 || (nbFiles>0 && files.elementAt(0).isDirectory()));
        int nbFormats = formats.length;

        int initialFormat = formats[0];		// this value will only be used if last format is not available
        int initialFormatIndex = 0;			// this value will only be used if last format is not available
        for(int i=0; i<nbFormats; i++) {
            if(formats[i]==lastFormat) {
                initialFormat = formats[i];
                initialFormatIndex = i;
                break;
            }
        }
        oldFormatIndex = initialFormatIndex;
		
        Container contentPane = getContentPane();
		
        YBoxPanel mainPanel = new YBoxPanel(5);
        JLabel label = new JLabel(Translator.get("pack_dialog_description")+" :");
        mainPanel.add(label);

        FileTable activeTable = mainFrame.getInactiveTable();
        String initialPath = (isShiftDown?"":activeTable.getCurrentFolder().getAbsolutePath(true));
        String fileName;
        // Computes the archive's default name:
        // - if it only contains one file, uses that file's name.
        // - if it contains more than one file, uses the FileSet's parent folder's name.
        if(files.size() == 1)
            fileName = files.elementAt(0).getNameWithoutExtension();
        else if(files.getBaseFolder().getParent() != null)
            fileName = files.getBaseFolder().getName();
        else
            fileName = "";

        // Create a path field with auto-completion capabilities
        filePathField = new FilePathField(initialPath + fileName + "." + Archiver.getFormatExtension(initialFormat));

        // Selects the file name.
        filePathField.setSelectionStart(initialPath.length());
        filePathField.setSelectionEnd(initialPath.length() + fileName.length());

        mainPanel.add(filePathField);
		
        mainPanel.addSpace(10);

        // Archive formats combo box

        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(new JLabel(Translator.get("pack_dialog.archive_format")));		
        this.formatsComboBox = new JComboBox();
        for(int i=0; i<nbFormats; i++)
            formatsComboBox.addItem(Archiver.getFormatName(formats[i]));

        formatsComboBox.setSelectedIndex(initialFormatIndex);
		
        formatsComboBox.addItemListener(this);
        tempPanel.add(formatsComboBox);
		
        mainPanel.add(tempPanel);		
        mainPanel.addSpace(10);
		
        // Comment area, enabled only if selected archive format has comment support
		
        label = new JLabel(Translator.get("comment"));
        mainPanel.add(label);
        commentArea = new JTextArea();
        commentArea.setRows(4);
        mainPanel.add(commentArea);

        mainPanel.addSpace(10);

        // Create file details button and OK/cancel buttons and lay them out a single row
        JPanel fileDetailsPanel = createFileDetailsPanel();

        okButton = new JButton(Translator.get("pack"));
        cancelButton = new JButton(Translator.get("cancel"));

        mainPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this)));
        mainPanel.add(fileDetailsPanel);
        
        // Text field will receive initial focus
        setInitialFocusComponent(filePathField);		
		
        contentPane.add(mainPanel, BorderLayout.NORTH);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }
	
	
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        if (source==okButton)  {
            // Start by disposing the dialog
            dispose();

            // Check that destination file can be resolved 
            String filePath = filePathField.getText();
            // TODO: move those I/O bound calls to job as they can lock the main thread
            PathUtils.ResolvedDestination resolvedDest = PathUtils.resolveDestination(filePath, mainFrame.getActiveTable().getCurrentFolder());
            if (resolvedDest==null || resolvedDest.getDestinationType()==PathUtils.ResolvedDestination.EXISTING_FOLDER) {
                // Incorrect destination
                QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("pack_dialog.error_title"), Translator.get("invalid_path", filePath), mainFrame,
                                                           new String[] {Translator.get("ok")},
                                                           new int[]  {0},
                                                           0);
                dialog.getActionValue();
                return;
            }

            // Start packing
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("pack_dialog.packing"));
            int format = formats[formatsComboBox.getSelectedIndex()];

            ArchiveJob archiveJob = new ArchiveJob(progressDialog, mainFrame, files, resolvedDest.getDestinationFile(), format, Archiver.formatSupportsComment(format)?commentArea.getText():null);
            progressDialog.start(archiveJob);
        
            // Remember last format used, for next time this dialog is invoked
            lastFormat = format;
        }
        else if (source==cancelButton)  {
            // Simply dispose the dialog
            dispose();			
        }
    }


    //////////////////////////
    // ItemListener methods //
    //////////////////////////

    public void itemStateChanged(ItemEvent e) {
        int newFormatIndex;

        // Updates the GUI if, and only if, the format selection has changed.
        if(oldFormatIndex != (newFormatIndex = formatsComboBox.getSelectedIndex())) {
            String fileName = filePathField.getText();  // Name of the destination archive file.
            String oldFormatExtension = Archiver.getFormatExtension(formats[oldFormatIndex]);	// Old/current format's extension
            if(fileName.endsWith("." + oldFormatExtension)) {
                int selectionStart;
                int selectionEnd;

                // Saves the old selection.
                selectionStart = filePathField.getSelectionStart();
                selectionEnd   = filePathField.getSelectionEnd();

                // Computes the new file name.
                fileName = fileName.substring(0, fileName.length() - oldFormatExtension.length()) +
                    Archiver.getFormatExtension(formats[newFormatIndex]);

                // Makes sure that the selection stays somewhat coherent.
                if(selectionEnd == filePathField.getText().length())
                    selectionEnd = fileName.length();

                // Resets the file path field.
                filePathField.setText(fileName);
                filePathField.setSelectionStart(selectionStart);
                filePathField.setSelectionEnd(selectionEnd);
            }

            commentArea.setEnabled(Archiver.formatSupportsComment(formats[formatsComboBox.getSelectedIndex()]));
            oldFormatIndex = newFormatIndex;
        }

        // Transfer focus back to the text field 
        filePathField.requestFocus();
    }
}
