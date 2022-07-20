package enigma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Austin Nicola Ardisaputra
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _notInc = new ArrayList<Character>();
        _perm = new HashMap<Character, Character>();

        for (int i = 0; i < alphabet.size(); i++) {
            _notInc.add(alphabet.toChar(i));
        }

        Scanner s = new Scanner(cycles.trim());
        s.useDelimiter("\\(");

        while (s.hasNext()) {
            String next = s.next();
            addCycle(next);
        }

        for (int i = 0; i < _notInc.size(); i++) {
            _perm.put(_notInc.get(i), _notInc.get(i));
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        char curr = Character.MIN_VALUE;
        char next = Character.MIN_VALUE;
        char tempFirst = Character.MIN_VALUE;

        try {
            assert cycle.trim().length() > 0;
        } catch (AssertionError e) {
            throw new EnigmaException("wtf empty cycle");
        }

        Scanner s2 = new Scanner(cycle.trim());
        s2.useDelimiter("");
        while (s2.hasNext()) {
            if (curr == Character.MIN_VALUE) {
                tempFirst = curr = s2.next().charAt(0);
                try {
                    assert alphabet().contains(curr);
                } catch (AssertionError e) {
                    throw new EnigmaException("bad cycle");
                }
            } else {

                try {
                    assert alphabet().contains(curr);
                } catch (AssertionError e) {
                    throw new EnigmaException("bad cycle");
                }

                alphabet().contains(curr);
                curr = next;
            }
            _notInc.remove((Character) curr);
            next = s2.next().charAt(0);
            if (next == ')') {
                next = tempFirst;

                try {
                    assert !_perm.containsKey(curr);
                    assert alphabet().contains(curr);
                } catch (AssertionError e) {
                    throw new EnigmaException("WTF DUPLiCATE or not contained");
                }

                _perm.put(curr, next);
                return;
            }

            try {
                assert !_perm.containsKey(curr) && alphabet().contains(curr);
            } catch (AssertionError e) {
                throw new EnigmaException("WTF DUPLiCATE or not in alphabet");
            }
            _perm.put(curr, next);
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int real = wrap(p);
        char start = _alphabet.toChar(real);
        return _alphabet.toInt(_perm.get(start));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int real = wrap(c);
        char start = _alphabet.toChar(real);
        char result = Character.MIN_VALUE;
        for (Map.Entry<Character, Character> entry : _perm.entrySet()) {
            if (entry.getValue() == start) {
                result = entry.getKey();
            }
        }
        return _alphabet.toInt(result);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int idx = permute(_alphabet.toInt(p));
        return _alphabet.toChar(idx);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int idx = invert(_alphabet.toInt(c));
        return _alphabet.toChar(idx);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (Map.Entry<Character, Character> entry : _perm.entrySet()) {
            if (entry.getValue() == entry.getKey()) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Hasmap of permutations. */
    private HashMap<Character, Character> _perm;

    /** ArrayList of characters without mappings (maps to themselves). */
    private ArrayList<Character> _notInc;
}
