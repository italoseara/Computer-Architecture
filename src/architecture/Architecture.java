package architecture;

import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

//teste de branch
public class Architecture {

  private final boolean simulation;
  private boolean halt;

  private Bus intbus;
  private Bus extbus;

  private Register PC;
  private Register IR;
  private Register RPG;
  private Register RPG1;
  private Register RPG2;
  private Register RPG3;
  private Register flags;
  private Demux demux;

  private Ula ula;

  private Memory statusMemory;
  private Memory memory;
  private int memorySize;

  private ArrayList<String> commandsList;
  private ArrayList<Register> registersList;

  /**
   * Instantiates all components in this architecture
   */
  private void componentsInstances() {
    // Buses
    extbus = new Bus();
    intbus = new Bus();

    // Registers
    PC = new Register("PC", intbus, intbus);
    IR = new Register("IR", extbus, intbus);
    RPG = new Register("RPG0", extbus, extbus);
    RPG1 = new Register("RPG1", extbus, extbus);
    RPG2 = new Register("RPG2", extbus, extbus);
    RPG3 = new Register("RPG3", extbus, extbus);

    // We had to connect the flags to the bus, so it can be useful, although our architecture doesn't do that
    flags = new Register(2, extbus);
    fillRegistersList();

    // ULA
    ula = new Ula(extbus, intbus);
    statusMemory = new Memory(2, extbus);

    // Memory
    memorySize = 128;
    memory = new Memory(memorySize, extbus);

    // Demux
    demux = new Demux(); //this bus is used only for multiple register operations

    fillCommandsList();

    implementMultiplication();
  }

  private void implementMultiplication() {
    // Variables
    int x = 127;
    int y = 126;
    int i = 125;
    int laco = 113;

    memory.getDataList()[x] = 0;
    memory.getDataList()[y] = 0;
    memory.getDataList()[i] = 0;

    // move %RPG0 x
    memory.getDataList()[98] = 10;
    memory.getDataList()[99] = 0;
    memory.getDataList()[100] = x;

    // move %RPG1 y
    memory.getDataList()[101] = 10;
    memory.getDataList()[102] = 1;
    memory.getDataList()[103] = y;

    // move 0 %RPG0
    memory.getDataList()[104] = 12;
    memory.getDataList()[105] = 0;
    memory.getDataList()[106] = 0;

    // move 0 %RPG1
    memory.getDataList()[107] = 12;
    memory.getDataList()[108] = 0;
    memory.getDataList()[109] = 1;

    // add x %RPG0
    memory.getDataList()[110] = 1; // laco:
    memory.getDataList()[111] = x;
    memory.getDataList()[112] = 0;

    // inc %RPG1
    memory.getDataList()[113] = 13;
    memory.getDataList()[114] = 1;

    // move y i
    memory.getDataList()[115] = 23;
    memory.getDataList()[116] = y;
    memory.getDataList()[117] = i;

    // sub %RPG1 i
    memory.getDataList()[118] = 5;
    memory.getDataList()[119] = 1;
    memory.getDataList()[120] = i;

    // jn laco
    memory.getDataList()[121] = 16;
    memory.getDataList()[122] = laco;

    // jmp back
    memory.getDataList()[123] = 15;
    memory.getDataList()[124] = 0; // return address
  }

  /**
   * This method fills the registers list inserting into them all the registers we have.
   * IMPORTANT!
   * The first register to be inserted must be the default RPG
   */
  private void fillRegistersList() {
    registersList = new ArrayList<>();
    registersList.add(RPG); // Default RPG
    registersList.add(RPG1);
    registersList.add(RPG2);
    registersList.add(RPG3);
    registersList.add(IR);
    registersList.add(PC);
    registersList.add(flags);
  }

  /**
   * Constructor that instanciates all components according the architecture diagram
   *
   * @param simulation Set the simulation mode
   */
  public Architecture(boolean simulation) {
    componentsInstances();
    this.simulation = simulation;
  }

  public Architecture() {
    this(false);
  }

  /*
   * Getters
   */
  public Bus getExtbus() {
    return extbus;
  }

  public Bus getIntbus() {
    return intbus;
  }

  public Register getIR() {
    return IR;
  }

  public Register getPC() {
    return PC;
  }

  public Register getRPG() {
    return RPG;
  }

  public Register getRPG1() {
    return RPG1;
  }

  public Register getRPG2() {
    return RPG2;
  }

  public Register getRPG3() {
    return RPG3;
  }

  public Register getFlags() {
    return flags;
  }

  public Demux getDemux() {
    return demux;
  }

  public Ula getUla() {
    return ula;
  }

