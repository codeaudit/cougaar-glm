/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.glm.execution.eg;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Filter objects in a SortedSet into another SortedSet having a
 * selected subset of the originals. The applicability of any
 * particular filter is controlled by associating the filter with
 * class that it is supposed to filter.
 **/
public class FilteredSet extends AbstractSet implements SortedSet {
  public interface Filter {
    Class getElementClass();
    boolean apply(Object element);
  }

  private SortedSet theBaseSet;
  private SortedSet theFilteredSet = null;
  private HashMap filters = new HashMap(13);

  public FilteredSet(SortedSet aBase) {
    theBaseSet = aBase;
  }

  /**
   * Remove all filters. With no filters, the filtered set will be
   * identical to the underlying set.
   **/
  public void clearFilters() {
    filters.clear();
    refilter();
  }

  /**
   * Add a filte. The added filter is applied to object whose class
   * matches the class that the filter is designed to filter.
   * @param filter the filter to add.
   **/
  public void addFilter(Filter filter) {
    Class cls = filter.getElementClass();
    ArrayList filterList = (ArrayList) filters.get(cls);
    if (filterList == null) {
      filterList = new ArrayList(3);
      filters.put(cls, filterList);
    }
    filterList.add(filter);
    refilter();
  }

  // Implementation of abstract and unsupported Collection methods

  /**
   * Get the current size of the filtered set.
   * @return the size of the filtered set.
   **/
  public int size() {
    return getFilteredSet().size();
  }

  public Iterator iterator() {
    return getFilteredSet().iterator();
  }

  /**
   * Add an object to the underlying set of the filtered set.
   * @param o the object to add.
   * @return true if the filtered set changed as a result of adding
   * the object.
   **/
  public boolean add(Object o) {
    if (theBaseSet.add(o)) {
      if (applyFilters(o)) {
        if (theFilteredSet != null) {
          theFilteredSet.add(o);
        }
        return true;
      }
    }
    return false;
  }

  public boolean contains(Object o) {
    if (theBaseSet.contains(o)) {
      return applyFilters(o);
    }
    return false;
  }

  public boolean remove(Object o) {
    if (theBaseSet.remove(o)) {
      if (theFilteredSet != null) {
        theFilteredSet.remove(o);
      }
      return true;
    }
    return false;
  }

  // May delegate more later

  // Implementation of SortSet

  public Comparator comparator() {
    return theBaseSet.comparator();
  }

  public SortedSet subSet(Object fromElement, Object toElement) {
    return getFilteredSet().subSet(fromElement, toElement);
  }

  public SortedSet headSet(Object toElement) {
    return getFilteredSet().headSet(toElement);
  }

  public SortedSet tailSet(Object fromElement) {
    return getFilteredSet().tailSet(fromElement);
  }

  public Object first() {
    return getFilteredSet().first();
  }

  public Object last() {
    return getFilteredSet().last();
  }

  /**
   * Override this to alter the filtering. Most subclasses should call
   * super.applyFilter and return true only if super.applyFilter
   * returns true.
   * @return true if the element should be included in the filtered set.
   **/
  protected boolean applyFilters(Object element) {
    return applyFilters(element, element.getClass());
  }

  protected boolean applyFilters(Object element, Class cls) {
    while (cls != null && cls != Object.class) {
      ArrayList filterList = (ArrayList) filters.get(cls);
      if (filterList != null) {
        for (int i = 0, n = filterList.size(); i < n; i++) {
          Filter filter = (Filter) filterList.get(i);
          if (!filter.apply(element)) return false;
        }
      }
      Class[] interfaces = cls.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        if (!applyFilters(element, interfaces[i])) return false;
      }
      cls = cls.getSuperclass();
    }
    return true;
  }

  private SortedSet getFilteredSet() {
    if (theFilteredSet == null) {
      theFilteredSet = new TreeSet(theBaseSet.comparator());
    outer:
      for (Iterator iterator = theBaseSet.iterator(); iterator.hasNext(); ) {
        Object element = iterator.next();
        if (applyFilters(element)) {
          theFilteredSet.add(element);
        }
      }
    }
    return theFilteredSet;
  }

  /**
   * Call this to indicate that the underlying set or the filtering
   * parameters have changed and the filtered set needs to be
   * recomputed.
   **/
  protected void refilter() {
    theFilteredSet = null;
  }
}
