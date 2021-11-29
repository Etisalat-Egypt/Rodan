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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 * @author sergey vetyutnev
 */
public class AsnOutputStream extends OutputStream {

	// charset used to encode real data
	private static final String _REAL_BASE10_CHARSET = "US-ASCII";
	// out patterns for bool
	private static final byte _BOOLEAN_POSITIVE = (byte) 0xFF;
	private static final byte _BOOLEAN_NEGATIVE = 0x00;

	private byte[] buffer;
	
	private int pos;
	private int length;
	
	
	public AsnOutputStream() {
		
		this.length = 256;
		this.buffer = new byte[this.length];
	}
	
	
	/**
	 * Returns the written to the stream data as a byte array
	 * 
	 * @return
	 */
	public byte[] toByteArray() {
		
		if (this.pos == this.length)
			return this.buffer;
		else {
			byte[] res = new byte[this.pos];
			System.arraycopy(this.buffer, 0, res, 0, this.pos);
			return res;
		}
	}
	
	/**
	 * Returns the written bytes count
	 * 
	 * @return
	 */
	public int size() {
		return this.pos;
	}
	
	/**
	 * Clears the data from the stream for reusing it 
	 */
	public void reset() {
		this.pos = 0;
	}
	
	private void checkIncreaseArray( int addCount ) {
		
		if (this.pos + addCount > this.length) {
			int newLength = this.length * 2;
			if (newLength < this.pos + addCount)
				newLength = this.pos + addCount + this.length;
			byte[] newBuf = new byte[newLength];
			System.arraycopy(this.buffer, 0, newBuf, 0, this.buffer.length);
			
			this.buffer = newBuf;
			this.length = newLength;
		}
	}
	
	/**
	 * Writes a byte into the stream
	 * 
	 * @param b
	 */
	@Override
	public void write( int b ) {
		this.checkIncreaseArray(1);
		this.buffer[this.pos++] = (byte)b;
	}
	
	/**
	 * Writes a byte array content into the stream
	 * 
	 * @param b
	 * @param off
	 * @param len
	 */
	@Override
	public void write(byte[] b, int off, int len) {
		this.checkIncreaseArray(len);
		System.arraycopy(b, off, this.buffer, this.pos, len);
		this.pos += len;
	}
	
	/**
	 * Writes a byte array content into the stream
	 * 
	 * @param b
	 */
	@Override
	public void write(byte[] b) {
		this.write(b, 0, b.length);
	}


	/**
	 * Writes a tag field into the atream
	 * 
	 * @param tagClass
	 * @param primitive
	 * @param tag
	 * @throws AsnException
	 */
	public void writeTag(int tagClass, boolean primitive, int tag) throws AsnException {
		
		if (tag < 0)
			throw new AsnException("Tag must not be negative");

		if (tag <= 30) {
			
			int toEncode = (tagClass & 0x03) << 6;
			toEncode |= (primitive ? 0 : 1) << 5;
			toEncode |= tag & 0x1F;
			this.write(toEncode);
		} else {
			
			int toEncode = (tagClass & 0x03) << 6;
			toEncode |= (primitive ? 0 : 1) << 5;
			toEncode |= 0x1F;
			this.write(toEncode);

			int byteArr = 8;
			byte[] buf = new byte[byteArr];
			int pos = byteArr;
			while (true) {
				int dd;
				if (tag <= 0x7F) {
					dd = tag;
					if (pos != byteArr)
						dd = dd | 0x80;
					buf[--pos] = (byte) dd;
					break;
				} else {
					dd = (tag & 0x7F);
					tag >>= 7;
					if (pos != byteArr)
						dd = dd | 0x80;
					buf[--pos] = (byte) dd;
				}
			}
			this.write(buf, pos, byteArr - pos);
		}
	}
	
