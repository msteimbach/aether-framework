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

package com.mucommander.shell;

import com.mucommander.AppLogger;
import com.mucommander.RuntimeConstants;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import java.io.OutputStream;
import java.util.Iterator;

/**
 * Used to save the content of the {@link com.mucommander.shell.ShellHistoryManager} to a file.
 * @author Nicolas Rinaudo
 */
class ShellHistoryWriter implements ShellHistoryConstants {
    /**
     * Writes the content of the {@link com.mucommander.shell.ShellHistoryManager} to the specified output stream.
     * @param stream where to save the shell history.
     */
    public static void write(OutputStream stream) {
        Iterator<String> history; // Iterator on the shell history.
        XmlWriter        out;     // Where to write the shell history to.

        // Initialises writing.
        history = ShellHistoryManager.getHistoryIterator();

        try {
            // Opens the file for writing.
            out = new XmlWriter(stream);

            // Version the file
            XmlAttributes attributes = new XmlAttributes();
            attributes.add(ATTRIBUTE_VERSION, RuntimeConstants.VERSION);

            out.startElement(ROOT_ELEMENT, attributes);
            out.println();

            // Writes the content of the shell history.
            while(history.hasNext()) {
                out.startElement(COMMAND_ELEMENT);
                out.writeCData(history.next());
                out.endElement(COMMAND_ELEMENT);
            }
            out.endElement(ROOT_ELEMENT);
        }
        catch(Exception e) {
            AppLogger.fine("Failed to write shell history", e);
        }
    }
}
