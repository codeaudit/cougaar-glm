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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.LayoutManager;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Date;

public class TimeGUI extends JPanel implements ActionListener, Runnable {
    public static interface Listener {
        void timePlay();
        void timePause();
        void timeStop();
    }

    public static final int STOP = 0;
    public static final int PLAY = 1;
    public static final int PAUSE = 2;

  private static final int NROWS = 6;
  private static final int NCOLUMNS = 7;
  private static final int COLUMN_WIDTH = 16;
  private static final int ROW_HEIGHT = 12;
  private static final int XM = 4;
  private static final int YM = 4;
  private static final int CALENDAR_WIDTH = NCOLUMNS * COLUMN_WIDTH;
  private static final int CALENDAR_HEIGHT = NROWS * ROW_HEIGHT;
  private static final int CLOCK_WIDTH = CALENDAR_HEIGHT;
  private static final int CLOCK_HEIGHT = CALENDAR_HEIGHT;
  private static final int TOTAL_WIDTH = CALENDAR_WIDTH + CLOCK_WIDTH + 2*XM;
  private static final int BUTTON_Y = CALENDAR_HEIGHT + ROW_HEIGHT + YM;
  private static final int BUTTON_HEIGHT = 25;
  private static final int BUTTON_GAP = 2;
  private static final int TOTAL_HEIGHT = BUTTON_Y + BUTTON_HEIGHT + YM;
  private static final int HUB_RADIUS         = CLOCK_WIDTH * 1 / 32;
  private static final int HOUR_HAND_RADIUS   = CLOCK_WIDTH * 8 / 32;
  private static final int MINUTE_HAND_RADIUS = CLOCK_WIDTH * 11 / 32;
  private static final int SECOND_HAND_RADIUS = CLOCK_WIDTH * 14 / 32;
  private static final int TICK_LENGTH = 3;
  private static final TimeZone timeZone = TimeZone.getTimeZone("GMT");
  private static double[] sinTable = new double[60];
  private static double[] cosTable = new double[60];
  static {
    for (int i = 0; i < sinTable.length; i++) {
      sinTable[i] = Math.sin(Math.PI * i / 30.0);
    }
    for (int i = 0; i < cosTable.length; i++) {
      cosTable[i] = Math.cos(Math.PI * i / 30.0);
    }
  }
  private GregorianCalendar calendar = new GregorianCalendar(timeZone);
  private double theRate;             // The current rate
  private long lastNow;               // The start of this timing segment
  private long theOffset;             // The offset
  private Thread thread;
    
    private JButton playButton = new JButton(DrawnIcon.getPlayIcon(true));
    private JButton pauseButton = new JButton(DrawnIcon.getPauseIcon(true));
    private JButton stopButton = new JButton(DrawnIcon.getStopIcon(true));
    private int state = STOP;
    private Listener listener;

    public TimeGUI(Listener l) {
        super((LayoutManager) null);
        listener = l;
        playButton = new JButton(DrawnIcon.getPlayIcon(true));
        playButton.setDisabledIcon(DrawnIcon.getPlayIcon(false));
    
        pauseButton = new JButton("", DrawnIcon.getPauseIcon(true));
        stopButton = new JButton("", DrawnIcon.getStopIcon(true));
        pauseButton.setDisabledIcon(DrawnIcon.getPauseIcon(false));
        stopButton.setDisabledIcon(DrawnIcon.getStopIcon(false));
        disableControls();
        JButton[] buttons = new JButton[] {
            playButton,
            pauseButton,
            stopButton
        };
        int buttonWidth = (buttons.length - 1) * BUTTON_GAP;
        for (int i = 0; i < buttons.length; i++) {
            JButton b = buttons[i];
            buttonWidth += b.getPreferredSize().width;
        }
        int bx = (TOTAL_WIDTH - buttonWidth) / 2;
        for (int i = 0; i < buttons.length; i++) {
            JButton b = buttons[i];
//              b.setMargin(new Insets(0, 0, 0, 0));
//              b.setVerticalAlignment(JButton.CENTER);
//              b.setHorizontalAlignment(JButton.CENTER);
            b.addActionListener(this);
            Dimension sz = b.getPreferredSize();
            int by = BUTTON_Y + (BUTTON_HEIGHT - sz.height) / 2;
            b.setSize(sz);
            b.setLocation(bx, by);
            add(b);
            bx += sz.width + BUTTON_GAP;
        }
        clockFormat.setTimeZone(timeZone);
        noSecondsClockFormat.setTimeZone(timeZone);
        noMinutesClockFormat.setTimeZone(timeZone);
        noHoursClockFormat.setTimeZone(timeZone);
    }

    public void disableControls() {
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        state = STOP;
    }

    public void enableControls() {
        state = STOP;
        playButton.setEnabled(true);
    }
        
