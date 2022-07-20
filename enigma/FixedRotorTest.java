package enigma;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;

import static enigma.TestUtils.*;
import static org.junit.Assert.assertEquals;

/** The suite of all JUnit tests for the Fixed Rotor class.
 *  @author Austin Nicola Ardisaputra
 */

public class FixedRotorTest {
    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Rotor rotor;
    private String alpha = UPPER_STRING;

    /** Check that rotor has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkRotor(String testId,
                            String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, rotor.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d (%c)", ci, c),
                    ei, rotor.convertForward(ci));
            assertEquals(msg(testId, "wrong inverse of %d (%c)", ei, e),
                    ci, rotor.convertBackward(ei));
        }
    }

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setRotor(String name, HashMap<String, String> rotors) {
        rotor = new FixedRotor(name, new Permutation(rotors.get(name), UPPER));
    }

    @Test
    public void checkRotorAtA() {
        setRotor("Beta", NAVALA);
        checkRotor("Rotor Beta (A)", UPPER_STRING, NAVALA_MAP.get("Beta"));
    }

    @Test
    public void checkRotorAdvance() {
        setRotor("Beta", NAVALA);
        rotor.advance();
        checkRotor("advanced", UPPER_STRING, NAVALA_MAP.get("Beta"));
    }

    @Test
    public void checkRotorSet() {
        setRotor("Gamma", NAVALA);
        rotor.set(25);
        checkRotor("Rotor Gamma set", UPPER_STRING, NAVALZ_MAP.get("Gamma"));
    }

    @Test
    public void checkConvertForward() {
        setRotor("Gamma", NAVALA);
        checkRotor("Rotor I (A)", UPPER_STRING, NAVALA_MAP.get("Gamma"));
        assertEquals(alpha.indexOf('P'),
                rotor.convertForward(alpha.indexOf('T')));
        assertEquals(alpha.indexOf('F'),
                rotor.convertForward(alpha.indexOf('A')));
        assertEquals(alpha.indexOf('A'),
                rotor.convertForward(alpha.indexOf('E')));
    }
}
