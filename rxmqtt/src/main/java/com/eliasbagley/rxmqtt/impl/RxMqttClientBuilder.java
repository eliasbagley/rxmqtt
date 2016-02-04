package com.eliasbagley.rxmqtt.impl;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.eliasbagley.rxmqtt.utils.Strings;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import static com.eliasbagley.rxmqtt.constants.Constants.*;

//TODO message persistence for qos 1 and 2
//TODO add a buildAndConnect() method
public class RxMqttClientBuilder {
    @NonNull private String clientId; //TODO Use a default based on the device identifier
    @NonNull private String brokerUrl;
    @NonNull private String host;
    @NonNull private String port    = DEFAULT_PORT;
    @NonNull private String uriType = TCP;
    @Nullable private Will   will;
    @Nullable private String username;
    @Nullable private String password;

    // Default connection params
    private boolean cleanSession      = true;
    private int     keepAliveInterval = DEFAULT_KEEPALIVE;
    private int     timeout           = DEFAULT_TIMEOUT;

    //region builder methods

    @NonNull
    public RxMqttClientBuilder setHost(@NonNull String host) {
        this.host = host;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setPort(@NonNull String port) {
        this.port = port;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setClientId(@NonNull String clientId) {
        this.clientId = clientId;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setWill(@Nullable Will will) {
        this.will = will;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setKeepAliveInterval(int keepAliveInterval /* in seconds */) {
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setUriType(@NonNull String uriType) {
        this.uriType = uriType;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setCredentials(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    //endregion builder methods

    //region private methods

    @NonNull
    private MqttConnectOptions createConnectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(cleanSession);
        connectOptions.setConnectionTimeout(timeout);
        connectOptions.setKeepAliveInterval(keepAliveInterval);

        if (!Strings.isBlank(username) && !Strings.isBlank(password)) {
            connectOptions.setUserName(username);
            connectOptions.setPassword(password.toCharArray());
        }

        if (hasWill()) {
            connectOptions.setWill(will.getTopic(), will.getMessageBytes(), will.getQoS().getValue(), will.isRetained());
        }

        return connectOptions;
    }

    @NonNull
    private String createBrokerUrl() {
        return String.format("%s%s:%s", uriType, host, port);
    }

    private boolean hasWill() {
        return will != null;
    }

    //endregion


    @CheckResult
    @NonNull
    public RxMqttClient build() {
        this.brokerUrl = createBrokerUrl();

        ValidationResult validationResult = isValid();
        if (validationResult.isValid) {
            return new RxMqttAsyncClient(brokerUrl, clientId, null, createConnectOptions());
        } else {
            throw validationResult.getValidationException();
        }
    }

    //region validation

    @NonNull
    private ValidationResult isValid() {
        String validationErrors = "";

        if (brokerUrl == null) {
            validationErrors += "Broker URL cannot be null.\n";
            return failed(validationErrors);
        }

        if (!(brokerUrl.startsWith(TCP) || brokerUrl.startsWith(SSL))) {
            validationErrors += String.format("Broker URL \"%s\" must start with either %s or %s\n", brokerUrl, TCP, SSL);
        }

        if (!brokerUrl.matches("(.+):(\\d)+")) { // This pattern matches something like hostname.com:9999
            validationErrors += String.format("Broker URL \"%s\" must be in the form tcp://hostname:port or ssl://hostname:port. Example: tcp://example.com:5555\n", brokerUrl);
        }

        if (clientId == null) {
            validationErrors += "Missing clientId. Call setClientId() to set the clientId\n";
        }

        if (Strings.isBlank(username) != Strings.isBlank(password)) {
            validationErrors += "Username and password must either both be set, or both be unset.";
        }

        if (validationErrors.isEmpty()) {
            return new ValidationResult(true);
        } else {
            return failed(validationErrors);
        }
    }

    @NonNull
    private ValidationResult failed(String errors) {
        return new ValidationResult(false, new RuntimeException("Failed validating build():\n" + errors));
    }

    private static class ValidationResult {
        private           boolean          isValid;
        @Nullable private RuntimeException validationException;

        public ValidationResult(boolean isValid) {
            this(isValid, null);
        }

        public ValidationResult(boolean isValid, @Nullable RuntimeException exception) {
            this.isValid = isValid;
            this.validationException = exception;
        }

        public boolean isValid() {
            return isValid;
        }

        @Nullable
        public RuntimeException getValidationException() {
            return validationException;
        }
    }
}
