/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.eg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This plugin controls the failure/consumption rate of a set of
 * consumables by reading a sensor value from a URL. A multiplier is
 * computed from the reading and used to increase or decrease the mean
 * consumption rate. The URL is obtained by replacing a % in the url
 * pattern with the name of the sensor as entered from the GUI.
 **/
public class TwentyFourBySevenPlugin extends TripletFCPlugin implements TimeConstants {
  private static final long SENSOR_UPDATE_INTERVAL = 10 * ONE_MINUTE;
  private static final String DEFAULT_URL_PATTERN = "file:%.txt";

  private String urlPrefix;
  private String urlSuffix;

  private class Sensor {
    public URL url;
    public String name;
    public double reading;
    public Sensor(String name) {
      url = getSensorURL(name);
      this.name = name;
      reading = Double.NaN;
    }
    public String toString() {
      return name + "(" + url + ")=" + reading;
    }
  }

  private static String[] valueLabels = {
    "Start Time",
    "End Time",
    "Sensor",
    "Low-value",
    "Low-multiplier",
    "High-value",
    "High-Multiplier"
  };
  private static Class[] valueClasses = {
    EGDate.class,
    EGDate.class,
    String.class,
    Double.class,
    Double.class,
    Double.class,
    Double.class,
  };

  private static NumberFormat formatter;

  static {
    formatter = NumberFormat.getInstance();
    formatter.setMinimumFractionDigits(0);
    formatter.setMaximumFractionDigits(2);
  }

  private long nextSensorUpdateTime;
  private Map sensors = new HashMap();

  protected class TripletValueImpl implements TripletValue {
    private long startDate;
    private long endDate;
    private Sensor sensor;
    private double base;
    private double highValue;
    private double midValue;
    private double lowValue;

    public TripletValueImpl(long start, long end, Sensor sensor,
                            double lowValue, double lowMultiplier,
                            double highValue, double highMultiplier)
    {
      startDate = start;
      endDate = end;
      this.sensor = sensor;
      this.highValue = highValue;
      this.lowValue = lowValue;
      double loghm = Math.log(highMultiplier);
      double loglm = Math.log(lowMultiplier);
      double q = (loghm - loglm) / (highValue - lowValue);
      base = Math.exp(q);
      if (q == 0.0) {
        midValue = (lowValue + highValue) * 0.5;
      } else {
        midValue = lowValue - Math.log(lowMultiplier) / q;
      }
    }

    public long getStartDate() {
      return startDate;
    }
    public long getEndDate() {
      return endDate;
    }
    public AnnotatedDouble getMultiplier() {
      double reading = sensor.reading;
      return new AnnotatedDouble(Double.isNaN(reading)
                                 ? 1.0
                                 : getMultiplier(reading),
                                 sensor.name + "=" + reading);
    }

    private double getMultiplier(double sensorValue) {
      return Math.pow(base, sensorValue - midValue);
    }

    private double getLowValue() {
      return lowValue;
    }

    private double getHighValue() {
      return highValue;
    }

    private double getLowMultiplier() {
      return getMultiplier(lowValue);
    }

    private double getHighMultiplier() {
      return getMultiplier(highValue);
    }

    public int getFieldCount() {
      return 5;
    }
    public Object getFieldValue(int ix) {
      switch (ix) {
      case 0: return new EGDate(startDate);
      case 1: return new EGDate(endDate);
      case 2: return sensor.name;
      case 3: return formatter.format(getLowValue());
      case 4: return formatter.format(getLowMultiplier());
      case 5: return formatter.format(getHighValue());
      case 6: return formatter.format(getHighMultiplier());
      default: return "";
      }
    }
    public String toString() {
      return new StringBuffer()
        .append(getFieldValue(0))
        .append(",")
        .append(getFieldValue(1))
        .append(",")
        .append(getFieldValue(2))
        .append(",")
        .append(getFieldValue(3))
        .append(",")
        .append(getFieldValue(4))
        .toString();
    }
  }

  public TwentyFourBySevenPlugin() {
    setURLPattern(DEFAULT_URL_PATTERN);
  }

  private void setURLPattern(String pattern) {
    int ix = pattern.indexOf('%');
    if (ix < 0) {
      urlPrefix = pattern;
      urlSuffix = "";
    } else {
      urlPrefix = pattern.substring(0, ix);
      urlSuffix = pattern.substring(ix + 1);
    }
  }

  private URL getSensorURL(String name) {
    try {
      return new URL(urlPrefix + name + urlSuffix);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Bad sensor name");
    }
  }

  protected TripletValue createTripletValue(String[] args) {
    Sensor sensor = (Sensor) sensors.get(args[2]);
    if (sensor == null) {
      final Sensor newSensor = new Sensor(args[2]);
      sensors.put(args[2], newSensor);
      new Thread("SensorUpdate " + args[2]) {
        public void run() {
          updateSensor(newSensor);
        }
      }.start();
      sensor = newSensor;
    }
    return new TripletValueImpl(new EGDate(args[0]).getTime(),
                                new EGDate(args[1]).getTime(),
                                sensor,
                                Double.parseDouble(args[3]),
                                Double.parseDouble(args[4]),
                                Double.parseDouble(args[5]),
                                Double.parseDouble(args[6])
                                );
  }

  protected String[] getTripletValueNames() {
    return valueLabels;
  }

  protected Class[] getTripletValueClasses() {
    return valueClasses;
  }

  protected Object[] getTripletDefaultValues() {
    Object[] result = {
      new EGDate(theEventGenerator.getExecutionTime()),
      new EGDate(theEventGenerator.getExecutionTime() + 1800L * ONE_DAY),
      "temperature",
      new Double(65.0),
      new Double(1.0),
      new Double(75.0),
      new Double(1.1),
    };
    return result;
  }

  protected JPanel createMessage() {
    JPanel message = super.createMessage();
//      addItem(message, COMMENT_ROW, new JLabel("Multiplier = Base ^ (value - midValue)"));
    return message;
  }

  /**
   * @return the name of this plugin
   **/
  public String getPluginName() {
    return "TwentyFourBySeven";
  }

  public String getDescription() {
    return "Special plugin for controlling consumption from sensor readings";
  }

  public void setParameter(String parameter) {
    setURLPattern(parameter);
  }

  protected void checkExecutionTime(long executionTime) {
    if (executionTime > nextSensorUpdateTime) {
      try {
        updateSensors();
      } catch (RuntimeException re) {
        re.printStackTrace();
      }
      nextSensorUpdateTime = executionTime + SENSOR_UPDATE_INTERVAL;
    }
    super.checkExecutionTime(executionTime);
  }

  private void updateSensors() {
    for (Iterator keys = sensors.keySet().iterator(); keys.hasNext(); ) {
      String key = (String) keys.next();
      Sensor sensor = (Sensor) sensors.get(key);
      updateSensor(sensor);
    }
  }

  private void updateSensor(Sensor sensor) {
    try {
      sensor.reading = readSensor(sensor.url);
      System.out.println("Sensor " + sensor);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private double readSensor(URL url) throws IOException {
    URLConnection conn = url.openConnection();
    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line = reader.readLine();
    reader.close();
    return Double.parseDouble(line);
  }
}
