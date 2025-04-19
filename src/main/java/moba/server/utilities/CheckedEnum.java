package moba.server.utilities;

import com.google.common.base.Enums;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.utilities.exceptions.ClientErrorException;

public class CheckedEnum {

    public static <T extends Enum<T>> T getFromString(Class<T> enumClass, String value)
    throws ClientErrorException {
        if(value == null) {
            throw new ClientErrorException(
                ClientError.FAULTY_MESSAGE,
                "null-value for enum <" + enumClass.getName() + "> given."
            );
        }

        var e = Enums.getIfPresent(enumClass, value).orNull();

        if(e == null) {
            throw new ClientErrorException(
                ClientError.FAULTY_MESSAGE,
                "unknown value <" + value + "> of enum <" + enumClass.getName() + ">."
            );
        }
        return e;
    }
}
