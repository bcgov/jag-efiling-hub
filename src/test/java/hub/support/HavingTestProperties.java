package hub.support;

import org.junit.Before;

public class HavingTestProperties {

    @Before
    public void propertiesSetting() {
        System.setProperty("CSO_ACCOUNT_INFO_ENDPOINT", "http4://localhost:8111");
        System.setProperty("COA_SEARCH_ENDPOINT", "http4://localhost:8111");
        System.setProperty("OR_ENDPOINT_INITIALIZE", "http4://localhost:8111");
        System.setProperty("OR_ENDPOINT_CREATE", "http4://localhost:8222");
    }
}
