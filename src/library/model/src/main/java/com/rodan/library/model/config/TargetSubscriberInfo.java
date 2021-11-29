/*
 * Etisalat Egypt, Open Source
 * Copyright 2021, Etisalat Egypt and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author Ayman ElSherif
 */

package com.rodan.library.model.config;

import com.rodan.library.model.Validator;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Getter @ToString @NoArgsConstructor
public class TargetSubscriberInfo {
    String msisdn;
    String imsi;
    String imei;
    String tmsi;
    String msrn;

    @Builder
    public TargetSubscriberInfo(String msisdn, String imsi, String imei, String tmsi, String msrn) {
        this.msisdn = msisdn;
        this.imsi = imsi;
        this.imei = imei;
        this.tmsi = tmsi;
        this.msrn = msrn;
    }

    public void validate() throws ValidationException {
        if (StringUtils.isNotBlank(msisdn))
            Validator.validateMsisdn(msisdn);
        if (StringUtils.isNotBlank(imsi))
            Validator.validateImsi(imsi);
        if (StringUtils.isNotBlank(imei))
            Validator.validateImei(imei);
        if (StringUtils.isNotBlank(tmsi))
            Validator.validateTmsi(tmsi);
        if (StringUtils.isNotBlank(msrn))
            Validator.validateMsrn(msrn);

    }
}
