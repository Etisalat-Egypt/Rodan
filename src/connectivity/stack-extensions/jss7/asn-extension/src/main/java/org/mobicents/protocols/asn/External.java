/**
 * 
 */
package org.mobicents.protocols.asn;

import java.io.IOException;

/**
 * Represents external type, should be extended to allow getting real type of
 * data, once External ends decoding. Decoding should be done as follows in
 * subclass:<br>
 * 
 * <pre>
 * &lt;b&gt;decode(){
 *   super.decode();
 *   if(super.getType == requiredType)
 *       this.decode();
 *   }else
 *   {
 *   	//indicate error
 *   }
 *   &lt;/b&gt;
 * </pre>
 *  . Also encode/decode methods should be extended
 * 
 * @author baranowb
 * @author amit bhayani
 * @author sergey vetyutnev
 * 
 */
public class External {
	// FIXME: makes this proper, it should be kind of universal container....
	protected static final int _TAG_EXTERNAL_CLASS = Tag.CLASS_UNIVERSAL; // universal
	protected static final boolean _TAG_EXTERNAL_PC_PRIMITIVE = false; // isPrimitive

	// ENCODING TYPE
	protected static final int _TAG_ASN = 0x00;
	protected static final int _TAG_ASN_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; // context
	// spec
	protected static final boolean _TAG_ASN_PC_PRIMITIVE = false; // isPrimitive

	// in case of Arbitrary and OctetAligned, we dont make decision if its
	// constructed or primitive, its done for us :)
	protected static final int _TAG_ARBITRARY = 0x02; // this is bit string
	protected static final int _TAG_ARBITRARY_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; // context
	// spec

	protected static final int _TAG_OCTET_ALIGNED = 0x01; // this is bit
															// string
	protected static final int _TAG_OCTET_ALIGNED_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; // context
	// spec

	protected static final int _TAG_IMPLICIT_SEQUENCE = 0x08;

	// some state vars
	// ENCODE TYPE - wtf, ASN is really mind blowing, cmon....
	// If Amit reads this, he will smile.

	// ENCODE AS.... boom
	protected boolean oid = false;
	protected boolean integer = false;
	protected boolean objDescriptor = false;

	// actual vals
	protected long[] oidValue = null;
	protected long indirectReference = 0;
	protected String objDescriptorValue = null;

	// ENCoDING
	private boolean asn = false;
	private boolean octet = false;
	private boolean arbitrary = false;

	// data in binary form for ASN and octet string
	private byte[] data;
	private BitSetStrictLength bitDataString;

	//FIXME: ensure structure from file and if it does not allow more than one type of data, enforce that!
	
	public void decode(AsnInputStream ais) throws AsnException {
		
		this.oid = false;
		this.integer = false;
		this.objDescriptor = false;
		this.oidValue = null;
		this.indirectReference = 0;
		this.objDescriptorValue = null;
		this.asn = false;
		this.octet = false;
		this.arbitrary = false;
		this.data = null;
		this.bitDataString = null;
		
		try {

			// The definition of EXTERNAL is
			//			
			// EXTERNAL ::= [UNIVERSAL 8] IMPLICIT SEQUENCE {
			// direct-reference OBJECT IDENTIFIER OPTIONAL,
			// indirect-reference INTEGER OPTIONAL,
			// data-value-descriptor ObjectDescriptor OPTIONAL,
			// encoding CHOICE {
			// single-ASN1-type [0] ANY,
			// octet-aligned [1] IMPLICIT OCTET STRING,
			// arbitrary [2] IMPLICIT BIT STRING }}
			//
			//			

			AsnInputStream localAsnIS = ais.readSequenceStream();
			
			while( true ) {
				if (localAsnIS.available() == 0)
					break;
				
				int tag = localAsnIS.readTag();
				if (localAsnIS.getTagClass() == Tag.CLASS_UNIVERSAL) {
					switch(tag) {
					case Tag.INTEGER:
						this.indirectReference = localAsnIS.readInteger();
						this.setInteger(true);
						break;
						
					case Tag.OBJECT_IDENTIFIER:
						this.oidValue = localAsnIS.readObjectIdentifier();
						this.setOid(true);
						break;
						
					case Tag.OBJECT_DESCRIPTOR:
						this.objDescriptorValue = localAsnIS.readGraphicString();
						this.setObjDescriptor(true);
						break;
						
					default:
						throw new AsnException("Error while decoding External: Unrecognized tag value=" + tag + ", tagClass=" + localAsnIS.getTagClass());
					}
				} else if (localAsnIS.getTagClass() == Tag.CLASS_CONTEXT_SPECIFIC) {
					
					switch(tag) {
					case External._TAG_ASN:
						this.data = localAsnIS.readSequence();
						this.setAsn(true);
						break;
						
					case External._TAG_OCTET_ALIGNED:
						this.setEncodeType(localAsnIS.readOctetString());
						setOctet(true);
						break;
						
					case External._TAG_ARBITRARY:
						this.setEncodeBitStringType(localAsnIS.readBitString());
						setArbitrary(true);
						break;
						
					default:
						throw new AsnException("Error while decoding External: Unrecognized tag value=" + tag + ", tagClass=" + localAsnIS.getTagClass());
					}

					// check: this field  must be the last
					if (localAsnIS.available() != 0)
						throw new AsnException("Error while decoding External: data field must be the last");

				} else {
					throw new AsnException("Error while decoding External: Unrecognized tag value=" + tag + ", tagClass=" + localAsnIS.getTagClass());
				}
			}
		} catch (IOException e) {
			throw new AsnException("IOException while decoding External: " + e.getMessage(), e);
		}
	}

