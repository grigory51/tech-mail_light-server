package ru.techMail.LightServer.exceptions;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class SettingsException extends AbstractException {
    public static final int PARAMETER_REQUIRED = 1;
    public static final int INVALID_CONFIG_FORMAT = 2;
    public static final int CONFIG_FILE_NOT_READABLE = 3;
    public static final int INVALID_PARAMETER = 4;

    private static final String[] exceptionDescription = {"Unknown problem", "Parameter required", "Invalid config format", "Config file not readable", "Invalid parameter"};

    private final String additionalInfo;
    private final int problemCode;

    public SettingsException(int problemCode, String additionalInfo) {
        this.additionalInfo = additionalInfo;
        this.problemCode = problemCode;
    }

    @Override
    public String toString() {
        return "SettingsException. " + exceptionDescription[problemCode] + ": " + additionalInfo;
    }
}
