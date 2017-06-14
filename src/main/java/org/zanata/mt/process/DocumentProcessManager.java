package org.zanata.mt.process;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleCode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handle locking of api request for
 * {@link org.zanata.mt.api.service.DocumentResource#translate(DocumentContent, LocaleCode)}
 *
 * Limit to process one translation request at a time.
 * This lock uses single thread lock library which does not support clustering.
 *
 * TODO: To support clustering
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class DocumentProcessManager {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentProcessManager.class);

    private static final String DOC_LOCK_CACHE = "DOCUMENT_LOCK_CACHE";

    private DefaultCacheManager cacheManager;

    private Cache<DocumentProcessKey, Boolean> documentProcessCache;

    private final ReentrantLock lock = new ReentrantLock(true);

    @SuppressWarnings("unused")
    public DocumentProcessManager() {
    }

    @Inject
    public DocumentProcessManager(DefaultCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        documentProcessCache = this.cacheManager.getCache(DOC_LOCK_CACHE);
    }

    public void lock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking document translation request:{}", key.toString());
        }
        documentProcessCache.put(key, true);
//        lock.lock();
    }

    public void unlock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("release document translation lock:{}", key.toString());
        }
        documentProcessCache.remove(key);
//        lock.unlock();
    }

    public boolean isLocked(@NotNull DocumentProcessKey key) {
        return documentProcessCache.containsKey(key);
    }

    public ReentrantLock getLock(@NotNull DocumentProcessKey key) {
        return lock;
    }
}
