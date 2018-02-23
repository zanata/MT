/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.magpie.action;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class FrontendManifest implements Serializable {
    @JsonProperty("frontend.css")
    private String frontendCss;

    @JsonProperty("frontend.js")
    private String frontendJs;

    @JsonProperty("vendor.js")
    private String vendorJs;

    public String getFrontendCss() {
        return frontendCss;
    }

    public void setFrontendCss(String frontendCss) {
        this.frontendCss = frontendCss;
    }

    public String getFrontendJs() {
        return frontendJs;
    }

    public void setFrontendJs(String frontendJs) {
        this.frontendJs = frontendJs;
    }

    public String getVendorJs() {
        return vendorJs;
    }

    public void setVendorJs(String vendorJs) {
        this.vendorJs = vendorJs;
    }
}
