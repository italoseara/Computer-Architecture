package architecture;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestArchitecture {

  @Test
  public void testJgt() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the registers values
    arch.getExtbus().put(1);
    arch.getRegistersList().get(0).store(); // RPG0 has 0
    arch.getExtbus().put(0);
    arch.getRegistersList().get(1).store(); // RPG1 has 1

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;
    arch.getMemory().getDataList()[33] = 100;

    // JEQ %rpg0 %rpg1 100
    arch.jgt();

    // Testing if PC points to position 100
    assertEquals(100, arch.getPC().getData());

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the registers values
    arch.getExtbus().put(0);
    arch.getRegistersList().get(0).store(); // RPG0 has 0
    arch.getExtbus().put(0);
    arch.getRegistersList().get(1).store(); // RPG1 has 1

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;
    arch.getMemory().getDataList()[33] = 100;

    // JEQ %rpg0 %rpg1 100
    arch.jgt();

    // Testing if PC points to position 100
    assertEquals(34, arch.getPC().getData());

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the registers values
    arch.getExtbus().put(0);
    arch.getRegistersList().get(0).store(); // RPG0 has 0
    arch.getExtbus().put(1);
    arch.getRegistersList().get(1).store(); // RPG1 has 1

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;
    arch.getMemory().getDataList()[33] = 100;

    // JEQ %rpg0 %rpg1 100
    arch.jgt();

    // Testing if PC points to position 100
    assertEquals(34, arch.getPC().getData());
  }

  @Test
  public void testJeq() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the registers values
    arch.getExtbus().put(0);
    arch.getRegistersList().get(0).store(); // RPG0 has 0
    arch.getExtbus().put(1);
    arch.getRegistersList().get(1).store(); // RPG1 has 1

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;
    arch.getMemory().getDataList()[33] = 100;

    // JEQ %rpg0 %rpg1 100
    arch.jeq();

    // Testing if PC points to position 100
    assertEquals(34, arch.getPC().getData());

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the registers values
    arch.getExtbus().put(1);
    arch.getRegistersList().get(0).store(); // RPG0 has 0
    arch.getExtbus().put(1);
    arch.getRegistersList().get(1).store(); // RPG1 has 1

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;
    arch.getMemory().getDataList()[33] = 100;

    // JEQ %rpg0 %rpg1 100
    arch.jeq();

    // Testing if PC points to position 100
    assertEquals(100, arch.getPC().getData());
  }

  @Test
  public void testJn() {
    Architecture arch = new Architecture();

    // Simulate an operation
    arch.setStatusFlags(-1);

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JN 100
    arch.jn();

    // Testing if PC points to position 100
    assertEquals(100, arch.getPC().getData());

    // Simulate an operation
    arch.setStatusFlags(1);

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JN 100
    arch.jn();

    // Testing if PC points to position 100
    assertEquals(32, arch.getPC().getData());
  }

  @Test
  public void testJz() {
    Architecture arch = new Architecture();

    // Simulate an operation
    arch.setStatusFlags(0);

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JZ 100
    arch.jz();

    // Testing if PC points to position 100
    assertEquals(100, arch.getPC().getData());

    // Simulate an operation
    arch.setStatusFlags(1);

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JZ 100
    arch.jz();

    // Testing if PC points to position 100
    assertEquals(32, arch.getPC().getData());
  }

  @Test
  public void testJnz() {
    Architecture arch = new Architecture();

    // Simulate an operation
    arch.setStatusFlags(1);

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JN 100
    arch.jnz();

    // Testing if PC points to position 100
    assertEquals(100, arch.getPC().getData());

    // Simulate an operation
    arch.setStatusFlags(0);

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JN 100
    arch.jnz();

    // Testing if PC points to position 100
    assertEquals(32, arch.getPC().getData());
  }

  @Test
  public void testSubRegMem() {
    Architecture arch = new Architecture();
    // making PC points to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 100;
    arch.getMemory().getDataList()[100] = 70;
    arch.getExtbus().put(77);
    arch.getRegistersList().get(0).store(); //RPG0 has 77

    arch.subRegMem();

    assertEquals(7, arch.getMemory().getDataList()[100]);
    arch.getRegistersList().get(0).read();
    assertEquals(77, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
  }

  @Test
  public void testAddRegReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;

    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRegistersList().get(0).store();
    arch.getExtbus().put(33);
    arch.getRegistersList().get(1).store();

    // ADD %rpg0 %rpg1
    arch.addRegReg();

    // Testing if RPG0 stores the value 77
    arch.getRegistersList().get(0).read();
    assertEquals(77, arch.getExtbus().get());

    // Testing if RPG1 stores the value 110
    arch.getRegistersList().get(1).read();
    assertEquals(110, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
  }

  @Test
  public void testAddMemReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;
    arch.getMemory().getDataList()[32] = 0;
    arch.getMemory().getDataList()[100] = 20;

    // Now setting the registers values
    arch.getExtbus().put(50);
    arch.getRPG().store(); //RPG0 has 50

    // ADD 20 %rpg0
    arch.addMemReg();

    assertEquals(20, arch.getMemory().getDataList()[100]);
    assertEquals(70, arch.getRPG().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testAddRegMem() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 100;
    arch.getMemory().getDataList()[100] = 20;

    // Now setting the registers values
    arch.getExtbus().put(30);
    arch.getRPG().store(); //RPG0 has 30

    // ADD %rpg0 20
    arch.addRegMem();

    assertEquals(30, arch.getRPG().getData());
    assertEquals(50, arch.getMemory().getDataList()[100]);

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testSubRegReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 1;

    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRPG().store();
    arch.getExtbus().put(33);
    arch.getRPG1().store();

    // SUB %rpg0 %rpg1
    arch.subRegReg();

    // Testing if RPG0 stores the value 77
    assertEquals(77, arch.getRPG().getData());

    // Testing if RPG1 stores the value 44
    assertEquals(44, arch.getRPG1().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testSubMemReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;
    arch.getMemory().getDataList()[32] = 0;
    arch.getMemory().getDataList()[100] = 45;

    // Now setting the registers values
    arch.getExtbus().put(10);
    arch.getRPG().store(); // RPG0 has 10

    // SUB 45 %rpg0
    arch.subMemReg();

    assertEquals(45, arch.getMemory().getDataList()[100]);
    assertEquals(35, arch.getRPG().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testMoveMemReg() {
    Architecture arch = new Architecture();

    // making PC points to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // setting the memory values
    arch.getMemory().getDataList()[31] =
        100; // in the fist position we have 100 that's the address we want to move value of the register to memory
    arch.getMemory().getDataList()[32] =
        0; // in the second position we have address  that's register id
    arch.getMemory().getDataList()[100] =
        2000; //in third position we have the value that we want to move to memory

    // MOVE &100 %rpg0
    arch.moveMemReg();

    assertEquals(2000, arch.getMemory().getDataList()[100]);
    assertEquals(2000, arch.getRPG().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testMoveRegMem() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 100;

    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRPG().store(); // RPG0 has 77

    // MOVE %rpg0 100
    arch.moveRegMem();

    // Testing if memory position 100 stores the value 77
    assertEquals(77, arch.getMemory().getDataList()[100]);
    // Testing if RPG0 stores the value 77
    assertEquals(77, arch.getRPG().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testMoveRegReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 1;
    arch.getMemory().getDataList()[32] = 0;

    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRPG().store(); //RPG0 has 77
    arch.getExtbus().put(109);
    arch.getRPG1().store(); //RPG1 has 109

    // MOVE %rpg1 %rpg0
    arch.moveRegReg();

    // Testing if both REG1 and REG0 store the same value: 109
    assertEquals(109, arch.getRPG().getData());
    assertEquals(109, arch.getRPG1().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testMoveImmReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 101;
    arch.getMemory().getDataList()[32] = 0;

    // MOVE 101 %rpg0
    arch.moveImmReg();

    // Testing if REG0 stores the value 101
    assertEquals(101, arch.getRPG().getData());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    assertEquals(33, arch.getPC().getData());
  }

  @Test
  public void testIncReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;

    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRPG().store(); // RPG0 has 77

    // INC %rpg0
    arch.incReg();

    // Testing if RPG0 stores the value 78
    assertEquals(78, arch.getRPG().getData());

    // Testing if PC points to 2 positions after the original
    // PC was pointing to 30; now it must be pointing to 32
    assertEquals(32, arch.getPC().getData());
  }

  @Test
  public void testIncMem() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;
    arch.getMemory().getDataList()[100] = 2000;

    // INC 100
    arch.incMem();

    // Testing if memory position 100 stores the value 2001
    assertEquals(2001, arch.getMemory().getDataList()[100]);

    // Testing if PC points to 2 positions after the original
    // PC was pointing to 30; now it must be pointing to 32
    assertEquals(32, arch.getPC().getData());
  }

  @Test
  public void testJmp() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 100;

    // JMP 100
    arch.jmp();

    // Testing if PC points to position 100
    assertEquals(100, arch.getPC().getData());
  }
}
