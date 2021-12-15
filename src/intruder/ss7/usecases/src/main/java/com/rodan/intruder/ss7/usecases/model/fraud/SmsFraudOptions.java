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

package com.rodan.intruder.ss7.usecases.model.fraud;

import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Option;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter @ToString
public class SmsFraudOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "imsi", description = "Target IMSI", mandatory = true)
    String imsi;

    @Option(name = "sender", description = "SMS sender address", mandatory = true)
    String sender;

    @Option(name = "content", description = "SMS content", mandatory = true)
    String content;

    @Option(name = "type", description = "Message type (normal, replace, flash, silent)", mandatory = true)
    String messageType;

    @Option(name = "msc", description = "Target MSC GT", mandatory = true)
    String targetMscGt;

    @Option(name = "smsc", description = "Target SMSC GT (required for spoofing SMSC, empty means use local GT)")
    private String smscGt;

    @Option(name = "spoof", description = "Spoof SMSC address in SCCP CgPA (yes/no)", mandatory = true)
    private String spoofSmsc;

    @Option(name = "acv", description = "MAP app context version (2)", mandatory = true)
    private String mapVersion;

    @Builder
    public SmsFraudOptions(IntruderNodeConfig nodeConfig, String sender, String content, String messageType,
                           String smscGt, String imsi, String targetMscGt, String spoofSmsc, String mapVersion) {
        super(nodeConfig);
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.sender = Objects.requireNonNullElse(sender, "");
        this.content = Objects.requireNonNullElse(content, "");
        this.messageType = Objects.requireNonNullElse(messageType, "normal");
        this.targetMscGt = Objects.requireNonNullElse(targetMscGt, "");
        this.smscGt = Objects.requireNonNullElse(smscGt, "");
        this.spoofSmsc = Objects.requireNonNullElse(spoofSmsc, "No");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "2");
    }

    @Override
    public String getSpoofSender() {
        return spoofSmsc;
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImsi(imsi);
        Validator.validateSmSender(sender);
        Validator.validateSmContent(content);
        Validator.validateSmType(messageType);
        Validator.validateMsc(targetMscGt);
        Validator.validateToggleOption(spoofSmsc, "spoofSmsc");
        Validator.validateMapVersion(mapVersion);

        if ("Yes".equalsIgnoreCase(spoofSmsc))
            Validator.validateSmsc(smscGt);
    }
}