	/**
	 * Write the length field into the stream
	 * Use Tag.Indefinite_Length for writing the indefinite length 
	 * 
	 * @param v
	 * @throws IOException
	 */
	public void writeLength(int v) throws IOException {
		
		if (v == Tag.Indefinite_Length) {
			
			this.write(0x80);
			return;
		} else if (v > 0x7F) {
            int count;
            byte[] buf = new byte[4];
            if ((v & 0xFF000000) > 0) {
                buf[0] = (byte)((v >> 24) & 0xFF);
                buf[1] = (byte)((v >> 16) & 0xFF);
                buf[2] = (byte)((v >> 8) & 0xFF);
                buf[3] = (byte)(v & 0xFF);
                count = 4;
            } else if ((v & 0x00FF0000) > 0) {
                buf[0] = (byte)((v >> 16) & 0xFF);
                buf[1] = (byte)((v >> 8) & 0xFF);
                buf[2] = (byte)(v & 0xFF);
                count = 3;
            } else if ((v & 0x0000FF00) > 0) {
                buf[0] = (byte)((v >> 8) & 0xFF);
                buf[1] = (byte)(v & 0xFF);
                count = 2;
            } else {
                buf[0] = (byte)(v & 0xFF);
                count = 1;
            }

            this.buffer[pos] = (byte) (0x80 | count);
            for (int i1 = 0; i1 < count; i1++) {
                this.buffer[pos + i1 + 1] = buf[i1];
            }
            this.pos += count + 1;

//			int posLen = this.pos;
//			this.write(0);
//			int count = this.writeIntegerData(v);
//			this.buffer[posLen] = (byte) (count | 0x80);
		} else { // short
			
			this.write(v);
		}
	}

	/**
	 * Start recording of primitive or constructed context with definite field
	 * length. After content writing finishing the user must invoke
	 * FinalizeContent() method with the parameter that is returned by this
	 * method
	 * 
	 * @return
	 */
	public int StartContentDefiniteLength() {

		int lenPos = this.pos;
		this.write(0);
		return lenPos;
	}

	/**
	 * Start recording of constructed context with indefinite field length.
	 * After content writing finishing the user must invoke
	 * FinalizeContent() method with the parameter that is returned by this
	 * method
	 * 
	 * @return
	 */
	public int StartContentIndefiniteLength() {

		this.write(0x80);
		return Tag.Indefinite_Length;
	}
	
	/**
	 * This method must be invoked after finishing the content writing
	 * 
	 * @param lenPos
	 *            This parameter is the the return value of the
	 *            StartContentDefiniteLength() or StartContentIndefiniteLength()
	 *            methods
	 */
	public void FinalizeContent( int lenPos ) {

		if (lenPos == Tag.Indefinite_Length) {
			
			this.write(0);
			this.write(0);
		} else {
			
			int length = this.pos - lenPos - 1;
			if (length <= 0x7F) {
				this.buffer[lenPos] = (byte) length;
			} else {

				int count;
				byte[] buf = new byte[4];
				if ((length & 0xFF000000) > 0) {
					buf[0] = (byte)((length >> 24) & 0xFF);
					buf[1] = (byte)((length >> 16) & 0xFF);
					buf[2] = (byte)((length >> 8) & 0xFF);
					buf[3] = (byte)(length & 0xFF);
					count = 4;
				} else if ((length & 0x00FF0000) > 0) {
					buf[0] = (byte)((length >> 16) & 0xFF);
					buf[1] = (byte)((length >> 8) & 0xFF);
					buf[2] = (byte)(length & 0xFF);
					count = 3;
				} else if ((length & 0x0000FF00) > 0) {
					buf[0] = (byte)((length >> 8) & 0xFF);
					buf[1] = (byte)(length & 0xFF);
					count = 2;
				} else {
					buf[0] = (byte)(length & 0xFF);
					count = 1;
				}
				
				this.checkIncreaseArray(count);
				System.arraycopy(this.buffer, lenPos + 1, this.buffer, lenPos + 1 + count, length);
				this.pos += count;
				this.buffer[lenPos] = (byte) (0x80 | count);
				for (int i1 = 0; i1 < count; i1++) {
					this.buffer[lenPos + i1 + 1] = buf[i1];
				}
			}
		}
	}
	
	
	public void writeSequence(byte[] data) throws IOException, AsnException
	{

		this.writeSequence(Tag.CLASS_UNIVERSAL, Tag.SEQUENCE, data);
	}
	
