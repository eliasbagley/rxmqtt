package com.eliasbagley.rxmqtt.impl;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.eliasbagley.rxmqtt.utils.Strings;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.UUID;

import static com.eliasbagley.rxmqtt.constants.Constants.*;

//TODO message persistence for qos 1 and 2
public class RxMqttClientBuilder {
    @NonNull private String clientId = UUID.randomUUID().toString(); // Default to a random string
    @NonNull private String brokerUrl;
    @NonNull private String host;
    @NonNull private String port    = DEFAULT_PORT;
    @NonNull private String uriType = TCP;
    @Nullable private Will   will;
    @Nullable private String username;
    @Nullable private String password;
    @NonNull private Gson gson = new Gson();

    // Default connection params
    private boolean cleanSession          = true;
    private int     keepAliveInterval     = DEFAULT_KEEPALIVE;
    private int     timeout               = DEFAULT_TIMEOUT;
    private boolean userHasSetOwnClientId = false;

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
    public RxMqttClientBuilder setGson(@NonNull Gson gson) {
        this.gson = gson;
        return this;
    }

    @NonNull
    public RxMqttClientBuilder setClientId(@NonNull String clientId) {
        this.clientId = clientId;
        this.userHasSetOwnClientId = true;
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
            return new RxMqttClient(brokerUrl, clientId, null, createConnectOptions(), gson);
        } else {
            throw validationResult.getValidationException();
        }
    }

    @CheckResult @NonNull
    public RxMqttClient buildAndConnect() {
        RxMqttClient client = build();
        client.connect();
        return client;
    }

    //region validation

    @CheckResult @NonNull
    private ValidationResult isValid() {
        String validationErrors = "";

        if (brokerUrl == null) {
            validationErrors += "Broker URL cannot be null.\n";
            return failed(validationErrors); // return early to avoid NPE
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

        if (cleanSession == false && userHasSetOwnClientId == false) {
            validationErrors += "You must provide your own client id if you want a persistent session. Use setClientId()";
        }

        if (validationErrors.isEmpty()) {
            return new ValidationResult(true);
        } else {
            return failed(validationErrors);
        }
    }

    @CheckResult @NonNull
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
