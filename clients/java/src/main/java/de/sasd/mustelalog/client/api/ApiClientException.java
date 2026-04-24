package de.sasd.mustelalog.client.api;

public final class ApiClientException extends Exception {
    private final int statusCode;
    private final String errorCode;
    private final String requestId;
    private final String responseBody;

    public ApiClientException(String message) { this(message, 0, "", "", "", null); }
    public ApiClientException(String message, Throwable cause) { this(message, 0, "", "", "", cause); }
    public ApiClientException(String message, int statusCode, String errorCode, String requestId, String responseBody) {
        this(message, statusCode, errorCode, requestId, responseBody, null);
    }

    public ApiClientException(String message, int statusCode, String errorCode, String requestId, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode == null ? "" : errorCode;
        this.requestId = requestId == null ? "" : requestId;
        this.responseBody = responseBody == null ? "" : responseBody;
    }

    public int getStatusCode() { return statusCode; }
    public String getErrorCode() { return errorCode; }
    public String getRequestId() { return requestId; }
    public String getResponseBody() { return responseBody; }
}
