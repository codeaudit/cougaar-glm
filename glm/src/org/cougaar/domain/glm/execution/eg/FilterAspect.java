/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.eg;

import java.util.Date;

/**
 * Describes an aspect of a managed item that can be filtered. It
 * specifies the kinds of items that have the aspect, the kinds of
 * comparisons that can be made and any common patterns that the
 * aspect can be compared against.
 **/     
public abstract class FilterAspect implements TimeConstants {
  public interface ParsedPattern {
  }
  public static class ParsedStringPattern implements ParsedPattern {
    String s;
    public ParsedStringPattern(String s) {
      this.s = s;
    }
    public String toString() {
      return s;
    }
  }
  public static class ParsedDoublePattern implements ParsedPattern {
    double value;
    String stringValue;
    public ParsedDoublePattern(String s) {
      value = Double.parseDouble(s);
      stringValue = s;
    }
    public double doubleValue() {
      return value;
    }
    public String toString() {
      return stringValue;
    }
  }
  public static class ParsedTimePattern implements ParsedPattern {
    private long theTime;
    boolean isRelative;
    public ParsedTimePattern(String pattern) {
      try {
        theTime = parseInterval(pattern);
        isRelative = true;
      } catch (RuntimeException re) {
        theTime = new EGDate(pattern).getTime();
        isRelative = false;
      }
    }
    public long getTime(long theExecutionTime) {
      if (isRelative) {
        return theTime + theExecutionTime;
      } else {
        return theTime;
      }
    }
    public String toString() {
      if (isRelative) {
        return formatInterval(theTime);
      } else {
        return new EGDate(theTime).toString();
      }
    }
  }

  public static abstract class Str_ng extends FilterAspect {
    public Str_ng(String anAspectName,
                  Class anApplicableClass,
                  Comparison[] someComparisons,
                  String[] someCommonPatterns)
    {
      super(anAspectName, anApplicableClass, someComparisons, someCommonPatterns);
    }
    public ParsedPattern parsePattern(String pattern) {
      return new ParsedStringPattern(pattern);
    }
  }

  public static abstract class Time extends FilterAspect {
    public Time(String anAspectName,
                Class anApplicableClass,
                Comparison[] someComparisons,
                String[] someCommonPatterns)
    {
      super(anAspectName, anApplicableClass, someComparisons, someCommonPatterns);
    }
    public ParsedPattern parsePattern(String pattern) {
      return new ParsedTimePattern(pattern);
    }
  }

  public static abstract class Do_ble extends FilterAspect {
    public Do_ble(String anAspectName,
                  Class anApplicableClass,
                  Comparison[] someComparisons,
                  String[] someCommonPatterns)
    {
      super(anAspectName, anApplicableClass, someComparisons, someCommonPatterns);
    }
    public ParsedPattern parsePattern(String pattern) {
      return new ParsedDoublePattern(pattern);
    }
  }

  public static abstract class Comparison {
    private String theName;
    public Comparison(String aName) {
      theName = aName;
    }
    public String getName() {
      return theName;
    }
    public String toString() {
      return theName;
    }
    public abstract boolean apply(Object object, ParsedPattern pattern);
  }

  public String theAspectName;

  public Class theApplicableClass;

  public Comparison[] theComparisons;

  public String[] theCommonPatterns;

  public static Comparison theNullComparison = new Comparison("") {
    public boolean apply(Object object, ParsedPattern pattern) {
      return false;
    }
  };

  public static FilterAspect nullFilterAspect =
    new FilterAspect("",
                     Object.class,
                     new FilterAspect.Comparison[] {FilterAspect.theNullComparison},
                     null)
  {
    public Object getItem(Object from) {
      return null;
    }
    public ParsedPattern parsePattern(String pattern) {
      return new ParsedStringPattern(pattern);
    }
  };
  
  public FilterAspect(String anAspectName,
                      Class anApplicableClass,
                      Comparison[] someComparisons,
                      String[] someCommonPatterns)
  {
    theAspectName = anAspectName;
    theApplicableClass = anApplicableClass;
    theComparisons = someComparisons;
    if (someCommonPatterns == null || someCommonPatterns.length == 0) {
      theCommonPatterns = new String[] {""};
    } else {
      theCommonPatterns = someCommonPatterns;
    }
  }

  private static class PM {
    String suffix;
    long multiplier;
    public PM(String suffix, long multiplier) {
      this.suffix = suffix;
      this.multiplier = multiplier;
    }
  }

  private static PM[] pms = {
    new PM("millisecond", 1),
    new PM("second", ONE_SECOND),
    new PM("minute", ONE_MINUTE),
    new PM("hour", ONE_HOUR),
    new PM("day", ONE_DAY),
    new PM("week", ONE_WEEK),
  };

  public static long parseInterval(String pattern) {
    String lcPattern = pattern.toLowerCase();
    if (lcPattern.endsWith("s")) lcPattern = lcPattern.substring(0, lcPattern.length() - 1);
    for (int i = 0; i < pms.length; i++) {
      String suffix = pms[i].suffix;
      if (lcPattern.endsWith(suffix)) {
        try {
          String stripped = lcPattern.substring(0, lcPattern.length() - suffix.length()).trim();
          return Long.parseLong(stripped) * pms[i].multiplier;
        } catch (RuntimeException re) {
        }
      }
    }
    throw new IllegalArgumentException("parseInterval failed " + pattern);
  }

