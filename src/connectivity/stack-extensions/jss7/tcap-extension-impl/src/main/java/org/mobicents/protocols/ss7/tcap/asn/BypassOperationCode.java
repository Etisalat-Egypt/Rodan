package org.mobicents.protocols.ss7.tcap.asn;

import org.mobicents.protocols.asn.AsnException;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.tcap.asn.comp.OperationCodeType;

import java.io.IOException;
// ########### TRX: PATCH START
public class BypassOperationCode extends OperationCodeImpl {
    @Override
    public void encode(AsnOutputStream aos) throws EncodeException {
        try {
            if (this.getLocalOperationCode() == null)
                throw new EncodeException("Operation code: No Operation code set!");
            if (this.getOperationType() != OperationCodeType.Local)
                throw new EncodeException("Operation code: No Operation code set!");

            aos.writeInteger(0, 6, getLocalOperationCode());

        } catch (IOException | AsnException e) {
            throw new EncodeException(e);
        }
    }
}
// ########### TRX: PATCH END