	public void encode(AsnOutputStream aos) throws AsnException {

		this.encode(aos, Tag.CLASS_UNIVERSAL, Tag.EXTERNAL);
	}
	
	public void encode(AsnOutputStream aos, int tagClass, int tag) throws AsnException {

		if( !this.oid && !this.integer )
			throw new AsnException("Error while encoding External: oid value or integer value must be definite");
		if( !this.asn && !this.octet && !this.arbitrary )
			throw new AsnException("Error while encoding External: asn value, octect value or arbitrary value must be definite");
		
		try {
			
			aos.writeTag(tagClass, false, tag);
			int pos1 = aos.StartContentDefiniteLength();
			
			// something to do encoding
			if (this.oid)
				aos.writeObjectIdentifier(this.oidValue);
			if (this.integer)
				aos.writeInteger(this.indirectReference);
			if (this.objDescriptor)
				aos.writeStringGraphic(Tag.CLASS_UNIVERSAL, Tag.OBJECT_DESCRIPTOR, this.objDescriptorValue);

			if (asn) {
				byte[] childData = this.getEncodeType();
				aos.writeTag(Tag.CLASS_CONTEXT_SPECIFIC, false, _TAG_ASN);
				aos.writeLength(childData.length);
				aos.write(childData);
			} else if (octet) {
				byte[] childData = this.getEncodeType();
				aos.writeOctetString(Tag.CLASS_CONTEXT_SPECIFIC, _TAG_OCTET_ALIGNED, childData);
			} else if (arbitrary) {
				BitSetStrictLength bs = this.bitDataString;
				aos.writeBitString(Tag.CLASS_CONTEXT_SPECIFIC, _TAG_ARBITRARY, bs);
			}
			
			aos.FinalizeContent(pos1);
		} catch (IOException e) {
			throw new AsnException(e);
		}
	}

	public byte[] getEncodeType() throws AsnException {
		return data;
	}

	public void setEncodeType(byte[] data) {
		this.data = data;
	}

	public BitSetStrictLength getEncodeBitStringType() throws AsnException {
		return (BitSetStrictLength) bitDataString;
	}

	public void setEncodeBitStringType(BitSetStrictLength data) {
		this.bitDataString = data;
		this.setArbitrary(true);
	}

	/**
	 * @return the oid
	 */
	public boolean isOid() {
		return oid;
	}

	/**
	 * @param oid
	 *            the oid to set
	 */
	public void setOid(boolean oid) {
		this.oid = oid;
//		if (oid) {
//			setInteger(false);
//			setObjDescriptor(false);
//		}
	}

	/**
	 * @return the integer
	 */
	public boolean isInteger() {
		return integer;
	}

	/**
	 * @param integer
	 *            the integer to set
	 */
	public void setInteger(boolean integer) {
		this.integer = integer;
//		if (integer) {
//			setOid(false);
//			setObjDescriptor(false);
//		}
	}

	/**
	 * @return the objDescriptor
	 */
	public boolean isObjDescriptor() {
		return objDescriptor;
	}

	/**
	 * @param objDescriptor
	 *            the objDescriptor to set
	 */
	public void setObjDescriptor(boolean objDescriptor) {
		this.objDescriptor = objDescriptor;
//		if (objDescriptor) {
//			setOid(false);
//			setInteger(false);
//		}
	}

	/**
	 * @return the oidValue
	 */
	public long[] getOidValue() {
		return oidValue;
	}

	/**
	 * @param oidValue
	 *            the oidValue to set
	 */
	public void setOidValue(long[] oidValue) {
		this.oidValue = oidValue;
	}

	/**
	 * @return the integerValue
	 */
	public long getIndirectReference() {
		return indirectReference;
	}

	/**
	 * @param integerValue
	 *            the integerValue to set
	 */
	public void setIndirectReference(long indirectReference) {
		this.indirectReference = indirectReference;
	}

	/**
	 * @return the objDescriptorValue
	 */
	public String getObjDescriptorValue() {
		return objDescriptorValue;
	}

	/**
	 * @param objDescriptorValue
	 *            the objDescriptorValue to set
	 */
	public void setObjDescriptorValue(String objDescriptorValue) {
		this.objDescriptorValue = objDescriptorValue;
	}

	/**
	 * @return the asn
	 */
	public boolean isAsn() {
		return asn;
	}

	/**
	 * @param asn
	 *            the asn to set
	 */
	public void setAsn(boolean asn) {
		this.asn = asn;
		if (asn) {
			setArbitrary(false);
			setOctet(false);
		}
	}

	/**
	 * @return the octet
	 */
	public boolean isOctet() {
		return octet;
	}

	/**
	 * @param octet
	 *            the octet to set
	 */
	public void setOctet(boolean octet) {
		this.octet = octet;
		if (octet) {
			setArbitrary(false);
			setAsn(false);
		}
	}

	/**
	 * @return the arbitrary
	 */
	public boolean isArbitrary() {
		return arbitrary;
	}

	/**
	 * @param arbitrary
	 *            the arbitrary to set
	 */
	public void setArbitrary(boolean arbitrary) {
		this.arbitrary = arbitrary;
		if (arbitrary) {
			setObjDescriptor(false);
			setAsn(false);
		}
	}

}
