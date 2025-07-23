package moba.server.routing;

import moba.server.routing.nodes.Direction;

public enum SymbolType {
    END              (Direction.TOP.getDirection()),
    STRAIGHT         (Direction.TOP.getDirection() | Direction.BOTTOM.getDirection()),
    RIGHT_SWITCH     (Direction.TOP.getDirection() | Direction.BOTTOM.getDirection() | Direction.TOP_RIGHT.getDirection()),
    CROSS_OVER_SWITCH(Direction.TOP.getDirection() | Direction.BOTTOM.getDirection() | Direction.TOP_RIGHT.getDirection() | Direction.BOTTOM_LEFT.getDirection()),
    LEFT_SWITCH      (Direction.TOP.getDirection() | Direction.BOTTOM.getDirection() | Direction.TOP_LEFT.getDirection()),
    THREE_WAY_SWITCH (Direction.TOP.getDirection() | Direction.BOTTOM.getDirection() | Direction.TOP_LEFT.getDirection() | Direction.TOP_RIGHT.getDirection()),
    CROSS_OVER       (Direction.TOP.getDirection() | Direction.BOTTOM.getDirection() | Direction.RIGHT.getDirection() | Direction.LEFT.getDirection()),
    BEND             (Direction.TOP.getDirection() | Direction.BOTTOM_LEFT.getDirection());

    private final int weight;

    SymbolType(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public static SymbolType fromId(int id) {
        for(SymbolType type : values()) {
            if(type.weight == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown direction id [" + id + "].");
    }
}
