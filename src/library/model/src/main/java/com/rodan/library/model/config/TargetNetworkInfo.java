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
public class TargetNetworkInfo {
    String hlrGt;
    String vlrGt;
    String mscGt;
    String sgsnGt;
    String smscGt;
    String gsmScfGt;
    String gmlcGt;

    String realm;
    String mmeHostname;

    String cc;
    String ndc;
    String mcc;
    String mnc;

    @Builder
    public TargetNetworkInfo(String hlrGt, String vlrGt, String mscGt, String sgsnGt, String smscGt, String gsmScfGt,
                             String gmlcGt, String realm, String mmeHostname, String cc, String ndc, String mcc, String mnc) {
        this.hlrGt = hlrGt;
        this.vlrGt = vlrGt;
        this.mscGt = mscGt;
        this.sgsnGt = sgsnGt;
        this.smscGt = smscGt;
        this.gsmScfGt = gsmScfGt;
        this.gmlcGt = gmlcGt;

        this.realm = realm;
        this.mmeHostname = mmeHostname;

        this.cc = cc;
        this.ndc = ndc;
        this.mcc = mcc;
        this.mnc = mnc;
    }

    public void validate() throws ValidationException {
        if (StringUtils.isNotBlank(hlrGt))
            Validator.validateHlr(hlrGt);
        if (StringUtils.isNotBlank(vlrGt))
            Validator.validateVlr(vlrGt);
        if (StringUtils.isNotBlank(mscGt))
            Validator.validateMsc(mscGt);
        if (StringUtils.isNotBlank(sgsnGt))
            Validator.validateSgsn(sgsnGt);
        if (StringUtils.isNotBlank(smscGt))
            Validator.validateSmsc(smscGt);
        if (StringUtils.isNotBlank(gsmScfGt))
            Validator.validateGsmScf(gsmScfGt);
        if (StringUtils.isNotBlank(gmlcGt))
            Validator.validateGmlc(gmlcGt);

        // TODO IMP: Validate realm and mmeHostname;
        // target Realm should exists in peer realms

        Validator.validateMcc(mcc);
        Validator.validateMnc(mnc);
        Validator.validateCc(cc);
        Validator.validateNdc(ndc);
    }
}
