package dev.kerman.freight;

import net.minestom.server.ServerFlag;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EnvironmentTest {

    @Test
    void testEnv(Env env) {
        assertNotNull(env);
    }

    @Test
    void inside() {
        assertTrue(ServerFlag.INSIDE_TEST);
    }

    @Test
    void viewablePackets() { // Audience groups could be put away
        assertFalse(ServerFlag.VIEWABLE_PACKET);
    }
}
