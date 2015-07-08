/**
 * Copyright (c) 2014, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the 
 *   distribution.
 * * Neither the name of the Sana nor the names of its contributors may 
 *   be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.net.queue;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Long.SIZE;


/**
 * 
 * 
 */
public class MessageQueue<E>{

    public static enum Priority{
        LOW(SIZE),
        HIGH(1),
        FIRST(0);
        public final long priority;
        Priority(long i){
            this.priority = i;
        }
        public long val(){
            return priority;
        }
    }
    private AtomicLong current = new AtomicLong(Priority.FIRST.val());

    private ConcurrentHashMap<Long, Set<E>> queue;

    /**
     * Creates a new <code>MessageQueue</code> that is initially empty.
     */
    public MessageQueue(){
        queue = new ConcurrentHashMap<Long, Set<E>>();
    }
    
    public E clear(){
        return null;
    }
    
    public E element(){
        return null;
    }
    
    /**
     * Inserts the specified element into this queue if it is possible 
     * to do so immediately without violating capacity restrictions. 
     * When using a capacity-restricted queue, this method is generally preferable to add(E), which can fail to insert an element only by throwing an exception.
     * 
     * @param   message the message to add
     * @return  <code>true</code> if the message was added, else 
     *          <code>false</code>
     * @throws  ClassCastException if the class of the specified 
     *          element prevents it from being added to this queue
     * @throws  NullPointerException if the specified element is null 
     *          and this queue does not permit null elements
     * @throws  IllegalArgumentException if some property of this 
     *          element prevents it from being added to this queue
     */
    public boolean offer(E message){
        return false;
    }
    
    public E peek(){
        return null;
    }
    
    public E poll(){
        return null;
    }
    
    public E remove(){
        return null;
    }
    
    public boolean isEmpty(){
        boolean empty = true;
        for(Set<E> e: queue.values()){
            // exit on first non empty set
            if(!e.isEmpty()) { break; }
        }
        return empty;
    }
    
    public int size(){
        int size = 0;
        for(Set<E> e: queue.values()){
            size += e.size();
        }
        return size;
    }
}
