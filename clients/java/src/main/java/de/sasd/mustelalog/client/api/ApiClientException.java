package de.sasd.mustelalog.client.api;

/**
 * Exception raised for API communication or protocol problems.
 */
public final class ApiClientException extends Exception
{
    public ApiClientException(String message) { super(message); }
    public ApiClientException(String message, Throwable cause) { super(message, cause); }
}
