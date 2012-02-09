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

package com.mucommander.file.impl.nfs;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;

import java.io.IOException;

/**
 * This class is the provider for the NFS filesystem implemented by {@link com.mucommander.file.impl.nfs.NFSFile}.
 *
 * @author Nicolas Rinaudo
 * @see com.mucommander.file.impl.nfs.NFSFile
 */
public class NFSProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return new NFSFile(url);
    }
}