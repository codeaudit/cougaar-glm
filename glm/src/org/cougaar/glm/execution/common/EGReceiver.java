/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.common;

import java.io.EOFException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Low level EG communications. Invokes handlers for received
 * EGObjects. Terminates on connection error (usually eof).
 **/

public class EGReceiver implements Runnable {
  private static class HandlerMethod {
    Method method;
    Object handler;
    public HandlerMethod(Object handler, Method method) {
      this.method = method;
      this.handler = handler;
    }
    public void invoke(String source, Object egObject) {
      try {
        method.invoke(handler, new Object[] {source, egObject});
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    public String toString() {
      return
        handler.getClass().getName()
        + "."
        + method.getName()
        + "(Object, "
        + method.getParameterTypes()[1].getName()
        + ")";
    }
  }

  private HandlerMethod[] handlers = new HandlerMethod[EGObject.egObjectClasses.length];
  private HandlerMethod[] arrayHandlers = new HandlerMethod[EGObject.egObjectClasses.length];

  private LineReader reader;

  private String source;
  private String myName;

  public EGReceiver(String receiverName, String source) {
    this.myName = receiverName;
    this.source = source.intern();
  }

  public void setReader(LineReader reader) {
    this.reader = reader;
  }

  public void addHandler(Object handler) {
    Class handlerClass = handler.getClass();
    Method[] methods = handlerClass.getMethods();
    int nHandlers = 0;
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (method.getName().equals("execute")) {
        Class[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 2) {
          if (String.class.isAssignableFrom(parameterTypes[0])) {
            Class arg2Type = parameterTypes[1];
            if (arg2Type.isArray()) {
              arg2Type = arg2Type.getComponentType();
              if (EGObject.class.isAssignableFrom(arg2Type)) {
                int classIndex = EGObjectBase.findClassIndex(arg2Type);
                arrayHandlers[classIndex] = new HandlerMethod(handler, method);
                nHandlers++;
              }
            } else {
              if (EGObject.class.isAssignableFrom(arg2Type)) {
                int classIndex = EGObjectBase.findClassIndex(arg2Type);
                handlers[classIndex] = new HandlerMethod(handler, method);
                nHandlers++;
              }
            }
          }
        }
      }
    }
    if (nHandlers == 0) {
      throw new IllegalArgumentException(handler.getClass().getName()
                                         + " has no execute methods handling EGObjects");
    }
  }

  public void run() {
    try {
      while (true) {
        EGObject egObject = reader.readEGObject();
        Object arg;
        HandlerMethod handler;
        Class componentClass;
        if (egObject instanceof EGObjectArray) {
          EGObjectArray array = (EGObjectArray) egObject;
          if (array.egObjects.length == 0) continue; // Ignore empty arrays
          EGObject component =  array.egObjects[0];
          handler = arrayHandlers[component.getClassIndex()];
          componentClass = component.getClass(); // Array should be homogeneous
          EGObject[] args = (EGObject[]) Array.newInstance(componentClass, array.egObjects.length);
          System.arraycopy(array.egObjects, 0, args, 0, args.length);
          arg = args;
        } else {
          handler = handlers[egObject.getClassIndex()];
          arg = egObject;
          componentClass = arg.getClass();
        }
        if (handler == null) {
          System.err.println("No handler for " + componentClass);
        } else {
//            System.out.println(myName + ": " + handler);
          handler.invoke(source, arg);
        }
      }
    } catch (EOFException eofe) {
      return; // This is normal.
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

