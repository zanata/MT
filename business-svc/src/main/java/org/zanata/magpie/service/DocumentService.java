/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.service;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.dao.DocumentDAO;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class DocumentService {
    private DocumentDAO documentDAO;

    @Inject
    public DocumentService(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    @SuppressWarnings("unused")
    public DocumentService() {
    }

    @Transactional
    public Document getOrCreateByUrl(String url, Locale fromLocale, Locale toLocale) {
        Document doc = documentDAO.getByUrl(url, fromLocale, toLocale);

        if (doc == null) {
            doc = new Document(url, fromLocale, toLocale);
            doc = documentDAO.persist(doc);
            documentDAO.flush();
        }
        return doc;
    }

    @Transactional
    public List<Document> getByUrl(@NotNull String url,
            Optional<LocaleCode> fromLocaleCode, Optional<LocaleCode> toLocaleCode,
            Optional<DateRange> dateParam) {
        return documentDAO.getByUrl(url, fromLocaleCode, toLocaleCode, dateParam);
    }

    @Transactional
    public Document incrementDocRequestCount(Document doc) {
        doc.incrementCount();
        return documentDAO.merge(doc);
    }
}
