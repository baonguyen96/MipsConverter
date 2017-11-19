import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class HexMIPSConverter static class to convert from MIPS <-> binary <-> hex
 */
public class HexMIPSConverter {

    public static ArrayList<String[]> table = new ArrayList<>();
    public static ArrayList<String[]> registers = new ArrayList<>();
    public static final int NAME = 0;
    public static final int TYPE = 1;
    public static final int OPCODE = 2;
    public static final int FUNCT = 3;
    public static final int FORMAT = 4;
    public static final int REGISTER_NAME = 0;
    public static final int REGISTER_NUMBER = 1;

    public static void init() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("table.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        scanner.useDelimiter("\n");
        while (scanner.hasNext()) {
            table.add(scanner.next().split("(, )|(,)"));
        }

        try {
            scanner = new Scanner(new File("registers.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        scanner.useDelimiter("\n");
        while (scanner.hasNext()) {
            registers.add(scanner.next().split("(, )|(,)"));
        }
        scanner.close();
    }

    public static String[] searchMipsCode(String subject, int category, String function) {
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i)[category].equalsIgnoreCase(subject)) {
                if (function == null) {
                    return table.get(i);
                }
                if (table.get(i)[FUNCT].equals(function)) {
                    return table.get(i);
                }
            }
        }
        return null;
    }

    /***
     * method: searchRegister find the number or the name of the register
     *
     * @param subject
     *            register name or number string
     * @param category
     *            mode of subject (name or number)
     * @return the name if passed in the number and vice versa
     */
    public static String[] searchRegister(String subject, int category) {
        for (String[] register : registers) {
            if (subject.equals(register[category])) {
                return register;
            }
        }
        return null;
    }

