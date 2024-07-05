package assembler;

import architecture.Architecture;
import components.Register;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Assembler {
  private ArrayList<String> lines;
  private ArrayList<String> execProgram;
  private final ArrayList<String> objProgram;
  private final Architecture arch;
  private final ArrayList<String> commands;
  private final ArrayList<String> labels;
  private final ArrayList<Integer> labelsAdresses;
  private final ArrayList<String> variables;

  private final int reserved = 40; // Reserved for `imul` command

  public Assembler() {
    lines = new ArrayList<>();
    labels = new ArrayList<>();
    labelsAdresses = new ArrayList<>();
    variables = new ArrayList<>();
    objProgram = new ArrayList<>();
    execProgram = new ArrayList<>();
    arch = new Architecture();
    commands = arch.getCommandsList();
  }

  //getters

  public ArrayList<String> getObjProgram() {
    return objProgram;
  }

  /**
   * These methods getters and set below are used only for TDD purposes
   */

  protected ArrayList<String> getLabels() {
    return labels;
  }

  protected ArrayList<Integer> getLabelsAddresses() {
    return labelsAdresses;
  }

  protected ArrayList<String> getVariables() {
    return variables;
  }

  protected ArrayList<String> getExecProgram() {
    return execProgram;
  }

  protected void setExecProgram(ArrayList<String> lines) {
    this.execProgram = lines;
  }

  /*
   * An assembly program is always in the following template
   * <variables>
   * <commands>
   * Obs.
   * 		variables names are always started with alphabetical char
   * 	 	variables names must contain only alphabetical and numerical chars
   *        variables names never uses any command name
   * 		names ended with ":" identifies labels i.e. address in the memory
   * 		Commands are only that ones known in the architecture. No comments allowed
   *
   * 		The assembly file must have the extention .dsf
   * 		The executable file must have the extention .dxf
   */

  protected void setLines(ArrayList<String> lines) {
    this.lines = lines;
  }

  /**
   * This method reads an entire file in assembly
   *
   * @param filename the name of the file without the extension
   * @throws IOException if the file is not found
   */
  public void read(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename + ".dsf"));
    String linha;
    while ((linha = br.readLine()) != null) {
      lines.add(linha);
    }
    br.close();

  }

  /**
   * This method scans the strings in lines
   * generating, for each one, the corresponding machine code
   */
  public void parse() {
    for (String s : lines) {
      String[] tokens = s.split(" ");
      if (findCommandNumber(tokens) >= 0) { // The line is a command
        processCommand(tokens);
      } else { // The line is not a command: so, it can be a variable or a label
        if (tokens[0].endsWith(":")) {
          // if it ends with : it is a label
          String label = tokens[0].substring(0, tokens[0].length() - 1);
          labels.add(label);
          labelsAdresses.add(objProgram.size());
        } else {
          // Otherwise, it must be a variable
          variables.add(tokens[0]);
        }
      }
    }
  }

  /**
   * This method processes a command, putting it and its parameters (if they have)
   * into the final array
   *
   * @param tokens the command and its parameters
   */
  protected void processCommand(String[] tokens) {
    String command = tokens[0];
    String parameter = tokens.length > 1 ? tokens[1] : "";
    String parameter2 = tokens.length > 2 ? tokens[2] : "";
    String parameter3 = tokens.length > 3 ? tokens[3] : "";
    int commandNumber = findCommandNumber(tokens);

    objProgram.add(Integer.toString(commandNumber));
    if (!parameter.isEmpty()) {
      objProgram.add(parameter.replace("&", ""));
    }
    if (!parameter2.isEmpty()) {
      objProgram.add(parameter2.replace("&", ""));
    }
    if (!parameter3.isEmpty()) {
      objProgram.add(parameter3.replace("&", ""));
    }
  }

  /**
   * This method uses the tokens to search a command
   * in the commands list and returns its id.
   * Some commands (such as move) can have multiple formats
   * and each format has its own id
   *
   * @param tokens the command and its parameters
   * @return the id of the command
   */
  private int findCommandNumber(String[] tokens) {
    int p = commands.indexOf(tokens[0]);
    if (p >= 0) { // The command is in the list
      return p;
    }

    // The command is not in the list. So it must have multiple formats
    return switch (tokens[0]) {
      case "add" -> processAdd(tokens);
      case "sub" -> processSub(tokens);
      case "imul" -> processImul(tokens);
      case "inc" -> processInc(tokens);
      case "move" -> processMove(tokens);
      default -> -1;
    };
  }

  /**
   * This method process an add command.
   * It must have different formats, meaning different internal commands
   *
   * @param tokens the command and its parameters
   * @return the id of the command
   */
  private int processAdd(String[] tokens) {
    String p1 = tokens[1];
    String p2 = tokens[2];

    if (p1.startsWith("%") && p2.startsWith("%")) {
      return commands.indexOf("addRegReg");
    } else if ((p1.startsWith("&") || isVariable(p1)) && p2.startsWith("%")) {
      return commands.indexOf("addMemReg");
    } else if (p1.startsWith("%") && (p2.startsWith("&") || isVariable(p2))) {
      return commands.indexOf("addRegMem");
    }

    return -1;
  }

  /**
   * This method process an sub command.
   * It must have different formats, meaning different internal commands
   *
   * @param tokens the command and its parameters
   * @return the id of the command
   */
  private int processSub(String[] tokens) {
    String p1 = tokens[1];
    String p2 = tokens[2];

    if (p1.startsWith("%") && p2.startsWith("%")) {
      return commands.indexOf("subRegReg");
    } else if ((p1.startsWith("&") || isVariable(p1)) && p2.startsWith("%")) {
      return commands.indexOf("subMemReg");
    } else if (p1.startsWith("%") && (p2.startsWith("&") || isVariable(p2))) {
      return commands.indexOf("subRegMem");
    }

    return -1;
  }

  /**
   * This method process an imul command.
   * It must have different formats, meaning different internal commands
   *
   * @param tokens the command and its parameters
   * @return the id of the command
   */
  private int processImul(String[] tokens) {
    String p1 = tokens[1];
    String p2 = tokens[2];

    if (p1.startsWith("%") && p2.startsWith("%")) {
      return commands.indexOf("imulRegReg");
    } else if ((p1.startsWith("&") || isVariable(p1)) && p2.startsWith("%")) {
      return commands.indexOf("imulMemReg");
    } else if (p1.startsWith("%") && (p2.startsWith("&") || isVariable(p2))) {
      return commands.indexOf("imulRegMem");
    }

    return -1;
  }

  /**
   * This method process an inc command.
   * It must have different formats, meaning different internal commands
   *
   * @param tokens the command and its parameters
   * @return the id of the command
   */
  private int processInc(String[] tokens) {
    String p1 = tokens[1];

    if (p1.startsWith("%")) {
      return commands.indexOf("incReg");
    } else if (p1.startsWith("&") || isVariable(p1)) {
      return commands.indexOf("incMem");
    }

    return -1;
  }

  /**
   * This method process a move command.
   * It must have different formats, meaning differents internal commands
   *
   * @param tokens the command and its parameters
   * @return the id of the command
   */
  private int processMove(String[] tokens) {
    String p1 = tokens[1];
    String p2 = tokens[2];

    if (p1.startsWith("%") && p2.startsWith("%")) {
      return commands.indexOf("moveRegReg");
    } else if ((p1.startsWith("&") || isVariable(p1)) && p2.startsWith("%")) {
      return commands.indexOf("moveMemReg");
    } else if (p1.startsWith("%") && (p2.startsWith("&") || isVariable(p2))) {
      return commands.indexOf("moveRegMem");
    } else if ((p1.startsWith("&") || isVariable(p1)) && (p2.startsWith("&") || isVariable(p2))) {
      return commands.indexOf("moveMemMem");
    } else if (p2.startsWith("%")) {
      return commands.indexOf("moveImmReg");
    } else {
      return commands.indexOf("moveImmMem");
    }
  }

  /**
   * This method creates the executable program from the object program
   * Step 1: check if all variables and labels mentioned in the object
   * program are declared in the source program
   * Step 2: allocate memory addresses (space), from the end to the begin (stack)
   * to store variables
   * Step 3: identify memory positions to the labels
   * Step 4: make the executable by replacing the labels and the variables by the
   * corresponding memory addresses
   *
   * @param filename the name of the executable file
   * @throws IOException if the file is not found
   */
  @SuppressWarnings("unchecked")
  public void makeExecutable(String filename) throws IOException {
    if (!checkLabels()) {
      return;
    }
    execProgram = (ArrayList<String>) objProgram.clone();
    replaceAllVariables();
    replaceLabels(); //replacing all labels by the address they refer to
    replaceRegisters(); //replacing all registers by the register id they refer to
    saveExecFile(filename);
    System.out.println("Finished");
  }

  /**
   * This method replaces all the registers names by its corresponding ids.
   * registers names must be prefixed by %
   */
  protected void replaceRegisters() {
    int p = 0;
    for (String line : execProgram) {
      if (line.startsWith("%")) { //this line is a register
        line = line.substring(1);
        int regId = searchRegisterId(line, arch.getRegistersList());
        String newLine = Integer.toString(regId);
        execProgram.set(p, newLine);
      }
      p++;
    }

  }

  /**
   * This method replaces all variables by their addresses.
   * The addresses o0f the variables startes in the end of the memory
   * and decreases (creating a stack)
   */
  protected void replaceAllVariables() {
    int position = arch.getMemorySize() - 1 - reserved; //the last position in the memory
    for (String var : this.variables) { //scanning all variables
      replaceVariable(var, position);
      position--;
    }
  }

  /**
   * This method saves the execFile collection into the output file
   *
   * @param filename the name of the output file
   * @throws IOException if the file is not found
   */
  private void saveExecFile(String filename) throws IOException {
    File file = new File(filename + ".dxf");
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    for (String l : execProgram) {
      writer.write(l + "\n");
    }
    writer.write("-1"); //-1 is a flag indicating that the program is finished
    writer.close();
  }

  /**
   * This method replaces all labels in the exec program by the corresponding
   * address they refer to
   */
  protected void replaceLabels() {
    int i = 0;
    for (String label : labels) { //searching all labels
      int labelPointTo = labelsAdresses.get(i);
      int lineNumber = 0;
      for (String l : execProgram) {
        if (l.equals(label)) {//this label must be replaced by the address
          String newLine = Integer.toString(labelPointTo); // the address
          execProgram.set(lineNumber, newLine);
          objProgram.set(lineNumber, "&" + newLine);
        }
        lineNumber++;
      }
      i++;
    }

  }

  /**
   * This method replaces all occurences of a variable
   * name found in the object program by his address
   * in the executable program
   *
   * @param var      the variable name
   * @param position the address of the variable
   */
  protected void replaceVariable(String var, int position) {
    int i = 0;
    for (String s : execProgram) {
      if (s.equals(var)) {
        s = Integer.toString(position);
        execProgram.set(i, s);
        objProgram.set(i, "&" + s);
        System.out.println("Variable " + var + " found at position " + i + " and replaced by " + s);
      }
      i++;
    }
  }

  /**
   * This method checks if all labels and variables in the object program were in the source
   * program.
   * The labels and the variables collection are used for this
   */
  protected boolean checkLabels() {
    System.out.println("Checking labels and variables");
    for (String line : objProgram) {
      boolean found = false;
      if (line.startsWith("$")) { //if starts with "&", it is a label or a variable
        line = line.substring(1);
        if (labels.contains(line)) {
          found = true;
        }
        if (variables.contains(line)) {
          found = true;
        }
        if (!found) {
          System.out.println("FATAL ERROR! Variable or label " + line + " not declared!");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * This method searches for a register in the architecture register list
   * by the register name
   *
   * @param line          the register name
   * @param registersList the list of registers
   * @return the register id
   */
  private int searchRegisterId(String line, ArrayList<Register> registersList) {
    int i = 0;
    for (Register r : registersList) {
      if (line.equals(r.getRegisterName())) {
        return i;
      }
      i++;
    }
    return -1;
  }

  private boolean isVariable(String token) {
    return variables.contains(token);
  }

  public static void main(String[] args) throws IOException {
    String filename = "program";
    Assembler assembler = new Assembler();
    System.out.println("Reading source assembler file: " + filename + ".dsf");
    assembler.read(filename);
    System.out.println("Generating the object program");
    assembler.parse();
    System.out.println("Generating executable: " + filename + ".dxf");
    assembler.makeExecutable(filename);
  }
}
