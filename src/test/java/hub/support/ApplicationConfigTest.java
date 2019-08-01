package hub.support;

import hub.helper.ApplicationConfig;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.lang.annotation.Annotation;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApplicationConfigTest {

    @Test
    public void isNeededByWildfly() {
        ApplicationConfig config = new ApplicationConfig();

        assertThat(config, is(instanceOf(Application.class)));
    }

    @Test
    public void hasTheExpectedAnnotation() {
        ApplicationConfig config = new ApplicationConfig();
        Annotation[] interfaces = config.getClass().getAnnotations();

        assertThat(interfaces.length, equalTo(1));
        assertThat(interfaces[0].annotationType(), equalTo(ApplicationPath.class));
        assertThat(((ApplicationPath)interfaces[0]).value(), equalTo("resources"));
    }
}
