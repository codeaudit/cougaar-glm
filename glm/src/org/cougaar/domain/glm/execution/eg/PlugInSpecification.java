package org.cougaar.domain.glm.execution.eg;

/**
 * Specify a particular plugin in terms of its class and parameter.
 **/
public class PlugInSpecification {
  private Class cls;
  private String parameter;
  private int hc;

  public PlugInSpecification(String specificationString) throws ClassNotFoundException {
    int pos = specificationString.indexOf('/');
    if (pos != -1) {
      parameter = specificationString.substring(pos + 1);
      cls = Class.forName(specificationString.substring(0, pos));
    } else {
      parameter = null;
      cls = Class.forName(specificationString);
    }
    hc = cls.hashCode() + (parameter == null ? 0 : parameter.hashCode());
  }

  public String getParameter() {
    return parameter;
  }

  public Class getPlugInClass() {
    return cls;
  }

  public int hashCode() {
    return hc;
  }

  public boolean equal(Object o) {
    if (o instanceof PlugInSpecification) {
      PlugInSpecification other = (PlugInSpecification) o;
      if (this.cls == other.cls) {
        if (this.parameter == null) return other.parameter == null;
        return this.parameter.equals(other.parameter);
      }
    }
    return false;
  }
}
