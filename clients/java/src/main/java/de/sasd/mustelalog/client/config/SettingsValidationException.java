package de.sasd.mustelalog.client.config;

public final class SettingsValidationException extends Exception {
    public SettingsValidationException(String message) { super(message); }
    public SettingsValidationException(String message, Throwable cause) { super(message, cause); }
}
