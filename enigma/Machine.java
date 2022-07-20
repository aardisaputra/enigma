package enigma;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Austin Nicola Ardisaputra
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotor = numRotors;
        _numPawls = pawls;
        _allRotors = allRotors;
        _chosenRotors = new ArrayList<>();
        _perm = new Permutation("", alpha);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotor;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _chosenRotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        try {
            for (String rotor : rotors) {
                for (Rotor r : _allRotors) {
                    if (r.name().equals(rotor)) {
                        try {
                            assert !_chosenRotors.contains(r);
                            _chosenRotors.add(r);
                        } catch (AssertionError e) {
                            throw new EnigmaException("tryna use it twice?");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new EnigmaException("Insertion error");
        }

        try {
            assert _chosenRotors.get(0).reflecting();
        } catch (AssertionError e) {
            throw new EnigmaException("First rotor must be reflector");
        }

        int numMovable = 0;
        for (Rotor r : _chosenRotors) {
            if (r.rotates()) {
                numMovable++;
            }
        }

        try {
            assert numMovable == _numPawls;
        } catch (AssertionError e) {
            throw new EnigmaException("wrong number of movable rotors");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        Scanner s = new Scanner(setting);
        String set1;
        s.useDelimiter("");
        int i = 1;

        while (s.hasNext()) {
            set1 = s.next();
            try {
                assert alphabet().contains(set1.charAt(0));
            } catch (AssertionError e) {
                throw new EnigmaException("No such letter in setting");
            }
            _chosenRotors.get(i).set(set1.charAt(0));
            i++;
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _perm;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _perm = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();

        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        Rotor curr = null;
        Rotor left = null;
        Rotor right = null;

        try {
            for (int i = 0; i < numRotors() - 2; i++) {
                left = _chosenRotors.get(i);
                curr = _chosenRotors.get(i + 1);
                right = _chosenRotors.get(i + 2);

                if (curr.rotates()) {
                    if ((left.rotates() && curr.atNotch()) || right.atNotch()) {
                        curr.advance();
                    }
                }
            }

            _chosenRotors.get(numRotors() - 1).advance();
        } catch (Exception e) {
            throw new EnigmaException("Cannot advance rotors, maybe no rotors");
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        int result = c;
        Rotor curr = null;
        for (int i = numRotors() - 1; i > 0; i--) {
            curr = getRotor(i);
            result = curr.convertForward(result);
        }

        curr = getRotor(0);
        result = curr.convertForward(result);

        for (int i = 1; i < numRotors(); i++) {
            curr = getRotor(i);
            result = curr.convertBackward(result);
        }

        return result;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        Scanner s = new Scanner(msg);
        s.useDelimiter("");
        int curr = 0;
        String result = "";

        while (s.hasNext()) {
            String currStr = s.next();
            if (currStr.equals(" ")) {
                continue;
            }

            try {
                assert alphabet().contains(currStr.charAt(0));
            } catch (AssertionError e) {
                throw new EnigmaException("Msg letter not found");
            }

            char currChar = currStr.charAt(0);
            curr = _alphabet.toInt(currChar);
            curr = convert(curr);
            result += Character.toString(_alphabet.toChar(curr));
        }

        return result;
    }

    /** Clear rotors. */
    public void clearRotors() {
        _chosenRotors = new ArrayList<Rotor>();
    }

    /** Set ring settings.
     * @param sc - ring settings
     * */
    public void ringSettings(String sc) {
        Scanner scan = new Scanner(sc);
        scan.useDelimiter("");
        int i = 1;

        while (scan.hasNext()) {
            String temp = scan.next();
            int tempInt = alphabet().toInt(temp.charAt(0));
            _chosenRotors.get(i).setRingSetting(tempInt);
            i++;
        }
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors. */
    private int _numRotor;

    /** Number of pawls. */
    private int _numPawls;

    /** Collection of rotors. */
    private Collection<Rotor> _allRotors;

    /** Chosen rotors. */
    private ArrayList<Rotor> _chosenRotors;

    /** Current permutation. */
    private Permutation _perm;
}
