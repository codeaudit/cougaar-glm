/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui.draw;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/** Class OMVector
 *
 *  a vector of objects and an encapsulated enumerator.
 *
 **/
public class OMVector {

    // vector of objects
    Vector vertices = null;
    int size_ = 0;		// number of elements
    int tail_ = -1;		// index for reverse iteration

    // vector iterator
    Enumeration enum = null;

    public OMVector() {
	vertices = new Vector();
    }

    public OMVector(int capacity) {
	vertices = new Vector(capacity);
    }
  
    public /*synchronized*/ void add(Object p) {
	vertices.addElement(p);
	++size_;
    }
    public /*synchronized*/ void add(Object[] pts, boolean decompose) {
	for (int i=0; i<pts.length; i++)
	    vertices.addElement(pts[i]);
	size_ = vertices.size();
    }
    public /*synchronized*/ void add(OMVector v) {
	vertices.addElement(v);
	++size_;
    }
    public /*synchronized*/ void add(OMVector v, boolean decompose) {
	v.resetEnumerator();
	while (v.hasMoreElements())
	    vertices.addElement(v.nextElement(true));
	size_ = vertices.size();
    }

    public /*synchronized*/ void insertAt(Object p, int index) {
	vertices.insertElementAt(p, index);
	++size_;
    }

    public /*synchronized*/ boolean remove(Object p) {
	boolean removed = vertices.removeElement(p);
	if (removed)
	    --size_;
	return removed;
    }
    public /*synchronized*/ void removeAt(int index) {
	vertices.removeElementAt(index);
	--size_;
    }

    public /*synchronized*/ int size() {
	return size_/*vertices.size()*/;
    }

    /** nextElement(true) - returns the next element, wrap if we've
        reached the end of the array, or return null if array is zero
        sized. */
    public /*synchronized*/ Object nextElement(boolean wrap)
    {
	if (size_ == 0)
	    return null;

	while (true) {
	    try {
		return enum.nextElement();
	    } catch (NoSuchElementException e) {
		resetEnumerator();
		continue;
	    }
	}
    }
    
    /** nextElement() - returns the next element or throws an
        exception. */
    public /*synchronized*/ Object nextElement()
	throws NoSuchElementException
    {
	return enum.nextElement();
    }

    /** elementAt() - returns object at the specified index or an
        exception is thrown */
    public /*synchronized*/ Object elementAt(int index) {
	return vertices.elementAt(index);
    }

    /** hasMoreElements() */
    public /*synchronized*/ boolean hasMoreElements() {
	return enum.hasMoreElements();
    }

    /** previousElement() - returns elements starting at the end of
        the vector and working to the front. Behavior is undefined if
        you add to or remove elements from the Vector while you're
        iterating. does not wrap the index. */
    public /*synchronized*/ Object previousElement()
    {
	return vertices.elementAt(tail_--);
    }

    /** previousElement(wrap) - returns elements starting at the end
        of the vector, working to the front.  Behavior is undefined if
        you add to or remove elements from the Vector while you're
        iterating.  wraps the index. */
    public /*synchronized*/ Object previousElement(boolean wrap)
    {
	Object obj = vertices.elementAt(tail_--);
	if (tail_ < 0) tail_ = size_ - 1;
	return obj;
    }

    /** resetEnumerator() - resets the Enumerator to a starting
        state. the state of the enumerator is invalid until the first
        call to resetEnumerator(). */
    public /*synchronized*/ void resetEnumerator() {
	enum = vertices.elements();
	tail_ = size_ - 1;
    }
}
