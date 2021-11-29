/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.mobicents.protocols.asn;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 * @author sergey vetyutnev
 */
public class AsnInputStream extends InputStream {

	private static final String _REAL_BASE10_CHARSET = "US-ASCII";
	private static final int DATA_BUCKET_SIZE = 1024;

	private byte[] buffer;
	
	private int start;
	private int length;
	private int pos;
	
	private int tagClass = 0;
	private int pCBit = 0;
	private int tag;
	
	
	public AsnInputStream( byte[] buf ) {
		this.buffer = buf;
		this.length = buf.length;
	}
	
	public AsnInputStream( byte[] buf, int tagClass, boolean isPrimitive, int tag ) {
		this.buffer = buf;
		this.length = buf.length;
		
		this.tagClass = tagClass;
		if (isPrimitive)
			this.pCBit = 0;
		else
			this.pCBit = 1;
		this.tag = tag;
	}
	
	protected AsnInputStream( AsnInputStream buf, int start, int length ) throws IOException {
		this.buffer = buf.buffer;
		this.start = buf.start + start;
		this.length = length;

		if (start < 0 || start > buf.length || this.start < 0 || this.start > this.buffer.length || this.length < 0
				|| this.start + this.length > this.buffer.length)
			throw new IOException("Bad start or length values when creating AsnInputStream");
		
		this.tagClass = buf.tagClass;
		this.pCBit = buf.pCBit;
		this.tag = buf.tag;
	}
	
	@Deprecated
	public AsnInputStream(InputStream in) {
		try {
			int av = in.available();
			byte[] buf = new byte[av];
			in.read(buf);
			
			this.buffer = buf;
			this.length = buf.length;
			
		} catch (IOException e) {
			e.printStackTrace();
			
			this.buffer = new byte[0];
		}
	}
	
	
	/**
	 * Return the current position in the stream
	 * 
	 * @return
	 */
	public int position() {
		return this.pos;
	}
	
	/**
	 * Set the new current position of the stream
	 * If the new position is bad - throws IOException
	 * 
	 * @param newPosition
	 * @throws IOException
	 */
	public void position( int newPosition ) throws IOException {
		if (newPosition < 0 || newPosition > this.length)
			throw new IOException("Bad newPosition value when setting the new position in the AsnInputStream");
		
		this.pos = newPosition;
	}

	/**
	 * Return the count of available bytes to read
	 * 
	 * @return
	 */
	@Override
	public int available() {
		return this.length - this.pos;
	}

	/**
	 * Advance the stream current position for byteCount bytes
	 * If the new position is bad - throws IOException
	 * 
	 * @param byteCount
	 * @throws IOException
	 */
	public void advance(int byteCount) throws IOException {
		this.position(this.pos + byteCount);
	}

