/*
Copyright (c) 2018 Noah Andrews

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Noah Andrews nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.robotcore.internal.webserver.websockets;

import org.jetbrains.annotations.NotNull;
// import android.util.Base64; FIXME (Sean) this is unused - does removing it break anything?

import com.google.gson.JsonSyntaxException;

import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

/**
 * A WebSocket message that conforms to our sub-protocol.
 *
 * Transmitted over the wire as JSON.
 *
 * The payload is transparently converted to and from Base64 during serialization and deserialization, respectively.
 * This ensures that there are no parsing issues when the payload is itself JSON.
 */
public final class FtcWebSocketMessage {
    private static final String TAG = "FtcWebSocketMessage";

    @NotNull private String namespace;
    @NotNull private String type;
    @NotNull private String payload = "";

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    /**
     * Create an FtcWebSocketMessage that does not include a payload
     *
     * @param namespace The namespace of the message (for example, robocol)
     * @param type The type of the message (for example, initRobot)
     */
    public FtcWebSocketMessage(@NotNull String namespace, @NotNull String type) {
        this(namespace, type, "");
    }

    /**
     * Create an FtcWebSocketMessage that optionally includes a payload
     *
     * @param namespace The namespace of the message (for example, robocol)
     * @param type The type of the message (for example, initRobot)
     * @param payload The payload of the message (may be a JSON string)
     */
    public FtcWebSocketMessage(@NotNull String namespace, @NotNull String type, @NotNull String payload) {
        this.namespace = namespace;
        this.type = type;
        this.payload = payload;
    }

    //----------------------------------------------------------------------------------------------
    // Public Methods
    //----------------------------------------------------------------------------------------------

    /**
     * Convert a JSON string into an FtcWebSocketMessage
     *
     * @throws JsonSyntaxException
     */
    @NotNull public static FtcWebSocketMessage fromJson(String json) {
        return SimpleGson.getInstance().fromJson(json, FtcWebSocketMessage.class);
    }

    /**
     * Serialize into a JSON string for network transmission
     */
    @NotNull public String toJson() {
        return SimpleGson.getInstance().toJson(this);
    }

    /**
     * Get the payload (may be JSON)
     */
    @NotNull public String getPayload() {
        return payload;
    }

    /**
     * Get the namespace
     */
    @NotNull public String getNamespace() {
        return namespace;
    }

    /**
     * Get the message type
     */
    @NotNull public String getType() {
        return type;
    }

    /**
     * Check if the message has a payload
     */
    public boolean hasPayload() {
        return !payload.isEmpty();
    }

    @Override public String toString() {
        return "FtcWebSocketMessage{" +
                "namespace='" + getNamespace() + '\'' +
                ", type='" + getType() + '\'' +
                ", payload='" + getPayload() + '\'' +
                '}';
    }
}