  public Memory getStatusMemory() {
    return statusMemory;
  }

  public Memory getMemory() {
    return memory;
  }

  public ArrayList<Register> getRegistersList() {
    return registersList;
  }

  public ArrayList<String> getCommandsList() {
    return commandsList;
  }

  public int getMemorySize() {
    return memorySize;
  }

  /**
   * This method fills the commands list arraylist with all commands used in this architecture
   * <p>
   * Command table:
   * add %<regA> %<regB>    || regB <- regA + regB
   * add <mem> %<regA>      || regA <- memory[mem] + regA
   * add %<regA> <mem>      || memory[mem] <- regA + memory[mem]
   * sub <regA> <regB>      || regB <- regA - regB
   * sub <mem> %<regA>      || regA <- memory[mem] - regA
   * sub %<regA> <mem>      || memory[mem] <- regA - memory[mem]
   * imul <mem> %<regA>     || regA <- regA x memory[mem] (product of integers)
   * imul %<regA> <mem>     || memory[mem] <- regA x memory[mem] (product of integers)
   * imul %<regA> <regB>    || regB <- regA x regB (product of integers)
   * move <mem> %<regA>     || regA <- memory[mem]
   * move %<regA> <mem>     || memory[mem] <- regA
   * move %<regA> %<regB>   || regB <- regA
   * move imm %<regA>       || regA <- immediate
   * inc %<regA>            || regA ++
   * inc <mem>              || memory[mem] ++
   * jmp <mem>              || PC <- mem (unconditional deviation)
   * jn <mem>               || if the last operation < 0 then PC <- mem (conditional deviation)
   * jz <mem>               || if the last operation == 0 then PC <- mem (conditional deviation)
   * jnz <mem>              || if the last operation != 0 then PC <- mem (conditional deviation)
   * jeq %<regA> %<regB> <mem>   || if regA == regB then PC <- mem (conditional deviation)
   * jgt %<regA> %<regB> <mem>   || if regA > regB then PC <- mem (conditional deviation)
   * jlw %<regA> %<regB> <mem>   || if regA < regB then PC <- mem (conditional deviation)
   */
  protected void fillCommandsList() {
    commandsList = new ArrayList<>();

    // Hingrid
    commandsList.add("addRegReg");    // 0 ✓
    commandsList.add("addMemReg");    // 1 ✓
    commandsList.add("addRegMem");    // 2 ✓
    commandsList.add("subRegReg");    // 3 ✓
    commandsList.add("subMemReg");    // 4 ✓

    // Wilson
    commandsList.add("subRegMem");    // 5 ✓
    commandsList.add("imulMemReg");   // 6
    commandsList.add("imulRegMem");   // 7
    commandsList.add("imulRegReg");   // 8
    commandsList.add("moveMemReg");   // 9 ✓

    // Italo
    commandsList.add("moveRegMem");   // 10 ✓
    commandsList.add("moveRegReg");   // 11 ✓
    commandsList.add("moveImmReg");   // 12 ✓
    commandsList.add("incReg");       // 13 ✓
    commandsList.add("incMem");       // 14 ✓

    // Luige
    commandsList.add("jmp");       // 15 ✓
    commandsList.add("jn");        // 16 ✓
    commandsList.add("jz");        // 17 ✓
    commandsList.add("jnz");       // 18 ✓
    commandsList.add("jeq");       // 19 ✓

    commandsList.add("jgt"); // 20 ✓
    commandsList.add("jlw"); // 21 ✓

    // Aditional commands
    commandsList.add("moveImmMem"); // 22 ✓
    commandsList.add("moveMemMem"); // 23 ✓
  }

  /**
   * This method is used after some ULA operations, setting the flags bits according the result.
   *
   * @param result is the result of the operation
   *               NOT TESTED!!!!!!!
   */
  public void setStatusFlags(int result) {
    flags.setBit(0, 0);
    flags.setBit(1, 0);
    if (result == 0) { // bit 0 in flags must be 1 in this case
      flags.setBit(0, 1);
    }
    if (result < 0) { // bit 1 in flags must be 1 in this case
      flags.setBit(1, 1);
    }
  }