	public void writeSequence(int tagClass, int tag, byte[] data) throws IOException, AsnException
	{
		
		this.writeTag(tagClass, false, tag);
		this.writeLength(data.length);
		this.write(data);
	}
	
	public int writeSequenceData(byte[] data) throws IOException, AsnException
	{
		
		this.write(data);
		return data.length;
	}
		
	public void writeBoolean(boolean value) throws IOException, AsnException {

		this.writeBoolean(Tag.CLASS_UNIVERSAL, Tag.BOOLEAN, value);
	}
	
	public void writeBoolean(int tagClass, int tag, boolean value) throws IOException, AsnException {

		this.writeTag(tagClass, true, tag);
		writeLength(0x01);

		this.writeBooleanData(value);
	}

	public int writeBooleanData(boolean value) throws IOException {

		int v = value ? _BOOLEAN_POSITIVE : _BOOLEAN_NEGATIVE;
		this.write(v);
		return 1;
	}
	
	public void writeInteger(long value) throws IOException, AsnException {

		this.writeInteger(Tag.CLASS_UNIVERSAL, Tag.INTEGER, value);
	}

	public void writeInteger(int tagClass, int tag, long v) throws IOException, AsnException {
		
		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeIntegerData(v);
		this.FinalizeContent(lenPos);
	}

	public int writeIntegerData(long v) throws IOException {
		
		// if its positive, we need trailing 0x00
		boolean wasPositive = v > 0;
		long v1 = v;
		if (!wasPositive) {
			v1 = -v;
		}
		// determine how much we should write :)
		
		int count = 0;
		if((v1 & 0xFF00000000000000L) != 0 ) {
			count = 8;
		} else if ((v1 & 0xFF000000000000L) != 0 ) {
			count = 7;
		} else if ((v1 & 0xFF0000000000L) != 0 ) {
			count = 6;
		} else if ((v1 & 0xFF00000000L) != 0 ) {
			count = 5;
		} else if ((v1 & 0xFF000000L) != 0 ) {
			count = 4;
		} else if ((v1 & 0xFF0000L) != 0 ) {
			count = 3;
		} else if ((v1 & 0xFF00L) != 0 ) {
			count = 2;
		} else {
			count = 1;
		}
		
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(v);
		byte[] dataToWrite = new byte[8];
		bb.flip();
		bb.get(dataToWrite, 0, 8);
		
		int extraCount = 0;
		if (wasPositive && (dataToWrite[8 - count] & 0x80) != 0) {
			this.write(0);
			extraCount = 1;
		}
		this.write(dataToWrite, 8 - count, count);
		
		return count + extraCount;
	}
	
	public void writeReal(String d, int NR) throws IOException, AsnException {

		this.writeReal(Tag.CLASS_UNIVERSAL, Tag.REAL, d, NR);
	}
	
	public void writeReal(double d) throws IOException, AsnException {

		this.writeReal(Tag.CLASS_UNIVERSAL, Tag.REAL, d);
	}

	public void writeReal(int tagClass, int tag, String d, int NR) throws IOException, AsnException {
		
		this.writeTag(tagClass, true, tag);

		int lenPos = this.pos;
		this.write(0);
		int length = this.writeRealData(d, NR);
		this.buffer[lenPos] = (byte) length;
	}

	public void writeReal(int tagClass, int tag, double d) throws IOException, AsnException {
		
		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeRealData(d);
		this.FinalizeContent(lenPos);
	}
	
