package architecture;

import static org.junit.Assert.assertEquals;

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
}
