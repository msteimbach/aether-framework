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

package com.mucommander.ui.action.impl;

import com.mucommander.bonjour.BonjourService;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * This action opens a specified location in the current active FileTable. The location can be designated by either a
 * FileURL, path, or AbstractFile.
 *
 * @author Maxence Bernard
 */
public class OpenLocationAction extends MuAction {

    private FileURL url;
    private AbstractFile file;
    private String path;


    /**
     * Creates a new OpenLocationAction instance using the provided url's string representation
     * (with credentials stripped out) as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, FileURL url) {
        this(mainFrame, properties, url, url.getScheme().equals(FileProtocols.FILE)?url.getPath():url.toString(false));
    }

    /**
     * Creates a new OpenLocationAction instance using the provided FileURL and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, FileURL url, String label) {
        super(mainFrame, properties);

        this.url = url;
        setLabel(label);
        setToolTipText(url.getScheme().equals(FileProtocols.FILE)?url.getPath():url.toString(false));
    }


    /**
     * Creates a new OpenLocationAction instance using the filename of the provided AbstractFile 
     * as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, AbstractFile file) {
        this(mainFrame, properties, file, file.getName());
    }

    /**
     * Creates a new OpenLocationAction instance using the provided AbstractFile and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, AbstractFile file, String label) {
        super(mainFrame, properties);

        this.file = file;
        setLabel(label);
        setToolTipText(file.getAbsolutePath());
    }


    /**
     * Creates a new OpenLocationAction instance using the provided path as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, String path) {
        this(mainFrame, properties, path, path);
    }

    /**
     * Creates a new OpenLocationAction instance using the provided path and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, String path, String label) {
        super(mainFrame, properties);

        this.path = path;
        setLabel(label);
        setToolTipText(path);
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, Hashtable, String, String)} with
     * {@link Bookmark#getLocation()} and {@link Bookmark#getName()}.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, Bookmark bookmark) {
        this(mainFrame, properties, bookmark.getLocation(), bookmark.getName());
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, Hashtable, FileURL, String)} with
     * {@link BonjourService#getURL()} and {@link BonjourService#getNameWithProtocol()} ()}.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable<String,Object> properties, BonjourService bonjourService) {
        this(mainFrame, properties, bonjourService.getURL(), bonjourService.getNameWithProtocol());
    }

    /**
     * Returns the {@link FolderPanel} on which to change the current folder. This method returns the currently active
     * panel but can be overridden if another panel should be used.
     *
     * @return the currently active panel
     */
    protected FolderPanel getFolderPanel() {
        return mainFrame.getActivePanel();
    }


    /////////////////////////////
    // MuAction implementation //
    /////////////////////////////

    @Override
    public void performAction() {
        FolderPanel folderPanel = getFolderPanel();
        if(url!=null) {
            folderPanel.tryChangeCurrentFolder(url);
        }
        else if(file!=null) {
            folderPanel.tryChangeCurrentFolder(file);
        }
        else if(path!=null) {
            folderPanel.tryChangeCurrentFolder(path);
        }
    }
}
