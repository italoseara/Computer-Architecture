package architecture;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestArchitecture {

  /*
   * The following tests are for the architecture components
   */
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
    arch.getRegistersList().get(0).store(); //RPG0 has 45
    arch.getExtbus().put(109);
    arch.getRegistersList().get(1).store(); //RPG1 has 99

    // MOVE %rpg1 %rpg0
    arch.moveRegReg();

    // Testing if both REG1 and REG0 store the same value: 99
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

    // MOVE 77 %rpg0
    arch.moveImmReg();

    // Testing if REG0 stores the value 77
    arch.getRegistersList().get(0).read();
    assertEquals(101, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
  }
}
