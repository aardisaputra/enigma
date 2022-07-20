package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Austin Nicola Ardisaputra
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        chars = chars.trim();
        _arr = new char[chars.length()];
        for (int i = 0; i < chars.length(); i++) {
            _arr[i] = chars.charAt(i);
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _arr.length;
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (char x: _arr) {
            if (x == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _arr[index];
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        for (int i = 0; i < _arr.length; i++) {
            if (_arr[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /** Array containing the characters. */
    private char[] _arr;
}
