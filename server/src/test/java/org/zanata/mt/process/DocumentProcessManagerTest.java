package org.zanata.mt.process;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleCode;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentProcessManagerTest {

    private DocumentProcessManager processManager;

    @Before
    public void init() {
        processManager = new DocumentProcessManager();
    }

    @Test
    public void testTryLock() {
        String url = "testing";
        DocumentProcessKey key =
                new DocumentProcessKey(url, LocaleCode.EN, LocaleCode.DE);
        processManager.withLock(key, () -> {
            assertThat(processManager.getTotalLockCount()).isEqualTo(1);
            return Response.ok().build();
        });
        assertThat(processManager.getTotalLockCount()).isEqualTo(0);
    }
}
