/*
 * Bitstream.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2001, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.dspace.content;

import java.io.InputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.storage.bitstore.BitstreamStorageManager;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called.  Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author   Robert Tansley
 * @version  $Revision$
 */
public class Bitstream implements DSpaceObject
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(Bitstream.class);

    /** Our context */
    private Context bContext;

    /** The row in the table representing this bitstream */
    private TableRow bRow;

    /** The bitstream format corresponding to this bitstream */
    private BitstreamFormat bitstreamFormat;

    /**
     * Private constructor for creating a Bitstream object
     * based on the contents of a DB table row.
     *
     * @param context  the context this object exists in
     * @param row      the corresponding row in the table
     */
    Bitstream(Context context, TableRow row)
        throws SQLException
    {
        bContext = context;
        bRow = row;

        // Get the bitstream format
        bitstreamFormat = BitstreamFormat.find(context, 
            row.getIntColumn("bitstream_format_id"));
        
        if (bitstreamFormat == null)
        {
            // No format: use "Unknown"
            bitstreamFormat = BitstreamFormat.findUnknown(context);

            // Panic if we can't find it
            if (bitstreamFormat == null)
            {
                throw new IllegalStateException("No Unknown bitsream format");
            }
        }

        // Cache ourselves
        context.cache(this, row.getIntColumn("bitstream_id"));
    }


    /**
     * Get a bitstream from the database.  The bitstream metadata is
     * loaded into memory.
     *
     * @param  context  DSpace context object
     * @param  id       ID of the bitstream
     *   
     * @return  the bitstream, or null if the ID is invalid.
     */
    public static Bitstream find(Context context, int id)
        throws SQLException
    {
        // First check the cache
        Bitstream fromCache = (Bitstream) context.fromCache(
            Bitstream.class, id);
            
        if (fromCache != null)
        {
            return fromCache;
        }

        TableRow row = DatabaseManager.find(context,
            "bitstream",
            id);

        if (row == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context,
                    "find_bitstream",
                    "not_found,bitstream_id=" + id));
            }

            return null;
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context,
                    "find_bitstream",
                    "bitstream_id=" + id));
            }

            return new Bitstream(context, row);
        }
    }
    

    /**
     * Create a new bitstream, with a new ID.  The checksum and file size
     * are calculated.  This method is not public, and does not check
     * authorisation; other methods such as Bundle.createBitstream() will
     * check authorisation.  The newly created bitstream has the "unknown"
     * format.
     *
     * @param  context   DSpace context object
     * @param  is        the bits to put in the bitstream
     *
     * @return  the newly created bitstream
     */
    static Bitstream create(Context context, InputStream is)
        throws IOException, SQLException
    {
        // Store the bits
        int bitstreamID = BitstreamStorageManager.store(context, is);

        log.info(LogManager.getHeader(context,
            "create_bitstream",
            "bitstream_id=" + bitstreamID));

        // Set the format to "unknown"
        Bitstream bitstream = find(context, bitstreamID);
        bitstream.setFormat(null);

        return bitstream;
    }


    /**
     * Get the internal identifier of this bitstream
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return bRow.getIntColumn("bitstream_id");
    }


    /**
     * Get the name of this bitstream - typically the filename, without
     * any path information
     *
     * @return  the name of the bitstream
     */
    public String getName()
    {
        return bRow.getStringColumn("name");
    }


    /**
     * Set the name of the bitstream
     *
     * @param  n   the new name of the bitstream
     */
    public void setName(String n)
    {
        bRow.setColumn("name", n);
    }


    /**
     * Get the source of this bitstream - typically the filename with
     * path information (if originally provided) or the name of the tool
     * that generated this bitstream
     *
     * @return  the source of the bitstream
     */
    public String getSource()
    {
        return bRow.getStringColumn("source");
    }


    /**
     * Set the source of the bitstream
     *
     * @param  n   the new source of the bitstream
     */
    public void setSource(String n)
    {
        bRow.setColumn("source", n);
    }


    /**
     * Get the description of this bitstream - optional free text, typically
     * provided by a user at submission time
     *
     * @return  the description of the bitstream
     */
    public String getDescription()
    {
        return bRow.getStringColumn("description");
    }


    /**
     * Set the description of the bitstream
     *
     * @param  n   the new description of the bitstream
     */
    public void setDescription(String n)
    {
        bRow.setColumn("description", n);
    }

    
    /**
     * Get the checksum of the content of the bitstream, for integrity checking
     *
     * @return the checksum
     */
    public String getChecksum()
    {
        return bRow.getStringColumn("checksum");
    }

    
    /**
     * Get the algorithm used to calculate the checksum
     *
     * @return the algorithm, e.g. "MD5"
     */
    public String getChecksumAlgorithm()
    {
        return bRow.getStringColumn("checksum_algorithm");
    }

    
    /**
     * Get the size of the bitstream
     *
     * @return  the size in bytes
     */
    public int getSize()
    {
        return bRow.getIntColumn("size");
    }


    /**
     * Set the user's format description.  This implies that the format of the
     * bitstream is uncertain, and the format is set to "unknown."
     *
     * @param desc   the user's description of the format
     */
    public void setUserFormatDescription(String desc)
        throws SQLException
    {
        // FIXME: Would be better if this didn't throw an SQLException,
        // but we need to find the unknown format!
        setFormat(null);
        bRow.setColumn("user_format_description", desc);
    }


    /**
     * Get the user's format description.  Returns null if the format is known
     * by the system.
     *
     * @return the user's format description.
     */
    public String getUserFormatDescription()
    {
        return bRow.getStringColumn("user_format_description");
    }
    

    /**
     * Get the description of the format - either the user's or the description
     * of the format defined by the system.
     *
     * @return a description of the format.
     */
    public String getFormatDescription()
    {
        if (bitstreamFormat.getShortDescription().equals("Unknown"))
        {
            // Get user description if there is one
            String desc = bRow.getStringColumn("user_format_description");

            if (desc == null)
            {
                return "Unknown";
            }
            else
            {
                return desc;
            }
        }
        else
        {
            return bitstreamFormat.getShortDescription();
        }
    }
    

    /**
     * Get the format of the bitstream
     *
     * @return   the format of this bitstream
     */
    public BitstreamFormat getFormat()
    {
        return bitstreamFormat;
    }


    /**
     * Set the format of the bitstream.  If the user has supplied a type
     * description, it is cleared.  Passing in <code>null</code> sets the
     * type of this bitstream to "unknown".
     *
     * @param  f  the format of this bitstream, or <code>null</code> for
     *            unknown
     */
    public void setFormat(BitstreamFormat f)
        throws SQLException
    {
        // FIXME: Would be better if this didn't throw an SQLException,
        // but we need to find the unknown format!
        if (f == null)
        {
            // Use "Unknown" format
            bitstreamFormat = BitstreamFormat.findUnknown(bContext);
        }
        else
        {
            bitstreamFormat = f;
        }

        // Remove user type description
        bRow.setColumnNull("user_format_description");

        // Update the ID in the table row
        bRow.setColumn("bitstream_format_id", bitstreamFormat.getID());
    }


    /**
     * Update the bitstream metadata.  Note that the content of the bitstream
     * cannot be changed - for that you need to create a new bitstream.
     */
    public void update()
        throws SQLException, AuthorizeException
    {
        // Check authorisation
        AuthorizeManager.authorizeAction(bContext, this, Constants.WRITE);

        log.info(LogManager.getHeader(bContext,
            "update_bitstream",
            "bitstream_id=" + getID()));

        DatabaseManager.update(bContext, bRow);
    }


    /**
     * Delete the bitstream, including any mappings to bundles
     */
    void delete()
        throws SQLException, IOException, AuthorizeException
    {
        // changed to a check on remove
        // Check authorisation
        //AuthorizeManager.authorizeAction(bContext, this, Constants.DELETE);

        log.info(LogManager.getHeader(bContext,
            "delete_bitstream",
            "bitstream_id=" + getID()));

        // Remove from cache
        bContext.removeCached(this, getID());

        // Remove policies
        AuthorizeManager.removeAllPolicies(bContext, this);

        // Remove bitstream itself
        BitstreamStorageManager.delete(bContext,
            bRow.getIntColumn("bitstream_id"));
    }
    

    /**
     * Retrieve the contents of the bitstream
     *
     * @return   a stream from which the bitstream can be read.
     */
    public InputStream retrieve()
        throws IOException, SQLException, AuthorizeException
    {
        // Maybe should return AuthorizeException??
        AuthorizeManager.authorizeAction(bContext, this, Constants.READ);

        return BitstreamStorageManager.retrieve(bContext,
            bRow.getIntColumn("bitstream_id"));
    }


    /**
     * Get the bundles this bitstream appears in
     *
     * @return  array of <code>Bundle</code>s this bitstream
     *          appears in
     */
    public Bundle[] getBundles()
        throws SQLException
    {
        // Get the bundle table rows
        TableRowIterator tri = DatabaseManager.query(bContext,
            "bundle",
            "SELECT bundle.* FROM bundle, bundle2bitstream WHERE " +
                "bundle.bundle_id=bundle2bitstream.bundle_id AND " +
                "bundle2bitstream.bitstream_id=" +
                bRow.getIntColumn("bitstream_id") + ";");

        // Build a list of Bundle objects
        List bundles = new ArrayList();
        
        while (tri.hasNext())
        {
            TableRow r = (TableRow) tri.next();

            // First check the cache
            Bundle fromCache = (Bundle) bContext.fromCache(
                Bundle.class, r.getIntColumn("bundle_id"));

            if (fromCache != null)
            {
                bundles.add(fromCache);
            }
            else
            {
                bundles.add(new Bundle(bContext, r));
            }
        }

        Bundle[] bundleArray = new Bundle[bundles.size()];
        bundleArray = (Bundle[]) bundles.toArray(bundleArray);

        return bundleArray;
    }

    /**
     * return type found in Constants
     */
    public int getType()
    {
        return Constants.BITSTREAM;
    }

}
