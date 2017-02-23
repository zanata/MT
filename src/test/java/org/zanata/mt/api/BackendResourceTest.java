package org.zanata.mt.api;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendResourceTest {

    private BackendResource backendResource;

    @Before
    public void setup() {
        backendResource = new BackendResource();
    }

    @Test
    public void testGetAttributionNullId() {
        Response response = backendResource.getAttribution(null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAttributionLowerCase() {
        String id = "ms";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getHeaders()).isNotEmpty()
                .containsKeys("Content-Disposition");
    }

    @Test
    public void testGetAttributionInvalidId() {
        String id = "google";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}