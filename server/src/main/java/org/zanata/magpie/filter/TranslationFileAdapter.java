/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

package org.zanata.magpie.filter;

import org.apache.commons.lang3.tuple.Pair;
import org.fedorahosted.tennera.jgettext.Message;
import org.jetbrains.annotations.NotNull;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.LocaleCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface TranslationFileAdapter {

    Pair<DocumentContent, Map<String, Message>> parseSourceDocument(
        InputStream inputStream,
        String url, LocaleCode fromLocaleCode);

    void writeTranslatedFile(@NotNull OutputStream output,
        LocaleCode fromLocaleCode, LocaleCode toLocaleCode,
        DocumentContent translatedDocContent, Map<String, Message> messages,
        String attribution) throws IOException;

    String getTranslationFileExtension();
}
