package org.cougaar.domain.glm.execution.eg;

public interface PlugIn {
  /**
   * A name to use in menus and such.
   **/
  String getPlugInName();

  /**
   * Get a description of this plugin.
   **/
  String getDescription();

  /**
   * Plugins can be parameterized with a string.
   **/
  void setParameter(String parameter);

  /**
   * Test if this plugin can be configured
   * @return true if it is possible to configure this plugin (has a GUI)
   **/
  boolean isConfigurable();


  void setEventGenerator(EventGenerator eg);


  void configure(java.awt.Component frame);

  /**
   * PlugIns should save and restore their state in a property file.
   * @param props the Properties map in which the values are saved.
   * @param prefix the prefix for every property name. The prefix
   * uniquely identifies the plugin.
   **/
  void save(java.util.Properties props, String prefix);
  void restore(java.util.Properties props, String prefix);
}