  /**
   * This method implements the microprogram for
   * add <reg1> <reg2>
   * In the machine language this command number is 0
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>), in positions just after the command, and
   * adds the value from the <reg1> register to the <reg2> register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the first register)
   * 7. memory -> extbus // sets the extbus value to the id of the first register
   * 8. demux <- extbus // sets the demux value to the id of the first register
   * 9. registers -> extbus // this performs the internal reading of the selected register
   * 10. ula(0) <- extbus // waits for the value of the seconds register
   * 11. pc -> intbus
   * 12. ula(1) <- intbus
   * 13. ula.inc
   * 14. ula(1) -> intbus
   * 15. ula(1) -> extbus
   * 16. pc <- intbus // pc++ (pointing to the second register)
   * 17. memory -> extbus // sets the extbus value to the id of the second register
   * 18. demux <- extbus // sets the demux value to the id of the second register
   * 19. registers -> extbus // this performs the internal reading of the selected register
   * 20. ula(1) <- extbus // Save the value of the second register in the ula
   * 21. ula.add // adds the value of the first register to the value of the second register
   * 22. ula(0) <- intbus
   * 23. ula(0) -> extbus
   * 24. memory -> extbus // sets the extbus value to the id of the second register
   * 25. demux <- extbus // sets the demux value to the id of the second register
   * 26. ula(1) -> extbus // moves the value from the ula to the extbus
   * 27. registers <- extbus // this performs the internal writing of the selected register
   * 28. pc -> intbus
   * 29. ula(1) <- intbus
   * 30. ula.inc
   * 31. ula(1) -> intbus
   * 32. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void addRegReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Add the value of the first register to the value of the second register
    ula.add();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * add <mem> <reg>
   * In the machine language this command number is 1
   * <p>
   * The method reads the memory position and the register id from the memory, in positions just after the command, and
   * adds the value from the memory position to the register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the first register)
   * 7. memory -> extbus // read the value forom the memory position
   * 8. memory -> extbus
   * 9. ula(0) <- extbus // waits for the value of the seconds register
   * 10. pc -> intbus
   * 11. ula(1) <- intbus
   * 12. ula.inc
   * 13. ula(1) -> intbus
   * 14. ula(1) -> extbus
   * 15. pc <- intbus // pc++ (pointing to the second register)
   * 16. memory -> extbus // sets the extbus value to the id of the second register
   * 17. demux <- extbus // sets the demux value to the id of the second register
   * 18. registers -> extbus // this performs the internal reading of the selected register
   * 19. ula(1) <- extbus // Save the value of the second register in the ula
   * 20. ula.add // adds the value of the first register to the value of the second register
   * 21. ula(0) <- intbus
   * 22. ula(0) -> extbus
   * 23. memory -> extbus // sets the extbus value to the id of the second register
   * 24. demux <- extbus // sets the demux value to the id of the second register
   * 25. ula(1) -> extbus // moves the value from the ula to the extbus
   * 26. registers <- extbus // this performs the internal writing of the selected register
   * 27. pc -> intbus
   * 28. ula(1) <- intbus
   * 29. ula.inc
   * 30. ula(1) -> intbus
   * 31. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void addMemReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the value from the memory
    memory.read();
    memory.read();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Add the value of the first register to the value of the second register
    ula.add();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * add <reg> <mem>
   * In the machine language this command number is 2
   * <p>
   * The method reads the register id and the memory position from the memory, in positions just after the command, and
   * adds the value from the register to the memory position
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the memory position)
   * 7. memory -> extbus // read the first parameter
   * 8. demux <- extbus // sets the demux value to the id of the register
   * 9. registers <- extbus // this performs the internal reading of the selected register
   * 10. ula(0) <- extbus // Save the value of the extbus in the ula
   * 11. pc -> intbus
   * 12. ula(1) <- intbus
   * 13. ula.inc
   * 14. ula(1) -> intbus
   * 15. ula(1) -> extbus
   * 16. pc <- intbus // pc++ (pointing to the register id)
   * 18. memory -> extbus // read the register id
   * 19. memory <- extbus // sets the extbus value to the position of memory
   * 20. memory -> read // read the value from the memory position
   * 21. ula(1) <- extbus // Save the value of the extbus in the ula
   * 22. ula.sub // subtract the value from the memory position to the register
   * 23. ula(1) -> extbus // moves the value from the ula to the extbus
   * 25. memory <- extbus // sets the extbus value to the position of memory
   * pc -> intbus
   * ula(1) <- intbus
   * ula.inc
   * ula(1) -> intbus
   * ula(1) -> extbus
   */
  public void addRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    demux.setValue(extbus.get());
    registersRead();
    ula.store(0);
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    memory.store();
    memory.read();
    ula.store(1);
    ula.add();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result
    memory.store();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * sub <reg1> <reg2>
   * In the machine language this command number is 3
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>) from the memory, in positions just after the command, and
   * subtracts the value from the <reg1> register to the <reg2> register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the first register)
   * 7. memory -> extbus // sets the extbus value to the id of the first register
   * 8. demux <- extbus // sets the demux value to the id of the first register
   * 9. registers -> extbus // this performs the internal reading of the selected register
   * 10. ula(0) <- extbus // waits for the value of the seconds register
   * 11. pc -> intbus
   * 12. ula(1) <- intbus
   * 13. ula.inc
   * 14. ula(1) -> intbus
   * 15. ula(1) -> extbus
   * 16. pc <- intbus // pc++ (pointing to the second register)
   * 17. memory -> extbus // sets the extbus value to the id of the second register
   * 18. demux <- extbus // sets the demux value to the id of the second register
   * 19. registers -> extbus // this performs the internal reading of the selected register
   * 20. ula(1) <- extbus // Save the value of the second register in the ula
   * 21. ula.sub // subtracts the value of the first register to the value of the second register
   * 22. ula(0) <- intbus
   * 23. ula(0) -> extbus
   * 24. memory -> extbus // sets the extbus value to the id of the second register
   * 25. demux <- extbus // sets the demux value to the id of the second register
   * 26. ula(1) -> extbus // moves the value from the ula to the extbus
   * 27. registers <- extbus // this performs the internal writing of the selected register
   * 28. pc -> intbus
   * 29. ula(1) <- intbus
   * 30. ula.inc
   * 31. ula(1) -> intbus
   * 32. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void subRegReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * sub <mem> <reg>
   * In the machine language this command number is 4
   * <p>
   * The method reads the memory position and the register id from the memory, in positions just after the command, and
   * subtracts the value from the memory position to the register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the first register)
   * 7. memory -> extbus
   * 8. memory -> extbus
   * 9. ula(0) <- extbus // waits for the value of the seconds register
   * 10. pc -> intbus
   * 11. ula(1) <- intbus
   * 12. ula.inc
   * 13. ula(1) -> intbus
   * 14. ula(1) -> extbus
   * 15. pc <- intbus // pc++ (pointing to the second register)
   * 16. memory -> extbus // sets the extbus value to the id of the second register
   * 17. demux <- extbus // sets the demux value to the id of the second register
   * 18. registers -> extbus // this performs the internal reading of the selected register
   * 19. ula(1) <- extbus // Save the value of the second register in the ula
   * 20. ula.sub // subtracts the value of the first register to the value of the second register
   * 21. ula(0) <- intbus
   * 22. ula(0) -> extbus
   * 23. memory -> extbus // sets the extbus value to the id of the second register
   * 24. demux <- extbus // sets the demux value to the id of the second register
   * 25. ula(1) -> extbus // moves the value from the ula to the extbus
   * 26. registers <- extbus // this performs the internal writing of the selected register
   * 27. pc -> intbus
   * 28. ula(1) <- intbus
   * 29. ula.inc
   * 30. ula(1) -> intbus
   * 31. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void subMemReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();
    memory.read();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * sub <reg> <mem> -> <reg>
   * In the machine language this command number is 5
   * <p>
   * The method reads the register id and the memory position from the memory, in positions just after the command, and
   * subtracts the value from the register to the memory position
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the memory position)
   * 7. memory -> extbus // read the first parameter
   * 8. demux <- extbus // sets the demux value to the id of the register
   * 9. registers <- extbus // this performs the internal reading of the selected register
   * 10. ula(0) <- extbus // Save the value of the extbus in the ula
   * 11. pc -> intbus
   * 12. ula(1) <- intbus
   * 13. ula.inc
   * 14. ula(1) -> intbus
   * 15. ula(1) -> extbus
   * 16. pc <- intbus // pc++ (pointing to the register id)
   * 18. memory -> extbus // read the register id
   * 19. memory <- extbus // sets the extbus value to the position of memory
   * 20. memory -> read // read the value from the memory position
   * 21. ula(1) <- extbus // Save the value of the extbus in the ula
   * 22. ula.sub // subtract the value from the memory position to the register
   * 23. ula(1) -> extbus // moves the value from the ula to the extbus
   * 25. memory <- extbus // sets the extbus value to the position of memory
   * pc -> intbus
   * ula(1) <- intbus
   * ula.inc
   * ula(1) -> intbus
   * ula(1) -> extbus
   */
  public void subRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    demux.setValue(extbus.get());
    registersRead();
    ula.store(0);
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    memory.store();
    memory.read();
    ula.store(1);
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result
    memory.store();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * imul <mem> <reg>
   * In the machine language this command number is 6
   * <p>
   * The method reads the memory position and the register id from the memory, in positions just after the command, and
   * multiplies the value from the memory position to the register
   * <p>
   * 1.
   */
  public void imulMemReg() {
    /*This function implements multiplication between two numbers*/
  }

  /**
   * This method implements the microprogram for
   * imul <reg> <mem>
   * In the machine language this command number is 7
   * <p>
   * The method reads the register id and the memory position from the memory, in positions just after the command, and
   * multiplies the value from the register to the memory position
   * <p>
   * 1.
   */
  public void imulRegMem() {

  }

  /**
   * This method implements the microprogram for
   * imul <reg1> <reg2>
   * In the machine language this command number is 8
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>) from the memory, in positions just after the command, and
   * multiplies the value from the <reg1> register to the <reg2> register
   * <p>
   */
  public void imulRegReg() {

  }

  /**
   * This method implements the microprogram for
   * move <mem> <reg>
   * In the machine language this command number is 9
   * <p>
   * The method reads the memory position and the register id from the memory, in positions just after the command, and
   * moves the value from the memory position to the register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the memory position)
   * 7. memory -> extbus // read the first parameter
   * 8. memory -> extbus // read the value from the memory position
   * 9. ula(0) <- extbus // Save the value of the extbus in the ula
   * 10. pc -> intbus
   * 11. ula(1) <- intbus
   * 12. ula.inc
   * 13. ula(1) -> intbus
   * 14. pc <- intbus // pc++ (pointing to the register id)
   * 15. memory -> extbus // read the register id
   * 16. demux <- extbus // sets the demux value to the id of the register
   * 17. ula(0) -> extbus // moves the value from the ula to the extbus
   * 18. registers -> extbus // this performs the internal reading of the selected register
   * 19. ula(0) <- extbus // Save the value of the selected register in the ula
   * 20. pc -> intbus
   * 21. ula(1) <- intbus
   * 22. ula.inc
   * 23. ula(1) -> intbus
   * 24. pc <- intbus // pc++ (pointing to the memory position)
   */
  public void moveMemReg() {
    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read(); // Get the first parameter
    memory.read(); // Get the value from the memory position
    ula.store(0); // Save the value of the extbus in the ula

    // Increment PC to point to the register id
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read(); // Get the register id

    demux.setValue(extbus.get()); // Set the demux value to the id of the register
    ula.read(0); // Move the value from the ula to the extbus
    registersStore(); // Write the value from the extbus to the selected register

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * move <reg> <mem>
   * In the machine language this command number is 10
   * <p>
   * The method reads the register id and the memory position from the memory, in positions just after the command, and
   * moves the value from the register to the memory position
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the register)
   * 7. memory -> extbus // sets the extbus value to the id of the register
   * 8. ula(0) <- extbus // Save the value of the extbus in the ula
   * 9. pc -> intbus
   * 10. ula(1) <- intbus
   * 11. ula.inc
   * 12. ula(1) -> intbus
   * 13. ula(1) -> extbus
   * 14. pc <- intbus // pc++ (pointing to the memory position)
   * 15. memory -> extbus // sets the extbus value to the memory position
   * 16. memory <- extbus // sends the memory position and waits for the value
   * 17. ula(0) -> extbus // moves the value from the ula to the extbus
   * 18. demux <- extbus // sets the demux value to the id of the register
   * 19. registers <- extbus // this performs the internal writing of the selected register
   * 20. memory <- extbus // sends the value to the memory and stores it in the memory position
   * 21. pc -> intbus
   * 22. ula(1) <- intbus
   * 23. ula.inc
   * 24. ula(1) -> intbus
   * 25. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void moveRegMem() {
    // Increment PC to point to the register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();
    ula.store(0); // storing the register id in the ula

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read(); // Get the value of the memory position
    memory.store(); // Sends the memory position and waits for the value

    // Move the value from the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the selected register
    demux.setValue(extbus.get());
    registersRead();

    // Write the value from the extbus to the memory
    memory.store();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  public void moveMemMem() {
    // Increment PC to point to the first memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first memory position
    memory.read();
    memory.read();

    ula.store(0); // Save the value of the first memory position in the ula

    // Increment PC to point to the second memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second memory position
    memory.read();
    memory.store();

    // Move the value from the ula to the extbus
    ula.read(0);

    // Write the value from the extbus to the memory
    memory.store();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * move <reg1> <reg2>
   * In the machine language this command number is 11
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>) from the memory, in positions just after the command, and
   * moves the value from the <reg1> register to the <reg2> register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the first register)
   * 7. memory -> extbus // sets the extbus value to the id of the first register
   * 8. ula(0) <- extbus // Save the value of the extbus in the ula (we call it GAMBIARRA)
   * 8. pc -> intbus
   * 9. ula(1) <- intbus
   * 10. ula.inc
   * 11. ula(1) -> intbus
   * 12. pc <- intbus // pc++ (pointing to the second register)
   * 13. extbus <- ula(0) // moves the value from the ula to the extbus
   * 14. demux <- extbus // sets the demux value to the id of the first register
   * 15. registers -> extbus // this performs the internal reading of the selected register
   * 16. ula(0) <- extbus // Save the value of the selected register in the ula
   * 17. pc -> intbus
   * 18. ula(1) <- intbus
   * 19. ula(1) -> extbus // MALABARISMO to move the value from the pc to the extbus
   * 20. memory -> extbus // sets the extbus value to the id of the second register
   * 21. demux <- extbus // sets the demux value to the id of the second register
   * 22. ula(0) -> extbus // moves the value from the ula to the extbus
   * 23. registers <- extbus // this performs the internal writing of the selected register
   * 24. pc -> intbus
   * 25. ula(1) <- intbus
   * 26. ula.inc
   * 27. ula(1) -> intbus
   * 28. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void moveRegReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();
    ula.store(0); // storing the register id in the ula

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    ula.read(0); // send the extbus value back to the extbus

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the first register in the ula
    ula.store(0);

    // Move the value from the pc to the extbus
    PC.internalRead();
    ula.internalStore(1);
    ula.read(1);

    // Read the register id from the memory
    memory.read();

    // Select the second register
    demux.setValue(extbus.get());

    // Move the first register value that was stored in the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the selected register
    registersStore();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * move imm <reg>
   * In the machine language this command number is 12
   * <p>
   * The method reads the immediate value and the register id from the memory, in positions just after the command, and
   * moves the immediate value to the register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the immediate value)
   * 7. memory -> extbus // sets the extbus value to the immediate value
   * 8. ula(0) <- extbus // Save the value of the extbus in the ula
   * 9. pc -> intbus
   * 10. ula(1) <- intbus
   * 11. ula.inc
   * 12. ula(1) -> intbus
   * 13. ula(1) -> extbus
   * 14. pc <- intbus // pc++ (pointing to the register)
   * 15. memory -> extbus // sets the extbus value to the id of the register
   * 16. demux <- extbus // sets the demux value to the id of the register
   * 17. ula(0) -> extbus // moves the value from the ula to the extbus
   * 18. registers <- extbus // this performs the internal writing of the selected register
   * 19. pc -> intbus
   * 20. ula(1) <- intbus
   * 21. ula.inc
   * 22. ula(1) -> intbus
   * 23. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void moveImmReg() {
    // Increment PC to point to the immediate value
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the immediate value from the memory
    memory.read();
    ula.store(0); // storing the immediate value in the ula

    // Increment PC to point to the register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();

    // Select the register
    demux.setValue(extbus.get());

    // Move the immediate value that was stored in the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the selected register
    registersStore();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  public void moveImmMem() {
    // Increment PC to point to the immediate value
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the immediate value from the memory
    memory.read();
    ula.store(0); // storing the immediate value in the ula

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    // Prepare the memory to store the immediate value
    memory.store();

    // Move the immediate value that was stored in the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the memory
    memory.store();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * inc <reg>
   * In the machine language this command number is 13
   * <p>
   * The method reads the register id from the memory, in positions just after the command, and
   * increments the value from the register
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the register)
   * 7. memory -> extbus // sets the extbus value to the id of the register
   * 8. demux <- extbus // sets the demux value to the id of the register
   * 9. registers -> extbus // this performs the internal reading of the selected register
   * 10. ula(1) <- extbus // Save the value of the selected register in the ula
   * 11. ula.inc // increment the value in the ula
   * 12. ula(1) -> extbus // moves the value from the ula to the extbus
   * 13. registers <- extbus // this performs the internal writing of the selected register
   * 14. pc -> intbus
   * 15. ula(1) <- intbus
   * 16. ula.inc
   * 17. ula(1) -> intbus
   * 18. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void incReg() {
    // Increment PC to point to the register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();

    // Select the register
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the selected register in the ula
    ula.store(1);

    // Increment the value in the ula
    ula.inc();

    // Move the value from the ula to the extbus
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Write the value from the ula to the selected register
    registersStore();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * inc <mem>
   * In the machine language this command number is 14
   * <p>
   * The method reads the memory position from the memory, in positions just after the command, and
   * increments the value from the memory position
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the memory position)
   * 7. memory -> extbus // sets the extbus value to the memory position
   * 8. memory <- extbus // sends the memory position and waits for the value
   * 9. memory -> extbus // sets the extbus value to the memory position
   * 10. ula(1) <- extbus // moves the value from the memory to the ula
   * 11. ula.inc // increment the value in the ula
   * 12. ula(1) -> extbus // moves the value from the ula to the extbus
   * 13. memory <- extbus // sends the value to the memory and stores it in the memory position
   * 14. pc -> intbus
   * 15. ula(1) <- intbus
   * 16. ula.inc
   * 17. ula(1) -> intbus
   * 18. pc <- intbus // pc++ (pointing to the next command) :D
   */
  public void incMem() {
    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();
    memory.store();

    // Read the value from the memory position
    memory.read();

    // Move the value from the memory to the ula
    ula.store(1);

    // Increment the value in the ula
    ula.inc();

    // Move the value from the ula to the extbus
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Write the value from the ula to the memory
    memory.store();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jmp <mem>
   * In the machine language this command number is 15
   * <p>
   * The method reads the memory position from the memory, in positions just after the command, and
   * jumps to the memory position
   * <p>
   * 1. pc -> intbus
   * 2. ula(1) <- intbus
   * 3. ula.inc
   * 4. ula(1) -> intbus
   * 5. ula(1) -> extbus
   * 6. pc <- intbus // pc++ (pointing to the memory position)
   * 7. memory -> extbus // sets the extbus value to the memory position
   * 8. ula(0) <- extbus // Save the value of the extbus in the ula
   * 9. ula(0) -> intbus // moves the value from the ula to the intbus
   * 10. pc <- intbus // jumps to the memory position
   */
  public void jmp() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jn <mem>
   * In the machine language this command number is 16
   * <p>
   * The method reads the memory position from the memory, in positions just after the command, and
   * jumps to the memory position if the last operation was negative
   * <p>
   * 1.
   */
  public void jn() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn0();

    extbus.put(flags.getBit(1));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jz <mem>
   * In the machine language this command number is 17
   * <p>
   * The method reads the memory position from the memory, in positions just after the command, and
   * jumps to the memory position if the last operation was zero
   * <p>
   * 1.
   */
  public void jz() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn0();

    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jnz <mem>
   * In the machine language this command number is 18
   * <p>
   * The method reads the memory position from the memory, in positions just after the command, and
   * jumps to the memory position if the last operation was not zero
   * <p>
   * 1.
   */
  public void jnz() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn0();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn1();

    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jeq <reg1> <reg2> <mem>
   * In the machine language this command number is 19
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>) and the memory position from the memory, in positions just after the command, and
   * jumps to the memory position if the two registers are equal
   * <p>
   * 1.
   */
  public void jeq() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    statusMemory.storeIn0();

    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jgt <reg1> <reg2> <mem>
   * In the machine language this command number is 20
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>) and the memory position from the memory, in positions just after the command, and
   * jumps to the memory position if the first register is greater than the second
   * <p>
   * 1.
   */
  public void jgt() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    statusMemory.storeIn0();

    // Check if it > and not >=
    extbus.put(flags.getBit(1));
    ula.store(0);
    extbus.put(flags.getBit(0));
    ula.store(1);
    ula.sub(); // If the result is 0, the first register is greater than the second and not equal
    ula.read(1);
    setStatusFlags(extbus.get());
    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

  /**
   * This method implements the microprogram for
   * jlw <reg1> <reg2> <mem>
   * In the machine language this command number is 21
   * <p>
   * The method reads the two register ids (<reg1> and <reg2>) and the memory position from the memory, in positions just after the command, and
   * jumps to the memory position if the first register is less than the second
   * <p>
   * 1.
   */
  public void jlw() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    statusMemory.storeIn0();

    // Check if it > and not >=
    extbus.put(flags.getBit(1));
    ula.store(0);
    extbus.put(flags.getBit(0));
    ula.store(1);
    ula.sub(); // If the result is not negative and not zero, the first register is less than the second
    ula.read(1);
    setStatusFlags(extbus.get());

    extbus.put(flags.getBit(1));
    ula.store(0);
    extbus.put(flags.getBit(0));
    ula.store(1);
    ula.sub(); // If the result is 0, the first register is greater than the second and not equal
    ula.read(1);
    setStatusFlags(extbus.get());
    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

  /**
   * This method performs an (external) read from a register into the register list.
   * The register id must be in the demux bus
   */
  private void registersRead() {
    registersList.get(demux.getValue()).read();
  }

  /**
   * This method performs an (internal) read from a register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalRead() {
    registersList.get(demux.getValue()).internalRead();
  }

  /**
   * This method performs an (external) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersStore() {
    registersList.get(demux.getValue()).store();
  }

  /**
   * This method performs an (internal) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalStore() {
    registersList.get(demux.getValue()).internalStore();
  }

  /**
   * This method reads an entire file in machine code and
   * stores it into the memory
   * NOT TESTED
   *
   * @param filename the name of the file to be read
   * @throws IOException if the file is not found
   */
  public void readExec(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename + ".dxf"));
    String linha;
    int i = 0;
    while ((linha = br.readLine()) != null) {
      extbus.put(i);
      memory.store();
      extbus.put(Integer.parseInt(linha));
      memory.store();
      i++;
    }
    br.close();
  }

  /**
   * This method executes a program that is stored in the memory
   */
  public void controlUnitEexec() {
    halt = false;
    while (!halt) {
      fetch();
      decodeExecute();
    }
  }

  /**
   * This method implements the decoding process,
   * that is to find the correct operation do be executed
   * according the command.
   * And the executing process, that is the execution itself of the command
   */
  private void decodeExecute() {
    IR.internalRead(); // the instruction is in the internal bus
    int command = intbus.get();
    simulationDecodeExecuteBefore(command);

    switch (command) {
      case 0:
        addRegReg();
        break;
      case 1:
        addMemReg();
        break;
      case 2:
        addRegMem();
        break;
      case 3:
        subRegReg();
        break;
      case 4:
        subMemReg();
        break;
      case 5:
        subRegMem();
        break;
      case 6:
        imulMemReg();
        break;
      case 7:
        imulRegMem();
        break;
      case 8:
        imulRegReg();
        break;
      case 9:
        moveMemReg();
        break;
      case 10:
        moveRegMem();
        break;
      case 11:
        moveRegReg();
        break;
      case 12:
        moveImmReg();
        break;
      case 13:
        incReg();
        break;
      case 14:
        incMem();
        break;
      case 15:
        jmp();
        break;
      case 16:
        jn();
        break;
      case 17:
        jz();
        break;
      case 18:
        jnz();
        break;
      case 19:
        jeq();
        break;
      case 20:
        jgt();
        break;
      case 21:
        jlw();
        break;
      case 22:
        moveImmMem();
        break;
      case 23:
        moveMemMem();
        break;
      default:
        halt = true;
        break;
    }

    if (simulation) {
      simulationDecodeExecuteAfter();
    }
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   *
   * @param command the command to be executed
   */
  private void simulationDecodeExecuteBefore(int command) {
    System.out.println("----------BEFORE Decode and Execute phases--------------");
    String instruction;
    int parameter = 0;
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    if (command != -1) {
      instruction = commandsList.get(command);
    } else {
      instruction = "END";
    }

    int operands = getOperandSize(instruction);
    System.out.print("Instruction: " + instruction);
    for (int i = 0; i < operands; i++) {
      parameter = memory.getDataList()[PC.getData() + i + 1];
      System.out.print(" " + parameter);
    }
    System.out.println();

    if ("read".equals(instruction)) {
      System.out.println("memory[" + parameter + "]=" + memory.getDataList()[parameter]);
    }
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   */
  private void simulationDecodeExecuteAfter() {
    String instruction;
    System.out.println("-----------AFTER Decode and Execute phases--------------");
    System.out.println("Internal Bus: " + intbus.get());
    System.out.println("External Bus: " + extbus.get());
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }

    Scanner entrada = new Scanner(System.in);
    System.out.println("Press <Enter>");
    String mensagem = entrada.nextLine();
  }

  /**
   * This method uses PC to find, in the memory,
   * the command code that must be executed.
   * This command must be stored in IR
   * NOT TESTED!
   */
  private void fetch() {
    PC.internalRead();
    ula.internalStore(0);
    ula.read(0);
    memory.read();
    IR.store();
    simulationFetch();
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED!!!!!!!!!
   */
  private void simulationFetch() {
    if (simulation) {
      System.out.println("-------Fetch Phase------");
      System.out.println("PC: " + PC.getData());
      System.out.println("IR: " + IR.getData());
    }
  }

  /**
   * This method is used to show in a correct way the operands (if there is any) of instruction,
   * when in simulation mode
   * NOT TESTED!!!!!
   *
   * @param instruction the instruction to be executed
   * @return the number of operands
   */
  private int getOperandSize(String instruction) {
    if (instruction.equals("END")) {
      return 0;
    }

    int operands = 0;
    for (int i = 0; i < instruction.length(); i++) {
      // check if the character is a capital letter
      if (Character.isUpperCase(instruction.charAt(i))) {
        operands++;
      }
    }
    return Math.max(1, operands); // at least one
  }

  public static void main(String[] args) throws IOException {
    Architecture arch = new Architecture(false);
    arch.readExec("program");
    arch.controlUnitEexec();
  }
}
