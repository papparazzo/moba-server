package moba.server.datatypes.enumerations;

public enum ActionType {
    DELAY,

    LOCO_HALT,
    LOCO_SPEED,
    LOCO_DIRECTION_BACKWARD,
    LOCO_DIRECTION_FORWARD,

    LOCO_FUNCTION_ON,
    LOCO_FUNCTION_OFF,
    LOCO_FUNCTION_TRIGGER,
    SWITCHING_RED,              // Weiche rund / Signal rot schalten
    SWITCHING_GREEN,            // Weiche gerade / Signal grün schalten

 // FIXME Werden diese Dinge auch durch Kontakte ausgelöst? Ja! Knopfdruckaktionen!

 //   FUNCTION_ON,
 //   FUNCTION_OFF,
 //   FUNCTION_TRIGGER,

    // SEND_PUSH_TRAIN,
    SEND_ROUTE_SWITCHED,
    SEND_ROUTE_RELEASED,
    SEND_BLOCK_RELEASED
};