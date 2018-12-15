/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2018 Stefan Paproth <pappi-@gmx.de>
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

public class CrossOverSwitch implements NodeI {

    protected NodeI inDiagonal;
    protected NodeI inVertical;
    protected NodeI outDiagonal;
    protected NodeI outVertical;

    protected SwitchState currentState;

    public enum SwitchState {
        BEND_1,
        STRAIGHT_1,
        BEND_2,
        STRAIGHT_2,
    };

    public CrossOverSwitch(SwitchState state) {
        currentState = state;
    }

    public void setInNodeDiagonal(NodeI node) {
        inDiagonal = node;
    }

    public void setInNodeVertical(NodeI node) {
        inVertical = node;
    }


    public void setOutNodeDiagonal(NodeI node) {
        outDiagonal = node;
    }

    public void setOutNodeVertical(NodeI node) {
        outVertical = node;
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
            case BEND_1:
                currentState = SwitchState.STRAIGHT_1;
                break;

            case STRAIGHT_1:
                currentState = SwitchState.BEND_2;
                break;

            case BEND_2:
                currentState = SwitchState.STRAIGHT_2;
                break;

            case STRAIGHT_2:
                currentState = SwitchState.BEND_1;
                break;
        }
        return currentState;
    }

    @Override
    public NodeI getJunctionNode(NodeI node) throws NodeException {
        if(node != inDiagonal && node != inVertical && node != outDiagonal && node != outVertical) {
            throw new NodeException("invalid node given!");
        }

        NodeI activeIn = getInNode();
        NodeI activeOut = getOutNode();

        if(node == activeIn) {
            return activeOut;
        }

        if(node == activeOut) {
            return activeIn;
        }

        return null;
    }

    protected NodeI getInNode() throws NodeException {
        switch(currentState) {
            case BEND_1:
            case BEND_2:
                return inDiagonal;

            case STRAIGHT_1:
            case STRAIGHT_2:
                return inVertical;
        }
        throw new NodeException("invalid state given!");
    }

    protected NodeI getOutNode() throws NodeException {
        switch(currentState) {
            case BEND_1:
            case STRAIGHT_1:
                return outDiagonal;

            case BEND_2:
            case STRAIGHT_2:
                return outVertical;
        }
        throw new NodeException("invalid state given!");
    }
}
