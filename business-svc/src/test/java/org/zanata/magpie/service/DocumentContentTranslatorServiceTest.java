package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.magpie.util.SegmentStringKt.segmentBySentences;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.TypeString;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;

import com.google.common.collect.ImmutableList;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.util.ShortString;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class DocumentContentTranslatorServiceTest {

    @Inject
    private DocumentContentTranslatorService documentContentTranslatorService;

    @Produces
    @Mock
    private PersistentTranslationService persistentTranslationService;

    @Test
    public void testEmptyConstructor() {
        assertThat(new DocumentContentTranslatorService()).isNotEqualTo(null);
    }

    @Test
    public void testGetMediaType() {
        assertThatThrownBy(
                () -> documentContentTranslatorService.getMediaType("notSupportType"))
                .isInstanceOf(BadRequestException.class);

        String mediaType = "text/plain";
        assertThat(documentContentTranslatorService.getMediaType(mediaType)).isNotNull()
                .isEqualTo(MediaType.TEXT_PLAIN_TYPE);

        mediaType = "text/html";
        assertThat(documentContentTranslatorService.getMediaType(mediaType)).isNotNull()
                .isEqualTo(MediaType.TEXT_HTML_TYPE);
    }

    @Test
    public void testLongHTMLWithNoTags() {
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        // Longer than maxLength; no sentences for segmentation.
        String html = "The quick brown fox jumps over the lazy dog.";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue()).isEqualTo(html);
        assertThat(translatedDocContent.getWarnings())
                .extracting(this::responseToString)
                .containsExactly(
                        "Warning: translation skipped: String length is over 25\n" +
                                "The quick brown fox jumps over the lazy dog.");
    }

    @Test
    public void testShortHTMLWithNoTags() {
        int maxLength = 250;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "The quick brown fox jumps over the lazy dog.";
        String expectedHtml = "Der schnelle braune Fuchs springt über den faulen Hund.";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("Der schnelle braune Fuchs springt über den faulen Hund."));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue()).isEqualTo(expectedHtml);
    }

    @Test
    public void testLongHTMLWithOuterDiv() {
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<div><span>content1</span><span>content2</span></div>";
        String expectedHtml = "<div><span>translated</span><span>translated</span></div>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("<span>translated</span>"));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(getContentAt(translatedDocContent)).isEqualTo(expectedHtml);
    }

    @Test
    public void testLongHTMLWithoutOuterDiv() {
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<span>content1</span><span>content2</span>";
        String expectedHtml = "<span>translated1</span><span>translated2</span>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                eq(ImmutableList.of("content1")), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("translated1"));

        when(persistentTranslationService.translate(any(),
                eq(ImmutableList.of("content2")), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("translated2"));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(getContentAt(translatedDocContent)).isEqualTo(expectedHtml);
    }

    @Test
    public void testInnerHTMLNodeWithOneLongSentence() {
        // force second span to exceed max length
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<div><span>content1</span><span>content too long cannot be translated</span></div>";
        String expectedHtml = "<div><span>translated</span><span>content too long cannot be translated</span></div>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("<span>translated</span>"));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(getContentAt(translatedDocContent))
                .isEqualTo(expectedHtml);
        assertThat(translatedDocContent.getWarnings())
                .extracting(this::responseToString)
                .containsExactly(
                        "Warning: translation skipped: String length is over 25\n" +
                                "<span>content too long cannot be translated</span>");
    }

    @Test
    public void testHTMLNodeDivWithMultipleSentences() {
        // force segmentation of the div contents:
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<div>Hello World. Goodbye World.</div>";
        String expectedHtml = "<div>Hallo Welt. Auf Wiedersehen Welt.</div>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document,
                ImmutableList.of("Hello World. "), srcLocale, transLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Hallo Welt. "));
        when(persistentTranslationService.translate(document,
                ImmutableList.of("Goodbye World."), srcLocale, transLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Auf Wiedersehen Welt."));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);

//        System.out.println(mockingDetails(persistentTranslationService).printInvocations());


        assertThat(translatedDocContent.getWarnings())
                .extracting(this::responseToString).as("warnings").isEmpty();
        assertThat(getContentAt(translatedDocContent)).isEqualTo(expectedHtml);
    }

    @Test
    public void testBareHTMLNodeWithMultipleSentences() {
        // force segmentation:
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "Hello World. Goodbye World.";
        String expectedHtml = "Hallo Welt. Auf Wiedersehen Welt.";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document,
                ImmutableList.of("Hello World. "), srcLocale, transLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Hallo Welt. "));

        when(persistentTranslationService.translate(document,
                ImmutableList.of("Goodbye World."), srcLocale, transLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Auf Wiedersehen Welt."));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);

//        System.out.println(mockingDetails(persistentTranslationService).printInvocations());

        assertThat(translatedDocContent.getWarnings())
                .extracting(this::responseToString).as("warnings").isEmpty();
        assertThat(getContentAt(translatedDocContent)).isEqualTo(expectedHtml);
    }

    @Test
    @Ignore("https://zanata.atlassian.net/browse/ZNTAMT-53")
    public void testInnerHTMLNodeWithMultipleSentences() {
        // force segmentation of the second span:
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<div><span>content1</span><span>Hello World. Goodbye World.</span></div>";
        String expectedHtml = "<div><span>translated1</span><span>Hallo Welt. Auf Wiedersehen Welt.</span></div>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document,
                ImmutableList.of("<span>content1</span>"), srcLocale, transLocale, BackendID.MS,
                StringType.HTML, Optional.of("tech")))
                .thenReturn(ImmutableList.of("<span>translated1</span>"));

        when(persistentTranslationService.translate(document,
                ImmutableList.of("Hello World. "), srcLocale, transLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Hallo Welt. "));
        when(persistentTranslationService.translate(document,
                ImmutableList.of("Goodbye World."), srcLocale, transLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Auf Wiedersehen Welt."));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);

//        System.out.println(mockingDetails(persistentTranslationService).printInvocations());


        assertThat(translatedDocContent.getWarnings())
                .extracting(this::responseToString).as("warnings").isEmpty();
        assertThat(getContentAt(translatedDocContent)).isEqualTo(expectedHtml);
    }

    @Test
    public void testXMLTags() {
        int maxLength = 250;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>]";
        String expectedHtml = "translated[网 The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>] 网]";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of(expectedHtml));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_XML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue()).isEqualTo(expectedHtml);
    }

    @Test
    public void testTranslateLongPlainText() {
        int maxLength = 50;

        // strings with 6 sentences
        String text = "Hurry! I am never at home on Sundays. The mysterious diary records the voice. She was too short to see over the fence. Everyone was busy, so I went to the movie alone. Sometimes it is better to just walk away from things and go back to them later when you’re in a better frame of mind.";
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", fromLocale, toLocale);
        List<String> strings = segmentBySentences(text, Optional.empty());

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document,
                strings.subList(0, 2), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:Hurray!", "Translated:I am never at home on Sundays. "));

        when(persistentTranslationService.translate(document,
                strings.subList(2, 3), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:The mysterious diary records the voice. "));

        when(persistentTranslationService.translate(document,
                strings.subList(3, 4), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:She was too short to see over the fence. "));

        when(persistentTranslationService.translate(document,
                strings.subList(4, 5), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:Everyone was busy, so I went to the movie alone. "));

        DocumentContent docContent =
                new DocumentContent(ImmutableList.of(
                        new TypeString(text, MediaType.TEXT_PLAIN, "meta")), "http://localhost", "en");

        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents()).hasSize(1);
        assertThat(StringUtils.countMatches(getContentAt(translatedDocContent),
                "Translated")).isEqualTo(5);
    }

    @Test
    public void testTranslateDocumentContent() {
        int maxLength = 10000;

        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");

        String longText = StringUtils.repeat("5", maxLength);
        String maxString = StringUtils.repeat("t", maxLength + 1);

        List<String> htmls =
                ImmutableList.of("<div>Entry 1</div>",
                        "<div>Entry 2</div>",
                        "<div>Entry 6</div>",
                        "<code>KCS code section</code>",
                        "<div translate=\"no\">non translatable node</div>",
                        "<div id=\"private-notes\"><span>private notes</span></div>");

        List<String> processedHtmls =
                ImmutableList.of("<div>Entry 1</div>",
                        "<div>Entry 2</div>",
                        "<div>Entry 6</div>",
                        "<var id=\"ZNTA-6-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-7-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-8-0\" translate=\"no\"></var>");

        List<String> text = ImmutableList.of("Entry 3", "Entry 4", longText);

        List<String> translatedHtmls =
                ImmutableList.of("<div>MS: Entry 1</div>",
                        "<div>MS: Entry 2</div>",
                        "<div>MS: Entry 6</div>",
                        "<var id=\"ZNTA-6-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-7-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-8-0\" translate=\"no\"></var>");

        List<String> translatedText = ImmutableList.of("MS: Entry 3", "MS: Entry 4", "MS: Long text");

        List<TypeString> contents = ImmutableList.of(
                new TypeString(htmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(htmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(text.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(text.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(text.get(2), MediaType.TEXT_PLAIN, "meta5"),
                new TypeString(htmls.get(2), MediaType.TEXT_HTML, "meta6"),
                new TypeString(htmls.get(3), MediaType.TEXT_HTML, "meta7"),
                new TypeString(htmls.get(4), MediaType.TEXT_HTML, "meta8"),
                new TypeString(htmls.get(5), MediaType.TEXT_HTML, "meta9"),
                new TypeString(maxString, MediaType.TEXT_PLAIN, "meta10"));

        List<TypeString> translatedContents = ImmutableList.of(
                new TypeString(translatedHtmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(translatedHtmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(translatedText.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(translatedText.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(translatedText.get(2), MediaType.TEXT_PLAIN, "meta5"),
                new TypeString(translatedHtmls.get(2), MediaType.TEXT_HTML, "meta6"),
                new TypeString(htmls.get(3), MediaType.TEXT_HTML, "meta7"),
                new TypeString(htmls.get(4), MediaType.TEXT_HTML, "meta8"),
                new TypeString(htmls.get(5), MediaType.TEXT_HTML, "meta9"));

        DocumentContent
                docContent = new DocumentContent(contents, "http://localhost", "en");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document, processedHtmls,
                srcLocale, transLocale, BackendID.MS,
                StringType.HTML, Optional.of("tech"))).thenReturn(translatedHtmls);

        when(persistentTranslationService
                .translate(document, text.subList(0, 2),
                        srcLocale, transLocale, BackendID.MS,
                        StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(translatedText.subList(0, 2));

        when(persistentTranslationService
                .translate(document, text.subList(2, 3),
                        srcLocale, transLocale, BackendID.MS,
                        StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(translatedText.subList(2, 3));

        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);

        assertThat(translatedDocContent.getLocaleCode())
                .isEqualTo(transLocale.getLocaleCode().getId());
        assertThat(translatedDocContent.getBackendId()).isEqualTo(BackendID.MS.getId());
        assertThat(translatedDocContent.getUrl()).isEqualTo(docContent.getUrl());

        assertThat(translatedDocContent.getWarnings())
                .extracting(APIResponse::getDetails)
                .containsExactly(ShortString.shorten(maxString));

        for (int i = 0; i < translatedDocContent.getContents().size() - 1; i++) {
            assertThat(translatedDocContent.getContents().get(i))
                    .isEqualTo(translatedContents.get(i));
        }

        int requestsCount = text.size();
        verify(persistentTranslationService,
                times(requestsCount))
                .translate(any(), anyList(), any(Locale.class),
                        any(Locale.class),
                        any(BackendID.class), any(StringType.class),
                        any());
    }

    @Test
    public void testEmptyPlainTextIsReturnedAsIs() {
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");

        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String emptyContent = "   ";

        DocumentContent plainTextContent = new DocumentContent(Lists.newArrayList(
                new TypeString("test", MediaType.TEXT_PLAIN, "meta"),
                new TypeString(emptyContent, MediaType.TEXT_PLAIN, "meta"),
                new TypeString("test2", MediaType.TEXT_PLAIN, "meta")),
                "http://localhost", "en");

        List<String> response = new ArrayList<>();
        response.add("check1");
        response.add("check2");

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any())).thenReturn(response);
        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(100);

        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, plainTextContent, BackendID.MS);

        assertThat(translatedDocContent.getContents()).hasSize(3);
        assertThat(getContentAt(translatedDocContent)).isEqualTo("check1");
        assertThat(getContentAt(translatedDocContent, 1)).isEqualTo(emptyContent);
        assertThat(getContentAt(translatedDocContent, 2)).isEqualTo("check2");
    }

    private String getContentAt(DocumentContent target) {
        return getContentAt(target, 0);
    }

    private String getContentAt(DocumentContent target, int index) {
        return target.getContents().get(index).getValue();
    }

    private String responseToString(APIResponse r) {
        return r.getTitle() + "\n" + r.getDetails();
    }

}