	public int writeRealData(String d, int NR) throws AsnException, NumberFormatException, IOException {
		
		// check?
		Double.parseDouble(d);
		// This is weird, BER does not allow L = 0 for zero on integer, but for
		// real it does.... cmon

		byte[] encoded = null;
		try {
			encoded = d.getBytes(_REAL_BASE10_CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}

		// FIXME: add check on length exceeding simple boundry!
		if (encoded.length + 1 > 127) {
			throw new AsnException("Not supported yet, is it even in specs?");
		}

		if (NR > 3 || NR < 1) {
			throw new AsnException("NR is out of range: <0,3>");
		}
		this.write(NR);

		this.write(encoded);

		return encoded.length + 1;
	}

	public int writeRealData(double d) throws AsnException, IOException {
		
		// This is weird, BER does not allow L = 0 for zero on integer, but for
		// real it does.... cmon
		if (d == 0) {
			return 0;
		}

		if (d == Double.POSITIVE_INFINITY) {
			this.write(0x40);
			return 1;
		}

		if (d == Double.NEGATIVE_INFINITY) {
			this.write(0x41);
			return 1;
		}

		// now that sucky stuff with FF,BB,EE ....
		// see:
		// http://en.wikipedia.org/wiki/Single_precision_floating-point_format
		// : http://en.wikipedia.org/wiki/Double_precision_floating-point_format

		// L: 8 for bits(however we have more +1 since exp and mantisa dont end
		// on octet boundry), 1 for info bits
		// get sign;
		long bits = Double.doubleToLongBits(d);
		// get sign bit
		int info = ((int) (bits >> 57)) & 0x40;
		// 10 00 00 01
		// binary+0 sign BB[2] FF[0] EE[2]
		info |= 0x81;

		this.write(info);

		// get 11 bits of exp
		byte[] exp = new byte[2];
		byte[] mantisa = new byte[7];

		// 3 bits
		exp[0] = (byte) (((int) (bits >> 60)) & 0x07);
		exp[1] = (byte) (bits >> 52);
		for (int index = 0; index < 7; index++) {
			mantisa[6 - index] = (byte) (bits >> (index * 8));

		}

		mantisa[0] &= 0x0F;

		this.write(exp);
		this.write(mantisa);

		return 10;
	}

	public void writeBitString(BitSetStrictLength bitString) throws AsnException, IOException {

		this.writeBitString(Tag.CLASS_UNIVERSAL, Tag.STRING_BIT, bitString);
	}

	public void writeBitString(int tagClass, int tag, BitSetStrictLength bitString) throws AsnException, IOException {

		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeBitStringData(bitString);
		this.FinalizeContent(lenPos);
	}

	public int writeBitStringData(BitSetStrictLength bitString) throws AsnException, IOException {

		int bitNumber = bitString.getStrictLength();

		// check if we can write it in simple form
		int octetCount = bitNumber / 8;
		int rest = bitNumber % 8;
		if (rest != 0) {
			octetCount++;
		}

		// the extra octet from bit string
		if (rest == 0) {
			this.write(0);
		} else {
			this.write(8 - rest);
		}

		// this will padd unused bits with zeros
		for (int i = 0; i < octetCount; i++) {
			byte byteRead = _getByte(i * 8, bitString);
			this.write(byteRead);
		}
		
		return octetCount;
	}

	/**
	 * Attepts to read up to 8 bits and store into byte. If less is found, only
	 * those are returned
	 * 
	 * @param startIndex
	 * @param set
	 * @return
	 * @throws AsnException
	 */
	private static byte _getByte(int startIndex, BitSetStrictLength set)
			throws AsnException {

		int count = 8;
		byte data = 0;

//		if (set.length() - 1 < startIndex) {
//			throw new AsnException();
//		}

		while (count > 0) {
			if (set.length() - 1 < startIndex) {
				break;
			} else {
				boolean lit = set.get(startIndex);
				if (lit) {
					data |= (0x01 << (count - 1));
				}

				startIndex++;
				count--;
			}

		}
		return data;
	}

	public void writeOctetString(byte[] value) throws IOException, AsnException {

		this.writeOctetString(Tag.CLASS_UNIVERSAL, Tag.STRING_OCTET, value);
	}

	public void writeOctetString(int tagClass, int tag, byte[] value) throws IOException, AsnException {

		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeOctetStringData(value);
		this.FinalizeContent(lenPos);
	}

	public int writeOctetStringData(byte[] value) {

		// TODO: we now implements the only primitive encoding here. This is
		// enough for ss7. For constructed encoding we should add another method

		this.write(value);
		return value.length;
	}	
	
	@Deprecated
	public void writeStringOctet(int tagClass, int tag, InputStream io) throws AsnException, IOException {
		// TODO Auto-generated method stub
		// if (io.available() <= 127) {

		// its simple :
		this.writeTag(tagClass, true, tag);
		this.writeLength(io.available());
		byte[] data = new byte[io.available()];
		io.read(data);
		this.write(data);

		// } else {
		// this.writeTag(tagClass, false, tag);
		// // indefinite
		// this.writeLength(0x80);
		// // now lets write fractions, 127 octet chunks
		//
		// int count = io.available();
		// while (count > 0) {
		//
		// byte[] dataChunk = new byte[count > 127 ? 127 : count];
		// io.read(dataChunk);
		// this.writeString(dataChunk, tag);
		// count -= dataChunk.length;
		// }
		// // terminate complex
		// this.write(Tag.NULL_TAG);
		// this.write(Tag.NULL_VALUE);
		//
		// }

	}
	
	@Deprecated
	public void writeStringOctet(InputStream io) throws AsnException,
			IOException {
		this.writeStringOctet(Tag.CLASS_UNIVERSAL,Tag.STRING_OCTET,io);
	}

	@Deprecated
	public void writeStringOctetData(InputStream io) throws AsnException, IOException {
		// TODO Auto-generated method stub
		if (io.available() <= 127) {
			// its simple :
			byte[] data = new byte[io.available()];
			io.read(data);
			this.write(data);
		} else {
			throw new AsnException("writeStringOctetData does not support octet strings more than 126 bytes length");
		}
	}

	public void writeNull() throws IOException, AsnException {
		this.writeNull(Tag.CLASS_UNIVERSAL, Tag.NULL);
	}
	
	public void writeNull(int tagClass, int tag) throws IOException, AsnException {
		writeTag(tagClass, true, tag);
		writeLength(0x00);
	}
	
	@Deprecated
	public void writeNULLData() throws IOException {
	}

	public int writeNullData() {
		return 0;
	}

	public void writeObjectIdentifier(long[] oid) throws IOException, AsnException {

		this.writeObjectIdentifier(Tag.CLASS_UNIVERSAL, Tag.OBJECT_IDENTIFIER, oid);
	}
	
	public void writeObjectIdentifier(int tagClass, int tag, long[] oid) throws IOException, AsnException {

		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeObjectIdentifierData(oid);
		this.FinalizeContent(lenPos);
	}

	public int writeObjectIdentifierData(long[] oidLeafs) throws IOException {
		if (oidLeafs.length < 2) {
			return 0;
		}

		// Let us find out the length first
		int len = 1;
		int i;
		for (i = 2; i < oidLeafs.length; ++i) {
			len += getOIDLeafLength(oidLeafs[i]);
		}

		// Now add the OID bytes
		
		//The first two OID's
		i = (int) (oidLeafs[0] * 40 + oidLeafs[1]);
		this.write(0x00FF & i);

		//Next OID's byte
		for (i = 2; i < oidLeafs.length; ++i) {
			long v = oidLeafs[i];
			len = getOIDLeafLength(v);

			for (int j = len - 1; j > 0; --j) {
				long m = 0x0080 | (0x007F & (v >> (j * 7)));
				this.write((int) m);
			}
			this.write((int) (0x007F & v));
		}
		
		return len;
	}
	
	private int getOIDLeafLength(long leaf) {
		if (leaf < 0) {
			return 10;
		}

		long l = 1;
		int i;
		for (i = 1; i < 9; ++i) {
			l <<= 7;
			if (leaf < l)
				break;
		}
		return i;
	}

	

	// ............................
	public void writeStringUTF8(String data) throws AsnException, IOException {

		this.writeStringUTF8(Tag.CLASS_UNIVERSAL, Tag.STRING_UTF8, data);
	}

	public void writeStringUTF8(int tagClass, int tag, String data) throws IOException, AsnException {

		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeStringUTF8Data(data);
		this.FinalizeContent(lenPos);
	}

	public void writeStringUTF8Data(String data) throws IOException, AsnException {

		byte[] dataEncoded = null;
		try {
			dataEncoded = data.getBytes(BERStatics.STRING_UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}
		this.write(dataEncoded);
	}

	public void writeStringIA5(String data) throws AsnException, IOException {

		this.writeStringIA5(Tag.CLASS_UNIVERSAL, Tag.STRING_IA5, data);
	}

	public void writeStringIA5(int tagClass, int tag, String data) throws IOException, AsnException {

		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeStringIA5Data(data);
		this.FinalizeContent(lenPos);
	}

	public void writeStringIA5Data(String data) throws IOException, AsnException {

		byte[] dataEncoded = null;
		try {
			dataEncoded = data.getBytes(BERStatics.STRING_IA5_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}
		this.write(dataEncoded);
	}

	public void writeStringGraphic(String data) throws AsnException, IOException {

		this.writeStringIA5(Tag.CLASS_UNIVERSAL, Tag.STRING_GRAPHIC, data);
	}

	public void writeStringGraphic(int tagClass, int tag, String data) throws IOException, AsnException {

		this.writeTag(tagClass, true, tag);

		int lenPos = this.StartContentDefiniteLength();
		this.writeStringIA5Data(data);
		this.FinalizeContent(lenPos);
	}

	public void writeStringGraphicData(String data) throws IOException, AsnException {

		byte[] dataEncoded = null;
		try {
			dataEncoded = data.getBytes(BERStatics.STRING_IA5_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}
		this.write(dataEncoded);
	}

	@Deprecated
	public void writeStringBinary(BitSet bitString) throws AsnException, IOException {
		int length = bitString.length();
		BitSetStrictLength bs = new BitSetStrictLength(length);
		for (int i1 = 0; i1 < length; i1++) {
			bs.set(i1, bitString.get(i1));
		}
		this.writeBitString(bs);
	}
	
	@Deprecated
	public void writeStringBinary(int tagClass, int tag, BitSet bitString) throws AsnException, IOException {
		int length = bitString.length();
		BitSetStrictLength bs = new BitSetStrictLength(length);
		for (int i1 = 0; i1 < length; i1++) {
			bs.set(i1, bitString.get(i1));
		}
		this.writeBitString(tagClass, tag, bs);
	}
	
	@Deprecated
	public void writeNULL() throws IOException, AsnException {
		this.writeNull();
	}

	@Deprecated
	public void writeStringBinaryData(BitSet bitString) throws AsnException, IOException {
		int length = bitString.length();
		BitSetStrictLength bs = new BitSetStrictLength(length);
		for (int i1 = 0; i1 < length; i1++) {
			bs.set(i1, bitString.get(i1));
		}
		this.writeBitStringData(bs);
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Size=");
		sb.append(this.pos);
		sb.append("\n");
		
		byte[] bf = this.toByteArray();
		sb.append(arrayToString(bf));
		
		return sb.toString();
	}
	
	protected static String arrayToString(byte[] bf) {

		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		int i1 = 0;
		for (byte b : bf) {
			int ib = (b & 0xFF);
			if (i1 == 0)
				i1 = 1;
			else
				sb.append(", ");
			sb.append(ib);
		}
		sb.append("]");

		return sb.toString();
	}
}

