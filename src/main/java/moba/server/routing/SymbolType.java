package moba.server.routing;

import moba.server.routing.nodes.Direction;

public enum SymbolType {
    END              (Direction.TOP),
    STRAIGHT         (Direction.TOP | Direction.BOTTOM),
    RIGHT_SWITCH     (Direction.TOP | Direction.BOTTOM | Direction.TOP_RIGHT),
    CROSS_OVER_SWITCH(Direction.TOP | Direction.BOTTOM | Direction.TOP_RIGHT | Direction.BOTTOM_LEFT),
    LEFT_SWITCH      (Direction.TOP | Direction.BOTTOM | Direction.TOP_LEFT),
    THREE_WAY_SWITCH (Direction.TOP | Direction.BOTTOM | Direction.TOP_LEFT | Direction.TOP_RIGHT),
    CROSS_OVER       (Direction.TOP | Direction.BOTTOM | Direction.RIGHT | Direction.LEFT),
    BEND             (Direction.TOP | Direction.BOTTOM_LEFT);

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
