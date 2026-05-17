module dev.kerman.freight.test {
    requires dev.kerman.freight;

    requires net.minestom.server;
    requires net.minestom.testing;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    exports dev.kerman.freight.test;
    opens dev.kerman.freight.test to org.junit.platform.commons;
}