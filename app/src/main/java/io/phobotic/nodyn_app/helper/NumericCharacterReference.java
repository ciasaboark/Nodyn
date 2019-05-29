/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.phobotic.nodyn_app.helper;

/**
 * Helper class to encode/decode Strings with Numeric Character References as defined in SGML.
 *
 * @author franz.willer
 * @version $Revision: 2101 $
 * @since 25.11.2005
 */
public class NumericCharacterReference {

    /**
     * Decodes a String with Numeric Character References.
     * <p>
     *
     * @param str        A NCR encoded String
     * @param unknownCh, A character that is used if nnnn of &#nnnn; is not a int.
     * @return The decoded String.
     */
    public static String decode(String str, char unknownCh) {
        if (str == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        int i1 = 0;
        int i2 = 0;

        while (i2 < str.length()) {
            i1 = str.indexOf("&#", i2);
            if (i1 == -1) {
                sb.append(str.substring(i2));
                break;
            }
            sb.append(str.substring(i2, i1));
            i2 = str.indexOf(";", i1);
            if (i2 == -1) {
                sb.append(str.substring(i1));
                break;
            }

            String tok = str.substring(i1 + 2, i2);
            try {
                int radix = 10;
                if (tok.charAt(0) == 'x' || tok.charAt(0) == 'X') {
                    radix = 16;
                    tok = tok.substring(1);
                }
                sb.append((char) Integer.parseInt(tok, radix));
            } catch (NumberFormatException exp) {
                sb.append(unknownCh);
            }
            i2++;
        }
        return sb.toString();
    }

    /**
     * Encode a String with Numeric Character Refernces.
     * <p>
     * Formats each character < 0x20 or > 0x7f to &#nnnn; where nnnn is the char value as int.
     * <p>
     *
     * @param str The raw String
     * @return The encoded String
     */
    public static String encode(String str) {
        char[] ch = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] < 0x20 || ch[i] > 0x7f)
                sb.append("&#").append((int) ch[i]).append(";");
            else
                sb.append(ch[i]);
        }
        return sb.toString();
    }
}