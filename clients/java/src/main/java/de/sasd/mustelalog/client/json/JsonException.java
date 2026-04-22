package de.sasd.mustelalog.client.json;

/**
 * Exception thrown for invalid JSON.
 */
public final class JsonException extends RuntimeException
{
    public JsonException(String message)
    {
        super(message);
    }
}
