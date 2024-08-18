package org.p2p.solanaj.ws;

import lombok.Getter;

@Getter
public class SignatureNotification {
    private final Object error;

    public SignatureNotification(Object error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }
}
