package org.zanata.magpie.api;

/**
 * Environment variables and HTTP header fields used in application
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface APIConstant {

    // prefix for all REST path
    String API_CONTEXT = "api";

    // list of url to allow access to {@link #API_CONTEXT}. White space separated
    String ORIGIN_WHITELIST = "MT_ORIGIN_WHITELIST";
    // see org.zanata.magpie.mt.servlet.APISecurityFilter
    // API_ID to be used for API request (for authentication purpose)
    String API_ID = "MT_API_ID";
    // API Key to be used for API request (for authentication purpose)
    String API_KEY = "MT_API_KEY";


    // AZURE Subscription key for MS backend
    String AZURE_KEY = "MT_AZURE_KEY";

    /**
     * Google Application Default Credentials content for google backend.
     * The content of this environment variable will override the content of the
     * file defined by {@link org.zanata.magpie.api.APIConstant#GOOGLE_ADC}.
     */
    String GOOGLE_CREDENTIAL_CONTENT = "MT_GOOGLE_CREDENTIAL_CONTENT";
    /**
     * Google Application Default Credentials for google backend.
     * Should point to a file that defines the credentials.
     * https://developers.google.com/identity/protocols/application-default-credentials#howtheywork
     */
    String GOOGLE_ADC = "GOOGLE_APPLICATION_CREDENTIALS";

    String DEFAULT_PROVIDER = "DEFAULT_TRANSLATION_PROVIDER";

    // HTTP header request authentication
    String HEADER_API_KEY = "X-Auth-Token";
    String HEADER_USERNAME = "X-Auth-User";
}