    /***
     * method: mipsToBin convert MIPS instruction into binary string
     *
     * @param mipsCode:
     *            MIPS instruction
     * @return a hexadecimal string
     */
    public static ArrayList<String> mipsToBin(String mipsCode) {
        String binary = "";

        // split the string using a [space] or a [comma-space] as the delimiters
        String[] tokens = mipsCode.split("( )|(, )");

        // check opcode and set type
        String instructionData[] = searchMipsCode(tokens[0], NAME, null);

        // throw new exception if the function name in token[0] is invalid
        if (instructionData == null)
            return null;

        String type = instructionData[TYPE];
        String opcode = hexToBin(instructionData[OPCODE], 6, 1, 4);
        if (opcode == null) {
            return null;
        }
        String rs = "00000", rt = "00000", rd = "00000", immediate = "0000000000000000", sa = "00000";
        String rsReg = "0", rtReg = "0", rdReg = "0";
        String[] format = instructionData[FORMAT].split("\\|");
        if (format[0].equals("null")) {
            if (format.length != tokens.length) {
                return null;
            }
        }
        if (format.length + 1 != tokens.length) {
            if (format[format.length - 1].equals("i(base)")) {
                if (format.length + 2 != tokens.length) {
                    return null;
                }
            } else {
                return null;
            }
        }
        String regInfo[] = new String[2];
        String hex = "";
        for (int i = 0, j = 1; i < format.length; i++, j++) {
            if (format[i].equals("rs")) {
                rsReg = tokens[j];
                regInfo = searchRegister(rsReg, REGISTER_NAME);
                if (regInfo == null) {
                    return null;
                }
                rs = hexToBin(regInfo[REGISTER_NUMBER], 5, 1, 4);
            } else if (format[i].equals("rt")) {
                rtReg = tokens[j];
                regInfo = searchRegister(rtReg, REGISTER_NAME);
                if (regInfo == null) {
                    return null;
                }
                rt = hexToBin(regInfo[REGISTER_NUMBER], 5, 1, 4);
            } else if (format[i].equals("rd")) {
                rdReg = tokens[j];
                regInfo = searchRegister(rdReg, REGISTER_NAME);
                if (regInfo == null) {
                    return null;
                }
                rd = hexToBin(regInfo[REGISTER_NUMBER], 5, 1, 4);
            } else if (format[i].equals("sa")) {
                hex = tokens[j];
                sa = hexToBin(hex, 5, 1, 4);
                if (sa == null) {
                    return null;
                }
            } else if (format[i].equals("i")) {
                hex = tokens[j];
                if (type.equals("J")) {
                    immediate = hexToBin(hex, 26, 1, 4);
                } else {
                    immediate = hexToBin(hex, 16, 1, 4);
                }
                if (immediate == null) {
                    return null;
                }
            } else if (format[i].equals("i(base)")) {
                if (tokens[j].matches("(0x)?[a-fA-F0-9]+\\(\\$[a-z0-9]{0,4}\\)")) {
                    hex = tokens[j].substring(0, tokens[j].indexOf("("));
                    rsReg = tokens[j].substring(tokens[j].indexOf("(") + 1, tokens[j].indexOf(")"));
                    immediate = hexToBin(hex, 16, 1, 4);
                    if (immediate == null) {
                        return null;
                    }
                    regInfo = searchRegister(rsReg, REGISTER_NAME);
                    if (regInfo == null) {
                        return null;
                    }
                    rs = hexToBin(regInfo[REGISTER_NUMBER], 5, 1, 4);
                } else {
                    hex = tokens[j];
                    immediate = hexToBin(hex, 16, 1, 4);
                    if (immediate == null) {
                        return null;
                    }
                    rsReg = tokens[j + 1];
                    regInfo = searchRegister(rsReg, REGISTER_NAME);
                    if (regInfo == null) {
                        return null;
                    }
                    rs = hexToBin(regInfo[REGISTER_NUMBER], 5, 1, 4);
                }
            }

        }

        // convert into binary according to the type of the instruction
        // convert R-type instructions
        if (type.equals("R")) {
			/*
			 * machine: opcode(6) rs(5) rt(5) rd(5) shamt(5) funct(6) MIPS:
			 * function rd rs rt opcode always 0
			 */
            String funct = hexToBin(instructionData[FUNCT], 6, 1, 4);

            binary = opcode + rs + rt + rd + sa + funct;
            ArrayList<String> data = new ArrayList<String>();
            data.add(binary);
            data.add("SPECIAL");
            data.add(opcode);
            data.add(rsReg);
            data.add(rs);
            data.add(rtReg);
            data.add(rt);
            data.add(rdReg);
            data.add(rd);
            data.add("Shamt");
            data.add(sa);
            data.add(instructionData[NAME]);
            data.add(funct);
            return data;
        }

        // convert I-type instructions
        if (type.equals("I")) {
			/*
			 * machine: opcode(6) rs(5) rt(5) immediate(16) MIPS: function rt rs
			 * immediate opcode is function
			 */
            binary = opcode + rs + rt + immediate;
            ArrayList<String> data = new ArrayList<String>();
            data.add(binary);
            data.add(instructionData[NAME]);
            data.add(opcode);
            data.add(rsReg);
            data.add(rs);
            data.add(rtReg);
            data.add(rt);
            data.add("offset");
            data.add(immediate);
            return data;
        }

        // convert J-type instructions
        if (type.equals("J")) {
			/*
			 * machine: opcode(6) address(26) MIPS: function amount opcode is
			 * function
			 */
            binary = opcode + immediate;
            ArrayList<String> data = new ArrayList<String>();
            data.add(binary);
            data.add(instructionData[NAME]);
            data.add(opcode);
            data.add("target");
            data.add(immediate);
            return data;
        }
        return null;
    }

    /***
     * method: mipsToHex convert a MIPS instruction into a hex string
     *
     * @param mipsInstruction:
     *            MIPS instruction as a string
     * @return its hexadecimal form
     */
    public static ArrayList<String> mipsToHex(String mipsInstruction) {
        ArrayList<String> binaryTable = mipsToBin(mipsInstruction);
        if (binaryTable == null) {
            return null;
        }
        String bin = binaryTable.get(0);
        String hex = "";
        for (int i = 0; i < 8; i++) {
            hex += binToHex(bin.substring(i * 4, (i + 1) * 4));
        }
        binaryTable.add(1, "0x" + hex);
        return binaryTable;
    }

    public static String binToHex(String bin) {
        return Integer.toString(Integer.parseInt(bin, 2), 16);
    }