  public static String formatInterval(long interval) {
    for (int i = pms.length; --i >= 0; ) {
      long multiplier = pms[i].multiplier;
      if (interval % multiplier == 0) {
        long val = interval / multiplier;
        return val + " " + pms[i].suffix + (val != 1L ? "s" : "");
      }
    }
    return "";                  // Never get here
  }

  public static long parseDatePattern(String pattern, long theExecutionTime) {
    try {
      return parseInterval(pattern) + theExecutionTime;
    } catch (RuntimeException re) {
    }
    return new Date(pattern).getTime();
  }

  public String toString() {
    return theAspectName;
  }

  public abstract ParsedPattern parsePattern(String pattern);

  public abstract Object getItem(Object from);

  public static Comparison[] stringComparisons = {
    new Comparison("equals") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((String) object).equals(pattern.toString());
      }
    },
    new Comparison("starts with") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((String) object).startsWith(pattern.toString());
      }
    },
    new Comparison("ends with") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((String) object).endsWith(pattern.toString());
      }
    },
    new Comparison("contains") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((String) object).indexOf(pattern.toString()) >= 0;
      }
    },
    new Comparison("doesn't equal") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return !((String) object).equals(pattern.toString());
      }
    },
    new Comparison("doesn't start with") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return !((String) object).startsWith(pattern.toString());
      }
    },
    new Comparison("doesn't end with") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return !((String) object).endsWith(pattern.toString());
      }
    },
    new Comparison("doesn't contain") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((String) object).indexOf(pattern.toString()) < 0;
      }
    },
  };

  public static class TimeObject {
    public long theItemTime;
    public long theExecutionTime;
    public TimeObject(long anItemTime, long anExecutionTime) {
      theItemTime = anItemTime;
      theExecutionTime = anExecutionTime;
    }
  }

  public static Comparison[] timeComparisons = {
    new Comparison("equals") {
      public boolean apply(Object object, ParsedPattern pattern) {
        TimeObject to = (TimeObject) object;
        ParsedTimePattern ptp = (ParsedTimePattern) pattern;
        return to.theItemTime == ptp.getTime(to.theExecutionTime);
      }
    },
    new Comparison("after") {
      public boolean apply(Object object, ParsedPattern pattern) {
        TimeObject to = (TimeObject) object;
        ParsedTimePattern ptp = (ParsedTimePattern) pattern;
        return to.theItemTime > ptp.getTime(to.theExecutionTime);
      }
    },
    new Comparison("before") {
      public boolean apply(Object object, ParsedPattern pattern) {
        TimeObject to = (TimeObject) object;
        ParsedTimePattern ptp = (ParsedTimePattern) pattern;
        return to.theItemTime < ptp.getTime(to.theExecutionTime);
      }
    },
    new Comparison("doesn't equal") {
      public boolean apply(Object object, ParsedPattern pattern) {
        TimeObject to = (TimeObject) object;
        ParsedTimePattern ptp = (ParsedTimePattern) pattern;
        return to.theItemTime != ptp.getTime(to.theExecutionTime);
      }
    },
    new Comparison("not after") {
      public boolean apply(Object object, ParsedPattern pattern) {
        TimeObject to = (TimeObject) object;
        ParsedTimePattern ptp = (ParsedTimePattern) pattern;
        return to.theItemTime <= ptp.getTime(to.theExecutionTime);
      }
    },
    new Comparison("not before") {
      public boolean apply(Object object, ParsedPattern pattern) {
        TimeObject to = (TimeObject) object;
        ParsedTimePattern ptp = (ParsedTimePattern) pattern;
        return to.theItemTime >= ptp.getTime(to.theExecutionTime);
      }
    },
  };

  public static Comparison[] numericComparisons = {
    new Comparison("==") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Double) object).doubleValue() == ((ParsedDoublePattern) pattern).doubleValue();
      }
    },
    new Comparison("!=") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Double) object).doubleValue() == ((ParsedDoublePattern) pattern).doubleValue();
      }
    },
    new Comparison("<") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Double) object).doubleValue() < ((ParsedDoublePattern) pattern).doubleValue();
      }
    },
    new Comparison(">") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Double) object).doubleValue() > ((ParsedDoublePattern) pattern).doubleValue();
      }
    },
    new Comparison("<=") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Double) object).doubleValue() <= ((ParsedDoublePattern) pattern).doubleValue();
      }
    },
    new Comparison(">=") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Double) object).doubleValue() >= ((ParsedDoublePattern) pattern).doubleValue();
      }
    },
  };

  public static String shortClassName(Class cls) {
    String className = cls.getName();
    int ix = className.lastIndexOf('.');
    return className.substring(ix + 1);
  }

  public static final String packagePrefix;
  static {
    String className = FilterAspect.class.getName();
    int ix = className.lastIndexOf('.');
    packagePrefix = className.substring(0, ix + 1);
  }

  public static Class classForName(String name) {
    String className = packagePrefix + name;
    try {
      return Class.forName(className);
    } catch (Exception e) {
      return Object.class;
    }
  }
    
  public static Comparison[] classComparisons = {
    new Comparison("is a") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Class) object) == classForName(pattern.toString());
      }
    },
    new Comparison("is not a") {
      public boolean apply(Object object, ParsedPattern pattern) {
        return ((Class) object) == classForName(pattern.toString());
      }
    },
  };
}
