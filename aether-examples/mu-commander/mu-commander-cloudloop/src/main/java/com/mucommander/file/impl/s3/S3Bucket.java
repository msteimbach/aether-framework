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

package com.mucommander.file.impl.s3;

import com.cloudloop.storage.CloudStore;
import com.cloudloop.storage.CloudStoreObject;
import com.mucommander.auth.AuthException;
import com.mucommander.file.*;
import com.mucommander.io.RandomAccessInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>S3Bucket</code> represents an Amazon S3 bucket.
 *
 * @author Maxence Bernard
 */
public class S3Bucket extends S3File {

    private String bucketName;
    private S3BucketFileAttributes atts;

    // TODO: add support for ACL ? (would cost an extra request per bucket)
    /** Default permissions for S3 buckets */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(448);   // rwx------


    protected S3Bucket(FileURL url, CloudStore service, String bucketName) throws AuthException {
        super(url, service);

        this.bucketName = bucketName;
        atts = new S3BucketFileAttributes();
    }

    protected S3Bucket(FileURL url, CloudStore service, CloudStoreObject bucket, String bucketName) throws AuthException {
        super(url, service);

        this.bucketName = bucketName;
        atts = new S3BucketFileAttributes(bucket);
    }


    ///////////////////////////
    // S3File implementation //
    ///////////////////////////

    @Override
    public FileAttributes getFileAttributes() {
        return atts;
    }


    /////////////////////////////////
    // ProtocolFile implementation //
    /////////////////////////////////

    @Override
    public String getOwner() {
        return atts.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return true;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return listObjects(bucketName, "", this);
    }

    @Override
    public void delete() throws IOException {
    	throw new UnsupportedFileOperationException(FileOperation.DELETE);    
    }

    @Override
    public void mkdir() throws IOException {
    	throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);    
    }


    // Unsupported operations

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    @UnsupportedFileOperation
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RENAME);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public InputStream getInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * S3BucketFileAttributes provides getters and setters for S3 bucket attributes. By extending
     * <code>SyncedFileAttributes</code>, this class caches attributes for a certain amount of time
     * after which fresh values are retrieved from the server.
     *
     * @author Maxence Bernard
     */
    private class S3BucketFileAttributes extends SyncedFileAttributes {

        private final static int TTL = 60000;

        private S3BucketFileAttributes() throws AuthException {
            super(TTL, false);      // no initial update

            fetchAttributes();      // throws AuthException if no or bad credentials
            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private S3BucketFileAttributes(CloudStoreObject bucket) throws AuthException {
            super(TTL, false);      // no initial update

            setAttributes(bucket);
            setExists(true);

            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private void setAttributes(CloudStoreObject bucket) {
            setDirectory(true);
            setDate(bucket.getLastModifiedDate().getTime());
            setPermissions(DEFAULT_PERMISSIONS);
            setOwner(null);
        }

        private void fetchAttributes() throws AuthException {
            setExists(true);
            setDirectory(true);
            setDate(0);
            setPermissions(FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS);
            setOwner(null);
        }


        /////////////////////////////////////////
        // SyncedFileAttributes implementation //
        /////////////////////////////////////////

        @Override
        public void updateAttributes() {
            try {
                fetchAttributes();
            }
            catch(Exception e) {        // AuthException
                FileLogger.fine("Failed to update attributes", e);
            }
        }
    }
}
