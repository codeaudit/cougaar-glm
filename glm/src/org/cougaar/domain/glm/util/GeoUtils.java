/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.util;

import java.lang.Math;
import org.cougaar.domain.planning.ldm.measure.Distance;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.glm.plan.*;

/**
 * A class containing static methods for geographic computations, 
 * particularly those related to great-circle distance calculations
 */
public class GeoUtils {

  // Circumference of earth in KM (around equator)
  public static final double EARTH_CIRCUMFERENCE = 40075.16;

  // Radius of earth in KM
  public static final double EARTH_RADIUS = 
      EARTH_CIRCUMFERENCE / (Math.PI * 2.0);


  /**
   * Compute great-circle distance (expressed as ALP Distance measure)
   * between two points on globe (expressed as ALP Position)
   * @param Position of first point
   * @param Position of second point
   * @return Distance of great-circle distance between points
   */
  public static Distance DistanceBetweenPositions(Position position1,
						  Position position2)
  {
    // Get distance as KM
    double distance = 
      DistanceBetweenPositions(position1.getLatitude().getDegrees(),
			       position1.getLongitude().getDegrees(),
			       position2.getLatitude().getDegrees(),
			       position2.getLongitude().getDegrees());

    // Return as Distance
    return Distance.newKilometers(distance);
  }

  /**
   * Compute great-circle distance in KM between two points on globe
   * expressed as latitude and longitude.
   * @param double latitude of first point (degrees)
   * @param double longitude of first point (degrees)
   * @param double latitude of second point (degrees)
   * @param double longitude of second point (degrees)
   * @return double great-circle distance between two points
   */
  public static double DistanceBetweenPositions(double latitude1,
						double longitude1, 
						double latitude2,
						double longitude2)
  {
    // Convert arguments to Radians
    double lon1_rad = Math.toRadians(longitude1);
    double lat1_rad = Math.toRadians(latitude1);
    double lon2_rad = Math.toRadians(longitude2);
    double lat2_rad = Math.toRadians(latitude2);

    // Convert to 3-D Cartesian coordinates (X,Y,Z with earth center at 0,0,0)
    double node_1_x = Math.cos(lat1_rad)*Math.cos(lon1_rad);
    double node_1_y = Math.cos(lat1_rad)*Math.sin(lon1_rad);
    double node_1_z = Math.sin(lat1_rad);

    double node_2_x = Math.cos(lat2_rad)*Math.cos(lon2_rad);
    double node_2_y = Math.cos(lat2_rad)*Math.sin(lon2_rad);
    double node_2_z = Math.sin(lat2_rad);

    // Calculate Cross-Product
    double cross_x = (node_1_y * node_2_z) - (node_1_z * node_2_y);
    double cross_y = (node_1_z * node_2_x) - (node_1_x * node_2_z);
    double cross_z = (node_1_x * node_2_y) - (node_1_y * node_2_x);

    // Calculate the length of the Cross-Product
    double norm_cross = 
      Math.sqrt((cross_x * cross_x) + 
		(cross_y * cross_y) + 
		(cross_z * cross_z));
    
    // Calculate the Dot-Product
    double dot_product = 
      (node_1_x * node_2_x) + (node_1_y * node_2_y) + (node_1_z * node_2_z); 

    // Calculate the central angle
    double angle = Math.atan2(norm_cross, dot_product);

    // Calculate the great-circle distance
    double distance = EARTH_RADIUS * angle;

    return distance;
  } 

  // Test procedure on great-circle calculations
  public static void main(String []args) 
  {
    System.out.println("GeoUtils...");

    // One degree longitude at equator should be ~111 KM.
    double one_degree_longitude_at_equator = 
      DistanceBetweenPositions(0.0, 0.0, 0.0, 1.0);
    System.out.println("One degree longitude at equator = " +
		       one_degree_longitude_at_equator);

    // One degree latitude at equator should be ~111 KM.
    double one_degree_latitude_at_equator = 
      DistanceBetweenPositions(0.0, 0.0, 1.0, 0.0);
    System.out.println("One degree latitude at equator = " +
		       one_degree_latitude_at_equator);

    // Distance between Boston and New York should be 183.9 mi (296 km)
    // Distance between NYC and LA should be 2464 mi (3943 km) 
    // (as the crow flies...).
    // Boston is 42-15'N, 71-07'W
    // NYC is 40.40'N, 73-58'W
    // LA is 34.03'N, 118.14'W
    double BOS_LAT = 42.0 +  (15.0/60.0);
    double BOS_LON = -(71.0 + (7.0/60.0));
    double NY_LAT = 40.0 + (40.0/60.0);
    double NY_LON = -(73.0 + (58.0/60.0));
    double LA_LAT = 34.0 + (3.0/60.0);
    double LA_LON = -(118.0 + (14.0/60.0));
    System.out.println("Distance between NYC and LA (km) = " + 
		       DistanceBetweenPositions(NY_LAT, NY_LON,
						LA_LAT, LA_LON));
    System.out.println("Distance between LA and NEW YORK (km) = " + 
		       DistanceBetweenPositions(LA_LAT, LA_LON, 
						NY_LAT, NY_LON));
    System.out.println("Distance between BOSTON and NEW YORK (km) = " + 
		       DistanceBetweenPositions(BOS_LAT, BOS_LON, 
						NY_LAT, NY_LON));
    Position BOSTON = 
      new org.cougaar.domain.glm.plan.PositionImpl
      (Latitude.newLatitude(BOS_LAT), Longitude.newLongitude(BOS_LON));
    Position NEW_YORK = 
      new org.cougaar.domain.glm.plan.PositionImpl
      (Latitude.newLatitude(NY_LAT), Longitude.newLongitude(NY_LON));
    Distance distance_between_BOS_and_NY = 
      DistanceBetweenPositions(BOSTON, NEW_YORK);
    System.out.println("Distance between BOSTON and NEW YORK (miles) = " + 
		       distance_between_BOS_and_NY.getMiles());

    
			       
  }
}
