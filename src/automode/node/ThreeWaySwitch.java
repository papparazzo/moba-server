/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2019 Stefan Paproth <pappi-@gmx.de>
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

package automode.node;


public class ThreeWaySwitch implements NodeI {

    protected NodeI in;
    protected NodeI outStraight;
    protected NodeI outBendMinor;
    protected NodeI outBendMajor;

    protected SwitchState currentState;

    public enum SwitchState {
        BEND_MINOR,
        STRAIGHT_1,
        BEND_MAJOR,
        STRAIGHT_2,
    };

    public ThreeWaySwitch(NodeI in, SwitchState state) {
        this.in = in;
        currentState = state;
    }

    public void setOutStraight(NodeI node) {
        outStraight = node;
    }

    public void setOutBendMinor(NodeI node) {
        outBendMinor = node;
    }

    public void setOutBendMajor(NodeI node) {
        outBendMajor = node;
    }

    public boolean turnSwitch(SwitchState state) {
        if(state == currentState) {
            return false;
        }
        currentState = state;
        return true;
    }

    public SwitchState turnSwitch() {
        switch(currentState) {
            case BEND_MINOR:
                currentState = SwitchState.STRAIGHT_1;
                break;

            case STRAIGHT_1:
                currentState = SwitchState.BEND_MAJOR;
                break;

            case BEND_MAJOR:
                currentState = SwitchState.STRAIGHT_2;
                break;

            case STRAIGHT_2:
                currentState = SwitchState.BEND_MINOR;
                break;
        }
        return currentState;
    }

    @Override
    public NodeI getJunctionNode(NodeI node) throws NodeException {
        if(node != in && node != outStraight && node != outBendMinor && node != outBendMajor) {
            throw new NodeException("invalid node given!");
        }

        if(node == in && currentState == SwitchState.BEND_MAJOR) {
            return outBendMajor;
        }

        if(node == in && currentState == SwitchState.BEND_MINOR) {
            return outBendMinor;
        }

        if(node == in) {
            return outStraight;
        }

        if(node == outBendMajor && currentState == SwitchState.BEND_MAJOR) {
            return in;
        }

        if(node == outBendMinor && currentState == SwitchState.BEND_MINOR) {
            return in;
        }

        if(node == outStraight && currentState == SwitchState.STRAIGHT_1) {
            return in;
        }

        if(node == outStraight && currentState == SwitchState.STRAIGHT_2) {
            return in;
        }

        return null;
    }
}
