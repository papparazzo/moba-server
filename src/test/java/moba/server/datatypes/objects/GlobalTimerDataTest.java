/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2022 Stefan Paproth <pappi-@gmx.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.objects;

import moba.server.datatypes.base.Time;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.Day;
import moba.server.utilities.exceptions.ClientErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalTimerDataTest {

    private GlobalTimerData globalTimerData;

    @BeforeEach
    void setUp() {
        globalTimerData = new GlobalTimerData();
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(4, globalTimerData.getMultiplicator());
        assertNotNull(globalTimerData.getModelTime());
        assertEquals(0, globalTimerData.getNightStartTime());
        assertEquals(0, globalTimerData.getSunriseStartTime());
        assertEquals(0, globalTimerData.getDayStartTime());
        assertEquals(0, globalTimerData.getSunsetStartTime());
    }

    @Test
    void testSetAndGetModelTime() {
        PointInTime pointOfTime = new PointInTime();
        pointOfTime.setDay(Day.MONDAY);
        pointOfTime.setTime(new Time(720)); // 12:00

        globalTimerData.setModelTime(pointOfTime);
        
        PointInTime retrievedTime = globalTimerData.getModelTime();
        assertEquals(Day.MONDAY, retrievedTime.getDay());
        assertEquals(720, retrievedTime.getTime().getTimeInMinutes());
    }

    @Test
    void testSetTick() {
        // Setup initial state
        PointInTime pointOfTime = new PointInTime();
        pointOfTime.setDay(Day.MONDAY);
        pointOfTime.setTime(new Time(1436)); // 23:56
        globalTimerData.setModelTime(pointOfTime);
        
        // Test tick that should change the day
        boolean dayChanged = globalTimerData.setTick();
        assertTrue(dayChanged);
        assertEquals(Day.TUESDAY, globalTimerData.getModelTime().getDay());
        
        // Reset and test tick that should not change the day
        pointOfTime = new PointInTime();
        pointOfTime.setDay(Day.MONDAY);
        pointOfTime.setTime(new Time(720)); // 12:00
        globalTimerData.setModelTime(pointOfTime);
        
        dayChanged = globalTimerData.setTick();
        assertFalse(dayChanged);
        assertEquals(Day.MONDAY, globalTimerData.getModelTime().getDay());
    }

    @Test
    void testSetAndGetMultiplicator() {
        globalTimerData.setMultiplicator(2);
        assertEquals(2, globalTimerData.getMultiplicator());
        
        globalTimerData.setMultiplicator(1);
        assertEquals(1, globalTimerData.getMultiplicator());
        
        globalTimerData.setMultiplicator(4);
        assertEquals(4, globalTimerData.getMultiplicator());
    }

    @Test
    void testInvalidMultiplicator() {
        assertThrows(IllegalArgumentException.class, () -> globalTimerData.setMultiplicator(0));
        assertThrows(IllegalArgumentException.class, () -> globalTimerData.setMultiplicator(5));
        assertThrows(IllegalArgumentException.class, () -> globalTimerData.setMultiplicator(-1));
    }

    @Test
    void testSetAndGetDayPhases() {
        // Test night start time
        globalTimerData.setNightStartTime(1320); // 22:00
        assertEquals(1320, globalTimerData.getNightStartTime());

        // Test sunrise start time
        globalTimerData.setSunriseStartTime(240); // 04:00
        assertEquals(240, globalTimerData.getSunriseStartTime());

        // Test day start time
        globalTimerData.setDayStartTime(300); // 05:00
        assertEquals(300, globalTimerData.getDayStartTime());

        // Test sunset start time
        globalTimerData.setSunsetStartTime(1260); // 21:00
        assertEquals(1260, globalTimerData.getSunsetStartTime());
    }

    @Test
    void testValidDayPhases() throws ClientErrorException {
        // Set valid day phases
        globalTimerData.setNightStartTime(1320); // 22:00
        globalTimerData.setSunriseStartTime(240); // 04:00
        globalTimerData.setDayStartTime(300); // 05:00
        globalTimerData.setSunsetStartTime(1260); // 21:00

        // This should not throw an exception
        globalTimerData.validate();
    }

    @Test
    void testInvalidDayPhases() {
        // Test sunrise after day
        globalTimerData.setNightStartTime(1320); // 22:00
        globalTimerData.setSunriseStartTime(360); // 06:00
        globalTimerData.setDayStartTime(300); // 05:00
        globalTimerData.setSunsetStartTime(1260); // 21:00

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> globalTimerData.validate());
        assertEquals(ClientError.INVALID_VALUE_GIVEN, exception.getErrorId());
        
        // Test day after sunset
        globalTimerData.setSunriseStartTime(240); // 04:00
        globalTimerData.setDayStartTime(1320); // 22:00
        globalTimerData.setSunsetStartTime(1260); // 21:00

        exception = assertThrows(ClientErrorException.class, () -> globalTimerData.validate());
        assertEquals(ClientError.INVALID_VALUE_GIVEN, exception.getErrorId());
        
        // Test sunset after night
        globalTimerData.setDayStartTime(300); // 05:00
        globalTimerData.setSunsetStartTime(1380); // 23:00
        globalTimerData.setNightStartTime(1320); // 22:00

        exception = assertThrows(ClientErrorException.class, () -> globalTimerData.validate());
        assertEquals(ClientError.INVALID_VALUE_GIVEN, exception.getErrorId());
    }

    @Test
    void testFromJsonObject() throws ClientErrorException {
        // Create a valid JSON object
        Map<String, Object> jsonObject = new HashMap<>();
        
        Map<String, Object> modelTime = new HashMap<>();
        modelTime.put("day", "MONDAY");
        modelTime.put("time", 720L);
        
        jsonObject.put("modelTime", modelTime);
        jsonObject.put("multiplicator", 2L);
        jsonObject.put("nightStartTime", 1320L);
        jsonObject.put("sunriseStartTime", 240L);
        jsonObject.put("dayStartTime", 300L);
        jsonObject.put("sunsetStartTime", 1260L);
        
        // Parse the JSON object
        globalTimerData.fromJsonObject(jsonObject);
        
        // Verify the parsed data
        assertEquals(Day.MONDAY, globalTimerData.getModelTime().getDay());
        assertEquals(720, globalTimerData.getModelTime().getTime().getTimeInMinutes());
        assertEquals(2, globalTimerData.getMultiplicator());
        assertEquals(1320, globalTimerData.getNightStartTime());
        assertEquals(240, globalTimerData.getSunriseStartTime());
        assertEquals(300, globalTimerData.getDayStartTime());
        assertEquals(1260, globalTimerData.getSunsetStartTime());
    }

    @Test
    void testFromJsonObjectWithInvalidData() {
        // Create a JSON object with invalid day phases
        Map<String, Object> jsonObject = new HashMap<>();
        
        Map<String, Object> modelTime = new HashMap<>();
        modelTime.put("day", "MONDAY");
        modelTime.put("time", 720L);
        
        jsonObject.put("modelTime", modelTime);
        jsonObject.put("multiplicator", 2L);
        jsonObject.put("nightStartTime", 1320L);
        jsonObject.put("sunriseStartTime", 360L); // 06:00
        jsonObject.put("dayStartTime", 300L); // 05:00 (before sunrise)
        jsonObject.put("sunsetStartTime", 1260L);
        
        // Parse the JSON object should throw an exception
        ClientErrorException exception = assertThrows(ClientErrorException.class, 
            () -> globalTimerData.fromJsonObject(jsonObject));
        assertEquals(ClientError.INVALID_VALUE_GIVEN, exception.getErrorId());
    }

    @Test
    void testFromJsonObjectWithInvalidMultiplicator() {
        // Create a JSON object with an invalid multiplicator
        Map<String, Object> jsonObject = new HashMap<>();
        
        Map<String, Object> modelTime = new HashMap<>();
        modelTime.put("day", "MONDAY");
        modelTime.put("time", 720L);
        
        jsonObject.put("modelTime", modelTime);
        jsonObject.put("multiplicator", 5L); // Invalid value
        jsonObject.put("nightStartTime", 1320L);
        jsonObject.put("sunriseStartTime", 240L);
        jsonObject.put("dayStartTime", 300L);
        jsonObject.put("sunsetStartTime", 1260L);
        
        // Parse the JSON object should throw an exception
        assertThrows(IllegalArgumentException.class, 
            () -> globalTimerData.fromJsonObject(jsonObject));
    }
}