	@Override
	public long skip(long n) throws IOException {
		if (n < 0)
			n = 0;
		int newPosition = this.pos + (int) n;
		if (newPosition < 0 || newPosition > this.length)
			newPosition = this.length;

		long skipCnt = newPosition - this.pos;
		this.pos = newPosition;

		return skipCnt;
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	/**
	 * Get a byte from stream and return it.
	 * If end of stream - throws IOException
	 * 
	 * @return
	 */
	@Override
	public int read() throws IOException {
		if (this.available() == 0)
			throw new EOFException("AsnInputStream has reached the end");
		
		return this.buffer[this.start + this.pos++];
	}

	/**
	 * Fill the byte with bytes from the stream. If stream contains not enough
	 * data - only the part of array is filled
	 * 
	 * @param b
	 *            The byte array to be filled by data
	 * @param off
	 *            Offset of byte array from which fill the array
	 * @param len
	 *            Bytes count to fill
	 * @return Bytes count that have really read
	 * @throws IOException
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		
		if (len > b.length)
			len = b.length;
		
		int cnt = this.available();
		if (cnt > len)
			cnt = len;
		
		if (b == null || off < 0 || len < 0 || off + len > b.length)
			throw new EOFException("Target byte array is null or bad off or len values");

		System.arraycopy(this.buffer, this.start + this.pos, b, off, cnt);
		this.pos += cnt;
		
		return cnt;
	}
	
	/**
	 * Fill the byte with bytes from the stream. If stream contains not enough
	 * data - only the part of array is filled
	 * 
	 * @param b
	 *            The byte array to be filled by data
	 * @return Bytes count that have really read
	 * @throws IOException
	 */
	@Override
	public int read(byte[] b) throws IOException {

		if (b == null )
			throw new EOFException("Target byte array is null");

		return this.read(b, 0, b.length);
	}

	/**
	 * Reads the tag field. Returns the tag value.
	 * Tag class and primitive / constructive mark can be get then by getTagClass() and isTagPrimitive() methods
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readTag() throws IOException {
		byte b = (byte) this.read();

		this.tagClass = (b & Tag.CLASS_MASK) >> 6;
		this.pCBit = (b & Tag.PC_MASK) >> 5;

		this.tag = b & Tag.TAG_MASK;

		// For larger tag values, the first octet has all ones in bits 5 to 1,
		// and the tag value is then encoded in
		// as many following octets as are needed, using only the least
		// significant seven bits of each octet,
		// and using the minimum number of octets for the encoding. The most
		// significant bit (the "more"
		// bit) is set to 1 in the first following octet, and to zero in the
		// last.
		if (tag == Tag.TAG_MASK) {
			byte temp;
			tag = 0;
			do {
				temp = (byte) this.read();
				tag = (tag << 7) | (0x7F & temp);
			} while (0 != (0x80 & temp));
		}

		return tag;
	}

	public int getTagClass() {
		return tagClass;
	}

	public int getTag() {
		return tag;
	}

	public boolean isTagPrimitive() {
		return pCBit == Tag.PC_PRIMITIVITE;
	}

	/**
	 * Reads and returns the length field.
	 * In case of indefinite length returns Tag.Indefinite_Length value
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readLength() throws IOException {
		int length = 0;

		byte b = (byte) this.read();

		// This is short form. The short form can be used if the number of
		// octets in the Value part is less than or
		// equal to 127, and can be used whether the Value part is primitive or
		// constructed. This form is identified by
		// encoding bit 8 as zero, with the length count in bits 7 to 1 (as
		// usual, with bit 7 the most significant bit
		// of the length).
		if ((b & 0x80) == 0) {
			return b;
		}

		// This is indefinite form. The indefinite form of length can only be
		// used (but does not have to be) if the V
		// part is constructed, that
		// is to say, consists of a series of TLVs. In the indefinite form of
		// length the first bit of the first octet is
		// set to 1, as for the long form, but the value N is set to zero.
		b = (byte) (b & 0x7F);
		if (b == 0) {
			return Tag.Indefinite_Length;
		}

		// If bit 8 of the first length octet is set to 1, then we have the long
		// form of length. In long form, the first
		// octet encodes in its remaining seven bits a value N which is the
		// length of a series of octets that themselves
		// encode the length of the Value part.
		byte temp;
		for (int i = 0; i < b; i++) {
			temp = (byte) this.read();
			length = (length << 8) | (0x00FF & temp);
		}

		return length;
	}
	
	/**
	 * This method can be invoked after the sequence tag has been read
	 * Returns the AsnInputStream that contains the sequence data
	 * The origin stream advances to the begin of the next record  
	 * 
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public AsnInputStream readSequenceStream() throws AsnException, IOException {

		int length = readLength();
		return this.readSequenceStreamData(length);
	}
	
	/**
	 * This method can be invoked after the sequence tag has been read
	 * Returns the byte array that contains the sequence data
	 * The origin stream advances to the begin of the next record
	 *   
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public byte[] readSequence() throws AsnException, IOException {

		int length = readLength();
		return this.readSequenceData(length);
	}

	/**
	 * This method can be invoked after the sequence tag and length has been
	 * read. Returns the AsnInputStream that contains the sequence data. The origin
	 * stream advances to the begin of the next record
	 * 
	 * @param length
	 *            The sequence length
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public AsnInputStream readSequenceStreamData(int length) throws AsnException, IOException {

		if (length == Tag.Indefinite_Length) {
			return this.readSequenceIndefinite();
		} else {
			int startPos = this.pos;
			this.advance(length);
			return new AsnInputStream(this, startPos, length);
		}
	}
	
	/**
	 * This method can be invoked after the sequence tag and length has been
	 * read. Returns the byte stream that contains the sequence data. The origin
	 * stream advances to the begin of the next record
	 * 
	 * @param length
	 *            The sequence length
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public byte[] readSequenceData(int length) throws AsnException, IOException {

		AsnInputStream ais = this.readSequenceStreamData(length);
		byte[] res = new byte[ais.length];
		System.arraycopy(ais.buffer, ais.start + ais.pos, res, 0, ais.length);
		return res;
	}

	public AsnInputStream readSequenceIndefinite() throws AsnException, IOException {
		
		int startPos = this.pos;
		this.advanceIndefiniteLength();
		return new AsnInputStream(this, startPos, this.pos - startPos - 2);
	}

	public byte[] readIndefinite() throws AsnException, IOException {
		
		int startPos = this.pos;
		this.advanceIndefiniteLength();
		
		byte[] res = new byte[this.pos - startPos - 2];
		System.arraycopy(this.buffer, this.start + startPos, res, 0, this.pos - startPos - 2);
		return res;
	
	}
	
	private void advanceIndefiniteLength() throws AsnException, IOException {
		
		while (this.available() > 0) {

			// found End-of-contents tag
			int tag = this.readTag();
			if (tag == 0 && this.tagClass == 0) {
				if (this.read() == 0)
					return;
				else
					throw new AsnException("End-of-contents tag must have the zero length");
			}			
			
			int length = this.readLength();
			if (length == Tag.Indefinite_Length)
				this.advanceIndefiniteLength();
			else
				this.advance(length);
		}
	}
	
	/**
	 * Skip length and content fields of primitive and constructed element (definite and indefinite length supported)
	 * 
	 * @throws IOException
	 * @throws AsnException
	 */
	public void advanceElement() throws IOException, AsnException {
		int length = this.readLength();
		this.advanceElementData(length);
	}
	
	/**
	 * Skip content field of primitive and constructed element (definite and indefinite length supported)
	 * 
	 * @param length
	 * @throws IOException
	 * @throws AsnException
	 */
	public void advanceElementData(int length) throws IOException, AsnException {
		if( length==Tag.Indefinite_Length )
			this.advanceIndefiniteLength();
		else
			this.advance(length);
	}
	
	public boolean readBoolean() throws AsnException, IOException {
		
		int length = readLength();
		return this.readBooleanData(length);
	}
	
	public boolean readBooleanData(int length) throws AsnException, IOException {

		if (this.pCBit != 0 || length != 1)
			throw new AsnException("Failed when parsing the Boolean field: this field must be primitive and the length must be equal 1");
		
		byte temp = (byte) this.read();

		// If temp is not zero stands for true irrespective of actual Value
		return (temp != 0);
	}
	
	public long readInteger() throws AsnException, IOException {
		
		int length = this.readLength();
		return this.readIntegerData(length);
	}
	
	public long readIntegerData(int length) throws AsnException, IOException {
		long value = 0;
		byte temp;

		if (this.pCBit != 0 || length == 0 || length == Tag.Indefinite_Length)
			throw new AsnException("Failed when parsing the Interger field: this field must be primitive and have the length more then zero");

		temp = (byte) this.read();
		value = temp;

		for (int i = 0; i < length - 1; i++) {
			temp = (byte) this.read();
			value = (value << 8) | (0x00FF & temp);
		}

		return value;
	}
	
	public double readReal() throws AsnException, IOException {

		int length = readLength();
		return readRealData(length);
	}
	
	public double readRealData(int length) throws AsnException, IOException {

		if (this.pCBit != 0 || length == Tag.Indefinite_Length)
			throw new AsnException("Failed when parsing the Real field: this field must be primitive");
		
		// universal part
		if (length == 0) {
			// yeah, nice
			return 0.0;
		}

		if (length == 1) {
			// +INF/-INF
			int b = this.read() & 0xFF;
			if (b == 0x40) {
				return Double.POSITIVE_INFINITY;
			} else if (b == 0x41) {
				return Double.NEGATIVE_INFINITY;
			} else {
				throw new AsnException(
						"Failed when parsing the Real field: Real length indicates positive/negative infinity, but value is wrong: "
								+ Integer.toBinaryString(b));
			}
		}
		int infoBits = this.read();
		// substract on for info bits
		length--;

		// only binary has first bit of info set to 1;
		// now the tricky part, this takes into account base10
		if ((infoBits & 0xC0) == 0) {
			
			// FIXME: add check on boundry of simple length
			// encoded as char string
			// IA5 == ASCII...?
			// .............................
			String nrRep = new String(this.buffer, this.start + this.pos, length, _REAL_BASE10_CHARSET);
			// this will swallow NR(1-3) and give proper double :)
			return Double.parseDouble(nrRep);
			// .............................
			
		} else if((infoBits & 0x80) == 0x80) {

			// encoded binary - mantisa and all that funny digits.
			// the REAL type has been semantically equivalent to the
			// type:
			// [UNIVERSAL 9] IMPLICIT SEQUENCE {
			// mantissa INTEGER (ALL EXCEPT 0),
			// base INTEGER (2|10),
			// exponent INTEGER }
			// sign x N x (2 ^ scale) x (base ^ E); --> base ^ E == 2 ^(E+x) ==
			// where x
			int tmp = 0;

			int signBit = (infoBits & BERStatics.REAL_BB_SIGN_MASK) << 1;
			// now lets determine length of e(exponent) and n(positive integer)
			long e = 0;
			int s = (infoBits & BERStatics.REAL_BB_SCALE_MASK) >> 2;

			tmp = infoBits & BERStatics.REAL_BB_EE_MASK;
			if (tmp == 0x0) {
				e = this.read() & 0xFF;
				length--;
				// real representation
			} else if (tmp == 0x01) {
				e = (this.read() & 0xFF) << 8;
				length--;
				e |= this.read() & 0xFF;
				length--;
				if (e > 0x7FF) {

					// to many bits... Double
					throw new AsnException(
							"Exponent part has to many bits lit, allowed are 11, present: "
									+ Long.toBinaryString(e));
				}
				// prepare E to become bits - this may cause loose of data,
				e &= 0x7FF;
			} else {
				// this is too big for java to handle.... we can have up to 11
				// bits..
				throw new AsnException(
						"Exponent part has to many bits lit, allowed are 11, but stream indicates 3 or more octets");
			}
			// now we may read up to 52bits
			// 7*8 == 56, we need up to 52
			if (length > 7) {
				throw new AsnException(
						"Length exceeds JAVA double mantisa size");
			}

			long n = 0;
			while (length > 0) {
				--length;
				long readV = (((long) this.read() << 32) >>> 32) & 0xFF;

				readV = readV << (length * 8);

				n |= readV;
			}

			// check for possible overflow
			if ((n & 0x0FFFFFFF) > 4503599627370495L) { // num is 11 bits lit to
				// "1"
				throw new AsnException("Overflow on mantisa");
			}
			// we have real part, now lets add that scale; this is M x (2^F),
			// which essentialy is bit shift :)
			int shift = (int) Math.pow(2, s) - 1; // -1 for 2, where we dont
			// shift
			n = n << (shift); // this might be bad code.

			// now lets take care of different base, we are base2: base8 ==
			// base2^3,base16== base2^4
			int base = (infoBits & BERStatics.REAL_BB_BASE_MASK) >> 4;
			// is this correct?
			if (base == 0x01) {
				e = e * 3; // (2^3)^e
			} else if (base == 0x10) {
				e = e * 4; // (2^4)^e
			}
			// do check again.
			if (e > 0x7FF) {
				// to many bits... Double
				throw new AsnException(
						"Exponent part has to many bits lit, allowed are 11, present: "
								+ Long.toBinaryString(e));
			}

			// double is 8bytes
			byte[] doubleRep = new byte[8];
			// set sign, no need to shift
			doubleRep[0] = (byte) (signBit);
			// now get first 7 bits of e;
			doubleRep[0] |= ((e >> 4) & 0xFF);
			doubleRep[1] = (byte) ((e & 0x0F) << 4);
			// from back its easier
			doubleRep[7] = (byte) n;
			doubleRep[6] = (byte) (n >> 8);
			doubleRep[5] = (byte) (n >> 16);
			doubleRep[4] = (byte) (n >> 24);
			doubleRep[3] = (byte) (n >> 32);
			doubleRep[2] = (byte) (n >> 40);
			doubleRep[1] |= (byte) ((n >> 48) & 0x0F);
			ByteBuffer bb = ByteBuffer.wrap(doubleRep);
			return bb.getDouble();
		} else {
			throw new AsnException("Failed when parsing the Real field: Unknown infoBits: " + infoBits);
		}
	}

	public BitSetStrictLength readBitString() throws AsnException, IOException {
		
		int length = this.readLength();
		return this.readBitStringData(length);
	}
	
	public BitSetStrictLength readBitStringData(int length) throws AsnException, IOException {

		BitSetStrictLength bitSet = new BitSetStrictLength(0);
		int bitCount = this._readBitString(bitSet, length, 0);
		bitSet.setStrictLength(bitCount);
		
		return bitSet;
	}

	@Deprecated
	public void readBitString(BitSet bitSet) throws AsnException, IOException {
		
		int length = this.readLength();
		this.readBitStringData(bitSet, length);
	}

	@Deprecated
	public void readBitStringData(BitSet bitSet, int length) throws AsnException, IOException {
		this._readBitString(bitSet, length, 0);
	}

	@Deprecated
	public void readBitStringData(BitSet bitSet, int length, boolean isTagPrimitive) throws AsnException, IOException {
		if (isTagPrimitive)
			this.pCBit = 0;
		else
			this.pCBit = 1;
		
		this._readBitString(bitSet, length, 0);
	}

	private int _readBitString(BitSet bitSet, int length, int counter) throws AsnException,
			IOException {

		int bits = 0;

		if (this.pCBit == 0) {
			int pad = this.read();

			// TODO We are assuming that there is always pad, even if it is 00.
			// This may not be true for some
			// Constructed
			// BitString where padding is only applied to last TLV. In which
			// case this algo is incorrect
			for (int count = 1; count < (length - 1); count++) {
				byte dataByte = (byte) this.read();
				for (bits = 0; bits < 8; bits++) {
					if (0 != (dataByte & (0x80 >> bits))) {
						bitSet.set(counter);
					}
					++counter;
				}
			}

			byte lastByte = (byte) this.read();
			for (bits = 0; bits < (8 - pad); bits++) {
				if (0 != (lastByte & (0x80 >> bits))) {
					bitSet.set(counter);
				}
				++counter;
			}

			return counter;

		} else {
			if (length == Tag.Indefinite_Length) {
				while (true) {
					int tag = this.readTag();
					if (tag == 0) {
						length = this.read();
						if (length == 0)
							break;
						else
							throw new AsnException("Error while decoding the bit-string: End-of-contents tag must have the zero length");
					}

					if (tag != Tag.STRING_BIT || this.tagClass != Tag.CLASS_UNIVERSAL)
						throw new AsnException("Error while decoding the bit-string: subsequent bit string tag must be CLASS_UNIVERSAL - STRING_BIT");

					int length2 = this.readLength();
					counter = _readBitString(bitSet, length2, counter);
				}
			} else {
				int startPos = this.pos; 
				while (true) {
					if (this.pos > startPos + length)
						throw new AsnException("Error while decoding the bit-string: constructed bit-string content do not fit its length");
					if (this.pos == startPos + length)
						break;

					int tag = this.readTag();
					if (tag != Tag.STRING_BIT || this.tagClass != Tag.CLASS_UNIVERSAL)
						throw new AsnException("Error while decoding the bit-string: subsequent bit string string tag must be CLASS_UNIVERSAL - STRING_BIT");

					int length2 = this.readLength();
					if (this.pos + length2 > startPos + length)
						throw new AsnException("Error while decoding the bit-string: subsequent bit string is unconsistent");
					
					counter = _readBitString(bitSet, length2, counter);
				}
			}
			return counter;
		}
	}
	
	public byte[] readOctetString() throws AsnException, IOException {
		
		int length = this.readLength();
		return this.readOctetStringData(length);
	}
		
	@Deprecated
	public void readOctetString(OutputStream outputStream) throws AsnException, IOException {
		
		int length = this.readLength();
		this.readOctetStringData(outputStream, length);
	}

	public byte[] readOctetStringData(int length) throws AsnException, IOException {

		if (this.pCBit == 0) {
			if (length == Tag.Indefinite_Length)
				throw new AsnException("Error while decoding the octet-string: primitive with Indefinite_Length");
			byte[] buf = new byte[length];
			int cnt = this.read(buf);
			if (cnt != length)
				throw new AsnException("Error while decoding the octet-string: not enouph data for the octet string");
			return buf;
		} else {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			this._readOctetString(outputStream, length);
			return outputStream.toByteArray();
		}
	}

	@Deprecated
	public void readOctetStringData(OutputStream outputStream, int length) throws AsnException, IOException {

		this._readOctetString(outputStream, length);
	}

	@Deprecated
	public void readOctetStringData(OutputStream outputStream, int length, boolean isTagPrimitive) throws AsnException, IOException {
		
		if (isTagPrimitive)
			this.pCBit = 0;
		else
			this.pCBit = 1;

		this._readOctetString(outputStream, length);
	}
	
	private void _readOctetString(OutputStream outputStream, int length) throws AsnException, IOException {

		if (this.pCBit == 0) {
			
			this.fillOutputStream(outputStream, length);
		} else {
			
			if (length == Tag.Indefinite_Length) {
				while (true) {
					int tag = this.readTag();
					if (tag == 0) {
						length = this.read();
						if (length == 0)
							break;
						else
							throw new AsnException("Error while decoding the octet-string: End-of-contents tag must have the zero length");
					}

					if (tag != Tag.STRING_OCTET || this.tagClass != Tag.CLASS_UNIVERSAL)
						throw new AsnException("Error while decoding the octet-string: subsequent octet string tag must be CLASS_UNIVERSAL - STRING_BIT");

					int length2 = this.readLength();
					this._readOctetString(outputStream, length2);
				}
			} else {
				int startPos = this.pos; 
				while (true) {
					if (this.pos == startPos + length)
						break;

					int tag = this.readTag();
					if (tag != Tag.STRING_OCTET || this.tagClass != Tag.CLASS_UNIVERSAL)
						throw new AsnException("Error while decoding the octet-string: subsequent octet string string tag must be CLASS_UNIVERSAL - STRING_BIT");

					int length2 = this.readLength();
					if (this.pos + length2 > startPos + length)
						throw new AsnException("Error while decoding the octet-string: subsequent octet string is unconsistent");
					
					this._readOctetString(outputStream, length2);
				}
			}
		}
	}	

	// private helper methods -------------------------------------------------
	private void fillOutputStream(OutputStream stream, int length)
			throws AsnException, IOException {
		byte[] dataBucket = new byte[DATA_BUCKET_SIZE];
		int readCount;

		while (length != 0) {
			int cnt = length < DATA_BUCKET_SIZE ? length : DATA_BUCKET_SIZE;
			readCount = read(dataBucket, 0, cnt);
			if (readCount < cnt)
				throw new AsnException("input stream has reached the end");
			stream.write(dataBucket, 0, readCount);
			length -= readCount;
		}
	}
	
	public void readNull() throws AsnException, IOException {
//		int tagValue = this.readTag();

		int length = readLength();
		this.readNullData(length);
	}

	public void readNullData(int length) throws AsnException, IOException {
		if (this.pCBit != 0 || length != 0)
			throw new AsnException("Failed when parsing the NULL field: this field must be primitive and the length must be equal 0");
	}
	
	public long[] readObjectIdentifier() throws AsnException, IOException {

		int length = readLength();
		return this.readObjectIdentifierData(length);
	}
		
	public long[] readObjectIdentifierData(int length) throws AsnException, IOException {

		if (this.pCBit != 0 || length == Tag.Indefinite_Length)
			throw new AsnException("Failed when parsing the ObjectIdentifier field: this field must be primitive and the length must be defined");
		
		byte[] data = new byte[length];
		read(data);

		length = 2;
		for (int i = 1; i < data.length; ++i) {
			if (data[i] >= 0)
				++length;
		}
		
		long[] oids = new long[length];
		int b = 0x00FF & data[0];

		// The first octet has value 40 * value1 + value2.
		oids[0] = b / 40;
		if (oids[0] == 0 || oids[0] == 1)
			oids[1] = b % 40;
		else {
			oids[0] = 2;
			oids[1] = b - 80;
		}

		int v = 0;
		length = 2;
		for (int i = 1; i < data.length; ++i) {

			byte b1 = data[i];
			if ((b1 & 0x80) != 0x0) {
				v = (v << 7) | ((b1 & 0x7F));
			} else {
				v = (v << 7) | (b1 & 0x7F);
				oids[length++] = v;
				v = 0;
			}
		}
		
		if (length == oids.length)
			return oids;
		else {
			long[] oids2 = new long[length];
			System.arraycopy(oids, 0, oids2, 0, length);
			return oids2;
		}
	}

	public String readIA5String() throws AsnException, IOException {

		int length = readLength();
		return readString(BERStatics.STRING_IA5_ENCODING, Tag.STRING_IA5, length);
	}

	public String readIA5StringData(int length) throws AsnException, IOException {

		return readString(BERStatics.STRING_IA5_ENCODING, Tag.STRING_IA5, length);
	}

	public String readUTF8String() throws AsnException, IOException {

		int length = readLength();
		return readString(BERStatics.STRING_UTF8_ENCODING, Tag.STRING_UTF8, length);
	}

	public String readUTF8StringData(int length) throws AsnException, IOException {

		return readString(BERStatics.STRING_UTF8_ENCODING, Tag.STRING_UTF8, length);
	}

	public String readGraphicString() throws AsnException, IOException {

		int length = readLength();
		return readString(BERStatics.STRING_IA5_ENCODING, Tag.STRING_GRAPHIC, length);
	}

	public String readGraphicStringData(int length) throws AsnException, IOException {

		return readString(BERStatics.STRING_IA5_ENCODING, Tag.STRING_GRAPHIC, length);
	}

	private String readString(String charset, int tagValue, int length) throws IOException,
			AsnException {
		
		if (pCBit == 0) {
			byte[] buf = new byte[length];
			int readCnt = this.read(buf);
			if (readCnt < length)
				throw new AsnException("Error decoding string fieald: not enough data in the stream");
			
			String s = new String(buf, charset);
			return s;
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			this.readConstructedString(bos, tagValue, length);

			String s = new String(bos.toByteArray(), charset);
			return s;
		}
	}

	private void readConstructedString(ByteArrayOutputStream bos, int parentTag, int length)
			throws AsnException, IOException {

		AsnInputStream ais = this.readSequenceStreamData(length);		
		
		while(true) {
			if (ais.available() == 0)
				break;
			
			int localTag = ais.readTag();
			if (parentTag != localTag)
				throw new AsnException("Error decoding string fieald: Parent tag=" + parentTag + ", does not match member tag=" + localTag);
			
			int localLength = ais.readLength();
			if (ais.pCBit == 0) {
				byte[] buf = new byte[localLength];
				int readCnt = ais.read(buf);
				if (readCnt < localLength)
					throw new AsnException("Error decoding string fieald: not enough data in the stream");
				bos.write(buf);
			} else {
				ais.readConstructedString(bos, parentTag, localLength);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Size=");
		sb.append(this.length);
		sb.append(", Pos=");
		sb.append(this.pos);
		sb.append(", Tag=");
		sb.append(this.tag);
		sb.append(", TagClass=");
		sb.append(this.tagClass);
		sb.append(", pCBit=");
		sb.append(this.pCBit);
		sb.append("\n");

		byte[] bf = new byte[this.length];
		System.arraycopy(this.buffer, this.start, bf, 0, this.length);
		sb.append(AsnOutputStream.arrayToString(bf));

		return sb.toString();
	}
}