    public void actionPerformed(ActionEvent e) {
        Object button = e.getSource();
        if (button == playButton) {
            if (state != PLAY) {
                listener.timePlay();
            }
            return;
        }
        if (button == pauseButton) {
            if (state == PLAY) {
                listener.timePause();
            } else if (state == PAUSE) {
                listener.timePlay();
            }
            return;
        }
        if (button == stopButton) {
            if (state != STOP) {
                listener.timeStop();
            }
            return;
        }
    }
        
    public void play() {
        state = PLAY;
        playButton.setEnabled(false);
        stopButton.setEnabled(true);
        pauseButton.setEnabled(true);
    }

    public void pause() {
        if (state == PLAY) {
            state = PAUSE;
            playButton.setEnabled(true);
        } else if (state == PAUSE) {
            state = PLAY;
            playButton.setEnabled(false);
        }
    }
    public void stop() {
        if (state != STOP) {
            state = STOP;
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

  public void setTime(long newTime) {
    synchronized (calendar) {
      int oldDay = calendar.get(Calendar.DAY_OF_YEAR);
      int oldYear = calendar.get(Calendar.YEAR);
      calendar.setTime(new Date(newTime));
      int newDay = calendar.get(Calendar.DAY_OF_YEAR);
      int newYear = calendar.get(Calendar.YEAR);
      if (oldDay != newDay || oldYear != newYear) {
        repaint(0, 0, TOTAL_WIDTH, TOTAL_HEIGHT);
      } else {
        repaint(CALENDAR_WIDTH, 0, CLOCK_WIDTH, CLOCK_HEIGHT);
      }
    }
    repaint(100);
  }

  public synchronized void setTime(long newTime, double newRate) {
    lastNow = System.currentTimeMillis();
    theRate = newRate;
    theOffset = newTime;
//      if (newRate == 0.0) {
//        rateString = " (Paused)";
//      } else if (newRate != 1.0) {
//        rateString = " (Rate " + newRate + ")";
//      } else {
//        rateString = " (Running)";
//      }
    if (thread == null) {
      thread = new Thread(this, "TimeGUI");
      thread.start();
    }
    notify();
  }

  public synchronized void run() {
    while (true) {
      long now = System.currentTimeMillis();
      long executionTime = (long) ((now - lastNow) * theRate + theOffset);
      setTime(executionTime);
      long nextExecutionTime = executionTime + 1000; // One second later
      long nextNow = (long) (lastNow + (nextExecutionTime - theOffset) / theRate);
      long delay = nextNow - now;
      if (delay < 100) delay = 100;
      if (delay > 10000) delay = 10000;
      try {
        wait(delay);
      } catch (InterruptedException ie) {
      }
    }
  }

  private Dimension size = new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT);

  public Dimension getPreferredSize() {
    return size;
  }

  public Dimension getMinSize() {
    return size;
  }

  public Dimension getMaxSize() {
    return size;
  }

  private boolean noMonths;
  private boolean noDays;
  private boolean noAMPM;
  private boolean noHours;
  private boolean noMinutes;
  private boolean noSeconds;
  private int hour24;
  private int hour;
  private int minute;
  private int second;
  private int year;
  private int month;            // Zero-based month index
  private int dayOfMonth;
  private int daysInMonth;
  private int dayOfWeekOfFirstDay;
  private int minDayOfWeek;
  private String digitalClock;
  private DateFormat clockFormat = new SimpleDateFormat("HH:mm:ss");
  private DateFormat noSecondsClockFormat = new SimpleDateFormat("HH:mm:00");
  private DateFormat noMinutesClockFormat = new SimpleDateFormat("HH:00:00");
  private DateFormat noHoursClockFormat = new SimpleDateFormat("00:00:00");
  private GregorianCalendar tempCalendar = new GregorianCalendar(timeZone);

  private Font font = null;
  private FontMetrics fm;
  private int baseline = 0;

  public void paintComponent(Graphics g) {
    Rectangle bounds = g.getClipBounds();
    boolean paintCalendar = bounds.x < XM + CALENDAR_WIDTH;
    g.setColor(getBackground());
    if (font == null) {
      font = new Font("Monospaced", Font.PLAIN, 12);
      fm = g.getFontMetrics(font);
      baseline = fm.getAscent() - 1;
    }
    g.setFont(font);
    g.fillRect(0, 0, TOTAL_WIDTH, TOTAL_HEIGHT);
    synchronized (calendar)  {
      noSeconds = theRate >                                   120.0; // No more than 2 revs/sec
      noMinutes = theRate >                            60.0 * 120.0; // No more than 2 revs/sec
      noHours   = theRate >                     12.0 * 60.0 * 120.0; // No more than 2 revs/sec
      noAMPM    = theRate >               2.0 * 12.0 * 60.0 * 120.0; // No more than 4 blinks/sec
      noDays    = theRate >        30.0 * 2.0 * 12.0 * 60.0 * 120.0; // No more than 60 days/sec
      noMonths  = theRate > 12.0 * 30.0 * 2.0 * 12.0 * 60.0 * 120.0; // No more than 24 months/sec
      if (noHours) {
        digitalClock = noHoursClockFormat.format(calendar.getTime());
      } else if (noMinutes) {
        digitalClock = noMinutesClockFormat.format(calendar.getTime());
      } else if (noSeconds) {
        digitalClock = noSecondsClockFormat.format(calendar.getTime());
      } else {
        digitalClock = clockFormat.format(calendar.getTime());
      }
      hour = calendar.get(Calendar.HOUR);
      hour24 = calendar.get(Calendar.HOUR_OF_DAY);
      minute = calendar.get(Calendar.MINUTE);
      second = calendar.get(Calendar.SECOND);
      year = calendar.get(Calendar.YEAR);
      int newMonth = calendar.get(Calendar.MONTH) - calendar.getActualMinimum(Calendar.MONTH);
      if (newMonth != month) {
        month = newMonth;
        tempCalendar.setTime(calendar.getTime());
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        dayOfWeekOfFirstDay =
          (tempCalendar.get(Calendar.DAY_OF_WEEK)
           - tempCalendar.getActualMinimum(Calendar.DAY_OF_WEEK));
        minDayOfWeek = tempCalendar.getActualMinimum(Calendar.DAY_OF_WEEK);
      }
      dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
      daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
      if (paintCalendar) drawCalendar(g);
      if (!noAMPM) drawClock(g);
    }
  }

  private static final String[] MONTH_NAMES = {
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  };
    
  private void drawCalendar(Graphics g) {
    g.setColor(Color.black);
    String title;
    if (noMonths) {
      title = Integer.toString(year);
    } else {
      title = MONTH_NAMES[month] + " " + year;
    }
    int tx = XM + (CALENDAR_WIDTH - fm.stringWidth(title)) / 2;
    g.drawString(title, tx, baseline);
    if (!noDays) {
      for (int day = 1; day <= daysInMonth; day++) {
        int ix = dayOfWeekOfFirstDay + day - 1;
        int row = ix / 7;
        int col = ix % 7;
        int x = XM + COLUMN_WIDTH * col;
        int y = YM + ROW_HEIGHT + ROW_HEIGHT * row;
        String dayString = "  ";
        if (day < 10) {
          dayString = " " + day;
        } else {
          dayString = "" + day;
        }
        if (day == dayOfMonth) {
          g.fillRect(x, y + 2, COLUMN_WIDTH, ROW_HEIGHT);
          g.setColor(Color.white);
          g.drawString(dayString, x, y + baseline);
          g.setColor(Color.black);
        } else {
          g.drawString(dayString, x, y + baseline);
        }
      }
    }
  }

  private Stroke stroke = new BasicStroke(2.0f);

  private void drawHand(Graphics g, int xc, int yc, int radius, int m) {
    Graphics2D g2d = (Graphics2D) g;
    int x = (int) (xc + radius * sinTable[m]);
    int y = (int) (yc - radius * cosTable[m]);
    g.drawLine(xc, yc, x, y);
  }
  private void drawClock(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
//      g2d.setStroke(stroke);
    g.setColor(Color.black);
    int xc = XM + CALENDAR_WIDTH + CLOCK_WIDTH / 2;
    int yc = YM + ROW_HEIGHT + CLOCK_HEIGHT / 2;
    if (!noHours) {
      g.drawString(digitalClock, xc - fm.stringWidth(digitalClock) / 2, baseline);
    }
    g.drawOval(xc - SECOND_HAND_RADIUS, yc - SECOND_HAND_RADIUS,
               2 * SECOND_HAND_RADIUS, 2 * SECOND_HAND_RADIUS);
    if (hour24 < 6 || hour24 >= 18) {
      g.fillOval(xc - SECOND_HAND_RADIUS, yc - SECOND_HAND_RADIUS,
                 2 * SECOND_HAND_RADIUS, 2 * SECOND_HAND_RADIUS);
      g.setColor(Color.white);
    }
    g.fillOval(xc - HUB_RADIUS, yc - HUB_RADIUS,
               2 * HUB_RADIUS + 1, 2 * HUB_RADIUS + 1);
    g.drawLine(xc, yc - SECOND_HAND_RADIUS, xc, yc - SECOND_HAND_RADIUS + TICK_LENGTH);
    g.drawLine(xc, yc + SECOND_HAND_RADIUS, xc, yc + SECOND_HAND_RADIUS - TICK_LENGTH);
    g.drawLine(xc + SECOND_HAND_RADIUS, yc, xc + SECOND_HAND_RADIUS - TICK_LENGTH, yc);
    g.drawLine(xc - SECOND_HAND_RADIUS, yc, xc - SECOND_HAND_RADIUS + TICK_LENGTH, yc);
    if (!noHours) {
      drawHand(g, xc, yc, HOUR_HAND_RADIUS, (hour * 60 + minute) / 12);
    }
    if (!noMinutes) {
      drawHand(g, xc, yc, MINUTE_HAND_RADIUS, minute);
    }
    if (!noSeconds) {
      drawHand(g, xc, yc, SECOND_HAND_RADIUS, second);
    }
  }
}
