package org.zanata.mt.api;

import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.APIErrorResponse;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.DocumentContentTranslatorService;

import org.zanata.mt.util.UrlUtil;

/**
 * API entry point for docContent translation: '/translate'
 *
 * See
 * {@link #translate(DocumentContent, LocaleId)}
 *
 */
@Path("/translate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class DocumentContentTranslatorResource {
    private static final Logger LOG =
        LoggerFactory.getLogger(DocumentContentTranslatorResource.class);

    private DocumentContentTranslatorService documentContentTranslatorService;

    private LocaleDAO localeDAO;

    private DocumentDAO documentDAO;

    @SuppressWarnings("unused")
    public DocumentContentTranslatorResource() {
    }

    @Inject
    public DocumentContentTranslatorResource(
            DocumentContentTranslatorService documentContentTranslatorService,
            LocaleDAO localeDAO, DocumentDAO documentDAO) {
        this.documentContentTranslatorService =
                documentContentTranslatorService;
        this.localeDAO = localeDAO;
        this.documentDAO = documentDAO;
    }

    @POST
    public Response translate(@NotNull DocumentContent docContent,
            @NotNull @QueryParam("targetLang") LocaleId targetLang) {
        // Default to MS engine for translation
        BackendID backendID = BackendID.MS;

        Optional<APIErrorResponse> errorResp =
                validatePostRequest(docContent, targetLang);
        if (errorResp.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResp.get()).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request translations:" + docContent + " target_lang"
                    + targetLang + " backendId:" + backendID.getId());
        }

        Locale srcLocale = getLocale(new LocaleId(docContent.getLocale()));
        Locale transLocale = getLocale(targetLang);

        org.zanata.mt.model.Document doc = documentDAO
                .getOrCreateByUrl(docContent.getUrl(), srcLocale, transLocale);

        try {
            DocumentContent newDocContent = documentContentTranslatorService
                    .translateDocument(docContent, srcLocale, transLocale,
                            backendID);
            doc.incrementUsedCount();
            documentDAO.persist(doc);
            return Response.ok().entity(newDocContent).build();
        } catch (BadRequestException e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                    .build();
        } catch (Exception e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                            e, title);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }

    private Optional<APIErrorResponse> validatePostRequest(DocumentContent docContent,
            LocaleId targetLang) {
        if (targetLang == null) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: targetLang"));
        }
        if (docContent == null || docContent.getContents() == null ||
                docContent.getContents().isEmpty()) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Empty content:" + docContent));
        }
        if (StringUtils.isBlank(docContent.getLocale())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Empty locale"));
        }
        if (StringUtils.isBlank(docContent.getUrl()) ||
                !UrlUtil.isValidURL(docContent.getUrl())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + docContent.getUrl()));
        }
        for (TypeString string : docContent.getContents()) {
            if (StringUtils.isBlank(string.getValue()) ||
                    StringUtils.isBlank(string.getType())) {
                return Optional
                        .of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                                "Empty content: " + string.toString()));
            }
            if (!documentContentTranslatorService
                    .isMediaTypeSupported(string.getType())) {
                return Optional
                        .of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                                "Invalid mediaType: " + string.getType()));
            }
        }
        return Optional.empty();
    }

    private Locale getLocale(@NotNull LocaleId localeId) {
        return localeDAO.getOrCreateByLocaleId(localeId);
    }
}