/**
 * 
 */
package org.mobicents.protocols.asn;

/**
 * This class holds some vital BER statics
 * 
 * @author baranowb
 * @author abhayani
 */
public interface BERStatics {

	public static final int REAL_BB_SIGN_POSITIVE = 0x00;
	public static final int REAL_BB_SIGN_NEGATIVE = 0x01;
	public static final int REAL_BB_SIGN_MASK = 0x40;
	/**
	 * Mask for base:
	 * <ul>
	 * <li><b>00</b> base 2</li>
	 * <li><b>01</b> base 8</li>
	 * <li><b>11</b> base 16</li>
	 * </ul>
	 */
	public static final int REAL_BB_BASE_MASK = 0x30;
	/**
	 * Mask for scale:
	 * <ul>
	 * <li><b>00</b> 0</li>
	 * <li><b>01</b> 1</li>
	 * <li><b>10</b> 2</li>
	 * <li><b>11</b> 3</li>
	 * </ul>
	 */
	public static final int REAL_BB_SCALE_MASK = 0xC;
	/**
	 * Mask for encoding exponent (length):
	 * <ul>
	 * <li><b>00</b> on the following octet</li>
	 * <li><b>01</b> on the 2 following octets</li>
	 * <li><b>10</b> on the 3 following octets</li>
	 * <li><b>11</b> encoding of the length of the 2's-complement encoding of
	 * exponent on the following octet, and 2's-complement encoding of exponent
	 * on the other octets</li>
	 * </ul>
	 */
	public static final int REAL_BB_EE_MASK = 0x3;
	/**
	 * Value for real encoding in NR1 format
	 */
	public static final int REAL_NR1 = 0x1;
	/**
	 * Value for real encoding in NR2 format
	 */
	public static final int REAL_NR2 = 0x2;
	/**
	 * Value for real encoding in NR3 format
	 */
	public static final int REAL_NR3 = 0x3;
	/**
	 * Name of encoding scheme for java utils in case of IA5
	 */
	public static final String STRING_IA5_ENCODING = "US-ASCII";
	/**
	 * Name of encoding scheme for java utils in case of UTF8
	 */
	public static final String STRING_UTF8_ENCODING = "UTF-8";

}
