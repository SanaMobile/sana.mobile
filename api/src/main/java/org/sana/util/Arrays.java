/**
 * Copyright (c) 2014, Sana
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
package org.sana.util;

public final class Arrays{

    private Arrays(){}

    /**
     * Copies <code>newLength</code> elements from original into a new
     * array. If <code>newLength</code> is greater than
     * <code>original.length</code>, the result is padded with the value
     * <code>null</code>.
     *
     * @param original  the original array
     * @param newLength the length of the new array
     * @throws NegativeArraySizeException   if newLength < 0
     * @throws NullPointerException if <code>original == null</code>
     */
    public static <T> T[] copyOf(T[] original, int newLength){
        if(original == null)
            throw new NullPointerException("Can not copy null array");
        if(newLength < 0)
            throw new NegativeArraySizeException("Array size must be >= 0");

        T[] copy = (T[]) new Object[newLength];
        int index = 0;
        while(index < newLength){
            copy[index] = (index < original.length)?original[index]:null;
            index++;
        }
        return copy;
    }
}
