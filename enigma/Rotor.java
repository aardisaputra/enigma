package enigma;

import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Austin Nicola Ardisaputra
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
        _ringSetting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        int posn = alphabet().toInt(cposn);
        set(posn);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int result = _permutation.permute(p + setting());

        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        result = permutation().wrap(result - setting());
        return result;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int result = _permutation.invert(e + setting());
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        result = permutation().wrap(result - setting());
        return result;
    }

    /** Returns the positions of the notches, as a string giving the letters
     *  on the ring at which they occur. */
    String notches() {
        return "";
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        Scanner s = new Scanner(notches());
        s.useDelimiter("");
        while (s.hasNext()) {
            char notch = s.next().charAt(0);
            int notchInt = alphabet().toInt(notch) - _ringSetting;
            notchInt = permutation().wrap(notchInt);
            if (notchInt == _setting) {
                return true;
            }
        }
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    /** Set alphabet ring setting.
     * @param x - integer value of ring setting
     * */
    public void setRingSetting(int x) {
        _ringSetting = x;
        _setting -= _ringSetting;
    }

    public int getRingSetting() {
        return _ringSetting;
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** Current setting. */
    private int _setting;

    /** Alphabet ring setting. */
    private int _ringSetting;

}
