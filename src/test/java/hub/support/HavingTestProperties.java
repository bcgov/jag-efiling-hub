package hub.support;

import org.junit.Before;

public class HavingTestProperties {

    @Before
    public void propertiesSetting() {
        System.setProperty("CSO_EXTENSION_ENDPOINT", "http4://localhost:8111");
        System.setProperty("COA_SEARCH_ENDPOINT", "http4://localhost:8111");
        System.setProperty("WEBCATS_UPDATE_ENDPOINT", "http4://localhost:8111");
        System.setProperty("OR_ENDPOINT_INITIALIZE", "http4://localhost:8111");
        System.setProperty("OR_ENDPOINT_CREATE", "http4://localhost:8222");
        System.setProperty("OR_ENDPOINT_CHANGEOWNER", "http4://localhost:8333");
    }
}
