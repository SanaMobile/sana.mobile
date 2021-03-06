/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Sana nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.content.core;

import org.sana.android.db.ModelWrapper;
import org.sana.api.IProcedure;

import android.database.Cursor;

/**
 * @author Sana Development
 *
 */
public class ProcedureWrapper extends ModelWrapper<IProcedure> implements
        IProcedure {
    public static final String TAG = ProcedureWrapper.class.getSimpleName();

    /**
     * @param cursor
     */
    public ProcedureWrapper(Cursor cursor) {
        super(cursor);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.sana.api.IProcedure#getAuthor()
     */
    @Override
    public String getAuthor() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sana.api.IProcedure#getVersion()
     */
    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sana.api.IProcedure#getDescription()
     */
    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sana.api.IProcedure#getSrc()
     */
    @Override
    public String getSrc() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sana.android.db.ModelWrapper#getObject()
     */
    @Override
    public IProcedure getObject() {
        // TODO Auto-generated method stub
        return null;
    }
}
