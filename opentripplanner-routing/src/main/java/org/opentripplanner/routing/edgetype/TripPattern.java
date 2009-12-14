/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

/* This program is free software: you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public License
as published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.opentripplanner.routing.edgetype;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.routing.edgetype.factory.TripOvertakingException;

public final class TripPattern implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * Represents a class of trips distinguished by service id, list of stops, dwell time at stops,
     * and running time between stops.
     */

    public Trip exemplar;

    private Vector<Integer>[] departureTimes;

    private Vector<Integer>[] runningTimes;

    private Vector<Integer>[] arrivalTimes;

    private Vector<Integer>[] dwellTimes;

    private Vector<Boolean>[] wheelchairAccessibles;

    @SuppressWarnings("unchecked")
    public TripPattern(Trip exemplar, List<StopTime> stopTimes) {
        this.exemplar = exemplar;
        int hops = stopTimes.size() - 1;
        departureTimes = (Vector<Integer>[]) Array.newInstance(Vector.class, hops);
        runningTimes = (Vector<Integer>[]) Array.newInstance(Vector.class, hops);
        dwellTimes = (Vector<Integer>[]) Array.newInstance(Vector.class, hops);
        arrivalTimes = (Vector<Integer>[]) Array.newInstance(Vector.class, hops);
        wheelchairAccessibles = (Vector<Boolean>[]) Array.newInstance(Vector.class, hops + 1);

        int i;
        for (i = 0; i < hops; ++i) {
            departureTimes[i] = new Vector<Integer>();
            runningTimes[i] = new Vector<Integer>();
            dwellTimes[i] = new Vector<Integer>();
            arrivalTimes[i] = new Vector<Integer>();
            wheelchairAccessibles[i] = new Vector<Boolean>();            
        }
        wheelchairAccessibles[i] = new Vector<Boolean>();
    }

    public void removeHop(int stopindex, int hop) {
        runningTimes[stopindex].removeElementAt(hop);
        departureTimes[stopindex].removeElementAt(hop);
        dwellTimes[stopindex].removeElementAt(hop);
        arrivalTimes[stopindex].removeElementAt(hop);
        wheelchairAccessibles[stopindex].removeElementAt(hop);
    }

    public int addHop(int stopIndex, int insertionPoint, int departureTime, int runningTime,
            int arrivalTime, int dwellTime, boolean wheelchairAccessible) {
        Vector<Integer> stopRunningTimes = runningTimes[stopIndex];
        Vector<Integer> stopDepartureTimes = departureTimes[stopIndex];
        Vector<Integer> stopArrivalTimes = arrivalTimes[stopIndex];
        Vector<Integer> stopDwellTimes = dwellTimes[stopIndex];
        Vector<Boolean> stopWheelchairAccessibles = wheelchairAccessibles[stopIndex];
        // throw an exception when this departure time is not between the departure times it
        // should be between, indicating a trip that overtakes another.

        if (insertionPoint > 0) {
            if (stopDepartureTimes.elementAt(insertionPoint - 1) > departureTime) {
                throw new TripOvertakingException();
            }
        }
        if (insertionPoint < stopDepartureTimes.size()) {
            if (stopDepartureTimes.elementAt(insertionPoint) < departureTime) {
                throw new TripOvertakingException();
            }
        }
        stopDepartureTimes.insertElementAt(departureTime, insertionPoint);
        stopRunningTimes.insertElementAt(runningTime, insertionPoint);
        stopArrivalTimes.insertElementAt(arrivalTime, insertionPoint);
        stopDwellTimes.insertElementAt(dwellTime, insertionPoint);
        stopWheelchairAccessibles.insertElementAt(wheelchairAccessible, insertionPoint);
        return insertionPoint;
    }

    public int getNextPattern(int stopIndex, int afterTime, boolean wheelchairAccessible) {
        Vector<Integer> stopDepartureTimes = departureTimes[stopIndex];
        int index = Collections.binarySearch(stopDepartureTimes, afterTime);
        if (index == -stopDepartureTimes.size() - 1)
            return -1;

        if (index < 0) {
            index = -index - 1;
        }

	if (wheelchairAccessible) {
	    Vector<Boolean> stopWheelchairAccessibles = wheelchairAccessibles[stopIndex];
	    while (!stopWheelchairAccessibles.get(index)) {
		index ++;
		if (index == stopWheelchairAccessibles.size()) {
		    return -1;
		}
	    }
	}
	return index;
    }

    public int getRunningTime(int stopIndex, int pattern) {
        Vector<Integer> stopRunningTimes = runningTimes[stopIndex];
        return stopRunningTimes.get(pattern);
    }

    public int getDepartureTime(int stopIndex, int pattern) {
        Vector<Integer> stopDepartureTimes = departureTimes[stopIndex];
        return stopDepartureTimes.get(pattern);
    }

    public int getDepartureTimeInsertionPoint(int departureTime) {
        Vector<Integer> stopDepartureTimes = departureTimes[0];
        int index = Collections.binarySearch(stopDepartureTimes, departureTime);
        return -index - 1;
    }

    public int getPreviousPattern(int stopIndex, int beforeTime, boolean wheelchairAccessible) {
        Vector<Integer> stopArrivalTimes = arrivalTimes[stopIndex];
        int index = Collections.binarySearch(stopArrivalTimes, beforeTime);
        if (index == -1)
            return -1;

        if (index < 0) {
            index = -index - 2;
        }


	if (wheelchairAccessible) {
	    Vector<Boolean> stopWheelchairAccessibles = wheelchairAccessibles[stopIndex + 1];
	    while (!stopWheelchairAccessibles.get(index)) {
		index --;
		if (index == -1) {
		    return -1;
		}
	    }
	}
	return index;
    }

    public int getArrivalTime(int stopIndex, int pattern) {
        Vector<Integer> stopArrivalTimes = arrivalTimes[stopIndex];
        return stopArrivalTimes.get(pattern);
    }

    public int getDwellTime(int stopIndex, int pattern) {
        Vector<Integer> stopDwellTimes = dwellTimes[stopIndex];
        return stopDwellTimes.get(pattern);
    }

    public void setDwellTime(int stopIndex, int pattern, int dwellTime) {
        Vector<Integer> stopDwellTimes = dwellTimes[stopIndex];
        stopDwellTimes.set(pattern, dwellTime);
    }


    public Vector<Integer> getDepartureTimes(int stopIndex) {
        return departureTimes[stopIndex];
    }

    public boolean getWheelchairAccessible(int stopIndex, int pattern) {
        Vector<Boolean> stopWheelchairAcessibles = wheelchairAccessibles[stopIndex];
        return stopWheelchairAcessibles.get(pattern);
    }

    public void setWheelchairAccessible(int stopIndex, int pattern, boolean wheelchairAccessible) {
        Vector<Boolean> stopWheelchairAccessibles = wheelchairAccessibles[stopIndex];
        if (pattern > stopWheelchairAccessibles.size()) {
            throw new RuntimeException("Pattern index out of bounds: " + pattern + " / " + stopWheelchairAccessibles.size());
        }
        if (pattern == stopWheelchairAccessibles.size() || true) {
            stopWheelchairAccessibles.insertElementAt(wheelchairAccessible, pattern);
        } else {
            stopWheelchairAccessibles.set(pattern, wheelchairAccessible);
        }
    }
}
