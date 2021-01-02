package io.github.bluelhf.dapper.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DapperException extends RuntimeException {

    public DapperException(Throwable t) {
        super("An exception occurred in Dapper: " + t.getMessage() + ":\n" + Arrays.stream(t.getStackTrace()).map(element -> "  " + element.toString()).collect(Collectors.joining("\n")));
    }
}
