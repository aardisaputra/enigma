package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Austin Nicola Ardisaputra
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }

        _prevRotorRead = "";
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = readConfig();
        _input.useDelimiter("\n");
        while (_input.hasNext()) {
            String curr = _input.next();
            if (curr.length() > 1 && curr.charAt(0) == '*') {
                setUp(m, curr);
            } else if (curr.isEmpty()) {
                _output.println();
            } else {
                System.setOut(_output);
                String output = m.convert(curr);
                printMessageLine(output);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabetStr = _config.nextLine().trim();
            assert !alphabetStr.contains("*");
            assert !alphabetStr.contains("(");
            assert !alphabetStr.contains(")");
            _alphabet = new Alphabet(alphabetStr);

            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();

            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            ArrayList<String> rotorStr = new ArrayList<String>();

            while (_config.hasNext()) {
                _config.useDelimiter("\n");
                String curr = _config.next();
                if (curr.trim().equals("")) {
                    continue;
                } else if (curr.trim().charAt(0) != '(') {
                    rotorStr.add(curr);
                } else {
                    String temp = rotorStr.get(rotorStr.size() - 1);
                    rotorStr.set(rotorStr.size() - 1, temp + curr);
                }
            }

            for (String s : rotorStr) {
                _prevRotorRead = s;
                allRotors.add(readRotor());
            }

            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException | AssertionError e) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String desc = _prevRotorRead;
            Scanner descScanner = new Scanner(desc);
            descScanner.useDelimiter("\\s+|\\n");

            String name = "";
            String mainType = "";
            String notches = "";
            String cycles = "";

            boolean first = true;

            if (descScanner.hasNext()) {
                name = descScanner.next();
                String type = descScanner.next();
                mainType = type.substring(0, 1);

                Scanner typeScanner = new Scanner(type.substring(1));

                if (mainType.equals("R")) {
                    assert !typeScanner.hasNext();
                }

                typeScanner.useDelimiter("");

                while (typeScanner.hasNext()) {
                    notches = notches.concat(typeScanner.next());
                }

                while (descScanner.hasNext()) {
                    String n = descScanner.next();
                    assert !first || n.charAt(0) == '(';
                    Pattern p = Pattern.compile("\\(.*?\\)");
                    Matcher m = p.matcher(n);
                    while (m.find()) {
                        cycles = cycles.concat(m.group() + " ");
                    }
                    first = false;
                }
            }

            Permutation perms = new Permutation(cycles, _alphabet);

            if (mainType.charAt(0) == 'M') {
                return new MovingRotor(name, perms, notches);
            } else if (mainType.charAt(0) == 'N') {
                return new FixedRotor(name, perms);
            } else if (mainType.charAt(0) == 'R') {
                assert mainType.equals("R");
                assert perms.derangement();
                return new Reflector(name, perms);
            } else {
                return new Rotor("", perms);
            }
        } catch (NoSuchElementException | AssertionError excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        try {
            M.clearRotors();
            Scanner s = new Scanner(settings);

            if (settings.length() < M.numRotors() + 1) {
                throw error("too little");
            }

            String[] rotors = new String[M.numRotors()];
            String bin = s.next();
            for (int r = 0; r < M.numRotors(); r++) {
                rotors[r] = s.next();
            }

            M.insertRotors(rotors);

            String setting = s.next();
            try {
                assert setting.length() == M.numRotors() - 1;
            } catch (AssertionError e) {
                throw error("wrong setting length");
            }
            M.setRotors(setting);

            if (M.numRotors() < 0) {
                throw new EnigmaException("Incorrect Configuration");
            }

            String plugboard = "";
            while (s.hasNext()) {
                String omg = s.next();
                if (omg.charAt(0) != '(') {
                    try {
                        assert omg.length() == M.numRotors() - 1;
                    } catch (AssertionError e) {
                        throw new EnigmaException("wrg num of alp set");
                    }
                    M.ringSettings(omg);
                } else {
                    assert plugTest(omg);
                    Pattern p = Pattern.compile("\\(.*?\\)");
                    Matcher m = p.matcher(omg);
                    if (m.find()) {
                        plugboard += m.group() + " ";
                    }
                }
            }
            M.setPlugboard(new Permutation(plugboard, _alphabet));
        } catch (Exception | AssertionError e) {
            throw new EnigmaException("wrong input form");
        }
    }

    /** Plugboard test.
     * @param plugboard - the plugboard.
     * @return If plugboard is valid.
     * */
    private boolean plugTest(String plugboard) {
        Scanner s = new Scanner(plugboard.trim());
        s.useDelimiter("");
        int count = 0;
        char curr = '.';

        if (s.next().charAt(0) != '(') {
            return false;
        }
        while (s.hasNext()) {
            curr = s.next().charAt(0);

            if (curr == ')') {
                return count > 0;
            } else if (!_alphabet.contains(curr)) {
                return false;
            } else {
                count++;
            }
        }
        if (curr != ')') {
            return false;
        }
        return true;
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        Scanner s = new Scanner(msg);
        s.useDelimiter("");
        while (s.hasNext()) {
            for (int i = 0; i < 5; i++) {
                String curr = "";
                if (s.hasNext()) {
                    curr = s.next();
                } else {
                    break;
                }
                if (curr.equals(" ")) {
                    i--;
                } else {
                    System.out.print(curr);
                }
            }
            System.out.print(" ");
        }
        System.out.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Backup string for readrotor. */
    private String _prevRotorRead;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
