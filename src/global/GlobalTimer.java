/*
 *  appserver2
 *
 *  Copyright (C) 2014 Stefan Paproth <pappi-@gmx.de>
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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package global;

import json.streamwriter.JSONStreamWriterStringBuilder;
import java.io.*;
import java.util.*;

import json.*;
import com.*;
import messages.*;

public class GlobalTimer extends Thread implements JSONToStringI {
    protected Dispatcher dispatcher      = null;
    protected long intervall             = 60;
    protected long multiplicator         = 60 * 60;
    protected long curModelTime          = 0;
    protected volatile boolean isRunning = false;

    protected long dimTime = 9 * 60 * 60;
    protected long brightTime = 21 * 60 * 60;

    public enum ColorTheme {
        BRIGHT,
		DIM
    };

    public GlobalTimer(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void startGlobalTimer() {
        this.isRunning = true;
        if(this.isAlive()) {
            return;
        }
        this.setName("globaltimer");
        this.start();
    }

    public void stopGlobalTimer() {
        this.isRunning = false;
    }

    public long getIntervall() {
        return this.intervall;
    }

    public long getMultiplicator() {
        return this.multiplicator;
    }

    public String getDimTimeString() {
        return this.getTimeAsString(this.dimTime);
    }

    public String getBrightTimeString() {
        return this.getTimeAsString(this.brightTime);
    }

    public void setMultiplicator(long multiplicator)
    throws GlobalTimerException {
        if(multiplicator > 3600) {
            multiplicator = 3600;
        }
        if(multiplicator % 15 != 0) {
            throw new GlobalTimerException(
                "modulo 15 check failed in multiplicator-setting"
            );
        }
        this.multiplicator = multiplicator;
    }

    public void setIntervall(long intervall)
    throws GlobalTimerException {
        if(intervall % 5 != 0) {
            throw new GlobalTimerException(
                "modulo 5 check failed in intervall-setting"
            );
        }
        this.intervall = intervall;
    }

    public boolean setModelTime(String modelTime) {
        String tokens[] = modelTime.split(" ");

        if(tokens.length != 2) {
            return false;
        }

        String day = tokens[0];

        long r;

        switch(day) {
            case "So":
                r = 0;
                break;

            case "Mo":
                r = 60 * 60 * 24;
                break;

            case "Di":
                r = 2 * 60 * 60 * 24;
                break;

            case "Mi":
                r = 3 * 60 * 60 * 24;
                break;

            case "Do":
                r = 4 * 60 * 60 * 24;
                break;

            case "Fr":
                r = 5 * 60 * 60 * 24;
                break;

            case "Sa":
                r = 6 * 60 * 60 * 24;
                break;

            default:
                return false;
        }

        tokens = tokens[1].split(":");

        r += Integer.parseInt(tokens[0]) * 60 * 60;
        r += Integer.parseInt(tokens[1]) * 60;

        this.curModelTime = r;
        return true;
    }

    public void setColorThemeChangeTimes(String dimTime, String brightTime) {
        String tokens[];

        tokens = dimTime.split(":");
        this.dimTime =
            Integer.parseInt(tokens[0]) * 60 * 60 +
            Integer.parseInt(tokens[1]) * 60;

        tokens = brightTime.split(":");
        this.brightTime =
            Integer.parseInt(tokens[0]) * 60 * 60 +
            Integer.parseInt(tokens[1]) * 60;
    }

    @Override
    public void run() {
        try {
            while(!isInterrupted()) {
                Thread.sleep(this.intervall * 1000);
                if(!this.isRunning) {
                    continue;
                }
                this.curModelTime = (
                    (this.curModelTime + this.intervall * this.multiplicator) % (60 * 60 * 24 * 7)
                );
                if(this.curModelTime % this.brightTime == 0) {
                    // FIXME: Is this really thread-save??
                    this.dispatcher.dispatch(
                        new Message(MessageType.COLOR_THEME_EVENT, ColorTheme.BRIGHT)
                    );
                } else if(this.curModelTime % this.dimTime == 0) {
                    // FIXME: Is this really thread-save??
                    this.dispatcher.dispatch(
                        new Message(MessageType.COLOR_THEME_EVENT, ColorTheme.DIM)
                    );
                }
                // FIXME: Is this really thread-save??
                this.dispatcher.dispatch(
                    new Message(MessageType.GLOBAL_TIMER_EVENT, this)
                );
            }
        } catch(InterruptedException e) {

        }
    }

    protected String getTimeAsString(long t) {
        t /= 60;
        long m = t % 60;
        t /= 60;
        long h = t % 24;

        StringBuilder sb = new StringBuilder();
        if(h < 10) {
            sb.append("0");
        }
        sb.append(h);
        sb.append(":");
        if(m < 10) {
            sb.append("0");
        }
        sb.append(m);
        return sb.toString();
    }

    public String getCurrentModelTime() {
        long f = this.curModelTime / 60;
        long m = f % 60;
        f /= 60;
        long h = f % 24;
        f /= 24;
        StringBuilder sb = new StringBuilder();
        switch((int)f) {
            case 0:
                sb.append("So ");
                break;

            case 1:
                sb.append("Mo ");
                break;

            case 2:
                sb.append("Di ");
                break;

            case 3:
                sb.append("Mi ");
                break;

            case 4:
                sb.append("Do ");
                break;

            case 5:
                sb.append("Fr ");
                break;

            case 6:
                sb.append("Sa ");
                break;
        }
        if(h < 10) {
            sb.append("0");
        }
        sb.append(h);
        sb.append(":");
        if(m < 10) {
            sb.append("0");
        }
        sb.append(m);
        return sb.toString();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("curModelTime",  this.getCurrentModelTime());
        map.put("intervall",     this.intervall);
        map.put("multiplicator", this.multiplicator);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}