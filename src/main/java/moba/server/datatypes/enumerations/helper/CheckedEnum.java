package moba.server.datatypes.enumerations.helper;

import com.google.common.base.Enums;
import moba.server.datatypes.enumerations.SystemError;
import moba.server.utilities.exceptions.SystemErrorException;

public class CheckedEnum {

    public static <T extends Enum<T>> T getFromString(Class<T> enumClass, String value)
    throws SystemErrorException {
        if(value == null) {
            throw new SystemErrorException(
                SystemError.FAULTY_MESSAGE,
                "null-value for enum <" + enumClass.getName() + "> given."
            );
        }

        var e = Enums.getIfPresent(enumClass, value).orNull();

        if(e == null) {
            throw new SystemErrorException(
                SystemError.FAULTY_MESSAGE,
                "unknown value <" + value + "> of enum <" + enumClass.getName() + ">."
            );
        }
        return e;
    }
}
