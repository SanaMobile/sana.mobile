/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
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
package org.sana.android.util;
 
import org.sana.android.app.Locales;

import android.content.Context;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Text and String utility methods.
 *
 * @author Sana Development
 */
public final class Strings{

    private static AtomicBoolean forceLocale = new AtomicBoolean(false);
    private static String locale = null;

    private Strings(){}

    public static void initialize(Context context, int resId){
        locale = context.getString(resId);
    }

    public static boolean getForceLocale(){
        return forceLocale.get();
    }

    public static void setForceLocale(boolean newValue){
        forceLocale.set(newValue);
    }

    /**
     * Returns the localized string from a resource id using the locale
     * provided when calling (@link #intialize}
     *
     * @param context The Context which contains the localized resource
     * @param resId The String resource id to fetch
     */
    public static String getLocalizedString(Context context, int resId){
        Locales.updateLocale(context, locale);
        return context.getString(resId);
    }

}
