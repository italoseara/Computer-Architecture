package architecture;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestArchitecture {

  /*
   * The following tests are for the architecture components
   */
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
    arch.getRegistersList().get(0).store(); // RPG0 has 77

    // MOVE %rpg0 100
    arch.moveRegMem();

    // Testing if memory position 100 stores the value 77
    assertEquals(77, arch.getMemory().getDataList()[100]);
    // Testing if RPG0 stores the value 77
    arch.getRegistersList().get(0).read();
    assertEquals(77, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
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
    arch.getRegistersList().get(0).store(); //RPG0 has 77
    arch.getExtbus().put(109);
    arch.getRegistersList().get(1).store(); //RPG1 has 109

    // MOVE %rpg1 %rpg0
    arch.moveRegReg();

    // Testing if both REG1 and REG0 store the same value: 109
    arch.getRegistersList().get(0).read();
    assertEquals(109, arch.getExtbus().get());
    arch.getRegistersList().get(1).read();
    assertEquals(109, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
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
    arch.getRegistersList().get(0).read();
    assertEquals(101, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
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
    arch.getRegistersList().get(0).store(); // RPG0 has 77

    // INC %rpg0
    arch.incReg();

    // Testing if RPG0 stores the value 78
    arch.getRegistersList().get(0).read();
    assertEquals(78, arch.getExtbus().get());

    // Testing if PC points to 2 positions after the original
    // PC was pointing to 30; now it must be pointing to 32
    arch.getPC().internalRead();
    assertEquals(32, arch.getIntbus().get());
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
    arch.getPC().internalRead();
    assertEquals(32, arch.getIntbus().get());
  }
}