    public static ArrayList<String> hexToMips(String hex) {
        String bin = hexToBin(hex, 32, 8, 8);
        if (bin == null) {
            return null;
        }
        String opcodeBin = bin.substring(0, 6);
        String opcode = binToHex(opcodeBin);
        if (opcode == null) {
            return null;
        }

        ArrayList<String> table = new ArrayList<>();
        if (opcode.equals("0")) {
            String funct = bin.substring(bin.length() - 6, bin.length());
            String instructionData[] = searchMipsCode(opcode, OPCODE, binToHex(funct));
            String[] format = instructionData[FORMAT].split("\\|");
            String rs = bin.substring(6, 11);
            String rsName = searchRegister(binToHex(rs), REGISTER_NUMBER)[REGISTER_NAME];
            if (rsName == null) {
                return null;
            }
            String rt = bin.substring(11, 16);
            String rtName = searchRegister(binToHex(rt), REGISTER_NUMBER)[REGISTER_NAME];
            if (rtName == null) {
                return null;
            }
            String rd = bin.substring(16, 21);
            String rdName = searchRegister(binToHex(rd), REGISTER_NUMBER)[REGISTER_NAME];
            if (rdName == null) {
                return null;
            }
            String sa = bin.substring(21, 26);
            String functName = instructionData[NAME];
            String mipsStatement = functName;
            for (int i = 0; i < format.length; i++) {
                if (format[i].equals("rs")) {
                    mipsStatement += " " + rsName;
                } else if (format[i].equals("rt")) {
                    mipsStatement += " " + rtName;
                } else if (format[i].equals("rd")) {
                    mipsStatement += " " + rdName;
                } else if (format[i].equals("sa")) {
                    mipsStatement += " 0x" + binToHex(sa);
                }
            }
            table.add(bin);
            table.add(mipsStatement);
            table.add("SPECIAL");
            table.add("000000");
            table.add(rsName);
            table.add(rs);
            table.add(rtName);
            table.add(rt);
            table.add(rdName);
            table.add(rd);
            table.add("shamt");
            table.add(sa);
            table.add(functName);
            table.add(funct);
        } else {
            String instructionData[] = searchMipsCode(opcode, OPCODE, null);
            if (instructionData == null) {
                return null;
            }
            if (instructionData[TYPE].equals("I")) {
                String[] format = instructionData[FORMAT].split("\\|");
                String rs = bin.substring(6, 11);
                String rsName = searchRegister(binToHex(rs), REGISTER_NUMBER)[REGISTER_NAME];
                if (rsName == null) {
                    return null;
                }
                String rt = bin.substring(11, 16);
                String rtName = searchRegister(binToHex(rt), REGISTER_NUMBER)[REGISTER_NAME];
                if (rtName == null) {
                    return null;
                }
                String immediate = bin.substring(16, 32);
                String mipsStatement = instructionData[NAME];
                for (int i = 0; i < format.length; i++) {
                    if (format[i].equals("rs")) {
                        mipsStatement += " " + rsName;
                    } else if (format[i].equals("rt")) {
                        mipsStatement += " " + rtName;
                    } else if (format[i].equals("i")) {
                        mipsStatement += " 0x" + binToHex(immediate);
                    } else if (format[i].equals("i(base)")) {
                        mipsStatement += " 0x" + binToHex(immediate) + " " + rsName;
                    }
                }
                table.add(bin);
                table.add(mipsStatement);
                table.add(instructionData[NAME]);
                table.add(opcodeBin);
                table.add(rsName);
                table.add(rs);
                table.add(rtName);
                table.add(rt);
                table.add("immediate");
                table.add(immediate);
            } else {
                String immediate = bin.substring(6, 32);
                String mipsStatement = instructionData[NAME] + " 0x" + binToHex(immediate);
                table.add(bin);
                table.add(mipsStatement);
                table.add(instructionData[NAME]);
                table.add(opcodeBin);
                table.add("immediate");
                table.add(immediate);
            }
        }
        return table;
    }

    /***
     * method: hexToBin (overloaded) convert a hex into a binary with specific
     * number of bits
     *
     * @param hex:
     *            hex string
     * @param bits:
     *            number of bits in the binary string
     * @return a binary string
     */
    private static String hexToBin(String hex, int bits, int min, int max) {
        if (!isValidHex(hex, min, max)) {
            return null;
        }
        hex = extractHexSignature(hex);
        String binary = new BigInteger(hex, 16).toString(2);

        // add preceding 0's if the bit string does not meet the required length
        if (binary.length() < bits) {
            StringBuilder reformattedBinary = new StringBuilder("");

            for (int i = 0; i < (bits - binary.length()); i++)
                reformattedBinary.append("0");

            reformattedBinary.append(binary);
            binary = reformattedBinary.toString();
        }
        return binary;
    }

    /***
     * method: extractHexSignature extract the preceding '0x' from the hex
     * string
     *
     * @param hex:
     *            valid hexadecimal
     * @return the hex string without '0x' in the front
     */
    private static String extractHexSignature(String hex) {
        return hex.replace("0x", "");
    }

    /***
     * method: isValidHex check to see if the hex code the user entered is valid
     *
     * @param input
     *            hex string from the user
     * @return true if input is a valid hex code, false if not
     */
    private static boolean isValidHex(String input, int min, int max) {
        if (input.matches("(0x)?[a-fA-F0-9]{" + min + "," + max + "}")) {
            return true;
        } else {
            return false;
        }
    }

}
