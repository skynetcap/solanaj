package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
public class SplTokenAccountInfo extends RpcResultObject {

    @JsonProperty("value")
    private TokenResultObjects.Value value;

    /**
     * Retrieves the "tokenMetadata" extension state from the token account information.
     *
     * @return an Optional containing the ExtensionState object of the "tokenMetadata" extension if present; otherwise, Optional.empty().
     */
    public Optional<TokenResultObjects.ExtensionState> getToken2022Metadata() {
        return Optional.ofNullable(value)
                .map(TokenResultObjects.Value::getData)
                .map(TokenResultObjects.Data::getParsed)
                .map(TokenResultObjects.ParsedData::getInfo)
                .map(TokenResultObjects.TokenInfo::getExtensions)
                .flatMap(extensions -> extensions.stream()
                        .filter(extension -> "tokenMetadata".equalsIgnoreCase(extension.getExtensionType()))
                        .findFirst())
                .map(TokenResultObjects.Extension::getState);
    }

    public Optional<TokenResultObjects.Extension> getExtension(String extensionType) {
        return Optional.ofNullable(value)
                .map(TokenResultObjects.Value::getData)
                .map(TokenResultObjects.Data::getParsed)
                .map(TokenResultObjects.ParsedData::getInfo)
                .map(TokenResultObjects.TokenInfo::getExtensions)
                .flatMap(extensions -> extensions.stream()
                        .filter(extension -> extensionType.equalsIgnoreCase(extension.getExtensionType()))
                        .findFirst());
    }

    public Optional<String> getTokenName() {
        return getToken2022Metadata()
                .map(TokenResultObjects.ExtensionState::getName);
    }

    public Optional<String> getTokenSymbol() {
        return getToken2022Metadata()
                .map(TokenResultObjects.ExtensionState::getSymbol);
    }

    public Optional<String> getTokenUri() {
        return getToken2022Metadata()
                .map(TokenResultObjects.ExtensionState::getUri);
    }
}
