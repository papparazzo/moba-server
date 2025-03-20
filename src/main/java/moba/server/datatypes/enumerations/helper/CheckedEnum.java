package moba.server.datatypes.enumerations.helper;

import com.google.common.base.Enums;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.utilities.exceptions.ErrorException;

public class CheckedEnum {

    public static <T extends Enum<T>> T getFromString(Class<T> enumClass, String value)
    throws ErrorException {
        if(value == null) {
            throw new ErrorException(
                ErrorId.FAULTY_MESSAGE,
                "null-value for enum <" + enumClass.getName() + "> given."
            );
        }

        var e = Enums.getIfPresent(enumClass, value).orNull();

        if(e == null) {
            throw new ErrorException(
                ErrorId.FAULTY_MESSAGE,
                "unknown value <" + value + "> of enum <" + enumClass.getName() + ">."
            );
        }
        return e;
    }
}
